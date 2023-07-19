package com.example.springkeycloak.config.keycloak;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KeycloakAdminClientConfig {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
}
