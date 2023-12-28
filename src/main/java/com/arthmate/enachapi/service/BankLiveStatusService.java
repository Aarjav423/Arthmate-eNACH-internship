package com.arthmate.enachapi.service;

import com.arthmate.enachapi.model.LiveBankStatusResponseBody;
import com.arthmate.enachapi.model.ResponseBodyBankLiveStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BankLiveStatusService {
    private WebClient webClient;

    public BankLiveStatusService() {
        this.webClient = WebClient.builder()
                .defaultHeaders(getHeaders())
                .exchangeStrategies(getExchangeStrategies())
                .build();
    }

    private Consumer<HttpHeaders> getHeaders() {
        return httpHeaders -> {
            httpHeaders.setContentType(MediaType.valueOf("application/json; charset=utf-8"));
            httpHeaders.setAccept(Collections.singletonList(MediaType.valueOf("application/json; charset=utf-8")));
        };
    }

    private ExchangeStrategies getExchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();
    }

    public Map<String, LiveBankStatusResponseBody> getNPCIBankStatus(String liveBankStatusUri) {
        Map<String, LiveBankStatusResponseBody> response = new TreeMap<>();
        try {
            ResponseBodyBankLiveStatus response1 = webClient.get()
                    .uri(new URI(liveBankStatusUri))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ResponseBodyBankLiveStatus.class)
                    .block();

            if (response1 != null && response1.getLiveBankList() != null) {
                response = response1.getLiveBankList().stream()
                        .filter(this::addBankDetailForRequiredData)
                        .collect(Collectors.toMap(
                                LiveBankStatusResponseBody::getBankId,
                                Function.identity(),
                                (oldValue, newValue) -> oldValue,
                                TreeMap::new
                        ));
            }
        } catch (WebClientRequestException ex) {
            log.error("Error while hitting the Live Bank status API, Exception {} ", ex.getMessage());
        } catch (WebClientResponseException ex) {
            log.error("Error while hitting the Live Bank status API, Exception {} ", ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error while hitting the Live Bank status API, Exception {} ", e.getMessage());
        }
        LiveBankStatusResponseBody onmg = new LiveBankStatusResponseBody();
        onmg.setBankId("ONMG");
        onmg.setBankName("ONMG");
        onmg.setAccessMode(new ArrayList<String>(){{add("A");add("N");add("D");}});
        response.put("ONMG", onmg);
        return response;
    }

    private boolean addBankDetailForRequiredData(LiveBankStatusResponseBody bankStatus) {
        return !bankStatus.getAccessMode().isEmpty();
    }
}
