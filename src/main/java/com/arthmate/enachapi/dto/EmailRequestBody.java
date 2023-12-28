package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmailRequestBody {

    @JsonProperty("from")
    private String from;
    @JsonProperty("to")
    private List<String> to;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("mail_body")
    private String mailBody;
    @JsonProperty("is_html_content")
    private boolean isHtmlContent;
    @JsonProperty("attachments")
    private List<EmailAttachment> attachments;

    @Data
    @Builder
    public static class EmailAttachment {
        @JsonProperty("filename")
        private String filename;
        @JsonProperty("file_base64")
        private String fileBase64;
    }

}
