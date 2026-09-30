package co.lettermint.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from sending an email.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendEmailResponse {

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("sandbox")
    private Boolean sandbox;

    @JsonProperty("sandbox_result")
    private String sandboxResult;

    public SendEmailResponse() {
    }

    public SendEmailResponse(String messageId, String status) {
        this.messageId = messageId;
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getSandbox() {
        return sandbox;
    }

    public void setSandbox(Boolean sandbox) {
        this.sandbox = sandbox;
    }

    public String getSandboxResult() {
        return sandboxResult;
    }

    public void setSandboxResult(String sandboxResult) {
        this.sandboxResult = sandboxResult;
    }

    @Override
    public String toString() {
        return "SendEmailResponse{" +
                "messageId='" + messageId + '\'' +
                ", status='" + status + '\'' +
                ", sandbox=" + sandbox +
                ", sandboxResult='" + sandboxResult + '\'' +
                '}';
    }
}
