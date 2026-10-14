package io.github.hossensyedriadh.atm_pos_switch;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.IsoMessageParser;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.net.Socket;

public class TerminalSimulatorTest {
    @Test
    void shouldSendIso8583RequestToSwitch() throws Exception {
        IsoMessageParser parser = new IsoMessageParser();

        ISOMsg request = new ISOMsg();
        request.setMTI("0200");
        request.set(3, "310000");
        request.set(4, "000000000000");
        request.set(7, "1015045959");
        request.set(11, "000123");
        request.set(41, "ATM00001");
        request.set(49, "050");

        byte[] payload = parser.pack(request);

        try (Socket socket = new Socket("127.0.0.1", 15000);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
            Assertions.assertTrue(payload.length > 0);
            Assertions.assertTrue(payload.length <= 8192);

            outputStream.writeShort(payload.length);
            outputStream.write(payload);
            outputStream.flush();
        }
    }
}
