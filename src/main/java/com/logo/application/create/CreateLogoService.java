package com.logo.application.create;

import com.logo.domain.model.Logo;
import com.logo.domain.port.incoming.CreateLogoCommand;
import com.logo.domain.port.incoming.CreateLogoUseCase;
import com.logo.domain.port.outgoing.LogoApiPort;
import com.logo.domain.port.outgoing.LogoPersistencePort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;

@ApplicationScoped
public class CreateLogoService implements CreateLogoUseCase {
    
    private final LogoPersistencePort logoPersistencePort;
    private final LogoApiPort logoApiPort;
    
    @Inject
    public CreateLogoService(LogoPersistencePort logoPersistencePort, LogoApiPort logoApiPort) {
        this.logoPersistencePort = logoPersistencePort;
        this.logoApiPort = logoApiPort;
    }
    
    @Override
    public Uni<CreateLogoUseCase.Result> execute(CreateLogoCommand createLogoCommand) {
        return logoPersistencePort.get(createLogoCommand.ticker())
                .flatMap(existingLogo -> {
                    if (existingLogo != null) {
                        return Uni.createFrom().item((CreateLogoUseCase.Result) new CreateLogoUseCase.Result.Success(existingLogo));
                    } else {
                        return fetchAndCreateLogo(createLogoCommand)
                                .map(savedLogo -> (CreateLogoUseCase.Result) new CreateLogoUseCase.Result.Success(savedLogo));
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    return (CreateLogoUseCase.Result) new CreateLogoUseCase.Result.Error(500);
                });
    }
    
    private Uni<Logo> fetchAndCreateLogo(CreateLogoCommand command) {
        return logoApiPort.fetchLogo(command.ticker())
                .flatMap(logoData -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    String logoUrl = String.format("https://img.logo.dev/ticker/%s", command.ticker());
                    
                    Logo newLogo = new Logo(
                            command.ticker(),
                            logoUrl,
                            logoData,
                            command.ticker() + ".jpeg",
                            "image/jpeg",
                            now,
                            now
                    );
                    
                    return logoPersistencePort.save(newLogo);
                });
    }
}
