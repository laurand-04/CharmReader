package com.tfg.charmreader.data.repository.admin;

import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiBackup;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;

public class BackupRepository {
    private final I_ApiBackup api;

    public BackupRepository() {
        this.api = API.getInstancia().create(I_ApiBackup.class);
    }

    public void descargarBackup(Callback<ResponseBody> callback) {
        api.descargarBackup().enqueue(callback);
    }

    public void importarBackup(MultipartBody.Part filePart, Callback<ResponseBody> callback) {
        api.importarBackup(filePart).enqueue(callback);
    }
}