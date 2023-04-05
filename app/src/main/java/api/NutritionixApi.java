package api;

import com.example.bitelens.NutritionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface NutritionixApi {

    @Headers({
            "x-app-id: 2d48b89c",
            "x-app-key: bc5049461bafe66df01e0f68b8c7dc78",
            "x-remote-user-id: 0"
    })
    @GET("/v2/natural/nutrients")
    Call<NutritionResponse> getNutritionInfo(@Query("query") String foodName);

}
