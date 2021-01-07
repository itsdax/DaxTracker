package scripts.dax.tracker;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import okhttp3.*;
import scripts.dax.tracker.data.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.tribot.api.General.getTRiBotUserID;

class DaxTrackerClient {

    private static final String BASE_URL = "https://tracker.dax.cloud/api/";

    private final OkHttpClient okHttpClient;
    private final Gson gson;
    private final String scriptId;
    private final String scriptSecret;

    DaxTrackerClient(String scriptId, String scriptSecret) {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .callTimeout(8, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(0, 10L, TimeUnit.MINUTES))
                .protocols(List.of(Protocol.HTTP_1_1))
                .build();
        this.gson = new Gson();
        this.scriptId = scriptId;
        this.scriptSecret = scriptSecret;
    }

    public UserCredentials createUser(String name) throws IOException {
        Request request = defaultRequestBuilder()
                .url(BASE_URL + "users")
                .post(RequestBody.create(gson.toJson(new UserCreationRequest(name)), MediaType.parse("application/json")))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() != 200)
                throw new IllegalStateException("Create user failed: " + response.code());

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null)
                    throw new IllegalStateException("Server returned empty response on success");
                return gson.fromJson(responseBody.string(), UserCredentials.class);
            }
        }
    }

    public JwtContainer login(String userId, String secret) throws IOException {
        Request request = defaultRequestBuilder()
                .url(BASE_URL + "login")
                .post(RequestBody.create(gson.toJson(new LoginRequest(userId, secret, null)), MediaType.parse("application/json")))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() != 200)
                throw new IllegalStateException("Login tracker failed: " + response.code());

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null)
                    throw new IllegalStateException("Server returned empty response on success");
                return gson.fromJson(responseBody.string(), JwtContainer.class);
            }
        }
    }

    public void trackData(JwtContainer jwtContainer, String userId, Map<String, Long> resources) throws IOException {
        Request request = defaultRequestBuilder()
                .url(String.format("%susers/%s/activities", BASE_URL, userId))
                .addHeader("Authorization", "Bearer " + jwtContainer.getToken())
                .addHeader("ScriptID", scriptId)
                .addHeader("Secret", scriptSecret)
                .post(RequestBody.create(gson.toJson(new ActivityUpdate(scriptId, resources)), MediaType.parse("application/json")))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() != 200)
                throw new IllegalStateException("Track data failed: " + response.code());
        }
    }

    public List<Activity> getData(String userId, JwtContainer jwtContainer) throws IOException {
        Request request = defaultRequestBuilder()
                .url(String.format("%susers/%s/activities?period=%s", BASE_URL, userId, "ONE_WEEK"))
                .addHeader("Authorization", "Bearer " + jwtContainer.getToken())
                .addHeader("ScriptID", scriptId)
                .addHeader("Secret", scriptSecret)
                .get()
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() != 200)
                throw new IllegalStateException("Get data failed: " + response.code());

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null)
                    throw new IllegalStateException("Illegal response returned from server.");

                return gson.fromJson(responseBody.string(), new TypeToken<List<Activity>>() {
                }.getType());
            }
        }
    }

    private static Request.Builder defaultRequestBuilder() {
        return new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent-" + getTRiBotUserID(), "Tribot");
    }

}
