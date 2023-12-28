package com.arthmate.enachapi.utils;

import java.util.HashMap;
import java.util.Map;

public final class ApplicationConstants {

    public static final String REQUEST_ID = "request_id";
    public static final String TRANSACTION_ID = "presentment_txn_id";
    public static final String RESP_SUCCESS = "Success";
    public static final String NCPI_REQUEST_STATUS_BATCH = "NPCI_REQUEST_STATUS_BATCH";
    public static final String TXN_STATUS_FOR_MERCHANT_API = "ENACH_TRANSACTION_STATUS_FOR_MERCHANT_API";
    public static final String RES_POSTED_TO_MERCHANT_API = "ENACH_RESPONSE_POSTED_TO_MERCHANT_API";
    public static final String REQUEST_STATUS_API = "ENACH_REQUEST_STATUS_API";
    public static final String NPCI_CALLBACK_API = "NPCI_CALLBACK_API";
    public static final String REQ_FLG_OPEN = "open";
    public static final String REQ_FLG_ACTIVE = "active";
    public static final String REQ_FLG_SUSPEND = "suspend";
    public static final String REQ_FLG_CANCEL = "cancel";
    public static final String PRODUCTION = "prod";
    public static final String REQ_FLG_SCC = "success";
    public static final String REQ_FLG_FAI = "fail";

    public static final String REQ_REASON_OPEN = "Register NPCI Request";

    public static final String REQ_FLG_INIT = "mandate_initiated";
    public static final String REQ_FLG_SUCCESS = "callback_success";
    public static final String REQ_FLG_FAIL = "callback_fail";

    public static final String REQ_AMND_FLG_REQ = "amend_requested";
    public static final String REQ_AMND_FLG_INIT = "amend_initiated";
    public static final String REQ_AMND_FLG_SUCCESS = "amend_success";
    public static final String REQ_AMND_FLG_FAIL = "amend_fail";
    public static final String REQ_REASON_AMND = "Amend NPCI Request";
    public static final String REQ_CANCEL_FLG_REQ = "cancel_requested";
    public static final String REQ_CANCEL_FLG_INIT = "cancel_initiated";
    public static final String REQ_CANCEL_FLG_SUCCESS = "cancel_success";
    public static final String REQ_CANCEL_FLG_FAIL = "cancel_fail";
    public static final String REQ_REASON_CANCEL = "Cancel NPCI Request";
    public static final String REQ_SUSPEND_FLG = "suspend";
    public static final String REQ_REASON_SUSPEND = "Suspend NPCI Request";

    public static final String REQ_REASON_REVOKE_SUSPEND = "Revoked Suspend NPCI Request";

    public static final Map<String, String> NPCI_CALLBACK_SUCCESS_STATUS = new HashMap<String, String>(){{
        put(REQ_FLG_INIT, REQ_FLG_SUCCESS);
        put(REQ_AMND_FLG_INIT, REQ_AMND_FLG_SUCCESS);
        put(REQ_CANCEL_FLG_INIT, REQ_CANCEL_FLG_SUCCESS);
    }};
    public static final Map<String, String> NPCI_CALLBACK_FAIL_STATUS = new HashMap<String, String>(){{
        put(REQ_FLG_INIT, REQ_FLG_FAIL);
        put(REQ_AMND_FLG_INIT, REQ_AMND_FLG_FAIL);
        put(REQ_CANCEL_FLG_INIT, REQ_CANCEL_FLG_FAIL);
    }};

    public static final String ACT_PATCH_REG = "act_patch_reg";
    public static final String ACT_PATCH_REG_NM = "Patch Request";
    public static final String ACT_PAYLOAD = "act_paylod";
    public static final String ACT_PAYLOAD_NM = "Prepare Paylod";
    public static final String ACT_AMEND = "act_amend";
    public static final String ACT_AMEND_NM = "Amend Mandate Request";
    public static final String ACT_CANCEL = "act_cancel";
    public static final String ACT_CANCEL_NM = "Cancel Mandate Request";
    public static final String ACT_DENIED = "Permission denied";
    public static final String NACH_SCHEDULE_TRANSACTION_BATCH = "NACH_SCHEDULE_TRANSACTION_BATCH";
    public static final String NACH_SCHEDULE_TRANSACTION_STATUS_BATCH = "NACH_SCHEDULE_TRANSACTION_STATUS_BATCH";
    public static final String TRANSACTION_SUCCESS_SMS_BATCH = "TRANSACTION_SUCCESS_SMS_BATCH";

    public static final String ACT_SUSPEND = "act_suspend";
    public static final String ACT_SUSPEND_NM = "Suspend Mandate Request";
    public static final String ACT_REVOKE_SUSPEND = "act_revoke_suspend";
    public static final String ACT_REVOKE_SUSPEND_NM = "Revoke suspend Mandate Request";
    public static final String SEQUENCE_NUMBER = "sequence_no";
    public static final String ACT_USEEXTREFNUM = "act_add_ext_ref_num";
    public static final String ACT_USEEXTREFNUM_NM = "Reuse External Ref Number";
    public static final String NACH_MANDATE_LINK_NOTIFICATION_BATCH = "NACH_MANDATE_LINK_NOTIFICATION_BATCH";
}
