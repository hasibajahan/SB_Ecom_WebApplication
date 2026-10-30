package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // filtering the products list
        Pattern pattern=Pattern.compile
                (
                        "(?i)(?:under|below|less than|within)\\s*(?:₹|rs\\.?\\s*)?(\\d+(?:\\.\\d+)?)" +
                                "|(?:budget\\s*(?:is|of)?\\s*)(?:₹|rs\\.?\\s*)?(\\d+(?:\\.\\d+)?)" +
                                "|(?:₹|rs\\.?\\s*)(\\d+(?:\\.\\d+)?)\\s*(?:max|maximum)"
                );

        Matcher matcher = pattern.matcher(message);

        if(matcher.find()){
            String priceText = matcher.group(1) != null ? matcher.group(1)
                    : matcher.group(2) != null ? matcher.group(2)
                    : matcher.group(3);

            double maxPrice = Double.parseDouble(priceText);

            products=products.stream()
                    .filter(product -> product.getSpecialPrice()<=maxPrice)
                    .toList();
        }

        //for testing
        System.out.println("Products sent to AI: " + products.size());

        //preparing the product catalog for the AI prompt
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
//construct the prompt
        return chatClient
                .prompt()
                .system("You are an AI assistant for a makeup and skincare e-commerce application.\n" +
                        "\n" +
                        "For general skincare and beauty questions, provide helpful answers using your general knowledge.\n" +
                        "\n" +
                        "When recommending, comparing, or answering questions about products available in the store, use the supplied product catalog as the source of truth for product names, prices, ingredients, benefits, features, and other product-specific details.\n" +
                        "\n" +
                        "When a customer provides a budget without specifying a product category or particular requirement, recommend suitable products from the supplied, budget-filtered catalog across different available categories. Do not assume that the customer is interested in a specific category.\n" +
                        "\n" +
                        "When a customer specifies a product category or requirement along with a budget, recommend only products that match those requirements and are within the specified budget.\n" +
                        "\n" +
                        "Do not invent or assume product information that is not explicitly provided in the catalog. If a requested product is not found in the catalog, or a specific detail is missing, clearly acknowledge this limitation instead of making up information.\n" +
                        "\n" +
                        "Keep your answers helpful, relevant, and honest.")
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
