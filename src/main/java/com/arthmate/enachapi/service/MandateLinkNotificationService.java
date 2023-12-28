package com.arthmate.enachapi.service;

import com.arthmate.enachapi.client.UtilApiClient;
import com.arthmate.enachapi.dto.*;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.SingleDataTranslation;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import com.arthmate.enachapi.repo.SingleDataTranslationRepo;
import com.arthmate.enachapi.secondary.model.DocUrlMapping;
import com.arthmate.enachapi.secondary.repo.DocUrlMappingRepo;
import com.arthmate.enachapi.utils.security.JwtTokenDetails;
import com.arthmate.enachapi.utils.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.arthmate.enachapi.utils.ApplicationConstants.NACH_MANDATE_LINK_NOTIFICATION_BATCH;

@Slf4j
@Service
@RequiredArgsConstructor
public class MandateLinkNotificationService {

    private final UtilApiClient utilApiClient;
    private final JwtTokenUtil jwtTokenUtil;
    private final EnachDetailsRepo enachDetailsRepo;
    private final DocUrlMappingRepo docUrlMappingRepo;
    private final SingleDataTranslationRepo singleDataTranslationRepo;
    private String mandateLink;
    private int retries = 0;
    private DocUrlMapping shortUrlMapping;

    @Value("${enach.redirect.url}")
    private String redirectWebUrl;
    @Value("${mandate.link.notification.email.from}")
    private String emailFrom;
    @Value("${mandate.link.notification.email.subject}")
    private String emailSubject;
    @Value("${mandate.link.notification.email.body}")
    private String emailBody;
    @Value("${mandate.link.notification.sms.sender}")
    private String smsSender;
    @Value("${mandate.link.notification.sms.body}")
    private String smsBody;
    @Value("${mandate.link.notification.retry.limit}")
    private int retryLimit;
    @Value("${short.url.base}")
    private String shortUrlBase;
    @Value("${sms.complain.toll-free-number}")
    private String tollFreeNumber;
    @Value("${sms.mandate.initiated}")
    private String mandateInitiatedSms;
    @Value("${sms.mandate.initiated.from}")
    private String mandateInitiatedSmsFrom;

    public MandateLinkNotificationDto sendMandateLinkNotification(EnachDetail enachDetail) {
        log.info("Sending mandate initiated, link notification for requestId: {}", enachDetail.getRequestId());
        mandateLink = null;
        shortUrlMapping = null;
        retries = enachDetail.getMandateLinkNotificationRetries() + 1;

        // send mandate initiation sms if not already sent
        if (!enachDetail.isMandateInitiatedSmsSent()) {
            enachDetail = sendMandateInitiatedSms(enachDetail);
        }

        // send mandate link notification email if not already sent
        if (enachDetail.isEmailRequired() && !enachDetail.isEmailSent()) {
            enachDetail = sendEmail(enachDetail);
        }

        // send mandate link notification sms if not already sent
        if (enachDetail.isSmsRequired() && !enachDetail.isSmsSent()) {
            enachDetail = sendSms(enachDetail);
        }

        // reset retry count if all notifications are sent successfully
        if (enachDetail.isMandateInitiatedSmsSent()
                && (enachDetail.isEmailRequired() == enachDetail.isEmailSent())
                && (enachDetail.isSmsRequired() == enachDetail.isSmsSent())) {
            enachDetail.setMandateLinkNotificationRetries(0);
        }

        return MandateLinkNotificationDto.builder()
                .enachDetail(enachDetail)
                .docUrlMapping(shortUrlMapping)
                .build();
    }

