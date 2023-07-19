package com.example.springkeycloak.config.keycloak;

import com.example.springkeycloak.dto.KeycloakCurrentUser;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.stream.Collectors;

public class CurrentUserProvider {

    @Value("${keycloak.resource}")
    private String clientId;

    public KeycloakCurrentUser getCurrentUser() {

        KeycloakCurrentUser result = new KeycloakCurrentUser();

        // Current Principal
        KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = (KeycloakPrincipal<RefreshableKeycloakSecurityContext>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccessToken accessToken = principal.getKeycloakSecurityContext().getToken();
        String userId =accessToken.getSubject();
        String username = accessToken.getPreferredUsername();
        String email = accessToken.getEmail();
        // Role
        Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
//        List<String> temp =new ArrayList<>();
//        temp.addAll(principal.getKeycloakSecurityContext().getToken().getResourceAccess(clientId).getRoles());
        result.setUserId(userId);
        result.setFirstName(accessToken.getGivenName());
        result.setLastName(accessToken.getFamilyName());
        result.setUsername(username);
        result.setEmail(email);
        result.setBirthday(accessToken.getBirthdate());
        result.setGender(accessToken.getGender());
        result.setRoles(authorities.stream().map(SimpleGrantedAuthority::getAuthority).collect(Collectors.toList()));
        result.setRole(accessToken.getResourceAccess(clientId)==null ? null: accessToken.getResourceAccess(clientId).getRoles().toString());
        return result;
    }
}
