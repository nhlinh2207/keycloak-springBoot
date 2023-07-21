package com.example.springkeycloak.config.keycloak;

import com.example.springkeycloak.dto.KeycloakCurrentUser;
import com.example.springkeycloak.dto.request.LoginRequest;
import com.example.springkeycloak.exception.UnSuccessException;
import com.example.springkeycloak.model.User;
import com.example.springkeycloak.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.*;

@Service
@Slf4j
public class KeyCloakUserService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Autowired
    private KeycloakPropertyReader keycloakPropertyReader;

    @Autowired
    private IUserRepository userRepository;

    // Đăng nhập
    public AccessTokenResponse login(LoginRequest loginRequest) {
        Map<String, Object> clientCredentials = new LinkedHashMap<>();
        clientCredentials.put("secret", clientSecret);
        Configuration configuration = new Configuration(authServerUrl, realm, clientId, clientCredentials, null);
        AuthzClient authzClient = AuthzClient.create(configuration);
        return authzClient.obtainAccessToken(loginRequest.getUsername(), loginRequest.getPassword());
    }

    // Tạo user
    public KeycloakCurrentUser userRegister(KeycloakCurrentUser keycloakCurrentUser) {

        // Create Keycloak
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .username("linh-master")
                .password("linh")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(ClientBuilder.newBuilder().build())
//                .resteasyClient( ClientBuilder.newBuilder().register(new CustomJacksonProvider()).build())
                .build();

        UserRepresentation user = new UserRepresentation();
                           user.setFirstName(keycloakCurrentUser.getFirstName());
                           user.setLastName(keycloakCurrentUser.getLastName());
                           user.setUsername(keycloakCurrentUser.getUsername());
                           user.setEmail(keycloakCurrentUser.getEmail());
                           user.setEnabled(true);
                           ;
        if(keycloakCurrentUser.getRequireUpdatePass()){
            // Update password
            System.out.println("Update password");
            List<String> listRequireAction = new ArrayList<>();
            listRequireAction.add("UPDATE_PASSWORD");
            user.setRequiredActions(listRequireAction);
        }

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResources = realmResource.users();
        ClientsResource clientsResource = realmResource.clients();
        usersResources.list().forEach(u -> System.out.println(u.getUsername()));
        clientsResource.findAll().forEach(c -> System.out.println(c.getClientId()));

        Response response = usersResources.create(user);
        keycloakCurrentUser.setStatusCode(response.getStatus());
        keycloakCurrentUser.setStatus(response.getStatusInfo().toString());

        if (response.getStatus() == 201) {
            // After created, get ID and update Password
            String userId = CreatedResponseUtil.getCreatedId(response);
            keycloakCurrentUser.setUserId(userId);
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(keycloakCurrentUser.getPassword());
            UserResource userResource = usersResources.get(userId);
            userResource.resetPassword(passwordCred);

            String client_uuid = keycloak.realm(this.realm).clients().findByClientId(this.clientId).get(0).getId();
            RoleRepresentation realmRole = realmResource.roles().get("REALM-USER").toRepresentation();
            RoleRepresentation clientRoleUser = realmResource.clients().get(client_uuid).roles().get(keycloakCurrentUser.getRole()).
                    toRepresentation();

            userResource.roles().clientLevel(client_uuid).add(Collections.singletonList(clientRoleUser));
            userResource.roles().realmLevel().add(Collections.singletonList(realmRole));
            userResource.update(user);
        }

        return keycloakCurrentUser;
    }

    //  Kích hoạt tài khoaarn user
    public void enableUser(String userID) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .username("linh-master")
                .password("linh")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(ClientBuilder.newBuilder().build())
//                .resteasyClient(ClientBuilder.newBuilder().register(new CustomJacksonProvider()).build())
                .build();
        UserResource userResource = keycloak.realm(realm).users().get(userID);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(true);
        userResource.update(user);
    }

    // Kiểm tra password
    public Boolean isPassCorrect(String username, String oldPass) {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", clientSecret);
        Configuration configuration = new Configuration(authServerUrl, realm, clientId, clientCredentials, null);
        AuthzClient authzClient = AuthzClient.create(configuration);
        AccessTokenResponse response = authzClient.obtainAccessToken(username, oldPass);
        return !response.getToken().isEmpty();
    }

    // Cập nhật password
    public void updatePassword(String userKeycloakId, String NewPassword) {
        @SuppressWarnings("unchecked")
                // Get Current Login user
        KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = (KeycloakPrincipal<RefreshableKeycloakSecurityContext>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KeycloakAdminClientConfig keycloakAdminClientConfig = KeycloakAdminClientUtils.loadConfig(keycloakPropertyReader);
        Keycloak keycloak = KeycloakAdminClientUtils.getKeycloakClient(principal.getKeycloakSecurityContext(), keycloakAdminClientConfig);

        // Get realm
        RealmResource realmResource = keycloak.realm(keycloakAdminClientConfig.getRealm());
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(userKeycloakId);
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(NewPassword);
        userResource.resetPassword(passwordCred);
        userResource.toRepresentation();
    }

    // Xóa user
    public Object deleteUser(String UserID){
        @SuppressWarnings("unchecked")
        KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = (KeycloakPrincipal<RefreshableKeycloakSecurityContext>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KeycloakAdminClientConfig keycloakAdminClientConfig = KeycloakAdminClientUtils.loadConfig(keycloakPropertyReader);
        Keycloak keycloak = KeycloakAdminClientUtils.getKeycloakClient(principal.getKeycloakSecurityContext(), keycloakAdminClientConfig);
        Response a = keycloak.realm(realm).users().delete(UserID);
        if(a.getStatus()==204)
            return "Deleted";
        return "not found";
    }

    // Tìm kiếm user
    public Object searchUsers(){
        @SuppressWarnings("unchecked")
        KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = (KeycloakPrincipal<RefreshableKeycloakSecurityContext>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KeycloakAdminClientConfig keycloakAdminClientConfig = KeycloakAdminClientUtils.loadConfig(keycloakPropertyReader);
        Keycloak keycloak = KeycloakAdminClientUtils.getKeycloakClient(principal.getKeycloakSecurityContext(), keycloakAdminClientConfig);
        Map<String, String > attibutes = new HashMap<>();
        attibutes.put("a","b");
        return keycloak.realm(realm).users().searchByAttributes(0,30,true,true,"subscriberType:Cá nhân");
    }

    // Refresh Token
    public AccessTokenResponse refreshToken(String refreshToken) {
        String url = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", clientSecret);
        Configuration configuration =
                new Configuration(authServerUrl, realm, clientId,
                        clientCredentials, null);
        Http http = new Http(configuration, (params, headers) -> {});

        return http.<AccessTokenResponse>post(url)
                .authentication()
                .client()
                .form()
                .param("grant_type", "refresh_token")
                .param("refresh_token", refreshToken)
                .param("client_id", clientId)
                .param("client_secret", clientSecret)
                .response()
                .json(AccessTokenResponse.class)
                .execute();
    }
}
