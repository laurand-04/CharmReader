package com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Valoracion;
import com.tfg.charmreader.data.repository.publ.ValoracionRepository;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValoracionesLibroViewModel extends ViewModel {
    private final ValoracionRepository valoracionRepository = new ValoracionRepository();
    private final MutableLiveData<List<Valoracion>> valoraciones = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

    public LiveData<List<Valoracion>> getValoraciones() { return valoraciones; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensajeError() { return mensajeError; }

    public void cargarValoraciones(int idLibro) {
        isLoading.setValue(true);
        valoracionRepository.verValoraciones(Valoracion.TipoValoracion.LIBRO, idLibro, new Callback<List<Valoracion>>() {
            @Override
            public void onResponse(Call<List<Valoracion>> call, Response<List<Valoracion>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    valoraciones.postValue(response.body());
                } else {
                    valoraciones.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Valoracion>> call, Throwable t) {
                isLoading.postValue(false);
                mensajeError.postValue("Error al conectar con el servidor");
            }
        });
    }
}