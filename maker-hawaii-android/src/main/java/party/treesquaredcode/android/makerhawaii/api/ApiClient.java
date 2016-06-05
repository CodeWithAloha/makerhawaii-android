package party.treesquaredcode.android.makerhawaii.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by rht on 6/4/16.
 */
public class ApiClient {
    private Api retrofitApi;

    private static ApiClient sharedInstance;

    public static ApiClient getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new ApiClient();
        }
        return sharedInstance;
    }

    private ApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://makerhawaii.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitApi = retrofit.create(Api.class);
    }

    public void getMakerSpaceList(final SuccessListener<List<MakerSpace>> successListener, final FailureListener failureListener) {
        retrofitApi.getMakerSpaceList().enqueue(new Callback<List<MakerSpace>>() {
            @Override
            public void onResponse(Call<List<MakerSpace>> call, Response<List<MakerSpace>> response) {
                List<MakerSpace> makerSpaceList = response.body();
                if (makerSpaceList != null) {
                    successListener.onSuccess(makerSpaceList);
                } else {
                    failureListener.onFailure();
                }
            }

            @Override
            public void onFailure(Call<List<MakerSpace>> call, Throwable t) {
                failureListener.onFailure();
            }
        });
    }

    public interface SuccessListener<T> {
        void onSuccess(T t);
    }

    public interface FailureListener {
        void onFailure();
    }
}
