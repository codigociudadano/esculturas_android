package com.jmv.codigociudadano.resistenciarte;

import com.polites.android.GestureImageView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

	public static void showHome(Context context, Bitmap imageToSHow) {
		Intent intent = new Intent(context, StandardImageProgrammatic.class);
		bmap = imageToSHow;
		context.startActivity(intent);
	}

}
