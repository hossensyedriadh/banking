package io.github.hossensyedriadh.cbs.product.api.service;

import io.github.hossensyedriadh.cbs.product.api.command.CreateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.CardProductView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface CardProductService {
    List<CardProductView> getCardProducts();

    Optional<CardProductView> getCardProduct(Long id);

    Optional<CardProductView> getCardProductByCode(String code);

    CardProductView createProduct(@Valid CreateCardProductCommand command);

    CardProductView updateProduct(Long id, @Valid UpdateCardProductCommand command);

    void activateProduct(Long id);

    void deactivateProduct(Long id);
}
