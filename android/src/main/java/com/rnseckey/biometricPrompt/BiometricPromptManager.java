package com.rnseckey.biometricPrompt;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.facebook.react.bridge.Callback;
import com.rnseckey.fingerprint.FingerprintHelper;

public class BiometricPromptManager {

    private Context context;

    public BiometricPromptManager(Context context) {
        this.context = context;
    }

    public BiometricPrompt getBiometricPrompt(String nonce, Callback c) {
        return new BiometricPrompt((FragmentActivity) context, ContextCompat.getMainExecutor(context), new BiometricPromptCallBack(nonce, c));
    }

    public boolean isFingerprintSupported() {
        int authenticateFlag = this.checkIfBiometricAvailable();
        return (authenticateFlag == BiometricManager.BIOMETRIC_SUCCESS || authenticateFlag == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED);
    }

    public boolean isEligibleForFingerprint() {
        int authenticateFlag = this.checkIfBiometricAvailable();
        return (authenticateFlag == BiometricManager.BIOMETRIC_SUCCESS || authenticateFlag == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public BiometricPrompt.CryptoObject constructCryptoObject(String type) {
        FingerprintHelper fingerprintHelper = new FingerprintHelper(context);
        return new BiometricPrompt.CryptoObject(fingerprintHelper.getSignature(type));
    }

    public BiometricPrompt.PromptInfo constructPromptInfo(String message) {
         return new BiometricPrompt.PromptInfo.Builder()
                 .setTitle("Biometric Verification.")
                 .setSubtitle(message)
                 .setDescription("b2bds123jdn12")
                 .setConfirmationRequired(false)
                 .setNegativeButtonText("Cancel")
                 .build();
    }

    private int checkIfBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(context);

        int authenticateFlag = biometricManager.canAuthenticate();

        switch (authenticateFlag) {
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
                context.startActivity(enrollIntent);
                break;
        }
        return authenticateFlag;
    }
}
