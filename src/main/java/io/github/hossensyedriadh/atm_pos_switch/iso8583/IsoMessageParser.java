package io.github.hossensyedriadh.atm_pos_switch.iso8583;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class IsoMessageParser {
    private final GenericPackager packager;

    public IsoMessageParser() {
        try (InputStream inputStream = new ClassPathResource("packager/iso87.xml").getInputStream()) {
            this.packager = new GenericPackager(inputStream);
        } catch (IOException | ISOException e) {
            throw new RuntimeException("Failed to load ISO 8583 packager", e);
        }
    }

    public ISOMsg unpack(byte[] message) throws ISOException {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(this.packager);
        isoMsg.unpack(message);

        return isoMsg;
    }

    public byte[] pack(ISOMsg isoMsg) throws ISOException {
        isoMsg.setPackager(this.packager);

        return isoMsg.pack();
    }
}
