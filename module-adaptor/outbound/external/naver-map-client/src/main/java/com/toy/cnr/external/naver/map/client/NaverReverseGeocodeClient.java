package com.toy.cnr.external.naver.map.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "naver-reverse-geocode-client", url = "${naver.map.apigw.reverse-geocode}")
public interface NaverReverseGeocodeClient {

    @GetMapping("/gc")
    Map<String, Object> reverseGeocode(
        @RequestHeader("X-NCP-APIGW-API-KEY-ID") String clientId,
        @RequestHeader("X-NCP-APIGW-API-KEY") String clientSecret,
        @RequestParam("coords") String coords,
        @RequestParam("orders") String orders,
        @RequestParam("output") String output
    );
}
