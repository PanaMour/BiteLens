package api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiHelper {

    private static final String BASE_URL = "https://api.nutritionix.com/";
    private static ApiHelper instance;
    private final NutritionixApi nutritionixApi;

    private ApiHelper() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        nutritionixApi = retrofit.create(NutritionixApi.class);
    }

    public static synchronized ApiHelper getInstance() {
        if (instance == null) {
            instance = new ApiHelper();
        }
        return instance;
    }

    public NutritionixApi getNutritionixApi() {
        return nutritionixApi;
    }
}

