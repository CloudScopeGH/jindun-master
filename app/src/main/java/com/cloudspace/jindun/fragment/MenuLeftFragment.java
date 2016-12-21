package com.cloudspace.jindun.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudspace.jindun.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuLeftFragment extends Fragment {


    public MenuLeftFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_left, container, false);

        return view;
    }

}
