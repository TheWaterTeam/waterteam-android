package com.aquarius.datacollector.api;

import com.aquarius.datacollector.api.responses.FileResponse;
import com.aquarius.datacollector.database.DataLogger;
import com.aquarius.datacollector.database.Project;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by matthewxi on 8/10/17.
 */

public interface ApiDeclaration {

    // Timeseries Upload
    @Multipart
    @POST("uploads")
    Call<FileResponse> uploadFile(@Part MultipartBody.Part file);

    @Headers("Content-Type: application/json")
    @GET("deployments/{projectId}/devices.json")
    Call<List<DataLogger>> getDataloggers(@Path("projectId") int projectId);

    @Headers("Content-Type: application/json")
    @GET("deploymemts.json")
    Call<List<Project>> getProjects();

}
