package io.github.hossensyedriadh.cbs.cheque.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeReturnReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class ReturnChequeCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -9011920953015952134L;

    @NotNull
    @JsonProperty("presentation_id")
    private Long presentationId;

    @NotNull
    private ChequeReturnReason reason;
}
