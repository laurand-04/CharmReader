package com.tfg.charmreader.viewmodel.publ.misGrupos;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.repository.UserRepository;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicViewModel extends ViewModel {
    private final MutableLiveData<Usuario> usuarioLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<Usuario> getUsuario() { return usuarioLiveData; }
    public LiveData<String> getError() { return error; }

    public void cargarDatosUsuario(Context context) {
        // Si ya tenemos el dato en el LiveData, no lo pedimos de nuevo (opcional)
        if (usuarioLiveData.getValue() != null) return;

        AuthRepository.getInstance(context).obtenerUsuario(new AuthRepository.RepositoryCallback<Usuario>() {
            @Override
            public void onComplete(Usuario usu) {
                // Cuando la API responde, "empujamos" el dato al LiveData
                usuarioLiveData.postValue(usu);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}