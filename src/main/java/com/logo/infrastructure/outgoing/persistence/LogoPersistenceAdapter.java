package com.logo.infrastructure.outgoing.persistence;

import com.logo.domain.model.Logo;
import com.logo.domain.port.outgoing.LogoPersistencePort;
import com.logo.infrastructure.outgoing.persistence.mapper.LogoEntityMapper;
import com.logo.infrastructure.outgoing.persistence.repository.LogoRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogoPersistenceAdapter implements LogoPersistencePort {

    private final LogoRepository logoRepository;

    private final LogoEntityMapper logoEntityMapper;

    public LogoPersistenceAdapter(LogoRepository logoRepository, LogoEntityMapper logoEntityMapper) {
        this.logoRepository = logoRepository;
        this.logoEntityMapper = logoEntityMapper;
    }

    @Override
    @WithTransaction
    public Uni<Logo> save(Logo logo) {
        return Uni.createFrom().item(() -> logoEntityMapper.toEntity(logo))
                .flatMap(logoRepository::persistAndFlush)
                .map(logoEntityMapper::toDomain);
    }

    @Override
    public Uni<Logo> get(String externalIdentifier) {
        return logoRepository.findByExternalIdentifier(externalIdentifier)
                .flatMap(entity -> {
                    if (entity != null) {
                        return Uni.createFrom().item(() -> logoEntityMapper.toDomain(entity));
                    }
                    return Uni.createFrom().nullItem();
                });
    }
}
