package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CbsTransactionPage implements Serializable {
    @Serial
    private static final long serialVersionUID = 2295028558160027713L;

    private List<CbsTransactionResponse> content;

    private PageModel page;
}
