package com.example.meubrecho.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.meubrecho.MainActivity;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Venda seus produtos e veja seu rendimento por aqui");
    }

    public LiveData<String> getText() {
        return mText;
    }
}