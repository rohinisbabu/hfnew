package com.healthyfish.healthyfish.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.healthyfish.healthyfish.R;
import com.healthyfish.healthyfish.manager.PrefManager;


/**
 * Created by RohiniAjith on 6/6/2016.
 */
public class YourAccount  extends Fragment {
    String toolbar_title;
    String name,email,mobile,location;
    TextView tv_name,tv_email,tv_mobile,tv_location;
    PrefManager prefManager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.youraccount, container, false);
        toolbar_title=getArguments().getString("category");
        getActivity().setTitle(toolbar_title);
        tv_name=(TextView)rootView.findViewById(R.id.ya_name);
        tv_email=(TextView)rootView.findViewById(R.id.ya_email);
        tv_mobile=(TextView)rootView.findViewById(R.id.ya_mobile);
        tv_location=(TextView)rootView.findViewById(R.id.ya_location);
        prefManager= new PrefManager(getActivity());
        name=prefManager.receivedFullName();
        email=prefManager.receivedEmail();
        mobile=prefManager.receivedMobile();
        location=prefManager.receivedLocation();
        tv_name.setText(name);
        tv_email.setText(email);
        tv_mobile.setText(mobile);
        tv_location.setText(location);
        return rootView;
    }
}
