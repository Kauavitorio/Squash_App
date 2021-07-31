package dev.kaua.squash.Data.Account;

import dev.kaua.squash.Data.Post.DtoPost;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface    AccountServices {

    @POST("user/register")
    Call<DtoAccount> registerUser (@Body DtoAccount account);

    @POST("user/edit")
    Call<DtoAccount> edit (@Body DtoAccount account);

    @POST("user/search/from/username")
    Call<DtoPost> search_with_username (@Body DtoAccount account);

    @POST("user/login")
    Call<DtoAccount> login (@Body DtoAccount account);

    @POST("user/info/user")
    Call<DtoAccount> getUserInfo (@Body DtoAccount account);

    @POST("user/action/get-followers-following")
    Call<DtoAccount> get_followers_following (@Body DtoAccount account);

    @POST("user/action/follow")
    Call<DtoAccount> follow_a_user (@Body DtoAccount account);

    @POST("user/action/un-follow")
    Call<DtoAccount> un_follow_a_user (@Body DtoAccount account);

    @POST("user/action/check-username")
    Call<DtoAccount> check_username (@Body DtoAccount account);

    @POST("user/action/request/forgot-password")
    Call<DtoAccount> forgot_password (@Body DtoAccount account);

    @POST("user/action/change-password")
    Call<DtoAccount> change_password (@Body DtoAccount account);

    @POST("user/action/check/validation-code")
    Call<DtoAccount> check_validation_code (@Body DtoAccount account);
}
