package com.rnseckey.fingerprint;

import android.app.Activity;
import android.app.Fragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.RequiresApi;

import com.rnseckey.R;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

/**
 * Created by woonchee.tang on 16/01/2017.
 */

@androidx.annotation.RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintHelper {
    private static final String DIALOG_FRAGMENT_TAG = "myFragment";

    KeyguardManager mKeyguardManager;
    FingerprintManager mFingerprintManager;
    FingerprintAuthenticationDialogFragment mFragment;
    KeyStore mKeyStore;
    KeyPairGenerator mKeyPairGenerator;
    Signature mSignature;
    SharedPreferences mSharedPreferences;
    public static final String KEY_NAME = "my_key";
    public static final String KEY_NAME_SIGN = "my_key_sign";


    @RequiresApi(api = Build.VERSION_CODES.M)
    public FingerprintHelper(Context context){
        FingerprintModule m = new FingerprintModule(context);
        mKeyguardManager = m.providesKeyguardManager(context);
        mFingerprintManager = m.providesFingerprintManager(context);
        mSignature = m.providesSignature(m.providesKeystore());
        mKeyStore = m.providesKeystore();
        mSignature = m.providesSignature(mKeyStore);
        mSharedPreferences = m.providesSharedPreferences(context);
        mKeyPairGenerator = m.providesKeyPairGenerator();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasEnrolledFingerprints(){
        if (mFingerprintManager == null) {
            return false;
        }
       return mFingerprintManager.hasEnrolledFingerprints();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasFingerprintSupport(){
        if (mFingerprintManager == null) {
            return false;
        }
        return mFingerprintManager.isHardwareDetected();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public PublicKey[] createKeyPair() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.

        try {
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            PublicKey[] publicKeys = getExistingKey();

            if(publicKeys[0]!=null){
                return publicKeys;
            }

            KeyPair key1 = createNewKey(KEY_NAME);
            KeyPair key2 = createNewKey(KEY_NAME_SIGN);

            publicKeys[0] = key1.getPublic();
            publicKeys[1] = key2.getPublic();
            return publicKeys;
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

    }
    private KeyPair createNewKey(String tag) throws InvalidAlgorithmParameterException{
        boolean requireUserAuthentication = mFingerprintManager != null && mFingerprintManager.isHardwareDetected();
        mKeyPairGenerator.initialize(
            new KeyGenParameterSpec.Builder(tag,
                KeyProperties.PURPOSE_SIGN)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        // Require the user to authenticate with a fingerprint to authorize
                        // every use of the private key
                        .setUserAuthenticationRequired(requireUserAuthentication)
                        .build());
        return mKeyPairGenerator.generateKeyPair();
    }
    public void clearKey(){
        try {
            mKeyStore.load(null);
            mKeyStore.deleteEntry(KEY_NAME);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private PublicKey[] getExistingKey(){
        PublicKey[] keys = new PublicKey[2];
        try {
            mKeyStore.load(null);
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(KEY_NAME, null);
            if (entry == null) {
                return keys;
            }
            keys[0] = entry.getCertificate().getPublicKey();
            entry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(KEY_NAME_SIGN, null);
            keys[1] = entry.getCertificate().getPublicKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return keys;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void authenticate(String type, String nonce, String message, Activity context, final FingerprintAuthenticationDialogFragment.FingerprintListener callable, String scode){

        final String tag = "SIGNING".equalsIgnoreCase(type)? KEY_NAME_SIGN: KEY_NAME;

        mFragment = (FingerprintAuthenticationDialogFragment)Fragment.instantiate(context,FingerprintAuthenticationDialogFragment.class.getName());
        try {

            boolean status = initSignature(tag);
            if ((status && hasFingerprintSupport() && hasEnrolledFingerprints()) || (status && !hasFingerprintSupport())) {
                // Show the fingerprint dialog. The user has the option to use the fingerprint with
                // crypto, or you can fall back to using a server-side verified password.
//                if(hasEnrolledFingerprints())
                mFragment.setCryptoObject(new FingerprintManager.CryptoObject(mSignature));
                mFragment.setSuccessRunable(callable);
                mFragment.setNonce(nonce);
                boolean useFingerprintPreference = mSharedPreferences
                        .getBoolean(context.getString(R.string.use_fingerprint_to_authenticate_key),
                                true);
                if (useFingerprintPreference) {
                    mFragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
                } else {
                    mFragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.PASSWORD);
                }

                if(scode!=null){
                    mFragment.setSecretCode(scode);
                }
                if(message!=null){
                    mFragment.setMessage(message);
                }

                mFragment.show(context.getFragmentManager(), DIALOG_FRAGMENT_TAG);
            } else {
                // This happens if the lock screen has been disabled or or a fingerprint got
                // enrolled. Thus show the dialog to authenticate with their password first
                // and ask the user if they want to authenticate with fingerprints in the
                // future
                callable.onFail(-10);
            }

        }catch (UserNotAuthenticatedException e){
            mFragment.setSuccessRunable(callable);
            mFragment.setNonce(nonce);
            mFragment.setStage(FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
            mFragment.show(context.getFragmentManager(), DIALOG_FRAGMENT_TAG);
            mFragment.setOnAuthenticatedListener(new FingerprintAuthenticationDialogFragment.OnAuthenticatedListener() {
                @Override
                public void onAuthenticated() {
                    try{
                        initSignature(tag);
                        mFragment.setCryptoObject(new FingerprintManager.CryptoObject(mSignature));
                    }catch (Exception e){
                        e.printStackTrace();
                        callable.onFail(-11);
                    }
                }
            });
        } catch (KeyPermanentlyInvalidatedException e) {
            throw new RuntimeException(e);
        }
    }

    public Signature getSignature(String type) throws KeyPermanentlyInvalidatedException {
        try {
            final String tag = "SIGNING".equalsIgnoreCase(type)? KEY_NAME_SIGN: KEY_NAME;
            boolean status = initSignature(tag);

            if (status) {
                return mSignature;
            }
        } catch (UserNotAuthenticatedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean initSignature(String tag) throws UserNotAuthenticatedException, KeyPermanentlyInvalidatedException {
        try {
            mKeyStore.load(null);
            PrivateKey key = (PrivateKey) mKeyStore.getKey(tag, null);
            mSignature.initSign(key);
            return true;
        }catch(UserNotAuthenticatedException e){
            throw e;
        }
        catch (KeyPermanentlyInvalidatedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}
