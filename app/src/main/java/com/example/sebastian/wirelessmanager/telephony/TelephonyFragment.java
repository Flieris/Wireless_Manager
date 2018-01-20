/*
 * Copyright (c) Sebastian Lenkiewicz 2018.
 */

package com.example.sebastian.wirelessmanager.telephony;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sebastian.wirelessmanager.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TelephonyFragment extends Fragment {
    Context context;
    TextView cellCardView;
    CardView dataView;
    View thisView;
    MyPhoneStateListener myPhoneStateListener;
    public TelephonyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_telephony, container, false);
        context = super.getContext();
        cellCardView = view.findViewById(R.id.cell_data);
        dataView= view.findViewById(R.id.cell_info);
        dataView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (cellCardView.getVisibility() == View.GONE) {
                    cellCardView.setVisibility(View.VISIBLE);
                } else {
                    cellCardView.setVisibility(View.GONE);
                }
                ObjectAnimator animation = ObjectAnimator.ofInt(cellCardView, "maxLines", cellCardView.getMaxLines());
                animation.setDuration(200).start();
            }
        });
        context = super.getContext();
        thisView = view;
        myPhoneStateListener = new MyPhoneStateListener(context,thisView);
        myPhoneStateListener.start();
        return view;
    }
    @Override
    public void onDestroyView(){
        myPhoneStateListener.off();
        super.onDestroyView();

    }

}
