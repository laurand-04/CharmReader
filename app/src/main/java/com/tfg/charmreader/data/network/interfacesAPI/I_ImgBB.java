package com.tfg.charmreader.data.network.interfacesAPI;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface I_ImgBB {
    @Multipart
    @POST("1/upload")
    Call<ResponseBody> uploadImage(
            @Query("key") String apiKey,
            @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("1/upload")
    Call<ResponseBody> uploadImagee(
            @Query("key") String apiKey,
            @Field("image") String base64Image
    );
}
