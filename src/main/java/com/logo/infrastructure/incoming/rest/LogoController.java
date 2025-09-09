package com.logo.infrastructure.incoming.rest;

import com.logo.domain.port.incoming.CreateLogoUseCase;
import com.logo.domain.port.incoming.GetLogoUseCase;
import com.logo.infrastructure.incoming.rest.mapper.LogoDtoMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1")
public class LogoController {
    
    private final CreateLogoUseCase createLogoUseCase;
    private final GetLogoUseCase getLogoUseCase;
    private final LogoDtoMapper logoDtoMapper;
    
    @Inject
    public LogoController(CreateLogoUseCase createLogoUseCase, GetLogoUseCase getLogoUseCase, LogoDtoMapper logoDtoMapper) {
        this.createLogoUseCase = createLogoUseCase;
        this.getLogoUseCase = getLogoUseCase;
        this.logoDtoMapper = logoDtoMapper;
    }

    @POST
    @Path("/logos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createLogo(CreateLogoRequest request) {        
        return Uni.createFrom().item(() -> logoDtoMapper.toCommand(request))
                .flatMap(createLogoUseCase::execute)
                .flatMap(result -> switch (result) {
                    case CreateLogoUseCase.Result.Success(var logo) -> 
                        Uni.createFrom().item(() -> logoDtoMapper.toResponse(logo))
                                .map(response -> Response.status(201).entity(response).build());
                    case CreateLogoUseCase.Result.Error(var code) -> 
                        Uni.createFrom().item(() -> Response.status(code).build());
                });
    }

    @GET
    @Path("/logos/external/{externalId}")
    @Produces("*/*")
    public Uni<Response> getLogoByExternalId(@PathParam("externalId") String externalId) {
        return getLogoUseCase.execute(externalId)
                .flatMap(result -> switch (result) {
                    case GetLogoUseCase.Result.Success(var logo) -> 
                        Uni.createFrom().item(() -> Response.ok(logo.fileContent())
                                .header("Content-Type", logo.contentType())
                                .header("Content-Disposition", "inline; filename=\"" + logo.fileName() + "\"")
                                .build());
                    case GetLogoUseCase.Result.NotFound() -> 
                        Uni.createFrom().item(() -> Response.status(404).build());
                    case GetLogoUseCase.Result.Error(var code) -> 
                        Uni.createFrom().item(() -> Response.status(code).build());
                });
    }
}
