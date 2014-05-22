package com.jmv.codigociudadano.resistenciarte.fragments;

import com.jmv.codigociudadano.resistenciarte.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jmv.codigociudadano.resistenciarte.HomeActivity;

public class PlaceholderFragment extends Fragment  {

	private int fragmentId;

    public int getFragmentId() {
		return fragmentId;
	}

	public void setFragmentId(int fragmentId) {
		this.fragmentId = fragmentId;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(fragmentId, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(Integer.toString(getArguments().getInt(HomeActivity.ARG_SECTION_NUMBER)));
        return rootView;
    }
}
