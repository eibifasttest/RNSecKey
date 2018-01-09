package com.rnseckey;


import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.telecom.Call;
import android.util.Base64;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.rnseckey.fingerprint.FingerprintAuthenticationDialogFragment;
import com.rnseckey.fingerprint.FingerprintHelper;

import org.json.JSONObject;

import java.security.PublicKey;
import java.util.HashMap;


public class TrustedDeviceModule extends ReactContextBaseJavaModule {

    public TrustedDeviceModule(ReactApplicationContext reactContext){
        super(reactContext);

    }

    @Override
    public String getName() {
        return "RNSecKey";
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void generateKey(Callback c){
        new FingerprintHelper(getReactApplicationContext()).clearKey();
        PublicKey key = new FingerprintHelper(getReactApplicationContext()).createKeyPair();
        String publicKeyString =  Base64.encodeToString(key.getEncoded(),Base64.DEFAULT);
        c.invoke(null,publicKeyString);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void getPublicKey(Callback c){
        PublicKey key = new FingerprintHelper(getReactApplicationContext()).createKeyPair();
        String publicKeyString =  Base64.encodeToString(key.getEncoded(),Base64.DEFAULT);
        c.invoke(null, publicKeyString);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void getSignature(String nonce,String message, final Callback c){
        new FingerprintHelper(getReactApplicationContext()).authenticate(nonce, message, getCurrentActivity(), new FingerprintAuthenticationDialogFragment.FingerprintListener(){
            @Override
            public void onSuccess(String sign) {
                c.invoke(null, sign);
            }

            @Override
            public void onFail(int code) {
                if(code == -10){
                    // fingerprint added or locked screen disabled
                    c.invoke(-10, "fingerprint added or locked screen disabled");
                    new FingerprintHelper(getReactApplicationContext()).clearKey();

                }else if(code == -11){
                    c.invoke(-11, "User cancel");
                }
            }
        }, null);
    }

    @ReactMethod
    public void removeKeyPair(Callback c){}
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void isFingerprintSupported(Callback c){
         c.invoke(null, new FingerprintHelper(getReactApplicationContext()).hasFingerprintSupport());
    }
    @ReactMethod
    public void isLockScreenEnabled(Callback c){
        c.invoke(null, isLockscreen());
    };

    private boolean isLockscreen(){
        KeyguardManager m = (KeyguardManager) getReactApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return m.isDeviceSecure();
        }else{
            return false;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void isEligibleForFingerprint(Callback c){
        if(!(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)) {
            c.invoke(null, false);
            return;
        }
        if(!isLockscreen()){
            c.invoke(null, false);
            return;
        }
        c.invoke(null, new FingerprintHelper(getReactApplicationContext()).hasEnrolledFingerprints());

    };
    @ReactMethod
    public void getDeviceName(Callback c){
        c.invoke(null, (""+Build.MANUFACTURER).toUpperCase()+" "+android.os.Build.MODEL+"-"+ Build.SERIAL);
    };
    @ReactMethod
    public void getDeviceVersion(Callback c){
        c.invoke(null, Build.VERSION.SDK_INT);
    };

    @ReactMethod
    public void getDeviceId(Callback c){
        SharedPreferences sharedPref = getCurrentActivity().getPreferences(Context.MODE_PRIVATE);
        c.invoke(sharedPref.getString("TrustedDeviceId", null));

    };
    @ReactMethod
    public void saveDeviceId(String id, Callback c){
        SharedPreferences sharedPref = getCurrentActivity().getPreferences(Context.MODE_PRIVATE);
        sharedPref.edit().putString("TrustedDeviceId", id).commit();
        c.invoke(true);
    };
    @ReactMethod
    public void removeDeviceId(Callback c){
        SharedPreferences sharedPref = getCurrentActivity().getPreferences(Context.MODE_PRIVATE);
        sharedPref.edit().putString("TrustedDeviceId", null).commit();
        c.invoke(true);
    };


}
