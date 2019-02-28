/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.rnseckey.fingerprint;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.rnseckey.R;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.Callable;

import static android.R.attr.data;
import static android.view.inputmethod.InputMethodManager.*;


/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements FingerprintUiHelper.Callback {

    private Button mCancelButton;
    private View mFingerprintContent, mVerifyTrusted;
    private View mBackupContent;
    private TextView mNewFingerprintEnrolledTextView;

    private Stage mStage = Stage.FINGERPRINT;
    private String nonce;

    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintUiHelper mFingerprintUiHelper;

    FingerprintUiHelper.FingerprintUiHelperBuilder mFingerprintUiHelperBuilder;
    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 32;


    FingerprintListener successRunable;
    OnAuthenticatedListener onAuthenticatedListener;
    private String scode = null;
    private String message = null;

    public void setSecretCode(String scode) {
        this.scode = scode;
    }

    public void setMessage(String message){
        this.message = message;
    }

    /**
     * code : -10 - new fingerprint add or lockscreen diabled, -11 - user cancel
     */
    public interface FingerprintListener{
        void onSuccess(String sign);
        void onFail(int code);
    }
    public interface OnAuthenticatedListener{
        void onAuthenticated();
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
    public void setOnAuthenticatedListener(OnAuthenticatedListener listener){
        onAuthenticatedListener = listener;
    }

    public void setSuccessRunable(FingerprintListener run){
        successRunable = run;
    }
    public FingerprintAuthenticationDialogFragment() {}

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FingerprintModule m = new FingerprintModule(getActivity());
        mFingerprintUiHelperBuilder = new FingerprintUiHelper.FingerprintUiHelperBuilder(m.providesFingerprintManager(getActivity()));
        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        // We register a new user account here. Real apps should do this with proper UIs.
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.sign_in));
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if(successRunable!=null){
                    if(mStage == Stage.NEW_FINGERPRINT_ENROLLED){
                        successRunable.onFail(-10);
                    }else {
                        successRunable.onFail(-11);
                    }
                }

            }
        });


        mFingerprintContent = v.findViewById(R.id.fingerprint_container);

        if(this.message!=null){
            if(message.toLowerCase().contains("<html>")){
                TextView tv = (TextView) v.findViewById(R.id.fingerprint_description);
                tv.setVisibility(View.GONE);
                WebView wv = (WebView) v.findViewById(R.id.fingerprint_description_wv);
                wv.loadDataWithBaseURL(null, message, "text/html", "utf-8", null);
                wv.setVisibility(View.VISIBLE);

            }else {
                TextView tv = (TextView) v.findViewById(R.id.fingerprint_description);
                tv.setText(message);
            }
        }
        mVerifyTrusted = v.findViewById(R.id.tv_verify_trusted);
        mBackupContent = v.findViewById(R.id.backup_container);


        mNewFingerprintEnrolledTextView = (TextView)
                v.findViewById(R.id.new_fingerprint_enrolled_description);
        mFingerprintUiHelper = mFingerprintUiHelperBuilder.build(
                (ImageView) v.findViewById(R.id.fingerprint_icon),
                (TextView) v.findViewById(R.id.fingerprint_status), this);
        updateStage();

        // If fingerprint authentication is not available, switch immediately to the backup
        // (password) screen.
        if (!mFingerprintUiHelper.isFingerprintAuthAvailable() &&  !mFingerprintUiHelper.isFingerprintDetected()) {
            goToBackup();
        }
        else if(scode!=null) {
            v.findViewById(R.id.fingerprint_container_with_code).setVisibility(View.VISIBLE);
            ((TextView) v.findViewById(R.id.fingerprint_container_with_code).findViewById(R.id.tv_secret_code)).setText(scode);
        }
        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        if(!mFingerprintUiHelper.isFingerprintAuthAvailable() && !mFingerprintUiHelper.isFingerprintDetected()){
            if(mCryptoObject==null){
                showAuthenticationScreen();
            }else {
                boolean secure =((KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE)).isDeviceSecure();
                if(secure) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onAuthenticated();
                        }
                    }, 2000);
                }else{

                }
            }
        }else if (mStage == Stage.FINGERPRINT) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showAuthenticationScreen() {
        // Create the Confirm Credentials screen. You can customize the title and description. Or
        // we will provide a generic one for you if you leave it null
        Intent intent = ((KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE)).createConfirmDeviceCredentialIntent("FSM One", "Unlock this device for verification...");
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == Activity.RESULT_OK) {
                if(onAuthenticatedListener!=null){
                    onAuthenticatedListener.onAuthenticated();
                }
            } else {
                if(successRunable!=null){
                    successRunable.onFail(-11);
                }

            }
        }
    }


    public void setStage(Stage stage) {
        mStage = stage;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void goToBackup() {
        mCancelButton.setText(R.string.cancel);
        mFingerprintContent.setVisibility(View.GONE);
        mBackupContent.setVisibility(View.VISIBLE);

        boolean secure = ((KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE)).isDeviceSecure();
        if(!secure){
            ((TextView)mBackupContent.findViewById(R.id.tv_verify_trusted)).setText(R.string.lockscreensettingchange);
            mCancelButton.setText(R.string.ok);
            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if(successRunable!=null){
                        successRunable.onFail(-10);
                    }
                }
            });
        }
    }


    /**
     * Checks whether the current entered password is correct, and dismisses the the dialog and lets
     * the activity know about the result.
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateStage() {
        switch (mStage) {
            case FINGERPRINT:
                mCancelButton.setText(R.string.cancel);
                mFingerprintContent.setVisibility(View.VISIBLE);
                mBackupContent.setVisibility(View.GONE);
                break;
            case NEW_FINGERPRINT_ENROLLED:
                // Intentional fall through
            case PASSWORD:
                mCancelButton.setText(R.string.ok);
                mFingerprintContent.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                if (mStage == Stage.NEW_FINGERPRINT_ENROLLED) {
                    mNewFingerprintEnrolledTextView.setVisibility(View.VISIBLE);
                    if(!mFingerprintUiHelper.isFingerprintAuthAvailable() && !mFingerprintUiHelper.isFingerprintDetected()){
                        mNewFingerprintEnrolledTextView.setText(R.string.lockscreensettingchange);
                    }
                    mVerifyTrusted.setVisibility(View.GONE);
                }
                break;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAuthenticated() {

                Signature signature = mCryptoObject.getSignature();
                // Include a client nonce in the transaction so that the nonce is also signed by the private
                // key and the backend can verify that the same nonce can't be used to prevent replay
                // attacks.
                try {
                    signature.update(nonce.getBytes());
                    final byte[] sigBytes = signature.sign();

                                if(successRunable!=null){
                                    successRunable.onSuccess(Base64.encodeToString(sigBytes, Base64.DEFAULT));

                                }
                                dismiss();

                } catch (SignatureException e) {
                    throw new RuntimeException(e);
                }


    }

    @Override
    public void onError(int code) {
        successRunable.onFail(code);
        if(scode!=null) {
            dismiss();
        }
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
        PASSWORD
    }
}
