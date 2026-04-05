package com.woorimarket.domain.order.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "매장 ID는 필수입니다.")
    private Long storeId;

    @NotNull(message = "주문 날짜는 필수입니다.")
    private LocalDate orderDate;

    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    private List<OrderItemRequest> items;

    private String paymentMethod; // CARD, TRANSFER, KAKAO_PAY

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "재고 ID는 필수입니다.")
        private Long inventoryId;

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;
    }
}
