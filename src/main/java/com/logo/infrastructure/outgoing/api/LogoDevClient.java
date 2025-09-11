package com.logo.infrastructure.outgoing.api;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "logo-dev-api")
@RegisterProvider(LogoDevClientExceptionMapper.class)
public interface LogoDevClient {
    
    @GET
    @Path("/ticker/{ticker}")
    Uni<byte[]> fetchLogo(
            @PathParam("ticker") String ticker, 
            @QueryParam("token") String token
    );
}
