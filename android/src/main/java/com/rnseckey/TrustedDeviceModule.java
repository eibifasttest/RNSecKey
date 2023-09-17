package com.rnseckey;


import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.security.keystore.KeyPermanentlyInvalidatedException;


import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.rnseckey.biometricPrompt.BiometricPromptManager;
import com.rnseckey.fingerprint.FingerprintAuthenticationDialogFragment;
import com.rnseckey.fingerprint.FingerprintHelper;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.PublicKey;



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
        PublicKey[] keys = new FingerprintHelper(getReactApplicationContext()).createKeyPair();
        String publicKeyString =  Base64.encodeToString(keys[0].getEncoded(),Base64.DEFAULT);
        System.out.println("publicKeyString" + publicKeyString);
        String publicKeySignString =  Base64.encodeToString(keys[1].getEncoded(),Base64.DEFAULT);
        System.out.println("publicKeySignString" + publicKeySignString);


        c.invoke(null,publicKeyString, publicKeySignString);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void getPublicKey(Callback c){
        PublicKey[] keys = new FingerprintHelper(getReactApplicationContext()).createKeyPair();
        String publicKeyString =  Base64.encodeToString(keys[0].getEncoded(),Base64.DEFAULT);
        String publicKeySignString =  Base64.encodeToString(keys[1].getEncoded(),Base64.DEFAULT);
        WritableMap resultData = new WritableNativeMap();
        resultData.putString("key", publicKeyString);
        resultData.putString("signKey", publicKeySignString);
        c.invoke(null, resultData);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void getSignature(final String type, final String nonce, final String message, final Callback c){
        Activity activity = getCurrentActivity();

        String[] messageArray = message.split("\\|");
        final String subtitle = messageArray[0];
        final String desc = messageArray.length > 1 ? messageArray[1] : null;


        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    try{
                        BiometricPromptManager biometricPromptManager = new BiometricPromptManager(getCurrentActivity());
                        BiometricPrompt.CryptoObject cryptoObject = biometricPromptManager.constructCryptoObject(type);
                        BiometricPrompt.PromptInfo promptInfo = biometricPromptManager.constructPromptInfo(subtitle, desc);
                        biometricPromptManager
                                .getBiometricPrompt(nonce, c)
                                .authenticate(promptInfo, cryptoObject);
                    } catch (KeyPermanentlyInvalidatedException e) {
                        c.invoke("KeyPermanentlyInvalidatedException", false);
                    }

                }
            });
        }
    }

    @ReactMethod
    public void removeKeyPair(Callback c){}
    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void isFingerprintSupported(Callback c){
         c.invoke(null, new BiometricPromptManager(getCurrentActivity()).isFingerprintSupported());
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
        c.invoke(null, new BiometricPromptManager(getCurrentActivity()).isEligibleForFingerprint());

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
        Activity activity = getCurrentActivity();
        if(activity==null){
            c.invoke("ERROR", null);
            return;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        if(sharedPref==null){
            c.invoke("ERROR", null);
            return;
        }
        c.invoke(null, sharedPref.getString("TrustedDeviceId", null));



    };

    @ReactMethod
    public void saveDeviceId(String id, Callback c){
        Activity activity = getCurrentActivity();
        if(activity==null){
            c.invoke("ERROR", null);
            return;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        if(sharedPref==null){
            c.invoke("ERROR", null);
            return;
        }
        sharedPref.edit().putString("TrustedDeviceId", id).commit();
        c.invoke(true);
    };
    @ReactMethod
    public void removeDeviceId(Callback c){
        Activity activity = getCurrentActivity();
        if(activity==null){
            c.invoke("ERROR", null);
            return;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        if(sharedPref==null){
            c.invoke("ERROR", null);
            return;
        }
        sharedPref.edit().putString("TrustedDeviceId", null).commit();
        c.invoke(true);
    };

    @ReactMethod
    public void isRooted(Callback c){
        c.invoke(RootUtil.isDeviceRooted());
    };

    public static class RootUtil {
        public static boolean isDeviceRooted() {
            return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
        }

        private static boolean checkRootMethod1() {
            String buildTags = android.os.Build.TAGS;
            return buildTags != null && buildTags.contains("test-keys");
        }

        private static boolean checkRootMethod2() {
            String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                    "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
            for (String path : paths) {
                if (new File(path).exists()) return true;
            }
            return false;
        }

        private static boolean checkRootMethod3() {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                if (in.readLine() != null) return true;
                return false;
            } catch (Throwable t) {
                return false;
            } finally {
                if (process != null) process.destroy();
            }
        }
    }


}
