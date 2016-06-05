package party.treesquaredcode.android.makerhawaii.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by rht on 6/4/16.
 */
public interface Api {
    @Headers("Content-Type: application/json")
    @GET("/makerspaces")
    Call<List<MakerSpace>> getMakerSpaceList();
}
