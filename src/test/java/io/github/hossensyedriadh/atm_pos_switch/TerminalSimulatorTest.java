package io.github.hossensyedriadh.atm_pos_switch;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.IsoMessageParser;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

@Log4j2
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
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
             DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
            Assertions.assertTrue(payload.length > 0);
            Assertions.assertTrue(payload.length <= 8192);

            outputStream.writeShort(payload.length);
            outputStream.write(payload);
            outputStream.flush();

            log.info("Request: {}", request.toString());

            int responseLength = inputStream.readUnsignedShort();

            Assertions.assertTrue(responseLength > 0);
            Assertions.assertTrue(responseLength <= 8192);

            byte[] responsePayload = inputStream.readNBytes(responseLength);

            Assertions.assertEquals(responseLength, responsePayload.length);

            ISOMsg response = parser.unpack(responsePayload);

            log.info("Response: {}", response.toString());
            
            Assertions.assertEquals("0210", response.getMTI());
            Assertions.assertEquals("00", response.getString(39));
            Assertions.assertEquals(request.getString(11), response.getString(11));
            Assertions.assertEquals(request.getString(41), response.getString(41));
        }
    }
}
