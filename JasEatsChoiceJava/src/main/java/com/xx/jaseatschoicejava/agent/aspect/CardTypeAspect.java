package com.xx.jaseatschoicejava.agent.aspect;

import com.xx.jaseatschoicejava.agent.annotation.CardType;
import com.xx.jaseatschoicejava.agent.context.ToolExecutionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 卡片类型切面
 * 拦截带有 @CardType 注解的工具方法，记录执行信息用于生成卡片数据
 *
 * @author Claude
 * @since 2026-03-24
 */
@Aspect
@Component
public class CardTypeAspect {

    private static final Logger log = LoggerFactory.getLogger(CardTypeAspect.class);

    /**
     * 拦截带有 @CardType 注解的方法
     */
    @Around("@annotation(com.xx.jaseatschoicejava.agent.annotation.CardType)")
    public Object aroundCardTypeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CardType cardType = signature.getMethod().getAnnotation(CardType.class);

        if (cardType == null) {
            return joinPoint.proceed();
        }

        // 获取方法名作为工具名称
        String toolName = signature.getName();
        String cardTypeValue = cardType.value();

        // 收集方法参数
        Map<String, Object> parameters = new HashMap<>();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (parameterNames != null && args != null) {
            for (int i = 0; i < Math.min(parameterNames.length, args.length); i++) {
                parameters.put(parameterNames[i], args[i]);
            }
        }

        // 记录工具执行开始
        ToolExecutionContext.startExecution(toolName, cardTypeValue, parameters);

        try {
            // 执行工具方法
            Object result = joinPoint.proceed();

            // 记录工具执行结束
            ToolExecutionContext.endExecution(result);

            log.info("✅ 工具执行完成: {}, 卡片类型: {}", toolName, cardTypeValue);

            return result;
        } catch (Exception e) {
            // 执行失败，清空执行信息
            ToolExecutionContext.endExecution(null);
            log.error("❌ 工具执行失败: {}, 错误: {}", toolName, e.getMessage());
            throw e;
        }
    }
}
