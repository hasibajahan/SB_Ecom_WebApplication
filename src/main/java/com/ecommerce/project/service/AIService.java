package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {
    private final ChatClient chatClient;

    private final ProductService productService;

    public AIService(ChatClient.Builder builder, ProductService productService) {
        this.chatClient = builder.build();
        this.productService = productService;
    }

    public String askAI(String message) {
        //retrieve product information from the DB
        List<ProductDTO> products = productService.getProductsForAI();

        StringBuilder productContext=new StringBuilder();

        for(ProductDTO product:products){
            productContext.append("Product: ").
                    append(product.getProductName())
                    .append("\nDescription: ")
                    .append(product.getDescription())
                    .append("\nPrice: ")
                    .append(product.getSpecialPrice())
                    .append("\n\n");
        }

        return chatClient
                .prompt()
                .system("You are an AI assistant for a makeup and skincare e-commerce application. " +
                        "Provide helpful, relevant skincare information.")
                .user("""
              Product catalog:
              %s

              Customer question:
              %s
              """.formatted(productContext, message))
                .call()
                .content();
    }

}
