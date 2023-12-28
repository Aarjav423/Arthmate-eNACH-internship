package com.arthmate.enachapi.utils.security;

import com.arthmate.enachapi.exception.EnachDetailsNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtEnachDetailsTokenService jwtEnachDetailsTokenService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.replace("Bearer ", "");

            try {
                // this method parses all claims and also checks for expiry of token if 'exp' claim != null
                Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);

                // get the scope from claims and make a list of GrantedAuthority for authorization
                List<GrantedAuthority> authorities = new ArrayList<>();
                List<String> scope = (ArrayList<String>) claims.get("scope");
                if (scope != null) {
                    scope.forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority)));
                }

                // create jwtTokenDetails which will be set to authentication in security context
                JwtTokenDetails jwtTokenDetails = JwtTokenDetails.builder()
                        .reqsuestId(claims.get("MandtReqid", String.class))
                        .companyId(claims.get("company_id", String.class))
                        .userId(claims.get("user_id", String.class))
                        .source(claims.get("source", String.class))
                        .isToken(claims.get("is_token", Boolean.class))
                        .build();


                UsernamePasswordAuthenticationToken authentication = null;

                if (jwtTokenDetails.getIsToken() != null && jwtTokenDetails.getIsToken()) {

                    // this request is for token generation and scope is set for token generation
                    authentication = UsernamePasswordAuthenticationToken
                            .authenticated(null, null, authorities);

                } else if (StringUtils.isNotBlank(jwtTokenDetails.getReqsuestId())) {

                    // this validates requestId which will come in nach ui authentication
                    try{
                        // it validates if the request id exists or not
                        if (jwtTokenUtil.validateRequestId(jwtTokenDetails.getReqsuestId())) {
                            authentication = UsernamePasswordAuthenticationToken
                                    .authenticated(jwtTokenDetails, null, authorities);
                        } else {
                            response.setHeader("authMsg","Enach request id sent in request doesn't exists.");
                        }

                    } catch (EnachDetailsNotFoundException e) {
                        log.warn("Record not found for the parsed request_id from SecurityContext principal.");
                        response.setHeader("authMsg","Enach request id doesn't exists.");
                    }

                } else if (StringUtils.isNotBlank(jwtTokenDetails.getUserId())) {

                    // this sets authentication for requests coming from nach portal
                    // to identify that this request comes from admin portal it checks for userId
                    authentication = UsernamePasswordAuthenticationToken
                            .authenticated(jwtTokenDetails, null, authorities);

                } else {
                    response.setHeader("authMsg","Invalid type of request");
                }

                SecurityContextHolder.getContext().setAuthentication(authentication);


            } catch (MalformedJwtException e) {
                log.warn("Malformed JWT Token");
                response.setHeader("authMsg", "Malformed JWT Token");
            }
            catch (IllegalArgumentException e) {
                log.warn("Unable to get JWT Token");
                response.setHeader("authMsg", "Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                log.warn("JWT Token has expired");
                response.setHeader("authMsg", "JWT Token has expired");
            }
        } else {
            //log.warn("Access Token missing in Header or does not begin with Bearer String");
            response.setHeader("authMsg","Access Token missing in Header or does not begin with Bearer String");
        }

        chain.doFilter(request, response);
    }

}