package com.woorimarket.domain.product.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class InventoryBulkRequest {
    @NotEmpty(message = "재고 항목은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<InventoryRequest> items;
}
