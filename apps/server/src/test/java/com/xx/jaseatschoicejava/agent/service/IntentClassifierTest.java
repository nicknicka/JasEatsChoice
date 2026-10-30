package com.xx.jaseatschoicejava.agent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.ChatModel;

class IntentClassifierTest {

    private final ChatModel chatModel = mock(ChatModel.class);
    private final IntentClassifier classifier = new IntentClassifier(chatModel);

    @Test
    void shortKnowledgeQuestionsAreBusinessIntent() {
        assertEquals(IntentClassifier.IntentType.BUSINESS, classifier.classify("有什么活动？"));
        assertEquals(IntentClassifier.IntentType.BUSINESS, classifier.classify("有优惠吗"));
        assertEquals(IntentClassifier.IntentType.BUSINESS, classifier.classify("公告说了什么"));
        assertEquals(IntentClassifier.IntentType.BUSINESS, classifier.classify("满减规则"));
        assertEquals(IntentClassifier.IntentType.BUSINESS, classifier.classify("退款说明"));
    }
}
