package com.java.liyonghui.ui.cluster;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ClusterViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ClusterViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is cluster fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}