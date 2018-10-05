package com.axzae.homeassistant.provider;

import android.annotation.SuppressLint;

import com.axzae.homeassistant.BuildConfig;
import com.axzae.homeassistant.util.CommonUtil;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ServiceProvider {
    private static final int CONNECTION_TIMEOUT_IN_SEC = 5;
    private static final int DEFAULT_READ_TIMEOUT_IN_SEC = 30;
    private static OkHttpClient okHttpClient;
    private static OkHttpClient wsOkHttpClient;
    private static OkHttpClient glideOkHttpClient;
    @SuppressLint("UseSparseArrays") private static HashMap<String, RestAPIService> apiServices = new HashMap<>();

    public static OkHttpClient getWebSocketOkHttpClientInstance() {
        if (wsOkHttpClient == null) {
            wsOkHttpClient = getOkHttpClientInstance(CONNECTION_TIMEOUT_IN_SEC, DEFAULT_READ_TIMEOUT_IN_SEC);
        }
        return wsOkHttpClient;
    }

    public static OkHttpClient getGlideOkHttpClientInstance() {
        if (glideOkHttpClient == null) {
            glideOkHttpClient = getOkHttpClientInstance(CONNECTION_TIMEOUT_IN_SEC, DEFAULT_READ_TIMEOUT_IN_SEC);
        }

        return glideOkHttpClient;
    }

    private static OkHttpClient getOkHttpClientInstance(final int readTimeoutInSec) {
        if (okHttpClient == null) {
            okHttpClient = getOkHttpClientInstance(CONNECTION_TIMEOUT_IN_SEC, readTimeoutInSec);
        }
        return okHttpClient;
    }

    @SuppressWarnings("SameParameterValue")
    private static OkHttpClient getOkHttpClientInstance(int connectTimeoutInSec, int readTimeoutInSec) {

        try {

            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeoutInSec, TimeUnit.SECONDS)
                    .readTimeout(readTimeoutInSec, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @SuppressLint("BadHostnameVerifier")
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

            if (!BuildConfig.BUILD_TYPE.equals("release")) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(interceptor);
            }


//            builder.networkInterceptors().add(new Interceptor() {
//                @Override
//                public Response intercept(Chain chain) throws IOException {
//                    Request request = chain.request().newBuilder().addHeader("x-request-date", new SimpleDateFormat(CommonUtil.DATE_FORMAT_UTC_MS, Locale.ENGLISH).format(new Date())).build();
//                    return chain.proceed(request);
//                }
//            });
            return builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static RestAPIService getApiService() {
        return getApiService("http://www.example.com");
    }

    public static RestAPIService getApiService(final String baseUrl) {
        //if (apiServices.get(baseUrl) == null) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(CommonUtil.getGson()))
                .client(getOkHttpClientInstance(DEFAULT_READ_TIMEOUT_IN_SEC))
                .build();

        //    apiServices.put(baseUrl, retrofit.create(RestAPIService.class));
        //}
        //return apiServices.get(baseUrl);
        return retrofit.create(RestAPIService.class);
    }

    public static RestAPIService getRawApiService(final String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(getOkHttpClientInstance(DEFAULT_READ_TIMEOUT_IN_SEC))
                .build();
        return retrofit.create(RestAPIService.class);
    }

}
