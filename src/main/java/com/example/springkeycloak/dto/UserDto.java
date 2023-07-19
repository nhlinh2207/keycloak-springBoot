package com.example.springkeycloak.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserDto {

    private Long id;
    private String keycloakUserId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String birthday;
    private String phoneNumber;
    private String createTime;
    private String updateTime;

}
