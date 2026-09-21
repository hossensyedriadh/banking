package io.github.hossensyedriadh.cbs.product.internal.impl;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.product.api.command.CreateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;
import io.github.hossensyedriadh.cbs.product.api.service.AccountProductService;
import io.github.hossensyedriadh.cbs.product.internal.domain.AccountProduct;
import io.github.hossensyedriadh.cbs.product.internal.mapper.AccountProductMapper;
import io.github.hossensyedriadh.cbs.product.internal.repository.AccountProductRepository;
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
public class AccountProductServiceImpl implements AccountProductService {
    private final AccountProductRepository accountProductRepository;
    private final AccountProductMapper accountProductMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AccountProductServiceImpl(AccountProductRepository accountProductRepository, AccountProductMapper accountProductMapper,
                                     HttpServletRequest httpServletRequest) {
        this.accountProductRepository = accountProductRepository;
        this.accountProductMapper = accountProductMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<AccountProductView> getAccountProducts() {
        return this.accountProductRepository.findAll().stream().map(this.accountProductMapper::toView).toList();
    }

    @Override
    public Optional<AccountProductView> getProduct(Long id) {
        return this.accountProductRepository.findById(id).map(this.accountProductMapper::toView);
    }

    @Override
    public Optional<AccountProductView> getProductByCode(String code) {
        return this.accountProductRepository.findByProductCode(code).map(this.accountProductMapper::toView);
    }

    @Transactional
    @Override
    public AccountProductView createProduct(@Valid CreateAccountProductCommand command) {
        if (this.accountProductRepository.existsByProductCode(command.getProductCode())) {
            log.warn("Account Product with code {} already exists", command.getProductCode());
            throw new ApiResourceException("Account Product with code " + command.getProductCode() + " already exists",
                    HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        AccountProduct product = this.accountProductMapper.toEntity(command);
        AccountProduct saved = this.accountProductRepository.save(product);

        log.info("Account product created with code: {}", saved.getProductCode());

        return this.accountProductMapper.toView(saved);
    }

    @Transactional
    @Override
    public AccountProductView updateProduct(Long id, @Valid UpdateAccountProductCommand command) {
        AccountProduct product = this.accountProductRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Account product not found with id: {}", id);
            return new ApiResourceException("Account product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (product.getStatus() != AccountProductStatus.ACTIVE) {
            log.warn("Account product with code {} is not active", product.getProductCode());
            throw new ApiResourceException("Account product not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.accountProductMapper.toPatchedEntity(command, product);
        AccountProduct updated = this.accountProductRepository.save(product);

        log.info("Account product updated with ID: {}", id);

        return this.accountProductMapper.toView(updated);
    }

    @Transactional
    @Override
    public void activateProduct(Long id) {
        this.accountProductRepository.findByIdForUpdate(id).ifPresentOrElse(product -> {
            if (product.getStatus() == AccountProductStatus.ACTIVE) {
                log.warn("Account product with code {} is already active", product.getProductCode());
                throw new ApiResourceException("Account product is already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            product.setStatus(AccountProductStatus.ACTIVE);
            this.accountProductRepository.save(product);

            log.info("Account product activated with code: {}", product.getProductCode());
        }, () -> {
            log.warn("Account product not found with id: {}", id);
            throw new ApiResourceException("Account product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void deactivateProduct(Long id) {
        this.accountProductRepository.findByIdForUpdate(id).ifPresentOrElse(product -> {
            if (product.getStatus() == AccountProductStatus.INACTIVE) {
                log.warn("Account product with code {} is already inactive", product.getProductCode());
                throw new ApiResourceException("Account product is already inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            product.setStatus(AccountProductStatus.INACTIVE);
            this.accountProductRepository.save(product);

            log.info("Account product deactivated with code: {}", product.getProductCode());
        }, () -> {
            log.warn("Account product not found with id: {}", id);
            throw new ApiResourceException("Account product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }
}
