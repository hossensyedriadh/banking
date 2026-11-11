package io.github.hossensyedriadh.atm_pos_switch.terminal.api.model;

import io.github.hossensyedriadh.atm_pos_switch.terminal.api.enumerator.TerminalStatus;
import io.github.hossensyedriadh.atm_pos_switch.terminal.api.enumerator.TerminalType;

import java.time.OffsetDateTime;

public record SwitchTerminal(String terminalId, TerminalType terminalType, TerminalStatus status,
                             String institutionCode, OffsetDateTime createdAt) {
}
