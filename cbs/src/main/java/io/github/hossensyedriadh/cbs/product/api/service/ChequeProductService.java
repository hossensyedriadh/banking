package io.github.hossensyedriadh.cbs.product.api.service;

import io.github.hossensyedriadh.cbs.product.api.command.CreateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.ChequeProductView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface ChequeProductService {
    List<ChequeProductView> getChequeProducts();

    Optional<ChequeProductView> getChequeProduct(Long id);

    Optional<ChequeProductView> getChequeProductByCode(String code);

    ChequeProductView createProduct(@Valid CreateChequeProductCommand command);

    ChequeProductView updateProduct(Long id, @Valid UpdateChequeProductCommand command);

    void activateProduct(Long id);

    void deactivateProduct(Long id);
}
