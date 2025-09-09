package com.logo.infrastructure.outgoing.persistence.mapper;

import com.logo.domain.model.Logo;
import com.logo.infrastructure.outgoing.persistence.LogoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA_CDI;

@Mapper(componentModel = JAKARTA_CDI)
public interface LogoEntityMapper {

    @Mapping(target = "id", ignore = true)
    LogoEntity toEntity(Logo logo);

    Logo toDomain(LogoEntity entity);
}
