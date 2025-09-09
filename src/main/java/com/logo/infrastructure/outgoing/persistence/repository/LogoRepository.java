package com.logo.infrastructure.outgoing.persistence.repository;

import com.logo.infrastructure.outgoing.persistence.LogoEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogoRepository implements PanacheRepository<LogoEntity> {

    @WithSession
    public Uni<LogoEntity> findByExternalIdentifier(String externalIdentifier) {
        return find("externalIdentifier", externalIdentifier).firstResult();
    }

    @WithTransaction
    public Uni<Boolean> existsByExternalIdentifier(String externalIdentifier) {
        return find("externalIdentifier", externalIdentifier).count()
                .map(count -> count > 0);
    }
}
