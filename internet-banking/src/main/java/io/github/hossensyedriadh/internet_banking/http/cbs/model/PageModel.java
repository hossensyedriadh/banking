package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import java.io.Serializable;

public record PageModel(Integer number, Integer size, Long totalElements, Integer totalPages) implements Serializable {
}
