package io.github.hossensyedriadh.atm_terminal.session;

import io.github.hossensyedriadh.atm_terminal.cli.AtmTerminalCli;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;

@Component
public class AtmTerminalSession {
    private final AtmTerminalCli atmTerminalCli;

    @Autowired
    public AtmTerminalSession(AtmTerminalCli atmTerminalCli) {
        this.atmTerminalCli = atmTerminalCli;
    }

    public void run(Socket socket) throws IOException {
        this.atmTerminalCli.run(socket.getInputStream(), socket.getOutputStream());
    }
}
