package com.koerber.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<InventoryUpdateResponse> reserve(Long productId, int quantity) {
        InventoryUpdateRequest req = new InventoryUpdateRequest(productId, quantity, "FEFO");

        return webClient.post()
                .uri("/inventory/update")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(InventoryUpdateResponse.class);
    }

    public record InventoryUpdateRequest(Long productId, Integer quantity, String strategy) {}

    public record InventoryUpdateResponse(
            Long productId,
            String productName,
            Integer requestedQty,
            Integer reservedQty,
            List<Long> reservedFromBatchIds,
            String message
    ) {}
}
