package com.example.login.security.oauth2.user;

import com.example.login.exception.OAuth2Exception;
import com.example.login.model.AuthProvider;

import java.util.Map;

public class UserInfoFactory {

    public static UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes)
    {
        if(registrationId.equalsIgnoreCase(AuthProvider.google.toString()))
        {
            return new GoogleUserInfo(attributes);
        }
        else if (registrationId.equalsIgnoreCase(AuthProvider.facebook.toString()))
        {
            return new FacebookUserInfo(attributes);
        }
        else
            {
            throw new OAuth2Exception("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