    public EnachDetail sendMandateInitiatedSms(EnachDetail enach) {
        String remarks = "";
        try {
            SmsResponseBody response = utilApiClient.callSmsApi(buildMandateInitiatedSmsRequestBody(enach));
            return processMandateInitiatedSmsResponse(enach, response);
        } catch (WebClientResponseException wce) {
            log.error("WebClientResponseException while calling sms api. Response body: {}",
                    wce.getResponseBodyAsString(), wce);
            remarks = "Api failure responseCode: " + wce.getStatusCode() + ", responseBody: "
                    + wce.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Error while sending mandate initiated sms for request id: {} :: {}",
                    enach.getRequestId(), e.getMessage());
            remarks = e.getMessage();
        }
        enach.setMandateInitiatedSmsSent(false);
        enach.setMandateInitiatedSmsRemarks(checkRetryLimit(enach, remarks));
        return enach;
    }

    private SmsRequestBody buildMandateInitiatedSmsRequestBody(EnachDetail enach) {
        Optional<SingleDataTranslation> purposeOpt = singleDataTranslationRepo
                .findByKeyAndType("enach_purpose", enach.getPurposeOfMandate());
        String purpose;
        if (purposeOpt.isPresent()) {
            purpose = purposeOpt.get().getValue();
        } else {
            purpose = enach.getPurposeOfMandate();
        }

        Optional<SingleDataTranslation> frequencyOpt = singleDataTranslationRepo
                .findByKeyAndType("enach_frequency", enach.getEmiFrequency());
        String frequency;
        if (frequencyOpt.isPresent()) {
            frequency = frequencyOpt.get().getValue();
        } else {
            frequency = enach.getEmiFrequency();
        }

        String message = MessageFormat.format(mandateInitiatedSms, enach.getCorporateName(),
                enach.getAccountNo().substring(enach.getAccountNo().length() - 4),
                purpose, frequency, tollFreeNumber);

        return SmsRequestBody.builder()
                .from(mandateInitiatedSmsFrom)
                .recipient(enach.getCustomerMobileNo())
                .message(message)
                .build();
    }

    private EnachDetail processMandateInitiatedSmsResponse(EnachDetail enach, SmsResponseBody response) {
        if (StringUtils.isNotBlank(response.getStatusCode())
                && response.getStatusCode().matches("^2\\d{2}$")) {
            enach.setMandateInitiatedSmsSent(true);
            enach.setMandateInitiatedSmsRemarks("Success");
            enach.setMandateInitiatedSmsSentAt(LocalDateTime.now());
            enach.setMandateInitiatedSmsTransactionId(response.getTransactionId());
            return enach;
        } else {
            throw new RuntimeException("Api response: " + response);
        }
    }

    private EnachDetail sendEmail(EnachDetail enachDetail) {
        String remarks = "";
        try {
            EmailResponseBody response = utilApiClient.callSendEmailApi(buildEmailRequestBody(enachDetail));
            return processEmailResponse(enachDetail, response);
        } catch (WebClientResponseException wce) {
            log.error("WebClientResponseException while calling email api. Response body: {}",
                    wce.getResponseBodyAsString(), wce);
            remarks = "Api failure responseCode: " + wce.getStatusCode() + ", responseBody: "
                    + wce.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Error in sending email for requestId: {}", enachDetail.getRequestId(), e);
            remarks = e.getMessage();
        }
        enachDetail.setEmailSent(false);
        enachDetail.setEmailRemarks(checkRetryLimit(enachDetail, remarks));
        return enachDetail;
    }

    private EnachDetail processEmailResponse(EnachDetail enachDetail, EmailResponseBody response) {
        if (response.isStatus()) {
            enachDetail.setEmailSent(true);
            enachDetail.setEmailRemarks("Success");
            enachDetail.setEmailSentCount(enachDetail.getEmailSentCount() + 1);
            enachDetail.setLastEmailSentAt(LocalDateTime.now());
            return enachDetail;
        } else {
            throw new RuntimeException("Api response: " + response);
        }
    }

    private EmailRequestBody buildEmailRequestBody(EnachDetail enachDetail) {
        String email = String.format(emailBody, enachDetail.getCustomerName(), getMandateLink(enachDetail.getRequestId()));
        return EmailRequestBody.builder()
                .from(emailFrom)
                .to(List.of(enachDetail.getCustomerEmailId()))
                .subject(emailSubject)
                .mailBody(email)
                .build();
    }

    private EnachDetail sendSms(EnachDetail enachDetail) {
        String remarks = "";
        try {
            SmsResponseBody response = utilApiClient.callSmsApi(buildSmsReqeustBody(enachDetail));
            return processSmsResponse(enachDetail, response);
        } catch (WebClientResponseException wce) {
            log.error("WebClientResponseException while calling sms api. Response body: {}",
                    wce.getResponseBodyAsString(), wce);
            remarks = "Api failure responseCode: " + wce.getStatusCode() + ", responseBody: "
                    + wce.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Error in sending sms for requestId: {}", enachDetail.getRequestId(), e);
            remarks = e.getMessage();
        }
        enachDetail.setSmsSent(false);
        enachDetail.setSmsRemarks(checkRetryLimit(enachDetail, remarks));
        return enachDetail;
    }

    private EnachDetail processSmsResponse(EnachDetail enachDetail, SmsResponseBody response) {
        if (StringUtils.isNotBlank(response.getStatusCode())
                && response.getStatusCode().matches("^2\\d{2}$")) {
            enachDetail.setSmsSent(true);
            enachDetail.setSmsRemarks("Success");
            enachDetail.setSmsSentCount(enachDetail.getSmsSentCount() + 1);
            enachDetail.setLastSmsSentAt(LocalDateTime.now());
            enachDetail.setSmsTransactionId(response.getTransactionId());
            return enachDetail;
        } else {
            throw new RuntimeException("Api response: " + response);
        }
    }

    private SmsRequestBody buildSmsReqeustBody(EnachDetail enachDetail) {
        return SmsRequestBody.builder()
                .from(smsSender)
                .recipient(enachDetail.getCustomerMobileNo())
                .message(String.format(smsBody, getMandateLink(enachDetail.getRequestId())))
                .build();
    }

    private String getMandateLink(String requestId) {
        if (mandateLink == null) {
            JwtTokenDetails jwtTokenDetails = JwtTokenDetails.builder().reqsuestId(requestId).build();
            String token = jwtTokenUtil.generateTokenForEnachDetails(jwtTokenDetails);
            String webUrl = redirectWebUrl.concat(token);
            mandateLink = createShortenedUrl(requestId, webUrl);
        }
        return mandateLink;
    }

    private String createShortenedUrl(String requestId, String url) {
        String key;
        Optional<DocUrlMapping> mappingOpt;

        do {
            key = RandomStringUtils.randomAlphanumeric(6);
            mappingOpt = docUrlMappingRepo.findByDocumentId(key);
        } while (mappingOpt.isPresent());

        // Create the mongodb mapping with the document id
        shortUrlMapping = DocUrlMapping.builder()
                .documentId(key)
                .documentUrl(url)
                .nachRequestId(requestId)
                .createdBy(NACH_MANDATE_LINK_NOTIFICATION_BATCH)
                .createdAt(LocalDateTime.now())
                .build();

        return shortUrlBase.concat(key);
    }

    private String checkRetryLimit(EnachDetail enachDetail, String remarks) {
        enachDetail.setMandateLinkNotificationRetries(retries);
        if (retries >= retryLimit) {
            return "Retry limit reached. ".concat(remarks);
        } else {
            return remarks;
        }
    }

    public void updateMandateLinkNotificationStatus(List<? extends MandateLinkNotificationDto> items) {
        List<Pair<Query, Update>> querUpdateList = items.stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getEnachDetail()))
                .map(MandateLinkNotificationDto::getEnachDetail)
                .map(this::getQueryUpdatePair)
                .toList();
        int result = enachDetailsRepo.updateByQueryUpdatePairs(querUpdateList);
        log.info("Number of enach details updated for mandate link notification: {}", result);

        List<DocUrlMapping> docUrlMappingList = items.stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getDocUrlMapping()))
                .map(MandateLinkNotificationDto::getDocUrlMapping)
                .toList();
        int result2 = docUrlMappingRepo.insert(docUrlMappingList);
        log.info("Number of doc url mappings inserted for short url: {}", result2);
    }

    private Pair<Query, Update> getQueryUpdatePair(EnachDetail enachDetail) {
        Update update = Update.update("mandate_link_notification_retries", enachDetail.getMandateLinkNotificationRetries())
                .set("updated_at", LocalDateTime.now())
                .set("updated_by", NACH_MANDATE_LINK_NOTIFICATION_BATCH)
                .set("is_mandate_initiated_sms_sent", enachDetail.isMandateInitiatedSmsSent())
                .set("mandate_initiated_sms_remarks", enachDetail.getMandateInitiatedSmsRemarks());

        if (enachDetail.isMandateInitiatedSmsSent()) {
            update.set("mandate_initiated_sms_sent_at", enachDetail.getMandateInitiatedSmsSentAt())
                    .set("mandate_initiated_sms_transaction_id", enachDetail.getMandateInitiatedSmsTransactionId());
        }
        if (enachDetail.isEmailRequired()) {
            update.set("is_email_sent", enachDetail.isEmailSent())
                    .set("email_remarks", enachDetail.getEmailRemarks())
                    .set("email_sent_count", enachDetail.getEmailSentCount())
                    .set("last_email_sent_at", enachDetail.getLastEmailSentAt());
        }
        if (enachDetail.isSmsRequired()) {
            update.set("is_sms_sent", enachDetail.isSmsSent())
                    .set("sms_remarks", enachDetail.getSmsRemarks())
                    .set("sms_sent_count", enachDetail.getSmsSentCount())
                    .set("last_sms_sent_at", enachDetail.getLastSmsSentAt())
                    .set("sms_transaction_id", enachDetail.getSmsTransactionId());
        }
        return Pair.of(
                Query.query(Criteria.where("request_id").is(enachDetail.getRequestId())),
                update
        );
    }

}
