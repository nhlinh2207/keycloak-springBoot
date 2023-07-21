package com.example.springkeycloak.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckCorrectPassRequest {

    private String username;
    private String oldPassword;
    private String newPassword;
}
