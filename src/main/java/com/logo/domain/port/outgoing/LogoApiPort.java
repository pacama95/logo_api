package com.logo.domain.port.outgoing;

import io.smallrye.mutiny.Uni;

public interface LogoApiPort {
    
    /**
     * Fetches a logo from logo.dev API
     * @param ticker The stock ticker symbol (e.g., "AMZN")
     * @return Uni containing the logo image data as byte array
     */
    Uni<byte[]> fetchLogo(String ticker);
}
