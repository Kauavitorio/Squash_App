package dev.kaua.squash.Notifications;

import dev.kaua.squash.EncryptDep.StorageKeys;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({

            "Content-Type:application/json",
            "Authorization:key=" + StorageKeys.NOTIFICATION_KEY_SOCIAL
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
