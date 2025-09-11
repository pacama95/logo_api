package com.logo.infrastructure.outgoing.api;

import com.logo.domain.exception.ServiceException;
import com.logo.domain.model.Errors;
import com.logo.domain.port.outgoing.LogoApiPort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LogoApiAdapter implements LogoApiPort {

    private static final Logger LOG = Logger.getLogger(LogoApiAdapter.class);

    private final LogoDevClient logoDevClient;
    private final String logoDevToken;

    public LogoApiAdapter(@RestClient LogoDevClient logoDevClient,
                          @ConfigProperty(name = "logo.dev.token") String logoDevToken) {
        this.logoDevClient = logoDevClient;
        this.logoDevToken = logoDevToken;
    }

    @Override
    public Uni<byte[]> fetchLogo(String ticker) {
        LOG.debug("Fetching logo for ticker: %s".formatted(ticker));
        
        return logoDevClient.fetchLogo(ticker, logoDevToken)
                .onFailure(ServiceException.class).transform(throwable -> {
                    // ServiceException already contains domain-specific error information
                    ServiceException serviceException = (ServiceException) throwable;
                    LOG.warn("Failed to fetch logo for ticker %s: %s".formatted(
                            ticker, serviceException.getError()));
                    return serviceException;
                })
                .onFailure().transform(throwable -> {
                    // Handle any unexpected exceptions that weren't mapped by the exception mapper
                    if (!(throwable instanceof ServiceException)) {
                        LOG.error("Unexpected error fetching logo for ticker %s".formatted(ticker), throwable);
                        return new ServiceException(
                                Errors.COMMUNICATION_ERROR, 
                                "Unexpected error communicating with logo service: " + throwable.getMessage(),
                                throwable
                        );
                    }
                    return throwable;
                });
    }
}
