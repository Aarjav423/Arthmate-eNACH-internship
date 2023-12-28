package com.arthmate.enachapi.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class EnachRequestStatusClient {

    private final WebClient webClient;

    @Value("${enach.txn.status.for.merchant.url}")
    private String enachTxnStatusForMerchantUrl;
    @Value("${enach.response.posted.to.merchant.url}")
    private String enachResponsePostedToMerchantUrl;

    public EnachRequestStatusClient(WebClient.Builder webClientBuilder) {
        this.webClient = WebClient.builder().defaultHeaders(getHeaders()).build();
    }

    private Consumer<HttpHeaders> getHeaders() {
        return httpHeaders -> {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        };
    }

    public String callTxnStatusForMerchantApi(String requestBody) {
        log.info("Txn status for merchant api request: {}", requestBody);
        String responseBody = null;
        try {
            responseBody = webClient.post()
                    .uri(new URI(enachTxnStatusForMerchantUrl))
                    .body(Mono.just(requestBody), String.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Txn status for merchant api response: {}", responseBody);
        } catch (WebClientResponseException wce) {
            log.error("Web Client Error while hitting the Txn Status For Merchant API, statusCode: {}" +
                    " responseBody: {}", wce.getStatusCode(), wce.getResponseBodyAsString());
            return wce.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Error while hitting the Txn Status For Merchant API, Exception {} ",e.getMessage());
        }
        return responseBody;
    }

    public String callResPostedToMerchantApi(String requestBody) {
        log.info("Response posted to merchant api request: {}", requestBody);
        String responseBody = null;
        try {
            responseBody = webClient.post()
                    .uri(new URI(enachResponsePostedToMerchantUrl))
                    .body(Mono.just(requestBody), String.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Response posted to merchant api response: {}", responseBody);
        } catch (WebClientResponseException wce) {
            log.error("Web Client Error while hitting the Response Posted to Merchant API, statusCode: {}" +
                    " responseBody: {}", wce.getStatusCode(), wce.getResponseBodyAsString());
            return wce.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Error while hitting the Response Posted to Merchant API, Exception {} ",e.getMessage());
        }
        return responseBody;
    }

}
