package com.logo.infrastructure.outgoing.api;

import com.logo.domain.port.outgoing.LogoApiPort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class LogoApiAdapter implements LogoApiPort {

    private final LogoDevClient logoDevClient;

    private final String logoDevToken;

    public LogoApiAdapter(@RestClient LogoDevClient logoDevClient,
                          @ConfigProperty(name = "logo.dev.token") String logoDevToken) {

        this.logoDevClient = logoDevClient;
        this.logoDevToken = logoDevToken;
    }

    @Override
    public Uni<byte[]> fetchLogo(String ticker) {
        return logoDevClient.fetchLogo(ticker, logoDevToken);
    }
}
