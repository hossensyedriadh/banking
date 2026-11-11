package io.github.hossensyedriadh.atm_terminal.iso;

import io.github.hossensyedriadh.atm_terminal.configuration.properties.AtmTerminalProperties;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Log4j2
@Component
public class IsoTcpClient {
    private static final int MAX_MESSAGE_LENGTH = 8192;

    private final GenericPackager genericPackager;
    private final AtmTerminalProperties.SwitchProperties switchConfiguration;

    public IsoTcpClient(GenericPackager genericPackager, AtmTerminalProperties atmTerminalProperties) {
        this.genericPackager = genericPackager;
        this.switchConfiguration = atmTerminalProperties.switchConfig();
    }

    public ISOMsg send(ISOMsg request) throws IOException, ISOException {
        request.setPackager(this.genericPackager);
        byte[] requestPayload = request.pack();

        if (requestPayload.length == 0 || requestPayload.length > MAX_MESSAGE_LENGTH) {
            log.error("Invalid ISO request length: {}", requestPayload.length);
            throw new IOException("Invalid ISO request length: " + requestPayload.length);
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(switchConfiguration.host(), switchConfiguration.port()),
                    switchConfiguration.connectTimeout());
            socket.setSoTimeout(switchConfiguration.readTimeout());

            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            outputStream.writeShort(requestPayload.length);
            outputStream.write(requestPayload);
            outputStream.flush();

            int responseLength = inputStream.readUnsignedShort();

            if (responseLength == 0 || responseLength > MAX_MESSAGE_LENGTH) {
                log.error("Invalid ISO response length: {}", responseLength);
                throw new IOException("Invalid ISO response length: " + responseLength);
            }

            byte[] responsePayload = new byte[responseLength];
            inputStream.readFully(responsePayload);

            ISOMsg response = new ISOMsg();
            response.setPackager(this.genericPackager);
            response.unpack(responsePayload);

            return response;
        }
    }
}
