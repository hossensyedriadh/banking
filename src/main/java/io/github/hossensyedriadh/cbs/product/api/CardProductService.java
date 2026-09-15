package io.github.hossensyedriadh.cbs.product.api;

import io.github.hossensyedriadh.cbs.product.api.model.CardProductView;
import io.github.hossensyedriadh.cbs.product.api.model.CreateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.UpdateCardProductCommand;

import java.util.List;
import java.util.Optional;

public interface CardProductService {
    List<CardProductView> getCardProducts();

    Optional<CardProductView> getCardProduct(Long id);

    Optional<CardProductView> getCardProductByCode(String code);

    CardProductView createProduct(CreateCardProductCommand command);

    Optional<CardProductView> updateProduct(Long id, UpdateCardProductCommand command);

    void activateProduct(Long id);

    void deactivateProduct(Long id);
}
