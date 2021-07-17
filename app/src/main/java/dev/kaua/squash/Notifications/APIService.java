package dev.kaua.squash.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAATWHVxYU:APA91bE55dkIzNnGNfdrfVp8dgLokjVNAsg87kulffBIb80SuAze6zxu34WfyUhsehs7Kz0_oH93zfs5WYcWGe2h1xFObj-7rk8Q54aPtCcjX7G21BwgSB7EWDt21q6tkmqjdDqbsZUo"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
