package com.logo.domain.port.incoming;

import com.logo.domain.model.Logo;
import io.smallrye.mutiny.Uni;

public interface CreateLogoUseCase {
    Uni<Result> execute(CreateLogoCommand createLogoCommand);

    sealed interface Result {
        record Success(Logo logo) implements Result {};
        record Error(int code) implements Result {};
    }
}
