package com.rnseckey.biometricPrompt;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.Callback;

import java.security.Signature;
import java.security.SignatureException;

public class BiometricPromptCallBack extends BiometricPrompt.AuthenticationCallback {

    private String nonce;
    private Callback callback;

    public BiometricPromptCallBack(String nonce, Callback callback) {
        this.nonce = nonce;
        this.callback = callback;
    }

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        Log.e("RNSecKey", String.valueOf(errorCode));
        Log.e("RNSecKey", String.valueOf(errString));
        callback.invoke(errorCode, errString);
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        BiometricPrompt.CryptoObject cryptoObject = result.getCryptoObject();
        Signature signature = cryptoObject.getSignature();
        // Include a client nonce in the transaction so that the nonce is also signed by the private
        // key and the backend can verify that the same nonce can't be used to prevent replay
        // attacks.
        try {
            signature.update(nonce.getBytes());
            final byte[] sigBytes = signature.sign();
            callback.invoke(null, Base64.encodeToString(sigBytes, Base64.DEFAULT));
        } catch (SignatureException e) {
            Log.e("RNSecKey", e.getMessage());
            if(e.getMessage() != null && e.getMessage().contains("Key user not authenticated")) {
                // handle as if were KeyPermanentlyInvalidatedException
                callback.invoke(-12, "Key user not authenticated");
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        Log.e("RNSecKey", "onAuthenticationFailed");
    }
}
