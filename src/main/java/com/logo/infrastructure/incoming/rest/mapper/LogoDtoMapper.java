package com.logo.infrastructure.incoming.rest.mapper;

import com.logo.domain.model.Logo;
import com.logo.domain.port.incoming.CreateLogoCommand;
import com.logo.infrastructure.incoming.rest.request.CreateLogoRequest;
import com.logo.infrastructure.incoming.rest.response.LogoResponse;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA_CDI;

@Mapper(componentModel = JAKARTA_CDI)
public interface LogoDtoMapper {

    CreateLogoCommand toCommand(CreateLogoRequest request);

    LogoResponse toResponse(Logo logo);
}
