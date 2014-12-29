package com.kyokomi.wifiproxysetting.ui.fragment;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kyokomi.wifiproxysetting.DemoApplication;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class DemoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        ((DemoApplication) getActivity().getApplication()).component().inject(this);

        return rootView;
    }

}
