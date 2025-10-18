package com.jason.purchase_agent.dto.products.update;

import com.jason.purchase_agent.dto.products.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManualUpdateAllFieldsMessage {
    private String batchId;
    private ProductDto productDto;
}
