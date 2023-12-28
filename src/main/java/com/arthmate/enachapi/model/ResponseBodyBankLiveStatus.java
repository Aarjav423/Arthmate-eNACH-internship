package com.arthmate.enachapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBodyBankLiveStatus {
    private List<LiveBankStatusResponseBody> liveBankList;
}
