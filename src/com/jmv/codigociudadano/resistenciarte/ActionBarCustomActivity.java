package com.jmv.codigociudadano.resistenciarte;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class ActionBarCustomActivity extends ActionBarActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_launcher_custom);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

}
