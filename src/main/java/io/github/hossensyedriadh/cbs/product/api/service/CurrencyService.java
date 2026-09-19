package io.github.hossensyedriadh.cbs.product.api.service;

import io.github.hossensyedriadh.cbs.product.api.command.CreateCurrencyCommand;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;

import java.util.List;
import java.util.Optional;

public interface CurrencyService {
    List<CurrencyView> getCurrencies();

    Optional<CurrencyView> getCurrency(Long id);

    Optional<CurrencyView> getCurrencyByCode(String code);

    CurrencyView createCurrency(CreateCurrencyCommand command);

    void activateCurrency(Long id);

    void deactivateCurrency(Long id);
}
