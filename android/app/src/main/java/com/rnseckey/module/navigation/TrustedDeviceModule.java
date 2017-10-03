package com.rnseckey.module.navigation;


import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
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


public class TrustedDeviceModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    public TrustedDeviceModule(ReactApplicationContext reactContext){
        super(reactContext);
        reactContext.addActivityEventListener(this);
        reactContext.addLifecycleEventListener(this);

    }

    @Override
    public String getName() {
        return "RNSecKey";
    }

    @ReactMethod
    public void generateKey(Callback c){

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void getPublicKey(Callback c){
        PublicKey key = new FingerprintHelper(getReactApplicationContext()).createKeyPair();
        String publicKeyString =  Base64.encodeToString(key.getEncoded(),Base64.DEFAULT);
        c.invoke(publicKeyString);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void getSignature(String nonce, final Callback c){
        new FingerprintHelper(getReactApplicationContext()).authenticate(nonce, getReactApplicationContext(), new FingerprintAuthenticationDialogFragment.FingerprintListener(){
            @Override
            public void onSuccess(String sign) {
                c.invoke(sign);
            }

            @Override
            public void onFail(int code) {
                if(code == -10){
                    // fingerprint added or locked screen disabled
                    c.invoke("VOID:"+getDeviceId());
                    new FingerprintHelper(cordova.getActivity()).clearKey();

                }else if(code == -11){
                    context.success("CANCEL");
                }
            }
        }, null);
    }
    @ReactMethod
    public void removeKeyPair(Callback c){}
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void isFingerprintSupported(Callback c){
         c.invoke(new FingerprintHelper(getReactApplicationContext()).hasFingerprintSupport());
    }
    @ReactMethod
    public void isLockScreenEnabled(Callback c){
        c.invoke(isLockscreen());
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
            c.invoke(false);
            return;
        }
        if(!isLockscreen()){
            c.invoke(false);
            return;
        }
        c.invoke(new FingerprintHelper(getReactApplicationContext()).hasEnrolledFingerprints());

    };
    @ReactMethod
    public void getDeviceName(Callback c){
        c.invoke((""+Build.MANUFACTURER).toUpperCase()+" "+android.os.Build.MODEL+"-"+ Build.SERIAL);
    };
    @ReactMethod
    public void getDeviceVersion(Callback c){};
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {

    }
}
