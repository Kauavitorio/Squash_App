package dev.kaua.squash.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAAbBwXSb8:APA91bGSCRlr7TOIJldceWpJ15RUS0fA_XURr16KFzlIxEKf31dYELWqwDHABMeYZRBlGjlHlVQXFHztA2zFmpe6IQiwQLkCzTiyxoafe6FGZYkn5Kdy_i8YUrCcDEqyqHLIsAFOW8Mx"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
