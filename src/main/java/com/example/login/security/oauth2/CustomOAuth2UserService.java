package com.example.login.security.oauth2;

import com.example.login.exception.OAuth2Exception;
import com.example.login.model.AuthProvider;
import com.example.login.model.UserProfile;
import com.example.login.repository.UserRepository;
import com.example.login.security.UserPrincipal;
import com.example.login.security.oauth2.user.UserInfo;
import com.example.login.security.oauth2.user.UserInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        UserInfo userInfo = UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if(StringUtils.isEmpty(userInfo.getEmail())) {
            throw new OAuth2Exception("Email not found from OAuth2 provider");
        }

        Optional<UserProfile> userOptional = userRepository.findByEmail(userInfo.getEmail());
        UserProfile userProfile;
        if(userOptional.isPresent()) {
            userProfile = userOptional.get();
            if(!userProfile.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2Exception("Looks like you're signed up with " +
                        userProfile.getProvider() + " account. Please use your " + userProfile.getProvider() +
                        " account to login.");
            }
            userProfile = updateExistingUser(userProfile, userInfo);
        } else {
            userProfile = registerNewUser(oAuth2UserRequest, userInfo);
        }

        return UserPrincipal.create(userProfile, oAuth2User.getAttributes());
    }

    private UserProfile registerNewUser(OAuth2UserRequest oAuth2UserRequest, UserInfo userInfo) {
        UserProfile userProfile = new UserProfile();

        userProfile.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        userProfile.setProviderId(userInfo.getId());
        userProfile.setName(userInfo.getName());
        userProfile.setEmail(userInfo.getEmail());
        userProfile.setImageUrl(userInfo.getImageUrl());
        return userRepository.save(userProfile);
    }

    private UserProfile updateExistingUser(UserProfile existingUserProfile, UserInfo userInfo) {
        existingUserProfile.setName(userInfo.getName());
        existingUserProfile.setImageUrl(userInfo.getImageUrl());
        return userRepository.save(existingUserProfile);
    }

}
