package api;

import com.example.bitelens.NutritionResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NutritionixApi {

    @Headers({
            "x-app-id: 2d48b89c",
            "x-app-key: 892b13836fa897c79885f4777b829e22",
            "x-remote-user-id: 0"
    })
    @POST("/v2/natural/nutrients")
    Call<NutritionResponse> getNutritionInfo(@Body Map<String, String> requestBody);
}

