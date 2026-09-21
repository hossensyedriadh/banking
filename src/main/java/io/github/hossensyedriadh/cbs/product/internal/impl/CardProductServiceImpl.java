package io.github.hossensyedriadh.cbs.product.internal.impl;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.product.api.command.CreateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardProductStatus;
import io.github.hossensyedriadh.cbs.product.api.model.CardProductView;
import io.github.hossensyedriadh.cbs.product.api.service.CardProductService;
import io.github.hossensyedriadh.cbs.product.internal.domain.CardProduct;
import io.github.hossensyedriadh.cbs.product.internal.mapper.CardProductMapper;
import io.github.hossensyedriadh.cbs.product.internal.repository.CardProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class CardProductServiceImpl implements CardProductService {
    private final CardProductRepository cardProductRepository;
    private final CardProductMapper cardProductMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CardProductServiceImpl(CardProductRepository cardProductRepository, CardProductMapper cardProductMapper,
                                  HttpServletRequest httpServletRequest) {
        this.cardProductRepository = cardProductRepository;
        this.cardProductMapper = cardProductMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<CardProductView> getCardProducts() {
        return this.cardProductRepository.findAll().stream().map(this.cardProductMapper::toView).toList();
    }

    @Override
    public Optional<CardProductView> getCardProduct(Long id) {
        return this.cardProductRepository.findById(id).map(this.cardProductMapper::toView);
    }

    @Override
    public Optional<CardProductView> getCardProductByCode(String code) {
        return this.cardProductRepository.findByProductCode(code).map(this.cardProductMapper::toView);
    }

    @Transactional
    @Override
    public CardProductView createProduct(@Valid CreateCardProductCommand command) {
        if (this.cardProductRepository.existsByProductCode(command.getProductCode())) {
            log.warn("Card Product with code {} already exists", command.getProductCode());
            throw new ApiResourceException("Card Product with code " + command.getProductCode() + " already exists",
                    HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        CardProduct cardProduct = this.cardProductMapper.toEntity(command);
        CardProduct saved = this.cardProductRepository.save(cardProduct);

        log.info("Card Product created with code: {}", saved.getProductCode());

        return this.cardProductMapper.toView(saved);
    }

    @Transactional
    @Override
    public CardProductView updateProduct(Long id, @Valid UpdateCardProductCommand command) {
        CardProduct product = this.cardProductRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Card product not found with id: {}", id);
            return new ApiResourceException("Card product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (product.getStatus() != CardProductStatus.ACTIVE) {
            log.warn("Card Product with code {} is not active", product.getProductCode());
            throw new ApiResourceException("Card Product not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.cardProductMapper.toPatchedEntity(command, product);
        CardProduct updated = this.cardProductRepository.save(product);

        log.info("Card Product updated with ID: {}", updated.getId());

        return this.cardProductMapper.toView(updated);
    }

    @Transactional
    @Override
    public void activateProduct(Long id) {
        this.cardProductRepository.findByIdForUpdate(id).ifPresentOrElse(product -> {
            if (product.getStatus() == CardProductStatus.ACTIVE) {
                log.warn("Card product with code {} is already active", product.getProductCode());
                throw new ApiResourceException("Card product is already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            product.setStatus(CardProductStatus.ACTIVE);
            this.cardProductRepository.save(product);

            log.info("Card product activated with code: {}", product.getProductCode());
        }, () -> {
            log.warn("Card product not found with id: {}", id);
            throw new ApiResourceException("Card product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void deactivateProduct(Long id) {
        this.cardProductRepository.findByIdForUpdate(id).ifPresentOrElse(product -> {
            if (product.getStatus() == CardProductStatus.INACTIVE) {
                log.warn("Card product with code {} is already inactive", product.getProductCode());
                throw new ApiResourceException("Card product is already inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            product.setStatus(CardProductStatus.INACTIVE);
            this.cardProductRepository.save(product);

            log.info("Card product deactivated with code: {}", product.getProductCode());
        }, () -> {
            log.warn("Card product not found with id: {}", id);
            throw new ApiResourceException("Card product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }
}
