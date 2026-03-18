package com.tfg.charmreader.viewmodel.priv.fragmentView;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tfg.charmreader.data.model.ObrasModel;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.tusObras.ObrasRepository;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TusObrasFragmentViewModel extends AndroidViewModel {

    private final ObrasRepository obrasRepo = new ObrasRepository();

    // Solo necesitamos una lista, ya que ObrasModel contiene todos los datos necesarios
    private final MutableLiveData<List<ObrasModel>> obras = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public TusObrasFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<ObrasModel>> getObras() { return obras; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void cargarObras() {
        int idU = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idU <= 0) return;

        isLoading.setValue(true);

        // Hacemos una única llamada para obtener las obras del usuario
        obrasRepo.obtenerObrasDeUsuario(idU, new Callback<List<ObrasModel>>() {
            @Override
            public void onResponse(Call<List<ObrasModel>> call, Response<List<ObrasModel>> response) {
                isLoading.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    obras.postValue(response.body());
                } else if (response.code() == 204) { // 204 No Content (La lista está vacía)
                    obras.postValue(new ArrayList<>());
                } else {
                    mensaje.postValue("Error al cargar tus obras");
                    obras.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<ObrasModel>> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error de conexión. Inténtalo de nuevo.");
            }
        });
    }

    public void eliminarObra(int idObra) {
        // Para obras no necesitamos el idU en la ruta del servidor, ya que el id de la obra es único
        obrasRepo.eliminarObra(idObra, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("Obra eliminada correctamente");
                    // Recargamos la lista para que desaparezca visualmente
                    cargarObras();
                } else {
                    mensaje.postValue("No se pudo eliminar la obra");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mensaje.postValue("Error de conexión al eliminar");
            }
        });
    }
}