package io.github.hossensyedriadh.cbs.eod.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStepStatus;
import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
public class EodStepResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 6155594050415505712L;

    private EodStep step;

    private EodStepStatus status;

    @JsonProperty("error_message")
    private String errorMessage;
}
