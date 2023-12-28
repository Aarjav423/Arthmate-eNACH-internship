package com.arthmate.enachapi.client;

import com.arthmate.enachapi.dto.EmailRequestBody;
import com.arthmate.enachapi.dto.EmailResponseBody;
import com.arthmate.enachapi.dto.SmsRequestBody;
import com.arthmate.enachapi.dto.SmsResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class UtilApiClient {

    private final WebClient webClient;

    @Value("${util.api.token}")
    private String utilApiToken;
    @Value("${util.api.url.send-email}")
    private String sendEmailUri;
    @Value("${sms.api.token}")
    private String smsApiToken;
    @Value("${sms.api.url}")
    private String smsApiUri;

    UtilApiClient() {
        this.webClient = WebClient.builder().build();
    }

    public String callRestPostApi(String requestBody, String url) {
        log.info("ICICI api url:{} request: {}", url, requestBody);
        String responseBody = null;
        try {
            responseBody = webClient.post()
                    .uri(new URI(url))
                    .body(Mono.just(requestBody), String.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Response from ICICI api : {}", responseBody);
        } catch (WebClientResponseException wce) {
            log.error("Web Client Error while hitting the ICICI request API, statusCode: {}" +
                    " responseBody: {}", wce.getStatusCode(), wce.getResponseBodyAsString());
            return wce.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Error while hitting the ICICI API, Exception {} ", e.getMessage());
        }
        return responseBody;
    }

    public EmailResponseBody callSendEmailApi(EmailRequestBody request) {
        log.info("Send email api request payload: {}", request);
        EmailResponseBody response = webClient.post()
                .uri(UriComponentsBuilder.fromUriString(sendEmailUri).toUriString())
                .headers(getUtilApiHeaders())
                .body(Mono.just(request), EmailRequestBody.class)
                .retrieve()
                .bodyToMono(EmailResponseBody.class)
                .block();
        log.info("Send-email api response: {}", response);
        return response;
    }

    private Consumer<HttpHeaders> getUtilApiHeaders() {
        return headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBasicAuth(utilApiToken);
        };
    }

    public SmsResponseBody callSmsApi(SmsRequestBody request) {
        log.info("Sms api request payload: {}", request);
        SmsResponseBody response = webClient.post()
                .uri(UriComponentsBuilder.fromUriString(smsApiUri).toUriString())
                .headers(getSmsApiHeaders())
                .body(Mono.just(request), SmsRequestBody.class)
                .retrieve()
                .bodyToMono(SmsResponseBody.class)
                .block();
        log.info("Sms api response body: {}", response);
        return response;
    }

    public Consumer<HttpHeaders> getSmsApiHeaders() {
        return headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBasicAuth(smsApiToken);
        };
    }
}
