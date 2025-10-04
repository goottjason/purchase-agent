package com.jason.purchase_agent.util.http;

import org.apache.http.client.utils.URIBuilder;

import java.util.Map;

public class UrlUtils {

    public static String buildPathWithParams(String path, Map<String, String> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setPath(path);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        return uriBuilder.build().toString();
    }

    public static String buildParamsString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder paramString = new StringBuilder("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramString.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        // 마지막 '&' 제거
        paramString.deleteCharAt(paramString.length() - 1);
        return paramString.toString();
    }
}
