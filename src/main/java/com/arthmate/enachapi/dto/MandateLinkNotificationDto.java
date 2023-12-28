package com.arthmate.enachapi.dto;

import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.secondary.model.DocUrlMapping;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MandateLinkNotificationDto {

    private EnachDetail enachDetail;
    private DocUrlMapping docUrlMapping;

}
