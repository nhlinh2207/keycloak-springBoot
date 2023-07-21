package com.example.springkeycloak.service.impl;

import com.example.springkeycloak.config.keycloak.CurrentUserProvider;
import com.example.springkeycloak.config.keycloak.KeyCloakUserService;
import com.example.springkeycloak.dto.KeycloakCurrentUser;
import com.example.springkeycloak.dto.UserDto;
import com.example.springkeycloak.dto.mapper.UserMapper;
import com.example.springkeycloak.dto.request.CheckCorrectPassRequest;
import com.example.springkeycloak.dto.response.ResponseObject;
import com.example.springkeycloak.dto.response.ResponseStatus;
import com.example.springkeycloak.exception.UnSuccessException;
import com.example.springkeycloak.model.User;
import com.example.springkeycloak.repository.IUserRepository;
import com.example.springkeycloak.service.IUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserService implements IUserService {

    private final KeyCloakUserService keyCloakUserService;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public boolean verify(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);

        if (user == null) {
            return false;
        }else {
            keyCloakUserService.enableUser(user.getKeyCloakUserId());
            return true;
        }
    }

    @Override
    public ResponseObject<UserDto> create(KeycloakCurrentUser req) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        ResponseObject<UserDto> res = new ResponseObject<>(true, ResponseStatus.DO_SERVICE_SUCCESSFUL);
       try{
           KeycloakCurrentUser currentUser = keyCloakUserService.userRegister(req);
           SimpleDateFormat smf = new SimpleDateFormat("dd-MM-yyyy");
           if(req.getUsername()==null || req.getEmail() == null || req.getPassword() == null || req.getRole() ==null)
           {
               throw new UnSuccessException("Missing information");
           }
           if (userRepository.findByEmail(req.getEmail()) != null || userRepository.findByUsername(req.getUsername()) != null || !currentUser.getStatus().equals("Created")) {
               keyCloakUserService.deleteUser(currentUser.getUserId());
               throw new UnSuccessException("username or email is duplicate");
           }
           // Create verify code
           String verificationCode = RandomString.make(64);
           User user = this.userRepository.saveAndFlush(
                   User.builder()
                           .username(req.getUsername())
                           .firstName(req.getFirstName())
                           .lastName(req.getLastName())
                           .fullName(req.getFullName()==null? req.getFirstName()+" "+ req.getLastName(): req.getFullName())
                           .birthday(smf.parse(req.getBirthday()))
                           .phoneNumber(req.getMobile())
                           .keyCloakUserId(currentUser.getUserId())
                           .email(currentUser.getEmail())
                           .gender(req.getGender())
                           .isNew(true)
                           .createdTime(new Date())
                           .updateTime(new Date())
                           .verificationCode(verificationCode)
                           .build()
           );
           res.setData(UserMapper.maptoDto(user));

           // Send email to user

           return res;
       }catch (Exception e){
           e.printStackTrace();
           throw new UnSuccessException(e.getMessage());
       }

    }

    @Override
    public void delete(Long userId) {
        try{
            User currentUser = userRepository.findByUsername(currentUserProvider.getCurrentUser().getUsername());
            User userToDelete = userRepository.findById(userId).orElseThrow(
                    () -> new UnSuccessException("Can not find user with id : "+userId)
            );
            // Delete in keycloak
            keyCloakUserService.deleteUser(userToDelete.getKeyCloakUserId());
            // Delete in database
            userRepository.deleteById(userId);
        }catch (Exception e){
            throw new UnSuccessException(e.getMessage());
        }
    }

    @Override
    public ResponseObject<List<UserDto>> getAll() {
        try{
            ResponseObject<List<UserDto>> res = new ResponseObject<>(true, ResponseStatus.DO_SERVICE_SUCCESSFUL);
            List<UserDto> data = userRepository.findAll().stream().map(UserMapper::maptoDto).collect(Collectors.toList());
            res.setData(data);
            return res;
        }catch (Exception e){
            throw new UnSuccessException(e.getMessage());
        }
    }

    @Override
    public ResponseObject<String> checkCorrectOldPass(CheckCorrectPassRequest request) {
        try{
            ResponseObject<String> res = new ResponseObject<>(true, ResponseStatus.DO_SERVICE_SUCCESSFUL);
            if (keyCloakUserService.isPassCorrect(request.getUsername(), request.getOldPassword())){
                res.setData("Correct");
            }else{
                res.setData("Wrong");
            }
            return res;
        }catch (Exception e){
            throw new UnSuccessException(e.getMessage());
        }
    }

    @Override
    public ResponseObject<String> updatePassword(CheckCorrectPassRequest request) {
        try{
            ResponseObject<String> res = new ResponseObject<>(true, ResponseStatus.DO_SERVICE_SUCCESSFUL);
            User currentUser = userRepository.findByUsername(currentUserProvider.getCurrentUser().getUsername());
            // Check if old password is correct

            keyCloakUserService.updatePassword(currentUser.getKeyCloakUserId(), request.getNewPassword());
            res.setData("Update password success");
            return res;
        }catch (Exception e){
            e.printStackTrace();
            throw new UnSuccessException(e.getMessage());
        }
    }
}
