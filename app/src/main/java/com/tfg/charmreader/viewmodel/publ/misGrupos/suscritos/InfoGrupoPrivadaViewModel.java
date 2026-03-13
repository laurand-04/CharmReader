package com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.Sesion;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;
import com.tfg.charmreader.data.repository.publ.SesionRepository;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoGrupoPrivadaViewModel extends ViewModel {
    private final InfoGrupoRepository grupoRepo = new InfoGrupoRepository();
    private final SesionRepository sesionRepo = new SesionRepository();
    private final BookRepository bookRepository = new BookRepository();


    private final MutableLiveData<BookEn> libroActual = new MutableLiveData<>();
    private final MutableLiveData<Sesion> proximaSesion = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<BookEn> getLibroActual() { return libroActual; }
    public LiveData<Sesion> getProximaSesion() { return proximaSesion; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void cargarDatos(int idGrupo) {
        isLoading.setValue(true);

        // 1. Cargar Lectura Actual
        grupoRepo.obtenerLibroActual(idGrupo, new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                if (response.isSuccessful()){
                    Log.e("Leer vm", "Libro: " + response.body().getUrlLibro() + " --- " + response.body().getTitulo());
                    libroActual.postValue(response.body());
                }
            }
            @Override public void onFailure(Call<BookEn> call, Throwable t) {}
        });

        // 2. Cargar Próxima Sesión
        sesionRepo.obtenerProximaSesion(idGrupo, new Callback<Sesion>() {
            @Override
            public void onResponse(Call<Sesion> call, Response<Sesion> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) proximaSesion.postValue(response.body());
            }
            @Override public void onFailure(Call<Sesion> call, Throwable t) { isLoading.postValue(false); }
        });
    }

    /*public BookEn actualizarLibroActual(int idBook) {
        BookEn book
        bookRepository.obtenerBookPorId(idBook, new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {

            }

            @Override
            public void onFailure(Call<BookEn> call, Throwable t) {

            }
        });
    }*/
}