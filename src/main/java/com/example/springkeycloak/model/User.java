package com.example.springkeycloak.model;

import com.example.springkeycloak.utils.CustomDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "tbl_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "KeycloakUserId")
    private String keyCloakUserId;

    @Column(name = "Username")
    private String username;

    @Column(name = "Email")
    private String email;

    @Column(name = "Firstname")
    private String firstName;

    @Column(name = "Lastname")
    private String lastName;

    @Column(name = "Fullname")
    private String fullName;

    @Column(name = "Gender")
    private String gender;

    @Column(name = "Birthday")
    private Date birthday;

    @Column(name = "IsNew")
    private Boolean isNew;

    @Column(name = "PhoneNNumber")
    private String phoneNumber;

    @Column(name = "VerificationCode")
    private String verificationCode;

    @JsonSerialize(using = CustomDateSerializer.class)
    @Column(name = "CreateTime")
    private Date createdTime;

    @JsonSerialize(using = CustomDateSerializer.class)
    @Column(name = "UpdateTime")
    private Date updateTime;

}
