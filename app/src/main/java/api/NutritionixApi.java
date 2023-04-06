package api;

import com.example.bitelens.NutritionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface NutritionixApi {

    @Headers({
            "x-app-id: 2d48b89c",
            "x-app-key: 892b13836fa897c79885f4777b829e22",
            "x-remote-user-id: 0"
    })
    @GET("/v2/natural/nutrients")
    Call<NutritionResponse> getNutritionInfo(@Query("query") String foodName);
}
