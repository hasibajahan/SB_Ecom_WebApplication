package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AIRequest;
import com.ecommerce.project.service.AIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

//    @GetMapping("/ask")
//    public String askAI(@RequestParam String message) {
//        return aiService.askAI(message);
//    }

    @PostMapping("/ask")
    public String askAI(@RequestBody AIRequest request) {
        return aiService.askAI(request.getMessage());
    }
}
