package com.arthmate.enachapi.dto;

import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.EnachReqResLog;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EnachStatusResponseDto {

    private EnachDetail enachDetail;
    private List<EnachReqResLog> enachReqResLog;

}
