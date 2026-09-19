package io.github.hossensyedriadh.cbs.product.api.service;

import io.github.hossensyedriadh.cbs.product.api.command.CreateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;

import java.util.List;
import java.util.Optional;

public interface AccountProductService {
    List<AccountProductView> getAccountProducts();

    Optional<AccountProductView> getProduct(Long id);

    Optional<AccountProductView> getProductByCode(String code);

    AccountProductView createProduct(CreateAccountProductCommand command);

    Optional<AccountProductView> updateProduct(Long id, UpdateAccountProductCommand command);

    void activateProduct(Long id);

    void deactivateProduct(Long id);
}
