package com.hp.demoappfortwilio.fcm;


import com.hp.demoappfortwilio.model.Binding;
import com.hp.demoappfortwilio.model.Identity;
import com.hp.demoappfortwilio.model.Notification;
import com.hp.demoappfortwilio.model.Token;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.hp.demoappfortwilio.activity.VideoInviteActivity.TWILIO_SDK_STARTER_SERVER_URL;

public class TwilioSDKStarterAPI {
    /**
     * Resources defined in the sdk-starter projects available in C#, Java, Node, PHP, Python, or Ruby.
     *
     * https://github.com/TwilioDevEd?q=sdk-starter
     */
    interface SDKStarterService {
        // Fetch an access token
        @GET("/token")
        Call<Token> fetchToken();
        // Fetch an access token with a specific identity
        @POST("/token")
        Call<Token> fetchToken(@Query("identity") String identity);
        // Register this binding with Twilio Notify
        @POST("/register-binding")
        Call<Void> register(@Query("identity") String identity,@Query("BindingType") String BindingType,@Query("Address") String address,@Query("endpoint") String endpoint);
        // Send notifications to Twilio Notify registrants
        @POST("/send-notification")
       Call<Void> sendNotification(@Query("identity") String identity ,@Query("body") String body,@Query("room") String room);

      //  Call<Void> sendNotification(@Body Notification body);
    }

    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(logging.setLevel(HttpLoggingInterceptor.Level.BODY));

    private static SDKStarterService sdkStarterService = new Retrofit.Builder()
            .baseUrl(TWILIO_SDK_STARTER_SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
            .create(SDKStarterService.class);

    // Fetch a token with a specific identity
    public static Call<Token> fetchToken(final String identity) {
        if(identity == null) {
            return sdkStarterService.fetchToken();
        } else {
            return sdkStarterService.fetchToken(identity);
        }
    }

    public static Call<Void> registerBinding(final Binding binding) {
        return sdkStarterService.register(binding.identity,binding.bindingType,binding.address,binding.endpoint);
    }

    public static Call<Void> notify(Notification notification) {
        return sdkStarterService.sendNotification("abc1","test","abctest");
    }
}
