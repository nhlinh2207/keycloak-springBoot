package com.example.springkeycloak.dto.mapper;

import com.example.springkeycloak.dto.UserDto;
import com.example.springkeycloak.model.User;

import java.text.SimpleDateFormat;

public class UserMapper {

    public static UserDto maptoDto(User user){
        SimpleDateFormat smf = new SimpleDateFormat("dd-MM-yyyy");
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .keycloakUserId(user.getKeyCloakUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .gender(user.getGender())
                .createTime(smf.format(user.getCreatedTime()))
                .updateTime(smf.format(user.getUpdateTime()))
                .build();
    }
}
