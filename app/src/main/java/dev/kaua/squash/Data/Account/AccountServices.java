package dev.kaua.squash.Data.Account;

import dev.kaua.squash.Data.Post.DtoPost;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AccountServices {
    String ROTE = "user";

    @POST(ROTE + "/register")
    Call<DtoAccount> registerUser (@Body DtoAccount account);

    @POST(ROTE + "/register/google")
    Call<DtoAccount> registerUserGoogle (@Body DtoAccount account);

    @POST(ROTE + "/edit")
    Call<DtoAccount> edit (@Body DtoAccount account);

    @POST(ROTE + "/account/active/google")
    Call<DtoAccount> active_google_login (@Body DtoAccount account);

    @POST(ROTE + "/search/from/username")
    Call<DtoPost> search_with_username (@Body DtoAccount account);

    @POST(ROTE + "/login-new")
    Call<DtoAccount> login (@Body DtoAccount account,  @Query("token") String token);

    @POST(ROTE + "/login-google")
    Call<DtoAccount> login_with_Google (@Body DtoAccount account,  @Query("token") String token);

    @POST(ROTE + "/info/user")
    Call<DtoAccount> getUserInfo (@Body DtoAccount account);

    @POST(ROTE + "/action/get-followers-following")
    Call<DtoAccount> get_followers_following (@Body DtoAccount account);

    @POST(ROTE + "/action/follow")
    Call<DtoAccount> follow_a_user (@Body DtoAccount account);

    @POST(ROTE + "/action/un-follow")
    Call<DtoAccount> un_follow_a_user (@Body DtoAccount account);

    @POST(ROTE + "/action/check-username")
    Call<DtoAccount> check_username (@Body DtoAccount account);

    @POST(ROTE + "/action/request/forgot-password")
    Call<DtoAccount> forgot_password (@Body DtoAccount account);

    @POST(ROTE + "/action/change-password")
    Call<DtoAccount> change_password (@Body DtoAccount account);

    @POST(ROTE + "/action/check/validation-code")
    Call<DtoAccount> check_validation_code (@Body DtoAccount account);

    @POST(ROTE + "/report/user/")
    Call<DtoAccount> report_an_user (@Body DtoAccount account);

    @POST(ROTE + "/action/update/base/info/")
    Call<DtoAccount> update_base_info (@Body DtoAccount account);

    @POST(ROTE + "/action/request/verification/")
    Call<DtoVerification> request_verification (@Body DtoVerification verification);

    @POST(ROTE + "/account/test/google")
    Call<DtoAccount> test_google_account (@Body DtoAccount account);
}
