package com.inventrax.rt.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.inventrax.rt.R;
import com.inventrax.rt.util.FragmentUtils;


public class HomeFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    Fragment fragment = null;

    LinearLayout ll_batchpick, ll_livestock, ll_transfer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        loadFormControls();


        return rootView;
    }

    private void loadFormControls() {
        ll_batchpick = (LinearLayout) rootView.findViewById(R.id.ll_batchpick);
        ll_livestock = (LinearLayout) rootView.findViewById(R.id.ll_livestock);
        ll_transfer = (LinearLayout) rootView.findViewById(R.id.ll_transfer);


        ll_batchpick.setOnClickListener(this);
        ll_livestock.setOnClickListener(this);
        ll_transfer.setOnClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ll_batchpick:
                BatchPick batchPick = new BatchPick();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, batchPick);
                break;

            case R.id.ll_livestock:
                LiveStockFragment liveStockFragment_ = new LiveStockFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, liveStockFragment_);
                break;
            case R.id.ll_transfer:
                InternalTransferFragment transferFragment = new InternalTransferFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, transferFragment);
                break;
        }

    }
}