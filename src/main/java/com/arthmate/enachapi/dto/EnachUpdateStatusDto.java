package com.arthmate.enachapi.dto;

import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.EnachDetailHistory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnachUpdateStatusDto {
    private EnachDetail enachDetail;
    private EnachDetailHistory enachDetailHistory;
}
