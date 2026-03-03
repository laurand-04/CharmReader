package com.tfg.charmreader.viewmodel.admin;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;

public class AdminMainViewModel extends ViewModel {
    private AuthRepository authRepository;
    private final MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>(false);

    public void setContext(Context context) {
        if (authRepository == null) {
            this.authRepository = AuthRepository.getInstance(context.getApplicationContext());
        }
    }

    public LiveData<Boolean> getNavigateToLogin() {
        return navigateToLogin;
    }

    public void logout() {
        authRepository.logout(); // Necesitaremos añadir este métod0 al Repo
        navigateToLogin.setValue(true);
    }
}