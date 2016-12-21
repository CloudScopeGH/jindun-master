package com.cloudspace.jindun.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudspace.jindun.R;

/**
 * Created by zengxianhua on 16/12/19.
 */

public class DianhuaFragment extends BaseFragment{
    private static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dianhua, container, false);

        return rootView;
    }

    @Override
    public void onFragmentCreated(Bundle savedInstanceState) {

    }

    @Override
    public void updateContent() {

    }

    public static DianhuaFragment newInstance(int sectionNumber){
        DianhuaFragment fragment = new DianhuaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
}
