package com.mycompany.john.pickaplace.retrofit;

import com.mycompany.john.pickaplace.models.LocationCode;
import com.mycompany.john.pickaplace.models.MyCustomLocation;
import com.mycompany.john.pickaplace.models.User;
import com.mycompany.john.pickaplace.models.UserId;
import com.mycompany.john.pickaplace.models.UserWrapper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface BackendService {

    @Headers("Content-Type: application/json")
    @POST("api/locations")
    Call<ResponseBody> createAnonymousLocation(@Body MyCustomLocation myCustomLocation);

    @Headers("Content-Type: application/json")
    @POST("api/locations/live")
    Call<ResponseBody> createLiveAnonymousLocation(@Body MyCustomLocation myCustomLocation);

    @Headers("Content-Type: application/json")
    @POST("api/users")
    Call<ResponseBody> registerUser(@Body UserWrapper user);

    @Headers("Content-Type: application/json")
    @POST("api/users/sign_in")
    Call<ResponseBody> signInUser(@Body User user);

    @Headers("Content-Type: application/json")
    @POST("api/users/sign_out")
    Call<ResponseBody> signOutUser();

    @Headers("Content-Type: application/json")
    @POST("api/locations/get_location")
    Call<ResponseBody> getLocationByCode(@Body LocationCode locationCode);

    @Headers("Content-Type: application/json")
    @POST("api/locations/get_live_location")
    Call<ResponseBody> getLiveLocationByCode(@Body LocationCode locationCode);

    @Headers("Content-Type: application/json")
    @POST("api/locations//get_locations_by_user_id")
    Call<ResponseBody> getLocationsByUserId(@Body UserId user_id);
}
