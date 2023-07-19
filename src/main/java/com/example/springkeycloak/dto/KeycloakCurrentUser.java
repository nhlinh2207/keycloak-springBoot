package com.example.springkeycloak.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KeycloakCurrentUser {

    private String userId;

    private String username;
    private String email;
    private String password;

    private String role;


    private List<String> roles;
    private String firstName;
    private String fullName;
    private String lastName;
    private String birthday;
    private String jobTitle;
    private String gender;
    private String avatar;
    private String personalCountryId;
    private String countryBirthPlace;
    private String provinceBirthPlace;
    private String districtBirthPlace;
    private String communeBirthPlace;
    private String streetBirthPlace;
    private String homeNumberBirthPlace;
    private Long residencePlace;
    private String countryResidencePlace;
    private String provinceResidencePlace;
    private String districtResidencePlace;
    private String communeResidencePlace;
    private String streetResidencePlace;
    private String homeNumberResidencePlace;
    private String mobile;
    private UUID organizationUnitId;


    private boolean requireUpdatePass;
    private int statusCode;
    private String status;
}
