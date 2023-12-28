package com.arthmate.enachapi.utils.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.arthmate.enachapi.dto.SubscriptionTokenReqBody;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Component
@RequiredArgsConstructor
public class JwtTokenUtil implements Serializable {

    private final JwtEnachDetailsTokenService jwtEnachDetailsTokenService;

    private static final long serialVersionUID = -2550185165626007488L;

    @Value("${internal.token.validity.minutes}")
    private long internalTokenValidityMins;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${external.token.expiry.enabled}")
    private boolean tokenExpiryEnabled;
    @Value("${external.token.expiry.minutes}")
    private long tokenExpiryMinutes;

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //for retrieveing any information from token we will need the secret key
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    //generate token for user for custom String
    public String generateTokenForEnachDetails(JwtTokenDetails jwtTokenDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("source", "nach_internal_ui");
        claims.put("scope", Arrays.asList("nach-int-readwrite"));
        if(jwtTokenDetails != null)
            claims.put("MandtReqid",jwtTokenDetails.getReqsuestId() );
        return doGenerateToken(claims, jwtTokenDetails.getReqsuestId());
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization
    //   compaction of the JWT (not JWS) to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (internalTokenValidityMins * 60 * 1000)))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public String generateExternalToken(SubscriptionTokenReqBody tokenDetails) {
        if (tokenDetails == null) {
            throw new RuntimeException("Token details not available to generate token");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("company_id", tokenDetails.getCompanyId());
        claims.put("user_id", tokenDetails.getUserId());
        claims.put("source", tokenDetails.getSource());
        claims.put("scope", tokenDetails.getScope());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(tokenExpiryEnabled
                        ? new Date(System.currentTimeMillis() + (tokenExpiryMinutes * 60 * 1000)) : null)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    //validate token
    public Boolean validateRequestId(String requestId) {
        JwtTokenDetails jwtTokenDetails = jwtEnachDetailsTokenService.loadEnachDetailsByRequestId(requestId);
        return (requestId.equals(jwtTokenDetails.getReqsuestId()));
    }

    public String getRequestIdByAuthenticationObj(Authentication authentication){
        Object principal = authentication.getPrincipal();
        String reqsuestId = principal.toString();
        if (principal instanceof JwtTokenDetails)
             reqsuestId = ((JwtTokenDetails)principal).getReqsuestId();
        return reqsuestId;
    }

    public String getCompanyIdByAuthenticationObj(Authentication authentication){
        Object principal = authentication.getPrincipal();
        String companyId = principal.toString();
        if (principal instanceof JwtTokenDetails)
            companyId = ((JwtTokenDetails)principal).getCompanyId();
        return companyId;
    }

    public String getUserIdByAuthenticationObj(Authentication authentication){
        Object principal = authentication.getPrincipal();
        String userId = principal.toString();
        if (principal instanceof JwtTokenDetails)
            userId = ((JwtTokenDetails)principal).getUserId();
        return userId;
    }
}
