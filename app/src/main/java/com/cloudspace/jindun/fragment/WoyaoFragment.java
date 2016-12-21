package com.cloudspace.jindun.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cloudspace.jindun.R;
import com.cloudspace.jindun.activity.HiddendangerActivity;
import com.cloudspace.jindun.activity.IntelligenceActivity;

/**
 * Created by zengxianhua on 16/12/19.
 */

public class WoyaoFragment extends BaseFragment{
    private LinearLayout mLinearLayout01,mLinearLayout02;
    private Button button;
    private static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_woyao, container, false);
        initView(rootView);
        setListener();
        return rootView;
    }



    private void initView(View rootView) {
        mLinearLayout01= (LinearLayout) rootView.findViewById(R.id.fragmentWY_reLayout_image01);
        mLinearLayout02= (LinearLayout) rootView.findViewById(R.id.fragmentWY_reLayout_image02);
        button= (Button) rootView.findViewById(R.id.fragmentWY_bt);
    }
    private void setListener() {
     mLinearLayout01.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent = new Intent();
             intent.setClass(getContext(), IntelligenceActivity.class);
             startActivity(intent);

         }
     });
     mLinearLayout02.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent = new Intent();
             intent.setClass(getContext(), HiddendangerActivity.class);
             startActivity(intent);
         }
     });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "333", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onFragmentCreated(Bundle savedInstanceState) {

    }

    @Override
    public void updateContent() {

    }

    public static WoyaoFragment newInstance(int sectionNumber){
        WoyaoFragment fragment = new WoyaoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
}
