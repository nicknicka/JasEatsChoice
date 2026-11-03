package com.xx.jaseatschoicejava.agent.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.xx.jaseatschoicejava.config.AiModelConfig;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 保存当前 AI 模型客户端集合并暴露动态代理。
 *
 * HTTP 请求首次访问代理时会固定当时的一组客户端；管理员激活新配置后，
 * 同一个请求继续使用旧客户端，下一个请求读取新客户端。
 */
@Component
public class DynamicAiModelRegistry {

    private static final String REQUEST_MODELS_ATTRIBUTE =
        DynamicAiModelRegistry.class.getName() + ".REQUEST_MODELS";

    private final AtomicReference<ActiveModels> active = new AtomicReference<>(initial());

    public ChatModel chatModel() {
        return proxy(ChatModel.class, ModelKind.CHAT);
    }

    public ChatModel supervisorModel() {
        return proxy(ChatModel.class, ModelKind.SUPERVISOR);
    }

    public ChatModel visionModel() {
        return proxy(ChatModel.class, ModelKind.VISION);
    }

    public ActiveModels current() {
        return active.get();
    }

    public void pinForRequest(HttpServletRequest request) {
        if (request.getAttribute(REQUEST_MODELS_ATTRIBUTE) == null) {
            request.setAttribute(REQUEST_MODELS_ATTRIBUTE, active.get());
        }
    }

    public void activate(
            AiModelConfig config,
            ChatModel chat,
            ChatModel supervisor,
            ChatModel vision,
            long version) {
        active.set(new ActiveModels(config, chat, supervisor, vision, version));
    }

    public void activate(ActiveModels models) {
        active.set(models);
    }

    private <T> T proxy(Class<T> type, ModelKind kind) {
        return type.cast(Proxy.newProxyInstance(
            DynamicAiModelRegistry.class.getClassLoader(),
            new Class<?>[] {type},
            new DelegateHandler(kind)));
    }

    private static ActiveModels initial() {
        return new ActiveModels(null, new NoopChatModel(), new NoopChatModel(),
            new NoopChatModel(), 0L);
    }

    private final class DelegateHandler implements InvocationHandler {
        private final ModelKind kind;

        private DelegateHandler(ModelKind kind) {
            this.kind = kind;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                try {
                    return method.invoke(this, args);
                } catch (InvocationTargetException exception) {
                    throw exception.getTargetException();
                }
            }

            Object target = resolve().targetFor(kind);
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException exception) {
                throw exception.getTargetException();
            }
        }

        private ActiveModels resolve() {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                HttpServletRequest request = servletAttributes.getRequest();
                Object pinned = request.getAttribute(REQUEST_MODELS_ATTRIBUTE);
                if (pinned instanceof ActiveModels models) {
                    return models;
                }
                ActiveModels current = active.get();
                request.setAttribute(REQUEST_MODELS_ATTRIBUTE, current);
                return current;
            }
            return active.get();
        }
    }

    private enum ModelKind {
        CHAT, SUPERVISOR, VISION
    }

    public record ActiveModels(
        AiModelConfig config,
        ChatModel chat,
        ChatModel supervisor,
        ChatModel vision,
        long version
    ) {
        public Object targetFor(ModelKind kind) {
            return switch (kind) {
                case CHAT -> Objects.requireNonNull(chat, "chat model");
                case SUPERVISOR -> Objects.requireNonNull(supervisor, "supervisor model");
                case VISION -> Objects.requireNonNull(vision, "vision model");
            };
        }
    }
}
