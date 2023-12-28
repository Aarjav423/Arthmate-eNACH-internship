package com.arthmate.enachapi.utils.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;


@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenDetails implements Serializable {
    private static final long serialVersionUID = 570L;

    private String reqsuestId;
    private String companyId;
    private String userId;
    private String source;
    private List<String> scope;
    private Boolean isToken;

}
