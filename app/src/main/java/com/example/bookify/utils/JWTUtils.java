package com.example.bookify.utils;

import android.content.SharedPreferences;

import com.auth0.android.jwt.JWT;
import com.example.bookify.model.user.UserJWT;

import java.util.Date;

public abstract class JWTUtils {
    public static final String USER_ROLE = "userType";
    public static final String USER_ID = "id";
    public static final String USER_EMAIL = "subject";
    public static final String JWT_TOKEN = "token";
    public static final String EXPIRATION = "tokenExpires";

    public static void setCurrentLoginUser(SharedPreferences sharedPreferences, UserJWT token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JWT jwt = new JWT(token.getAccessToken());
        String role = jwt.getClaim("role").asString();
        String email = jwt.getSubject();
        Long id = jwt.getClaim("id").asLong();
        Long expiration = jwt.getExpiresAt().getTime();
        editor.putString(USER_ROLE, role);
        editor.putString(USER_EMAIL, email);
        editor.putLong(USER_ID, id);
        editor.putString(JWT_TOKEN, token.getAccessToken());
        editor.putLong(EXPIRATION, expiration);
        editor.commit();
    }

    public static void clearCurrentLoginUserData(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor
                .remove(USER_ROLE)
                .remove(USER_EMAIL)
                .remove(JWT_TOKEN)
                .remove(USER_ID)
                .remove(EXPIRATION)
                .commit();
    }
    public static boolean isLoggedIn(SharedPreferences sharedPreferences){
        String test = sharedPreferences.getString(JWT_TOKEN,"");
        return !test.isEmpty();
    }
    public static boolean hasTokenExpired(SharedPreferences sharedPreferences){
        Long time = sharedPreferences.getLong(EXPIRATION,-1);
        if(time < 0) return true;

        Date date = new Date(time);
        if((new Date()).after(date)) {
            clearCurrentLoginUserData(sharedPreferences);
            return true;
        }

        return false;
    }

}