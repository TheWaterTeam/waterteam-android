package com.aquarius.datacollector.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.aquarius.datacollector.api.responses.FileResponse;
import com.aquarius.datacollector.database.DataLogger;
import com.aquarius.datacollector.database.Project;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by matthewxi on 8/10/17.
 */

public class Api {

    private static final String TAG = com.aquarius.datacollector.api.Api.class.getSimpleName();

    private static Api instance;
    private ApiDeclaration service = null;
    private String url;
    private String authToken;

    private Api() {
    }

    public static Api getInstance() {

        if (instance == null) {
            instance = new Api();
            instance.initialize("http://167.99.110.39:3000/");
            }
        return instance;
    }

    public void initialize(String url) {
        this.url = url;
        this.authToken = null;
        reinitialize();
    }

    private void reinitialize() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging);

        if (authToken != null) {
            AuthenticationInterceptor authentication =
                    new AuthenticationInterceptor(authToken);
            clientBuilder.addInterceptor(authentication);
        }

        OkHttpClient client = clientBuilder.build();

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = restAdapter.create(ApiDeclaration.class);
    }


    // Common Methods for Synchronous Calls
    public Object handleResponse(Context context, Response response) throws ErrorMessageException {
        if (response.isSuccessful()) {
            return response.body();
        } else {
            switch (response.code()) {
                case 401:
   //                 SignInManager.logout(context);
                    break;
                default:
                    Log.v(TAG, response.toString());
                    throw new ErrorMessageException("Error: " + Integer.toString(response.code()));
            }
            return null;
        }
    }

    // Attachments - synchronous
    public FileResponse uploadTimeseries(Context context, File file) throws IOException, ErrorMessageException {
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part filePart =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<FileResponse> call = service.uploadFile(filePart);
        Response<FileResponse> response = call.execute();

        return (FileResponse) handleResponse(context, response);
    }


    // Asynchronous
    public void getDataloggers(Context context, int projectId,  Callback<List<DataLogger>> callback) throws IOException, ErrorMessageException {
        Call<List<DataLogger>> call = service.getDataloggers(projectId);
        call.enqueue(callback);
    }

    public void getProjects(Context context, Callback<List<Project>> callback) throws IOException, ErrorMessageException {
        Call<List<Project>> call = service.getProjects();
        call.enqueue(callback);
    }


}
