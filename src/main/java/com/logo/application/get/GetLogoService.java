package com.logo.application.get;

import com.logo.domain.model.Logo;
import com.logo.domain.port.incoming.GetLogoUseCase;
import com.logo.domain.port.outgoing.LogoApiPort;
import com.logo.domain.port.outgoing.LogoPersistencePort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;

@ApplicationScoped
public class GetLogoService implements GetLogoUseCase {

    private static final Logger LOG = Logger.getLogger(GetLogoService.class);

    private final LogoPersistencePort logoPersistencePort;
    private final LogoApiPort logoApiPort;

    public GetLogoService(LogoPersistencePort logoPersistencePort, LogoApiPort logoApiPort) {
        this.logoPersistencePort = logoPersistencePort;
        this.logoApiPort = logoApiPort;
    }

    @Override
    public Uni<GetLogoUseCase.Result> execute(String identifier) {
        return logoPersistencePort.get(identifier)
                .flatMap(logo -> {
                    if (logo != null) {
                        LOG.info("Returning pre-saved logo with identifier %s".formatted(identifier));
                        return Uni.createFrom().item((GetLogoUseCase.Result) new GetLogoUseCase.Result.Success(logo));
                    } else {
                        LOG.info("Fetching, saving and returning logo with identifier %s".formatted(identifier));
                        return fetchAndSaveLogo(identifier)
                                .map(savedLogo -> (GetLogoUseCase.Result) new GetLogoUseCase.Result.Success(savedLogo));
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    // Log error and return appropriate error code
                    return new GetLogoUseCase.Result.Error(500);
                });
    }

    private Uni<Logo> fetchAndSaveLogo(String identifier) {
        return logoApiPort.fetchLogo(identifier)
                .flatMap(logoData -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    String logoUrl = String.format("https://img.logo.dev/ticker/%s", identifier);

                    Logo newLogo = new Logo(
                            identifier,
                            logoUrl,
                            logoData,
                            identifier + ".jpeg",
                            "image/jpeg",
                            now,
                            now
                    );

                    return logoPersistencePort.save(newLogo);
                });
    }
}
