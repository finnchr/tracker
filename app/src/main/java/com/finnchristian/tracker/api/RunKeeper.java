package com.finnchristian.tracker.api;

import com.finnchristian.tracker.model.runkeeper.FitnessActivity;
import com.finnchristian.tracker.model.runkeeper.Token;
import com.finnchristian.tracker.model.runkeeper.User;
import com.google.common.base.Strings;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public class RunKeeper {
    public interface Service {
        @POST("/token")
        @Headers("Content-Type: application/x-www-form-urlencoded")
        Token getToken(@Query("code") final String code,
                       @Query("client_id") final String clientId,
                       @Query("client_secret") final String clientSecret,
                       @Query("grant_type") final String grantType,
                       @Query("redirect_uri") final String redirectUri);

        @GET("/user")
        @Headers("Accept: application/vnd.com.runkeeper.User+json")
        User getUser();

        @POST("/{fitnessActivitiesPath}")
        @Headers("Content-Type: application/vnd.com.runkeeper.NewFitnessActivity+json")
        Response postFitnessActivity(@Path("fitnessActivitiesPath") final String fitnessActivitiesPath, @Body final FitnessActivity fitnessActivity);
    }

    public static Service createService(final String endpoint, final Token token) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if(token != null) {
                            request.addHeader("Authorization", String.format("%s %s", token.getTokenType(), token.getAccessToken()));
                        }
                    }
                })
                .build();

        return restAdapter.create(Service.class);
    }

    public static Service createService(final String endpoint) {
        return createService(endpoint, null);
    }
}
