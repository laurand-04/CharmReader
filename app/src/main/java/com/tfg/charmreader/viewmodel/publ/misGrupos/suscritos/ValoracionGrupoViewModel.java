package com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.Valoracion;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.publ.ValoracionRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValoracionGrupoViewModel extends AndroidViewModel {
    private final ValoracionRepository valoracionRepository = new ValoracionRepository();


    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public ValoracionGrupoViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void publicarValoracion(int idGrupo, int idLibro, float estrellas, String comentario) {
        if (estrellas == 0) {
            mensaje.setValue("Por favor, selecciona una puntuación");
            return;
        }

        int idUsuario = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idUsuario == -1) {
            mensaje.setValue("Error: Sesión no válida");
            return;
        }

        isLoading.setValue(true);

        Valoracion v = new Valoracion();
        v.setIdUsuario(idUsuario);
        v.setIdGrupo(idGrupo);
        v.setCalificacion((int) estrellas);
        v.setDescripcion(comentario.trim());

        // Lógica de tipo de referencia
        if (idLibro != -1) {
            v.setIdReferencia(idLibro);
            v.setTipo(Valoracion.TipoValoracion.LIBRO);
        } else {
            v.setIdReferencia(idGrupo);
            v.setTipo(Valoracion.TipoValoracion.GRUPO);
        }

        valoracionRepository.crear(v, new Callback<Valoracion>() {
            @Override
            public void onResponse(Call<Valoracion> call, Response<Valoracion> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    isSuccess.postValue(true);
                } else {
                    mensaje.postValue("Error al publicar la reseña");
                }
            }

            @Override
            public void onFailure(Call<Valoracion> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error de red: comprueba tu conexión");
            }
        });
    }
}