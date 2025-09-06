package com.jason.purchase_agent.util.salechannelapi.coupang;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Base64;

public class HmacReturnbyday {

  private static final String HOST = "api-gateway.coupang.com";
  private static final int PORT = 443;
  private static final String SCHEMA = "https";
  private static final String ACCESS_KEY = "97211801-f495-4fe2-bef3-b614e9d8aaba";
  private static final String SECRET_KEY = "5f72579c452d8aaf5c718156956ba102dab7863c";
  private static final String VENDOR_ID = "A00213055";


  public static void test() {
    String method = "GET";
    String path = String.format("/v2/providers/openapi/apis/api/v4/vendors/%s/returnRequests", VENDOR_ID);

    CloseableHttpClient client = null;
    try {
      client = HttpClients.createDefault();
      URIBuilder uriBuilder = new URIBuilder()
        .setPath(path)
        .addParameter("createdAtFrom", "2025-08-01")
        .addParameter("createdAtTo", "2025-08-31")
        .addParameter("status", "UC");
      System.out.println("uriBuilder = " + uriBuilder);
      System.out.println("■■■uriBuilder = " + uriBuilder.build().toString());
      /********************************************************/
      String authorization = generate(method, uriBuilder.build().toString(), SECRET_KEY, ACCESS_KEY);
      System.out.println("■■■authorization = " + authorization);
      /********************************************************/

      uriBuilder.setScheme(SCHEMA).setHost(HOST).setPort(PORT);
      HttpGet get = new HttpGet(uriBuilder.build().toString());

      /********************************************************/
      // set header, demonstarte how to use hmac signature here
      get.addHeader("Authorization", authorization);
      /********************************************************/
      get.addHeader("content-type", "application/json");
      CloseableHttpResponse response = null;
      try {
        //execute get request
        response = client.execute(get);
        //print result
        System.out.println("status code:" + response.getStatusLine().getStatusCode());
        System.out.println("status message:" + response.getStatusLine().getReasonPhrase());
        HttpEntity entity = response.getEntity();
        System.out.println("result:" + EntityUtils.toString(entity));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (response != null) {
          try {
            response.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (client != null) {
        try {
          client.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static String generate(
    String method, String uri, String secretKey, String accessKey
  ) throws Exception {

    long timestamp = System.currentTimeMillis();
    String message = method + " " + uri + "\n" + timestamp + "\n" + accessKey;

    Mac hasher = Mac.getInstance("HmacSHA256");
    hasher.init(new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256"));
    byte[] hash = hasher.doFinal(message.getBytes("UTF-8"));
    String signature = Base64.getEncoder().encodeToString(hash);

    return "CEA algorithm=HmacSHA256, access-key=" + accessKey +
      ", signed-date=" + timestamp +
      ", signature=" + signature;
  }

}
