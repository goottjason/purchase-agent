package com.jason.purchase_agent.util.salechannelapi;

import com.jason.purchase_agent.dto.channel.SmartstoreProductDto;
import com.jason.purchase_agent.dto.channel.SmartstoreProductPageResultDto;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SmartstoreApiUtil {

  private static final String clientId = "1l5fRuKFzyNJGQF3AP27AE";
  private static final String clientSecret = "$2a$04$24Vxb0j6X3HK.ZVUA43Wk."; // 실제 앱 secret
  private static final String API_URL = "https://api.commerce.naver.com/external";

  // BCrypt로 client_secret_sign 생성
  public static String generateSignature(String clientId, String clientSecret, Long timestamp) {
    String password = clientId + "_" + timestamp;
    String hashedPw = BCrypt.hashpw(password, clientSecret);
    return Base64.getEncoder().encodeToString(hashedPw.getBytes(StandardCharsets.UTF_8));
  }

  public static String getAccessToken() throws Exception {
    Long timestamp = System.currentTimeMillis();
    String clientSecretSign = generateSignature(clientId, clientSecret, timestamp);
    String type = "SELF";

    OkHttpClient client = new OkHttpClient();

    String form = "grant_type=client_credentials"
      + "&client_id=" + clientId
      + "&timestamp=" + timestamp
      + "&client_secret_sign=" + clientSecretSign
      + "&type=" + type;

    RequestBody body = RequestBody.create(
      form,
      MediaType.parse("application/x-www-form-urlencoded")
    );

    Request request = new Request.Builder()
      .url("https://api.commerce.naver.com/external/v1/oauth2/token")
      .post(body)
      .addHeader("Content-Type", "application/x-www-form-urlencoded")
      .addHeader("Accept", "application/json")
      .build();

    try (Response response = client.newCall(request).execute()) {
      String result = response.body().string();
      JSONObject obj = new JSONObject(result);
      if (obj.has("access_token")) {
        return obj.getString("access_token");
      } else {
        throw new RuntimeException("토큰 발급 실패: " + result);
      }
    }
  }

  // 401+GW.AUTHN시 토큰 재발급, 1회만 자동재시도
  public static Optional<SmartstoreProductPageResultDto> fetchSmartStoreProducts(
    String method, String path, Map<String, Object> bodyParams
  ) throws Exception {
    Optional<SmartstoreProductPageResultDto> smartstoreProductPageResultDto = fetchSmartStoreProductsInternal(method, path, bodyParams, true);
    return smartstoreProductPageResultDto;
  }

  public static Optional<SmartstoreProductPageResultDto> fetchSmartStoreProductsInternal(
    String method, String path, Map<String, Object> bodyParams, boolean allowRetry
  ) throws Exception {

    String accessToken = getAccessToken();

    OkHttpClient client = new OkHttpClient();
    MediaType mediaType = MediaType.parse("application/json");
    JSONObject jsonBody = new JSONObject(bodyParams);
    String url = API_URL + path;

    RequestBody body = RequestBody.create(mediaType, jsonBody.toString());

    Request request = new Request.Builder()
      .url(url)
      .method(method, body)
      .addHeader("Content-Type", "application/json")
      .addHeader("Accept", "application/json")
      .addHeader("Authorization", "Bearer " + accessToken)
      .build();

    try (Response response = client.newCall(request).execute()) {
      String resStr = response.body().string();
      if (response.code() == 401 && resStr.contains("\"code\":\"GW.AUTHN\"") && allowRetry) {
        // 토큰 재발급 및 1회 재시도
        System.out.println("[재발급/재시도] 액세스 토큰 만료 또는 권한오류로 재시도...");
        fetchSmartStoreProductsInternal(method, path, bodyParams, false); // 두번째(재발급)에서는 재재시도하지 않음
      } else {
        System.out.println("■resStr = " + resStr);
        // JSON → DTO 매핑
        JSONObject obj = new JSONObject(resStr);
        System.out.println("■obj = " + obj);
        SmartstoreProductPageResultDto result = new SmartstoreProductPageResultDto();
        result.setPage(obj.optInt("page", 1));
        result.setSize(obj.optInt("size", 50));
        result.setTotalPages(obj.optInt("totalPages"));
        result.setTotalElements(obj.optInt("totalElements"));
        result.setFirst(obj.optBoolean("first"));
        result.setLast(obj.optBoolean("last"));

        System.out.println("■result = " + result);

        // 상품 리스트 파싱
        List<SmartstoreProductDto> prodList = new ArrayList<>();
        JSONArray contentsArr = obj.optJSONArray("contents");

        System.out.println("■contentsArr = " + contentsArr);
        if (contentsArr != null) {
          // 한 페이지에 반환된 상품의 개수만큼 순회
          for (int i = 0; i < contentsArr.length(); i++) {

            JSONObject prodObj = contentsArr.getJSONObject(i);
            SmartstoreProductDto dto = new SmartstoreProductDto();
            dto.setOriginProductNo(prodObj.optLong("originProductNo"));

            // channelProducts(리스트)
            List<SmartstoreProductDto.ChannelProduct> cprodList = new ArrayList<>();
            JSONArray chanArr = prodObj.optJSONArray("channelProducts");

            if (chanArr != null) {
              for (int j = 0; j < chanArr.length(); j++) {
                JSONObject chObj = chanArr.getJSONObject(j);
                System.out.println("■chObj = " + chObj);
                SmartstoreProductDto.ChannelProduct cp = new SmartstoreProductDto.ChannelProduct();

                cp.setStatusType(chObj.optString("statusType", ""));
                cp.setManufacturerName(chObj.optString("manufacturerName", ""));
                cp.setExchangeFee(chObj.optInt("exchangeFee", 0));
                cp.setManagerPurchasePoint(chObj.optInt("managerPurchasePoint", 0));
                cp.setSaleEndDate(chObj.optString("saleEndDate", ""));
                cp.setStockQuantity(chObj.optInt("stockQuantity", 0));
                cp.setSaleStartDate(chObj.optString("saleStartDate", ""));
                cp.setRegDate(chObj.optString("regDate", ""));
                cp.setWholeCategoryName(chObj.optString("wholeCategoryName", ""));
                cp.setDeliveryAttributeType(chObj.optString("deliveryAttributeType", ""));
                cp.setSellerManagementCode(chObj.optString("sellerManagementCode", ""));
                cp.setChannelProductNo(chObj.optLong("channelProductNo", 0L));
                cp.setBrandName(chObj.optString("brandName", ""));
                cp.setKnowledgeShoppingProductRegistration(chObj.optBoolean("knowledgeShoppingProductRegistration", false));
                cp.setSalePrice(chObj.optInt("salePrice", 0));
                cp.setMobileDiscountedPrice(chObj.optInt("mobileDiscountedPrice", 0));
                cp.setChannelProductDisplayStatusType(chObj.optString("channelProductDisplayStatusType", ""));
                cp.setChannelServiceType(chObj.optString("channelServiceType", ""));
                cp.setOriginProductNo(chObj.optLong("originProductNo", 0L));
                cp.setDeliveryFee(chObj.optInt("deliveryFee", 0));
                cp.setDiscountedPrice(chObj.optInt("discountedPrice", 0));
                cp.setReturnFee(chObj.optInt("returnFee", 0));
                cp.setName(chObj.optString("name", ""));
                cp.setModifiedDate(chObj.optString("modifiedDate", ""));
                cp.setWholeCategoryId(chObj.optString("wholeCategoryId", ""));
                cp.setCategoryId(chObj.optString("categoryId", ""));

                // representativeImage 파싱 (동일 계층)
                SmartstoreProductDto.RepresentativeImage rim = null;
                if (chObj.has("representativeImage") && !chObj.isNull("representativeImage")) {
                  JSONObject imgObj = chObj.getJSONObject("representativeImage");
                  rim = new SmartstoreProductDto.RepresentativeImage();
                  rim.setUrl(imgObj.optString("url", ""));
                }
                cp.setRepresentativeImage(rim);
                System.out.println("■cp = " + cp);
                cprodList.add(cp);
              }
            }

            dto.setChannelProducts(cprodList);
            System.out.println("■dto = " + dto);
            prodList.add(dto);
          }
        }
        result.setContents(prodList);
        System.out.println("■■■result = " + result);
        return Optional.of(result);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("API 호출 실패: " + e.getMessage());
    }
    return Optional.empty();
  }
}
