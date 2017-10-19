package com.aquarius.datacollector.api;

import com.aquarius.datacollector.api.responses.FileResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by matthewxi on 8/10/17.
 */

public interface ApiDeclaration {

    // Timeseries Upload
    @Multipart
    @POST("uploads")
    Call<FileResponse> uploadFile(@Part MultipartBody.Part file);

}
