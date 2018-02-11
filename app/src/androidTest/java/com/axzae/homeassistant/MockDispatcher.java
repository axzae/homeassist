package com.axzae.homeassistant;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.axzae.homeassistant.util.CommonUtil;

import org.json.JSONObject;

import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

public class MockDispatcher extends Dispatcher {
    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        try {
            String body = (request.getRequestLine().startsWith("POST")) ? request.getBody().readUtf8() : null;
            String requests[] = request.getRequestLine().split(" ");
            String requestMethod = requests[0];
            return processRequest(requestMethod, URI.create(requests[1]), body);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MockResponse().setResponseCode(444);
    }

    private MockResponse processRequest(String requestMethod, URI uri, String body) throws Exception {

        switch (uri.getPath()) {
            case "/api/bootstrap": {
                return new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .setBody(CommonUtil.readFromAssets(InstrumentationRegistry.getContext(), "debug.json"));
            }

            default:
                Log.d("YouQi", "UNKNOWN MOCK");
                Log.d("YouQi", "requestMethod" + requestMethod);
                Log.d("YouQi", "uri" + uri.toString());
                Log.d("YouQi", "body" + body);
        }

        return null;
    }

    private MockResponse handleOAuthToken(String requestMethod, URI uri, String body) throws Exception {
        if (!"POST".equals(requestMethod)) throw new RuntimeException(uri + " IS NOT POST");

        Map pairs = getQueryPairs(body);
        if (pairs.get("grant_type").equals("password")
                && pairs.get("username").equals("test")
                && pairs.get("password").equals("test")) {
            return new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .setBody(new JSONObject()
                            .put("expires_in", 900)
                            .put("token_type", "Bearer")
                            .toString());
        }
    }

    private Map getQueryPairs(String queryString) {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        try {
            if (queryString == null) throw new RuntimeException("QueryString is NULL");
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }

            Log.d("YouQi", "queryPairs:" + queryPairs.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return queryPairs;
    }
}