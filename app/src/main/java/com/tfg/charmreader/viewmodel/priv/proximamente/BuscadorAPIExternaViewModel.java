package com.tfg.charmreader.viewmodel.priv.proximamente;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.Book;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.BookResponse;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.priv.proximamente.SearchRepository;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscadorAPIExternaViewModel extends ViewModel {
    private final SearchRepository repository = new SearchRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final MutableLiveData<List<Book>> resultados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successSaving = new MutableLiveData<>(false);

    public LiveData<List<Book>> getResultados() { return resultados; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSaving() { return isSaving; }
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<Boolean> getSuccessSaving() { return successSaving; }

    public void buscarLibro(String query) {
        if (query.isEmpty()) {
            resultados.setValue(new ArrayList<>());
            return;
        }

        isLoading.setValue(true);
        repository.buscarEnOpenLibrary(query, new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    resultados.postValue(response.body().getBooks());
                } else {
                    resultados.postValue(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                isLoading.postValue(false);
                resultados.postValue(new ArrayList<>());
            }
        });
    }

    public void añadirADeseos(Context context, Book book) {
        int idU = AuthRepository.getInstance(context.getApplicationContext()).getIdUsuario();

        if (idU == -1) {
            mensaje.setValue("Error de sesión");
            return;
        }

        isSaving.setValue(true);
        BookEn bookEn = new BookEn(book, idU, true);

        bookRepository.anadirBook(bookEn, new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                isSaving.postValue(false);
                if (response.isSuccessful()) {
                    successSaving.postValue(true);
                } else {
                    mensaje.postValue("Error al guardar en el servidor");
                }
            }
            @Override
            public void onFailure(Call<BookEn> call, Throwable t) {
                isSaving.postValue(false);
                mensaje.postValue("Error de red");
            }
        });
    }
}