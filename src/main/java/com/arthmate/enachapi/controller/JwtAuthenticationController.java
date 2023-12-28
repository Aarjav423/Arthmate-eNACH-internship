package com.arthmate.enachapi.controller;

import com.arthmate.enachapi.dto.SubscriptionTokenReqBody;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import com.arthmate.enachapi.utils.ResponseHandler;
import com.arthmate.enachapi.utils.security.JwtEnachDetailsTokenService;
import com.arthmate.enachapi.utils.security.JwtResponse;
import com.arthmate.enachapi.utils.security.JwtTokenDetails;
import com.arthmate.enachapi.utils.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Validated
public class JwtAuthenticationController {

    private final JwtTokenUtil jwtTokenUtil;
    private final JwtEnachDetailsTokenService jwtEnachDetailsTokenService;
    private final EnachDetailsRepo enachDetailsRepo;

    private static final String SUCCESS = "Success";

    @Value("${enach.redirect.url}")
    private String redirectWebUrl;

    /**
     * Create token and url which redirects to nach internal ui, it is also exposed to external party
     *
     * @param requestId of the enach for which to generate token
     * @return response containing token and webUrl
     */
    @GetMapping("/enach-token/{requestId}")
    @PreAuthorize("hasAuthority('nach-ext-token')")
    public ResponseEntity<Object> getEnachAccessToken(@PathVariable  String requestId ) {
        log.info("Generate E-nach token by request Id {}",requestId);

        List<String> missingFields = enachDetailsRepo.getMissingFieldsForToken(requestId);
        if (!missingFields.isEmpty()) {
            String errorMessage = "The missing fields in " + requestId + ": "
                    + String.join(", ", missingFields);
            return ResponseHandler.responseBuilder("Error", HttpStatus.BAD_REQUEST, errorMessage);
        }

        final JwtTokenDetails jwtTokenDetails = jwtEnachDetailsTokenService.loadEnachDetailsByRequestId(requestId);

        final String token = jwtTokenUtil.generateTokenForEnachDetails(jwtTokenDetails);
        JwtResponse response = new JwtResponse(token, redirectWebUrl);
        return ResponseHandler.responseBuilder(SUCCESS, HttpStatus.OK,response);
    }

    /**
     * Create token for accessing all the apis exposed to external parties
     *
     * @param req containing data using which to generate token
     * @return response containing token
     */
    @PostMapping("/generate-token")
    @PreAuthorize("hasAuthority('nach-ext-token')")
    public ResponseEntity<Object> getSubscriptionToken(@Valid @RequestBody SubscriptionTokenReqBody req) {
        try {
            String token = jwtTokenUtil.generateExternalToken(req);
            return ResponseHandler.responseBuilder(SUCCESS, HttpStatus.OK, Map.of("token", token));
        } catch (Exception e) {
            log.error("Error while generating external token");
            return ResponseHandler.responseBuilder("Error while generating subscription token",
                    HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

}