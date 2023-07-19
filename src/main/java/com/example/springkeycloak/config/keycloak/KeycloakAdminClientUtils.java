package com.example.springkeycloak.config.keycloak;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
public class KeycloakAdminClientUtils {

    public static KeycloakAdminClientConfig loadConfig(KeycloakPropertyReader keycloakPropertyReader) {

        KeycloakAdminClientConfig.KeycloakAdminClientConfigBuilder builder = KeycloakAdminClientConfig.builder();

        try {
            String keycloakServer = System.getProperty("keycloak.url");

            builder = StringUtils.isBlank(keycloakServer) ?
                      builder.serverUrl(keycloakPropertyReader.getProperty("keycloak.auth-server-url")) :
                      builder.serverUrl(keycloakServer);

            String realm = System.getProperty("keycloak.realm");

            builder = StringUtils.isBlank(realm) ?
                    builder.realm(keycloakPropertyReader.getProperty("keycloak.realm")) :
                    builder.realm(realm);

            String clientId = System.getProperty("keycloak.clientId");

            builder = StringUtils.isBlank(clientId) ?
                    builder.clientId(keycloakPropertyReader.getProperty("keycloak.resource")) :
                    builder.clientId(clientId);


            String clientSecret = System.getProperty("keycloak.credentials.secret");

            builder = StringUtils.isBlank(clientSecret) ?
                    builder.clientSecret(keycloakPropertyReader.getProperty("keycloak.credentials.secret")) :
                    builder.clientSecret(clientSecret);

        } catch (Exception e) {
            log.error("Error: Loading keycloak admin configuration => {}", e.getMessage());
        }
        return builder.build();
    }

    public static Keycloak getKeycloakClient(KeycloakSecurityContext session, KeycloakAdminClientConfig config) {

        return KeycloakBuilder.builder()
                .serverUrl(config.getServerUrl())
                .realm(config.getRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .authorization(session.getTokenString())
//                .resteasyClient( ClientBuilder.newBuilder().register(new CustomJacksonProvider()).build())
//                .resteasyClient(ClientBuilder.newBuilder().build())
                .build();
    }

    public static void addRolesToListOf(Keycloak keycloak, KeycloakAdminClientConfig keycloakAdminClientConfig, List<String> roles, String compositeRole) {

        final String clientUuid = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients().findByClientId(keycloakAdminClientConfig.getClientId()).get(0).getId();
        RolesResource rolesResource = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients().get(clientUuid).roles();

        // Get existing roles of client
        final List<RoleRepresentation> existingRoles = rolesResource.list();
        List<RoleRepresentation> rolesAddToComposite = new LinkedList<>();
        roles.forEach(item-> rolesAddToComposite.add(rolesResource.get(item).toRepresentation()));

//        final boolean roleExists = existingRoles.stream().anyMatch(r -> r.getName().equals(role));
//
//        if (!roleExists) {
//            RoleRepresentation roleRepresentation = new RoleRepresentation();
//            roleRepresentation.setName(role);
//            roleRepresentation.setClientRole(true);
//            roleRepresentation.setComposite(false);
//
//            rolesResource.create(roleRepresentation);
//        }

        final boolean compositeExists = existingRoles.stream().anyMatch(r -> r.getName().equals(compositeRole));

        if (!compositeExists) {
            RoleRepresentation compositeRoleRepresentation = new RoleRepresentation();
            compositeRoleRepresentation.setName(compositeRole);
            compositeRoleRepresentation.setClientRole(true);
            compositeRoleRepresentation.setComposite(true);
            rolesResource.create(compositeRoleRepresentation);
        }

        final RoleResource compositeRoleResource = rolesResource.get(compositeRole);
        try {
            Set<RoleRepresentation> oldComposites = compositeRoleResource.getRoleComposites();
            List<RoleRepresentation> roleToDelete = new ArrayList<>(oldComposites);
            compositeRoleResource.deleteComposites(roleToDelete);

        } catch (NotFoundException e) {
            log.warn("Role {} does not exists!", compositeRole);
        }
        compositeRoleResource.addComposites(rolesAddToComposite);

//        final boolean alreadyAdded = compositeRoleResource.getRoleComposites().stream().anyMatch(r -> r.getName().equals(role));
//
//        if (!alreadyAdded) {
//            final RoleRepresentation roleToAdd = rolesResource.get(role).toRepresentation();

//
//            compositeRoleResource.addComposites(Collections.singletonList(roleToAdd));
//        }
    }

    public static void removeRoleInListOf(Keycloak keycloak, KeycloakAdminClientConfig keycloakAdminClientConfig, String role, String compositeRole) {

        final String clientUuid = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients().findByClientId(keycloakAdminClientConfig.getClientId()).get(0).getId();
        final RolesResource rolesResource = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients().get(clientUuid).roles();
        final RoleResource compositeRoleResource = rolesResource.get(compositeRole);

        try {
            final RoleRepresentation roleToDelete = rolesResource.get(role).toRepresentation();
            compositeRoleResource.getRoleComposites().remove(roleToDelete);

        } catch (NotFoundException e) {
            log.warn("Role {} does not exists!", role);
        }
    }

    public static List<RoleRepresentation> removeRoleInList(List<RoleRepresentation> listOfRoleRepresentation, RoleRepresentation roleToBeRemove) {

        listOfRoleRepresentation.remove(roleToBeRemove);

        List<RoleRepresentation> updatedListRoleRepresentation = new ArrayList<>();
        for (RoleRepresentation roleRepresentationItem : listOfRoleRepresentation) {
            if (!roleToBeRemove.getName().equalsIgnoreCase(roleRepresentationItem.getName())) {
                updatedListRoleRepresentation.add(roleRepresentationItem);
            }
        }

        return updatedListRoleRepresentation;
    }
}
