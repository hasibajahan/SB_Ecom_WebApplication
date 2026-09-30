package com.ecommerce.project.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    private final ChatClient chatClient;

    public AIService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String askAI(String message) {
        return chatClient
                .prompt(message)
                .call()
                .content();
    }
}
