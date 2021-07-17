package dev.kaua.squash.Data.Post;

import java.util.ArrayList;

import dev.kaua.squash.Data.Account.DtoAccount;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PostServices {

    @POST("post/list/")
    Call<ArrayList<DtoPost>> getRecommendedPosts(@Body DtoAccount account );

    @POST("post/list/an-user")
    Call<ArrayList<DtoPost>> getUserPosts(@Body DtoAccount account );

    @POST("user/post/new/")
    Call<DtoPost> do_new_post(@Body DtoPost post );
}