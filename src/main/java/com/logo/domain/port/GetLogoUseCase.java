package com.logo.domain.port;

import com.logo.domain.model.Logo;
import io.smallrye.mutiny.Uni;

public interface GetLogoUseCase {
    Uni<Logo> execute(String identifier);

    sealed interface Result {
        record Success(Logo logo) implements Result {};
        record NotFound() implements Result {};
        record Error(int code) implements Result {};
    }
}