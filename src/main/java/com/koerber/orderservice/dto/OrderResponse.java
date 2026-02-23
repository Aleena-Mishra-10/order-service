package com.koerber.orderservice.dto;

import java.util.List;

public class OrderResponse {
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private String status;
    private List<Long> reservedFromBatchIds;
    private String message;

    // getters and setters
}
