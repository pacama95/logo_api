package com.logo.domain.port.incoming;

import com.logo.domain.model.Errors;
import com.logo.domain.model.Logo;
import io.smallrye.mutiny.Uni;

public interface CreateLogoUseCase {
    Uni<Result> execute(CreateLogoCommand createLogoCommand);

    sealed interface Result {
        record Success(Logo logo) implements Result {};
        record Error(Errors error) implements Result {};
    }
}
