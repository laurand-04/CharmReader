package com.tfg.charmreader.viewmodel.priv.fragmentView;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProximamenteFragmentViewModel extends AndroidViewModel {

    private final BookRepository repository = new BookRepository();
    private final MutableLiveData<List<BookEn>> libros = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();

    public ProximamenteFragmentViewModel(@NonNull Application application) { super(application); }

    public LiveData<List<BookEn>> getLibros() { return libros; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensaje() { return mensaje; }

    public void cargarLibros() {
        int idUsuario = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idUsuario <= 0) return;

        isLoading.setValue(true);
        repository.obtenerBooksPorUsuario(idUsuario, new Callback<List<BookEn>>() {
            @Override
            public void onResponse(Call<List<BookEn>> call, Response<List<BookEn>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    libros.postValue(response.body());
                }
            }
            @Override public void onFailure(Call<List<BookEn>> call, Throwable t) {
                isLoading.postValue(false);
            }
        });
    }

    public void eliminarLibro(BookEn book) {
        repository.eliminarBook(book.getId(), new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    mensaje.postValue("Libro eliminado");
                    cargarLibros();
                } else {
                    mensaje.postValue("No se pudo eliminar el libro");
                }
            }
            @Override public void onFailure(Call<Boolean> call, Throwable t) {
                mensaje.postValue("Error de conexión");
            }
        });
    }
}