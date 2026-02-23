package com.koerber.orderservice.service;

import com.koerber.orderservice.dto.PlaceOrderRequest;
import com.koerber.orderservice.dto.PlaceOrderResponse;
import com.koerber.orderservice.client.InventoryClient;
import com.koerber.orderservice.entity.Order;
import com.koerber.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repo;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository repo, InventoryClient inventoryClient) {
        this.repo = repo;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest req) {
        // 1) call inventory to reserve
        var invResp = inventoryClient.reserve(req.productId, req.quantity).block();

        // 2) create order based on reserve result
        Order order = new Order();
        order.setProductId(req.productId);
        order.setQuantity(req.quantity);
        order.setOrderDate(LocalDate.now());

        PlaceOrderResponse resp = new PlaceOrderResponse();
        resp.productId = req.productId;
        resp.quantity = req.quantity;

        if (invResp == null || invResp.reservedQty() == null || invResp.reservedQty() == 0) {
            order.setStatus("REJECTED");
            order.setProductName(invResp != null ? invResp.productName() : null);
            order.setReservedBatchIds(null);
            repo.save(order);

            resp.orderId = order.getOrderId();
            resp.productName = order.getProductName();
            resp.status = "REJECTED";
            resp.reservedFromBatchIds = java.util.List.of();
            resp.message = (invResp != null) ? invResp.message() : "Inventory service not reachable.";
            return resp;
        }

        // If you want “all or nothing”, reject when reservedQty < requested.
        if (!invResp.reservedQty().equals(req.quantity)) {
            order.setStatus("REJECTED");
            order.setProductName(invResp.productName());
            repo.save(order);

            resp.orderId = order.getOrderId();
            resp.productName = invResp.productName();
            resp.status = "REJECTED";
            resp.reservedFromBatchIds = invResp.reservedFromBatchIds();
            resp.message = "Insufficient inventory for full order. Not placed.";
            return resp;
        }

        order.setStatus("PLACED");
        order.setProductName(invResp.productName());
        order.setReservedBatchIds(invResp.reservedFromBatchIds().stream()
                .map(String::valueOf).collect(Collectors.joining(",")));

        repo.save(order);

        resp.orderId = order.getOrderId();
        resp.productName = order.getProductName();
        resp.status = "PLACED";
        resp.reservedFromBatchIds = invResp.reservedFromBatchIds();
        resp.message = "Order placed. Inventory reserved.";
        return resp;
    }
}