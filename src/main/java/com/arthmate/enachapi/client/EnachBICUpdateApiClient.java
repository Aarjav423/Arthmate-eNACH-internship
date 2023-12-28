package com.arthmate.enachapi.client;

import com.arthmate.enachapi.dto.BICReqDto;
import com.arthmate.enachapi.dto.BICResponseDto;
import com.arthmate.enachapi.dto.EnachDtlRespBdy;
import com.arthmate.enachapi.dto.EnachDtlRqstBdy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class EnachBICUpdateApiClient {

    private final WebClient webClient;

    @Value("${enach.bic.api.token}")
    private String enachBICApiToken;

    @Value("${enach.bic.api.url}")
    private String enachBICUrl;

    EnachBICUpdateApiClient() {
        this.webClient = WebClient.builder().build();
    }

    public BICResponseDto updateBICApi(BICReqDto request) {
        log.info("BIC api request payload: {}", request);
        BICResponseDto response = webClient.patch()
                .uri(UriComponentsBuilder.fromUriString(enachBICUrl).toUriString())
                .headers(getUtilApiHeaders())
                .body(Mono.just(request), BICReqDto.class)
                .retrieve()
                .bodyToMono(BICResponseDto.class)
                .block();
        log.info("BIC api response: {}", response);
        return response;
    }

    private Consumer<HttpHeaders> getUtilApiHeaders() {
        return headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("Authorization", enachBICApiToken);
        };
    }
}
