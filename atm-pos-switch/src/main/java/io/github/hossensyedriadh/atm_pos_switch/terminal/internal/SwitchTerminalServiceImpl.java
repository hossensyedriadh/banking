package io.github.hossensyedriadh.atm_pos_switch.terminal.internal;

import io.github.hossensyedriadh.atm_pos_switch.terminal.api.model.SwitchTerminal;
import io.github.hossensyedriadh.atm_pos_switch.terminal.api.service.SwitchTerminalService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
class SwitchTerminalServiceImpl implements SwitchTerminalService {
    private final SwitchTerminalRepository switchTerminalRepository;

    @Autowired
    public SwitchTerminalServiceImpl(SwitchTerminalRepository switchTerminalRepository) {
        this.switchTerminalRepository = switchTerminalRepository;
    }

    public Optional<SwitchTerminal> findByTerminalId(String terminalId) {
        return this.switchTerminalRepository.findById(terminalId);
    }

    @Override
    public boolean isActive(String terminalId) {
        return this.switchTerminalRepository.isActive(terminalId);
    }
}
