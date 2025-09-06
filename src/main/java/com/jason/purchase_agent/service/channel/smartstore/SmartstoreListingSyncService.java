package com.jason.purchase_agent.service.channel.smartstore;

import com.jason.purchase_agent.dto.channel.SmartstoreProductDto;
import com.jason.purchase_agent.dto.channel.SmartstoreProductPageResultDto;
import com.jason.purchase_agent.repository.jpa.ListingRepository;
import com.jason.purchase_agent.util.salechannelapi.SmartstoreApiUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class SmartstoreListingSyncService {
  private final ListingRepository listingRepository;

  @Transactional
  public List<SmartstoreProductDto> syncSmartstoreProducts() throws Exception {
    List<SmartstoreProductDto> allProducts = new ArrayList<>();
    int page = 1, size = 500;
    int totalPages = 1;
    String BASE_PATH = "/v1/products/search";

    do {

      Map<String, Object> params = new HashMap();
      params.put("page", page);
      params.put("size", size);

      Optional<SmartstoreProductPageResultDto> optResult =
        SmartstoreApiUtil.fetchSmartStoreProducts("POST", BASE_PATH, params);

      SmartstoreProductPageResultDto resDto = optResult.get();

      System.out.println("resDto = " + optResult);
      
      if (resDto == null) break;
      if (resDto.getContents() != null) allProducts.addAll(resDto.getContents());

      totalPages = resDto.getTotalPages();
      page++;
    } while (page <= totalPages);
    return allProducts;
  }

  /*
  OkHttpClient client = new OkHttpClient().newBuilder()
  .build();
MediaType mediaType = MediaType.parse("application/json");
RequestBody body = RequestBody.create(mediaType, "{\n  \"searchKeywordType\": \"CHANNEL_PRODUCT_NO\",\n  \"channelProductNos\": [\n    0\n  ],\n  \"originProductNos\": [\n    0\n  ],\n  \"groupProductNos\": [\n    0\n  ],\n  \"sellerManagementCode\": \"string\",\n  \"productStatusTypes\": [\n    \"WAIT\"\n  ],\n  \"page\": 1,\n  \"size\": 50,\n  \"orderType\": \"NO\",\n  \"periodType\": \"PROD_REG_DAY\",\n  \"fromDate\": \"2024-07-29\",\n  \"toDate\": \"2024-07-29\"\n}");
Request request = new Request.Builder()
  .url("https://api.commerce.naver.com/external/v1/products/search")
  .method("POST", body)
  .addHeader("Content-Type", "application/json")
  .addHeader("Accept", "application/json;charset=UTF-8")
  .addHeader("Authorization", "Bearer <token>")
  .build();
Response response = client.newCall(request).execute();
   */
}
