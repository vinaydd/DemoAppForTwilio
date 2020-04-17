package com.hp.demoappfortwilio.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.hp.demoappfortwilio.R;
import com.hp.demoappfortwilio.activity.VideoInviteActivity;
import com.hp.demoappfortwilio.fcm.TwilioSDKStarterAPI;
import com.hp.demoappfortwilio.model.Binding;
import com.hp.demoappfortwilio.model.Token;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.hp.demoappfortwilio.activity.VideoInviteActivity.TWILIO_SDK_STARTER_SERVER_URL;
import static com.hp.demoappfortwilio.activity.VideoInviteActivity.token;
import static com.hp.demoappfortwilio.service.BindingSharedPreferences.ADDRESS;
import static com.hp.demoappfortwilio.service.BindingSharedPreferences.ENDPOINT;
import static com.hp.demoappfortwilio.service.BindingSharedPreferences.IDENTITY;


public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    /*
     * The notify binding type to use. Use FCM since GCM has been deprecated by Google
     */
    private static final String BINDING_TYPE = "fcm";

    private SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        register();
    }

    private void register() {
        if (TWILIO_SDK_STARTER_SERVER_URL.equals(getString(R.string.twilio_sdk_starter_server_url))) {
            String message = "Error: Set a valid sdk starter server url";
            Log.e(TAG, message);
            sendRegistrationFailure(message);
        } else {
            String identity = "abc";
          /*  TwilioSDKStarterAPI.fetchToken(identity).enqueue(new Callback<Token>() {
                @Override
                public void onResponse(Call<Token> call, Response<Token> response) {
                    if (response.isSuccessful()) {
                        Log.e("status","sucess");
                        bind(response.body().identity, response.body().token);
                    } else {
                        String message = "Fetching token failed: " + response.code() + " " + response.message();
                        Log.e(TAG, message);
                        sendRegistrationFailure(message);
                    }
                }

                @Override
                public void onFailure(Call<Token> call, Throwable t) {
                    String message = "Fetching token failed: " + t.getMessage();
                    Log.e(TAG, message);
                    sendRegistrationFailure(message);
                }
            });*/

            bind(identity, token);
        }
    }

    private void bind(final String identity, final String token) {
        // Load the old binding values from shared preferences if they exist
        final String endpoint = sharedPreferences.getString(ENDPOINT, null);
        final String address = sharedPreferences.getString(ADDRESS, null);

        /*
         * Generate a new endpoint based on the existing identity and the instanceID. This ensures
         * that we maintain stability of the endpoint even if the instanceID changes without the
         * identity changing. Android may change the instance id in some cases resulting in a call
         * from the FirebaseInstanceIDService
         */
        final String newEndpoint = identity + "@" + FirebaseInstanceId.getInstance().getId();

        /*
         * Obtain the new address based off the Firebase instance token
         */
        final String newAddress = FirebaseInstanceId.getInstance().getToken();

        if (newAddress == null) {
            /*
             * When the application is first installed it is possible that the token
             * generated by Firebase may not be assigned by the time register()
             * is called from the VideoInviteActivity.java causing this service to start.
             *
             * If this occurs, binding will be performed when onTokenRefresh() is called from
             * NotifyFirebaseInstanceIDService.
             */
            Log.w(TAG, "The Firebase token is not available yet.");
            return;
        }

        /*
         * Check whether a new binding registration is required by comparing the prior values that
         * were stored in shared preferences after the last successful binding registration.
         */
        if (newEndpoint.equals(endpoint) && newAddress.equals(address)) {
            Log.i(TAG, "A new binding registration was not performed because " +
                    "the binding values are the same as the last registered binding.");
            sendRegistrationSuccess(identity, token);
        } else {
            /*
             * Clear the existing binding from SharedPreferences and attempt to register
             * the new binding values.
             */
            sharedPreferences.edit()
                    .remove(IDENTITY)
                    .remove(ENDPOINT)
                    .remove(ADDRESS)
                    .apply();

            final Binding binding = new Binding(identity,
                    newEndpoint,
                    newAddress,
                    BINDING_TYPE,
                    VideoInviteActivity.NOTIFY_TAGS);

            TwilioSDKStarterAPI.registerBinding(binding).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        sharedPreferences.edit()
                                .putString(IDENTITY, identity)
                                .putString(ENDPOINT, newEndpoint)
                                .putString(ADDRESS, newAddress)
                                .apply();
                        sendRegistrationSuccess(identity, token);
                    } else {
                        String message = "Binding registration failed: " + response.code() + " " + response.message();
                        Log.e(TAG, message);
                        sendRegistrationFailure(message);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    String message = "Binding registration failed: " + t.getMessage();
                    Log.e(TAG, message);
                    sendRegistrationFailure(message);
                }
            });
        }
    }

    private void sendRegistrationSuccess(String identity, String token) {
        Log.e("sendsuccess","success");
        Intent intent = new Intent(VideoInviteActivity.ACTION_REGISTRATION);
        intent.putExtra(VideoInviteActivity.REGISTRATION_IDENTITY, identity);
        intent.putExtra(VideoInviteActivity.REGISTRATION_TOKEN, token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendRegistrationFailure(String message) {
        Log.e("failure","failure");
        Intent intent = new Intent(VideoInviteActivity.ACTION_REGISTRATION);
        intent.putExtra(VideoInviteActivity.REGISTRATION_ERROR, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
