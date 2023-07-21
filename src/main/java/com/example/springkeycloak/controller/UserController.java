package com.example.springkeycloak.controller;

import com.example.springkeycloak.config.keycloak.KeyCloakUserService;
import com.example.springkeycloak.dto.KeycloakCurrentUser;
import com.example.springkeycloak.dto.request.CheckCorrectPassRequest;
import com.example.springkeycloak.dto.request.LoginRequest;
import com.example.springkeycloak.dto.response.ResponseObject;
import com.example.springkeycloak.dto.response.ResponseStatus;
import com.example.springkeycloak.service.IUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
@Slf4j
public class UserController {

    private final KeyCloakUserService keyCloakUserService;
    private final IUserService userService;

    @PostMapping(path = "/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
         return ResponseEntity.ok(keyCloakUserService.login(request));
    }

    @PostMapping(path = "/create")
    public ResponseEntity<?> createUser(@RequestBody KeycloakCurrentUser request) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
         log.info("Create user");
         return ResponseEntity.ok(userService.create(request));
    }

    @GetMapping(path = "/verify")
    public ResponseEntity<?> verify(@RequestParam("code") String verificationCode) {
        log.info("Controller: Xác thực tài khoản người dùng");
        boolean verified = userService.verify(verificationCode);
        if (verified) {
            log.info("Verify successfully !");
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://sec.cmcati.vn/ssa/pages/authentication/active-account-v2")).build();
        }
        log.info("Verification failed !");
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://sec.cmcati.vn/ssa/pages/miscellaneous/error")).build();
    }

    @DeleteMapping(value = "/delete/{userId}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long userId) {
        log.info("Delete user");
        userService.delete(userId);
        return ResponseEntity.ok(new ResponseObject<>(true, ResponseStatus.DO_SERVICE_SUCCESSFUL, "Delete user successfully !"));
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAll() {
        log.info("Get all users");
        return ResponseEntity.ok(userService.getAll());
    }

    @PostMapping(value = "/isPassCorrect")
    public ResponseEntity<?> checkCorrectPass(@RequestBody CheckCorrectPassRequest request){
        return ResponseEntity.ok(userService.checkCorrectOldPass(request));
    }

    @PutMapping(value = "/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody CheckCorrectPassRequest request){
        return ResponseEntity.ok(userService.updatePassword(request));
    }

}
