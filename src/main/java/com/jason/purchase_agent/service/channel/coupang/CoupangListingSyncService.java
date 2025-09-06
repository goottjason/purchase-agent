package com.jason.purchase_agent.service.channel.coupang;

import com.jason.purchase_agent.repository.jpa.ListingRepository;
import com.jason.purchase_agent.util.salechannelapi.coupang.CoupangApiUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class CoupangListingSyncService {
  private final ListingRepository listingRepository;

//  private static final String API_URL = "/v2/providers/seller_api/apis/api/v1/marketplace/seller-products";

  @Transactional
  public void syncCoupangProducts() throws Exception {

    String method = "GET";
    String sellerProductId = "12737257700"; // 예시 sellerProductId, 실제로는 동기화할 상품의 ID를 사용
    String path = String.format("/v2/providers/seller_api/apis/api/v1/marketplace/seller-products");

    Map<String, String> params = Map.of(
      "vendorId", CoupangApiUtil.VENDOR_ID,
      "maxPerPage", "100", // 페이지당 최대 100개
      "nextToken", "1"
      // 추가적으로 필요한 파라미터 계속 넣으면 됨
    );
    CoupangApiUtil.executeRequest(method, path, params);

    /*int page = 1, size = 100;
    boolean hasMore = true;
    String BASE_PATH = "/v2/providers/seller_api/apis/api/v1/marketplace/seller-products";
    do {
      String path = BASE_PATH + "?page=" + page + "&size=" + size;

      *//*HttpHeaders headers = CoupangApiUtil.buildHeaders("GET", "/v2/providers/seller_api/apis/api/v1/marketplace/seller-products", "");

      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<Map> res = rt.exchange(url, HttpMethod.GET, entity, Map.class);
      List<Map<String,Object>> content = (List<Map<String,Object>>) ((Map)res.getBody().get("data")).get("content");*//*

      String response = CoupangApiUtil.executeRequest("GET", path);

      System.out.println("response = " + response);

      *//*for (Map<String,Object> item : content) {
        // 여기서 listing entity 맵핑/변환
        String channelProductCode = String.valueOf(item.get("channelProductCode")); // PK
        Listing listing = listingRepository.findByChannelProductCodeAndSaleChannel(channelProductCode, SaleChannel.COUPANG)
          .orElse(new Listing());
        listing.setChannelProductCode(channelProductCode);
        listing.setSaleChannel(SaleChannel.COUPANG);
//        listing.setProductCode((String)item.get("productId"));
//        listing.setKorName((String)item.get("sellerProductName"));
        // 기타 상세필드 추가 맵핑

        listingRepository.save(listing);
      }*//*

//      Integer totalPages = (Integer) ((Map)res.getBody().get("data")).get("totalPages");
      Integer totalPages = 2;
      hasMore = page < totalPages;
      page++;
    } while (hasMore);*/
  }
}
