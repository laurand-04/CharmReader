package com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Valoracion;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiValoracion;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResenasGrupoViewModel extends ViewModel {
    private final I_ApiValoracion api = API.getInstancia().create(I_ApiValoracion.class);

    private final MutableLiveData<List<Valoracion>> valoraciones = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

    public LiveData<List<Valoracion>> getValoraciones() { return valoraciones; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensajeError() { return mensajeError; }

    public void cargarResenas(int idGrupo) {
        isLoading.setValue(true);
        // idLibro = -1 para valoraciones de grupo según tu lógica
        api.verValoraciones(Valoracion.TipoValoracion.GRUPO, idGrupo).enqueue(new Callback<List<Valoracion>>() {
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
                mensajeError.postValue("Error de conexión al cargar reseñas");
            }
        });
    }
}