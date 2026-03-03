package com.tfg.charmreader.viewmodel.priv.proximamente;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProximoLibroViewModel extends ViewModel {
    private final BookRepository repository = new BookRepository();
    private final MutableLiveData<BookEn> libroLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successAction = new MutableLiveData<>(false);

    public LiveData<BookEn> getLibro() {
        return libroLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

    public LiveData<String> getMensaje() {
        return mensaje;
    }

    public LiveData<Boolean> getSuccessAction() {
        return successAction;
    }

    public void cargarLibro(int idLibro) {
        isLoading.setValue(true);
        repository.obtenerBookPorId(idLibro, new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) libroLiveData.postValue(response.body());
                else mensaje.postValue("No se pudo cargar el libro");
            }

            @Override
            public void onFailure(Call<BookEn> call, Throwable t) {
                isLoading.postValue(false);
                mensaje.postValue("Error de red");
            }
        });
    }

    public void actualizarLibro(BookEn libro, String subtitulo, String resumen, String temaStr) {
        isSaving.setValue(true);
        libro.setSubtitulo(subtitulo);
        libro.setResumen(resumen);
        libro.setTema(libro.mapearTema(temaStr));

        repository.anadirBook(libro, new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                isSaving.postValue(false);
                if (response.isSuccessful()) successAction.postValue(true);
                else mensaje.postValue("Error al guardar cambios");
            }

            @Override
            public void onFailure(Call<BookEn> call, Throwable t) {
                isSaving.postValue(false);
                mensaje.postValue("Fallo de conexión");
            }
        });
    }
}