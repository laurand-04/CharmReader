package com.tfg.charmreader.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.repository.admin.BackupRepository;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BackupViewModel extends ViewModel {
    private final BackupRepository repository = new BackupRepository();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<ResponseBody> downloadData = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getStatusMessage() { return statusMessage; }
    public LiveData<ResponseBody> getDownloadData() { return downloadData; }

    public void exportarDatos() {
        isLoading.setValue(true);
        repository.descargarBackup(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    downloadData.postValue(response.body());
                } else {
                    statusMessage.postValue("❌ Error al generar copia en servidor");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.postValue(false);
                statusMessage.postValue("Error de conexión");
            }
        });
    }

    public void importarDatos(MultipartBody.Part body) {
        isLoading.setValue(true);
        repository.importarBackup(body, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    statusMessage.postValue("🚀 Restauración completada con éxito");
                } else {
                    statusMessage.postValue("❌ Error en la restauración");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.postValue(false);
                statusMessage.postValue("Error de red");
            }
        });
    }
}