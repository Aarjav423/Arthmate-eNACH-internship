package com.arthmate.enachapi.utils.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
public class JwtResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -8091879091924046844L;
    private final String token;

    private final String webUrl;

    public JwtResponse(String token, String webUrl) {
        this.token = token;
        this.webUrl = webUrl+token;
    }

}

