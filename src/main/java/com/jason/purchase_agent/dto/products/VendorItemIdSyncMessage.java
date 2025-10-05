package com.jason.purchase_agent.dto.products;
import com.jason.purchase_agent.entity.Product;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorItemIdSyncMessage {
    private String productCode;
    private String sellerProductId;
    private String batchId;
    private boolean priceChanged;
    private boolean stockChanged;
    private Integer salePrice;
    private Integer stock;
}
