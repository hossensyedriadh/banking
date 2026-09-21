package io.github.hossensyedriadh.cbs.branch.internal.impl;

import io.github.hossensyedriadh.cbs.branch.api.command.CreateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.command.UpdateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.model.BranchView;
import io.github.hossensyedriadh.cbs.branch.api.service.BranchService;
import io.github.hossensyedriadh.cbs.branch.internal.domain.Branch;
import io.github.hossensyedriadh.cbs.branch.internal.mapper.BranchMapper;
import io.github.hossensyedriadh.cbs.branch.internal.repository.BranchRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import jakarta.servlet.http.HttpServletRequest;
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
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository, BranchMapper branchMapper,
                             HttpServletRequest httpServletRequest) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
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
    public BranchView createBranch(CreateBranchCommand command) {
        Branch branch = this.branchMapper.toEntity(command);
        Branch saved = this.branchRepository.save(branch);

        log.info("Created branch [Code: {}] with ID: {}", branch.getBranchCode(), branch.getId());

        return this.branchMapper.toView(saved);
    }

    @Transactional
    @Override
    public BranchView updateBranch(Long id, UpdateBranchCommand command) {
        Branch branch = this.branchRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("No Branch with ID: {}", id);
            return new ApiResourceException("Branch not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (branch.getActive() == Boolean.FALSE) {
            log.warn("Branch [ID: {}] is not active.", id);
            throw new ApiResourceException("Branch [" + branch.getBranchCode() + "] is not active",
                    HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.branchMapper.toPatchedEntity(command, branch);
        Branch updated = this.branchRepository.save(branch);

        log.info("Updated Branch with ID: {}", id);

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

            log.info("Activated Branch with ID: {}", id);
        }, () -> {
            log.warn("No Branch with ID: {}", id);
            throw new ApiResourceException("Branch not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
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

            log.info("Deactivated Branch with ID: {}", id);
        }, () -> {
            log.warn("No Branch with ID: {}", id);
            throw new ApiResourceException("Branch not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }
}
