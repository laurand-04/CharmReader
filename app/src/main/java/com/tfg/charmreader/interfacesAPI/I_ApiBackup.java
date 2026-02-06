package com.tfg.charmreader.interfacesAPI;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

public interface I_ApiBackup {
    @Streaming
    @GET("admin/backup/export")
    Call<ResponseBody> descargarBackup();
    @Multipart
    @POST("admin/backup/import")
    Call<ResponseBody> importarBackup(@Part MultipartBody.Part file);
}
