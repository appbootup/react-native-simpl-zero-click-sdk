package com.android.simplSdk;

import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.simpl.android.fingerprint.SimplFingerprint;
import com.simpl.android.fingerprint.SimplFingerprintListener;
import com.simpl.android.zeroClickSdk.Simpl;
import com.simpl.android.zeroClickSdk.SimplPaymentDueListener;
import com.simpl.android.zeroClickSdk.SimplPaymentUrlRequest;
import com.simpl.android.zeroClickSdk.SimplUser;
import com.simpl.android.zeroClickSdk.SimplUserApprovalListenerV2;
import com.simpl.android.zeroClickSdk.SimplZeroClickTokenAuthorization;
import com.simpl.android.zeroClickSdk.SimplZeroClickTokenListener;

import java.util.HashMap;

public class SimplSdkModule extends ReactContextBaseJavaModule {
    private static final String TAG = SimplSdkModule.class.getSimpleName();

    public SimplSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "SimplSdk";
    }

    @ReactMethod
    public void isApproved(final String merchantId, final String mobileNumber, final String emailId, final boolean isSandbox,
                           final Callback successCallback, final Callback errorCallback) {
        Simpl.init(getReactApplicationContext(), merchantId);
        Log.d(TAG, "isApproved(): merchantId: " + merchantId + " mobileNumber: " + mobileNumber + " emailId: " + emailId);
        if (isSandbox)
            Simpl.getInstance().runInSandboxMode();
        Simpl.getInstance().isUserApproved(new SimplUser(emailId, mobileNumber))
                .execute(new SimplUserApprovalListenerV2() {
                    @Override
                    public void onSuccess(final boolean b, String s, boolean b1) {
                        successCallback.invoke(b);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        errorCallback.invoke(throwable.getLocalizedMessage());
                    }
                });
    }

    @ReactMethod
    public void generateZeroClickToken(final Callback successCallback, final Callback errorCallback) {
        Simpl.getInstance().generateZeroClickToken(new SimplZeroClickTokenListener() {
            @Override
            public void onSuccess(SimplZeroClickTokenAuthorization simplZeroClickTokenAuthorization) {
                successCallback.invoke(simplZeroClickTokenAuthorization.getZeroClickToken());
            }

            @Override
            public void onFailure(Throwable throwable) {
                errorCallback.invoke(throwable.getLocalizedMessage());
            }
        });
    }

    @ReactMethod
    public void openRedirectionURL(final String paymentRedirectionUrl, final Callback successCallback, final Callback errorCallback) {
        SimplPaymentUrlRequest request = Simpl.getInstance().openRedirectionURL(getReactApplicationContext(), paymentRedirectionUrl);
        request.execute(new SimplPaymentDueListener() {
            @Override
            public void onSuccess(String message) {
                successCallback.invoke(message);
            }

            @Override
            public void onError(final Throwable throwable) {
                errorCallback.invoke(throwable.getLocalizedMessage());
            }
        });

    }

    @ReactMethod
    public void generateFingerprint(final String merchantId, final String phoneNo, final String email, final ReadableMap merchantParams, final Callback callback) {
        HashMap<String, String> params = merchantParams != null ? MapUtils.toHashMap(merchantParams) : new HashMap<String, String>();
        params.put("merchant", merchantId );
        params.put("phone_number", phoneNo);
        params.put("email", email );
        SimplFingerprint.getInstance().generateFingerprint(new SimplFingerprintListener() {
            @Override
            public void fingerprintData(String fingerprint) {
                callback.invoke(fingerprint);
            }
        }, params);
    }

}
