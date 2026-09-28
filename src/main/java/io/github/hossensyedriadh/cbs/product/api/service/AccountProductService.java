package io.github.hossensyedriadh.cbs.product.api.service;

import io.github.hossensyedriadh.cbs.product.api.command.CreateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface AccountProductService {
    List<AccountProductView> getAccountProducts();

    List<AccountProductView> getActiveAndInterestBearingAccountProducts(List<Long> ids);

    Optional<AccountProductView> getProduct(Long id);

    Optional<AccountProductView> getProductByCode(String code);

    AccountProductView createProduct(@Valid CreateAccountProductCommand command);

    AccountProductView updateProduct(Long id, @Valid UpdateAccountProductCommand command);

    void activateProduct(Long id);

    void deactivateProduct(Long id);
}
