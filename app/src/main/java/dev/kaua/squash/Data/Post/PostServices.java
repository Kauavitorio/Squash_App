package dev.kaua.squash.Data.Post;

import java.util.ArrayList;

import dev.kaua.squash.Data.Account.DtoAccount;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PostServices {

    @POST("post/list/")
    Call<ArrayList<DtoPost>> getRecommendedPosts(@Body DtoAccount account );

    @POST("post/list/an-user")
    Call<ArrayList<DtoPost>> getUserPosts(@Body DtoAccount account );

    @POST("post/action/like")
    Call<DtoPost> like_Un_Like_A_Post(@Body DtoPost post );

    @POST("post/action/like/comment")
    Call<DtoPost> like_Un_Like_A_Post_Comment(@Body DtoPost post );

    @POST("user/post/new/")
    Call<DtoPost> do_new_post(@Body DtoPost post );

    @POST("post/action/comment")
    Call<DtoPost> create_a_comment(@Body DtoPost post );

    @POST("post/action/info")
    Call<DtoPost> get_post_info(@Query("post_id") long post_id);

    @POST("user/post/delete/")
    Call<DtoPost> delete_post(@Body DtoPost post );

    @POST("user/admin/warn/user")
    Call<DtoPost> warn_an_user(@Body DtoAccount account );
}
