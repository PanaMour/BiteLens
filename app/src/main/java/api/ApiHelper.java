package api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiHelper {
    private static ApiHelper instance;
    private static NutritionixApi nutritionixApi;

    public static ApiHelper getInstance() {
        if (instance == null) {
            instance = new ApiHelper();

            // Set up the logging interceptor for debugging purposes
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Set up the OkHttpClient with the logging interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // Set up the Retrofit instance with the base URL and Gson converter
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://trackapi.nutritionix.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            // Create an instance of the Nutritionix API using Retrofit
            nutritionixApi = retrofit.create(NutritionixApi.class);
        }
        return instance;
    }

    public NutritionixApi getNutritionixApi() {
        return nutritionixApi;
    }
}

