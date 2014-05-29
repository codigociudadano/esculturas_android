package com.jmv.codigociudadano.resistenciarte;

import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.polites.android.GestureImageView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

public class StandardImageProgrammatic extends ActionBarCustomActivity {

	private static Bitmap bmap;

	protected GestureImageView view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.empty);
		

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_launcher_custom);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(getIntent().getStringExtra(Constants.TITLE_ACTIVITY));
		
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		view = new GestureImageView(this);

		view.setImageBitmap(bmap);
		view.setLayoutParams(params);

		ViewGroup layout = (ViewGroup) findViewById(R.id.layout);

		layout.addView(view);
		
		((ViewGroup) findViewById(R.id.layout2)).addView(View.inflate(this,
				R.layout.fragment_botones, null));
	}

	public static void showHome(Context context, Bitmap imageToSHow, String name) {
		Intent intent = new Intent(context, StandardImageProgrammatic.class);
		bmap = imageToSHow;
		intent.putExtra(Constants.TITLE_ACTIVITY, name);
		context.startActivity(intent);
	}

}
