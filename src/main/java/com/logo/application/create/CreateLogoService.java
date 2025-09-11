package com.logo.application.create;

import com.logo.domain.model.Logo;
import com.logo.domain.port.incoming.CreateLogoCommand;
import com.logo.domain.port.incoming.CreateLogoUseCase;
import com.logo.domain.port.outgoing.LogoPersistencePort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;


@ApplicationScoped
public class CreateLogoService implements CreateLogoUseCase {

    private static final Logger LOG = Logger.getLogger(CreateLogoService.class);

    private final LogoPersistencePort logoPersistencePort;

    @Inject
    public CreateLogoService(LogoPersistencePort logoPersistencePort) {
        this.logoPersistencePort = logoPersistencePort;
    }

    @Override
    public Uni<CreateLogoUseCase.Result> execute(CreateLogoCommand createLogoCommand) {
        return Uni.createFrom().item(() -> new Logo(
                        createLogoCommand.externalIdentifier(),
                        createLogoCommand.resourceUrl(),
                        createLogoCommand.fileContent(),
                        createLogoCommand.fileName(),
                        createLogoCommand.contentType(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()))
                .flatMap(logoPersistencePort::save)
                .onItem().transform(CreateLogoService::Success)
                .onFailure()
                .recoverWithItem(throwable -> {
                    LOG.error("Error creating logo for external identifier %s".formatted(createLogoCommand.externalIdentifier()),
                            throwable.getCause());
                    return Error();
                });

    }

    private static CreateLogoUseCase.Result Success(Logo logo) {
        return new Result.Success(logo);
    }

    private static CreateLogoUseCase.Result Error() {
        return new Result.Error(500);
    }
}
