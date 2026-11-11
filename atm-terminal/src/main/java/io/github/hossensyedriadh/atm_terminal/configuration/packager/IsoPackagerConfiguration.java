package io.github.hossensyedriadh.atm_terminal.configuration.packager;

import org.jpos.iso.ISOException;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class IsoPackagerConfiguration {
    @Bean
    public GenericPackager isoPackager() throws ISOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("/packager/iso87.xml")) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing ISO87.xml");
            }

            return new GenericPackager(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close ISO packager resource", e);
        }
    }
}
