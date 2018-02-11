package com.axzae.homeassistant;

public class TestAppController extends AppController {
  private String baseUrl;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String url) {
    baseUrl = url;
  }
}