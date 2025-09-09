package com.logo.domain.port.outgoing;

import com.logo.domain.model.Logo;
import io.smallrye.mutiny.Uni;

public interface LogoPersistencePort {

    Uni<Logo> save(Logo logo);

    Uni<Logo> get(String externalIdentifier);
}
