package dev.kaua.squash.Data.Validation;

import dev.kaua.squash.Data.Account.DtoAccount;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ValidationServices {

    @POST("/validation/email")
    Call<DtoAccount> validate_email (@Body DtoAccount account);

    @POST("/validation/resend-email")
    Call<DtoAccount> resend_validate_email (@Body DtoAccount account);
}
