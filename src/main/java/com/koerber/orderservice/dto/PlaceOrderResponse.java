package com.koerber.orderservice.dto;

import java.util.List;

public class PlaceOrderResponse {
    public Long orderId;
    public Long productId;
    public String productName;
    public Integer quantity;
    public String status;
    public List<Long> reservedFromBatchIds;
    public String message;
}
