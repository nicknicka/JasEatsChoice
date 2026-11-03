package com.xx.jaseatschoicejava.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

class AiQuotaPropertiesTest {

    @Test
    void bindsLimitsAndHumanReadableWindows() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("ai-quota.dish-recognition.limit", "7")
            .withProperty("ai-quota.dish-recognition.window", "30m")
            .withProperty("ai-quota.recipe.limit", "3")
            .withProperty("ai-quota.recipe.window", "1h");

        AiQuotaProperties properties = Binder.get(environment)
            .bind("ai-quota", Bindable.of(AiQuotaProperties.class))
            .get();

        assertEquals(7, properties.getDishRecognition().getLimit());
        assertEquals(Duration.ofMinutes(30), properties.getDishRecognition().getWindow());
        assertEquals(3, properties.getRecipe().getLimit());
        assertEquals(Duration.ofHours(1), properties.getRecipe().getWindow());
    }
}
