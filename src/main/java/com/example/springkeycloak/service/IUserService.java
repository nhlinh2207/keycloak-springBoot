package com.example.springkeycloak.service;

import com.example.springkeycloak.dto.KeycloakCurrentUser;
import com.example.springkeycloak.dto.UserDto;
import com.example.springkeycloak.dto.response.ResponseObject;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public interface IUserService {

    boolean verify(String verificationCode);

    // Create user
    ResponseObject<UserDto> create(KeycloakCurrentUser req) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException;


}
