package dev.kaua.squash.Data.GeoPlugin;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeoPluginService {

    @GET("json.gp")
    Call<DtoGeoPlugin> getGeo(@Query("ip") String ip);
}
