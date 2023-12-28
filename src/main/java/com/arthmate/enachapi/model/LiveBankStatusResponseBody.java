package com.arthmate.enachapi.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LiveBankStatusResponseBody {
    private String bankId;
    private String bankName;
    private List<String> accessMode;

    public LiveBankStatusResponseBody() {
        accessMode = new ArrayList<>();
    }

    public void setNetbankFlag(String netbankFlag) {
        updateAccessMode("N", netbankFlag);
    }

    public void setDebitcardFlag(String debitcardFlag) {
        updateAccessMode("D", debitcardFlag);
    }

    public void setAadhaarFlag(String aadhaarFlag) {
        updateAccessMode("A", aadhaarFlag);
    }

    private void updateAccessMode(String accessType, String accessFlag) {
        if (accessFlag.equalsIgnoreCase("Active")) {
            accessMode.add(accessType);
        } else if (accessFlag.equalsIgnoreCase("Inactive")) {
            accessMode.remove(accessType);
        }
    }
}
