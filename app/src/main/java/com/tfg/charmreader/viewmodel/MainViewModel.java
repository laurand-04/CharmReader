package com.tfg.charmreader.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.UserRepository;

public class MainViewModel extends ViewModel {
    private UserRepository userRepository;
    private AuthRepository authRepository;
    private final MutableLiveData<String> userPhotoUrl = new MutableLiveData<>();

    public MainViewModel() {
        this.authRepository = null;
        this.userRepository = null;
    }

    public void setContext(Context context) {
        this.authRepository = AuthRepository.getInstance(context.getApplicationContext());
        this.userRepository = new UserRepository(context);
    }

    public LiveData<String> getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void refreshUserProfile() {
        if (authRepository.getEmail() != null) {
            userRepository.obtenerUsuarioPorEmail(authRepository.getEmail(), new UserRepository.RepositoryCallback<Usuario>() {
                @Override
                public void onComplete(Usuario usuario) {
                    if (usuario != null) userPhotoUrl.postValue(usuario.getFoto());
                }

                @Override
                public void onError(String message) { /* Loguear error */ }
            });
        }
    }
}