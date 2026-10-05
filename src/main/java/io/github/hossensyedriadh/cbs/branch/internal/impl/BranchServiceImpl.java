package io.github.hossensyedriadh.cbs.branch.internal.impl;

import io.github.hossensyedriadh.cbs.branch.api.command.CreateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.command.UpdateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.event.BranchActivatedEvent;
import io.github.hossensyedriadh.cbs.branch.api.event.BranchCreatedEvent;
import io.github.hossensyedriadh.cbs.branch.api.event.BranchDeactivatedEvent;
import io.github.hossensyedriadh.cbs.branch.api.event.BranchUpdatedEvent;
import io.github.hossensyedriadh.cbs.branch.api.model.BranchView;
import io.github.hossensyedriadh.cbs.branch.api.service.BranchService;
import io.github.hossensyedriadh.cbs.branch.internal.domain.Branch;
import io.github.hossensyedriadh.cbs.branch.internal.mapper.BranchMapper;
import io.github.hossensyedriadh.cbs.branch.internal.repository.BranchRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
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
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository, BranchMapper branchMapper,
                             ApplicationEventPublisher applicationEventPublisher, HttpServletRequest httpServletRequest) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<BranchView> getBranches() {
        return this.branchRepository.findAll().stream().map(this.branchMapper::toView)
                .toList();
    }

    @Override
    public Optional<BranchView> getBranch(Long id) {
        return this.branchRepository.findById(id).map(this.branchMapper::toView);
    }

    @Override
    public Optional<BranchView> getBranchByCode(String code) {
        return this.branchRepository.findByBranchCodeIgnoreCase(code).map(this.branchMapper::toView);
    }

    @Transactional
    @Override
    public BranchView createBranch(@Valid CreateBranchCommand command) {
        if (this.branchRepository.findByBranchCodeIgnoreCase(command.getBranchCode()).isPresent()) {
            log.warn("Branch with code [{}] already exists.", command.getBranchCode());
            throw new ApiResourceException("Branch already exists with code", HttpStatus.CONFLICT, this.httpServletRequest);
        }

        Branch branch = this.branchMapper.toEntity(command);
        Branch saved = this.branchRepository.save(branch);

        log.info("Created branch [Code: {}] with ID: {}", branch.getBranchCode(), branch.getId());

        this.applicationEventPublisher.publishEvent(new BranchCreatedEvent(saved.getId(), saved.getBranchCode()));

        return this.branchMapper.toView(saved);
    }

    @Transactional
    @Override
    public BranchView updateBranch(Long id, @Valid UpdateBranchCommand command) {
        Branch branch = this.branchRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("No Branch with ID: {}", id);
            return new ApiResourceException("Branch not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });

        if (branch.getActive() == Boolean.FALSE) {
            log.warn("Branch [ID: {}] is not active.", id);
            throw new ApiResourceException("Branch [" + branch.getBranchCode() + "] is not active",
                    HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.branchMapper.toPatchedEntity(command, branch);
        Branch updated = this.branchRepository.save(branch);

        log.info("Updated Branch with ID: {}", id);

        this.applicationEventPublisher.publishEvent(new BranchUpdatedEvent(updated.getId(), updated.getBranchCode()));

        return this.branchMapper.toView(updated);
    }

    @Transactional
    @Override
    public void activateBranch(Long id) {
        this.branchRepository.findByIdForUpdate(id).ifPresentOrElse(branch -> {
            if (branch.getActive() == Boolean.TRUE) {
                log.warn("Branch with ID: {} is already active", id);
                throw new ApiResourceException("Branch is already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            branch.setActive(Boolean.TRUE);
            this.branchRepository.save(branch);

            this.applicationEventPublisher.publishEvent(new BranchActivatedEvent(branch.getId(), branch.getBranchCode()));

            log.info("Activated Branch with ID: {}", id);
        }, () -> {
            log.warn("No Branch with ID: {}", id);
            throw new ApiResourceException("Branch not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void deactivateBranch(Long id) {
        this.branchRepository.findByIdForUpdate(id).ifPresentOrElse(branch -> {
            if (branch.getActive() == Boolean.FALSE) {
                log.warn("Branch with ID: {} is already inactive", id);
                throw new ApiResourceException("Branch is already inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            branch.setActive(Boolean.FALSE);
            this.branchRepository.save(branch);

            this.applicationEventPublisher.publishEvent(new BranchDeactivatedEvent(branch.getId(), branch.getBranchCode()));

            log.info("Deactivated Branch with ID: {}", id);
        }, () -> {
            log.warn("No Branch with ID: {}", id);
            throw new ApiResourceException("Branch not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });
    }
}
