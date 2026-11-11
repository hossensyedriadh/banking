package io.github.hossensyedriadh.cbs.product.internal.impl;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.product.api.command.CreateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.enumerator.ChequeProductStatus;
import io.github.hossensyedriadh.cbs.product.api.event.chequeproduct.ChequeProductActivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.chequeproduct.ChequeProductCreatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.chequeproduct.ChequeProductDeactivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.chequeproduct.ChequeProductUpdatedEvent;
import io.github.hossensyedriadh.cbs.product.api.model.ChequeProductView;
import io.github.hossensyedriadh.cbs.product.api.service.ChequeProductService;
import io.github.hossensyedriadh.cbs.product.internal.domain.ChequeProduct;
import io.github.hossensyedriadh.cbs.product.internal.mapper.ChequeProductMapper;
import io.github.hossensyedriadh.cbs.product.internal.repository.ChequeProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class ChequeProductServiceImpl implements ChequeProductService {
    private final ChequeProductRepository chequeProductRepository;
    private final ChequeProductMapper chequeProductMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public ChequeProductServiceImpl(ChequeProductRepository chequeProductRepository, ChequeProductMapper chequeProductMapper,
                                    ApplicationEventPublisher applicationEventPublisher, HttpServletRequest httpServletRequest) {
        this.chequeProductRepository = chequeProductRepository;
        this.chequeProductMapper = chequeProductMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<ChequeProductView> getChequeProducts() {
        return this.chequeProductRepository.findAll().stream().map(this.chequeProductMapper::toView).toList();
    }

    @Override
    public Optional<ChequeProductView> getChequeProduct(Long id) {
        return this.chequeProductRepository.findById(id).map(this.chequeProductMapper::toView);
    }

    @Override
    public Optional<ChequeProductView> getChequeProductByCode(String code) {
        return this.chequeProductRepository.findByProductCode(code).map(this.chequeProductMapper::toView);
    }

    @Transactional
    @Override
    public ChequeProductView createProduct(@Valid CreateChequeProductCommand command) {
        if (this.chequeProductRepository.existsByProductCode(command.getProductCode())) {
            log.warn("Cheque product with code {} already exists", command.getProductCode());
            throw new ApiResourceException("Cheque product with code " + command.getProductCode() + " already exists",
                    HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        ChequeProduct chequeProduct = this.chequeProductMapper.toEntity(command);
        ChequeProduct saved = this.chequeProductRepository.save(chequeProduct);

        this.applicationEventPublisher.publishEvent(new ChequeProductCreatedEvent(saved.getId(), saved.getProductCode()));

        log.info("Cheque product created with code: {}", saved.getProductCode());

        return this.chequeProductMapper.toView(saved);
    }

    @Transactional
    @Override
    public ChequeProductView updateProduct(Long id, @Valid UpdateChequeProductCommand command) {
        ChequeProduct product = this.chequeProductRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Cheque product not found with id: {}", id);
            return new ApiResourceException("Cheque product not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });

        if (product.getStatus() != ChequeProductStatus.ACTIVE) {
            log.warn("Cheque product with code {} is not active", product.getProductCode());
            throw new ApiResourceException("Cheque product not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.chequeProductMapper.toPatchedEntity(command, product);
        ChequeProduct updated = this.chequeProductRepository.save(product);

        this.applicationEventPublisher.publishEvent(new ChequeProductUpdatedEvent(updated.getId(), updated.getProductCode()));

        log.info("Cheque product updated with ID: {}", updated.getId());

        return this.chequeProductMapper.toView(updated);
    }

    @Transactional
    @Override
    public void activateProduct(Long id) {
        this.chequeProductRepository.findByIdForUpdate(id).ifPresentOrElse(product -> {
            if (product.getStatus() == ChequeProductStatus.ACTIVE) {
                log.warn("Cheque product with code {} is already active", product.getProductCode());
                throw new ApiResourceException("Cheque product is already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            product.setStatus(ChequeProductStatus.ACTIVE);
            this.chequeProductRepository.save(product);

            this.applicationEventPublisher.publishEvent(new ChequeProductActivatedEvent(product.getId(), product.getProductCode()));

            log.info("Cheque product activated with code: {}", product.getProductCode());
        }, () -> {
            log.warn("Cheque product not found with id: {}", id);
            throw new ApiResourceException("Cheque product not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void deactivateProduct(Long id) {
        this.chequeProductRepository.findByIdForUpdate(id).ifPresentOrElse(product -> {
            if (product.getStatus() == ChequeProductStatus.INACTIVE) {
                log.warn("Cheque product with code {} is already inactive", product.getProductCode());
                throw new ApiResourceException("Cheque product is already inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            product.setStatus(ChequeProductStatus.INACTIVE);
            this.chequeProductRepository.save(product);

            this.applicationEventPublisher.publishEvent(new ChequeProductDeactivatedEvent(product.getId(), product.getProductCode()));

            log.info("Cheque product deactivated with code: {}", product.getProductCode());
        }, () -> {
            log.warn("Cheque product not found with id: {}", id);
            throw new ApiResourceException("Cheque product not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });
    }
}
