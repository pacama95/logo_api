package com.logo.domain.port.incoming;

import com.logo.domain.model.Errors;
import com.logo.domain.model.Logo;
import io.smallrye.mutiny.Uni;

public interface GetLogoUseCase {
    Uni<Result> execute(String identifier);

    sealed interface Result {
        record Success(Logo logo) implements Result {
        }

        ;

        record NotFound() implements Result {
        }

        ;

        record Error(Errors errors) implements Result {
        }

        ;
    }
}
