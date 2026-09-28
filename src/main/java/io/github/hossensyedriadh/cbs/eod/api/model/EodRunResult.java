package io.github.hossensyedriadh.cbs.eod.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodRunStatus;
import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class EodRunResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -3016675634599818437L;

    @JsonProperty("run_id")
    private Long runId;

    @JsonProperty("business_date")
    private LocalDate businessDate;

    private EodRunStatus status;

    private List<EodStepResult> steps;
}
