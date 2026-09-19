package io.github.hossensyedriadh.cbs.product.api.service;

import io.github.hossensyedriadh.cbs.product.api.command.CreateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.ChequeProductView;

import java.util.List;
import java.util.Optional;

public interface ChequeProductService {
    List<ChequeProductView> getChequeProducts();

    Optional<ChequeProductView> getChequeProduct(Long id);

    Optional<ChequeProductView> getChequeProductByCode(String code);

    ChequeProductView createProduct(CreateChequeProductCommand command);

    Optional<ChequeProductView> updateProduct(Long id, UpdateChequeProductCommand command);

    void activateProduct(Long id);
    
    void deactivateProduct(Long id);
}
