package com.jmv.codigociudadano.resistenciarte.fragments.sections;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Function;
import com.jmv.codigociudadano.resistenciarte.HomeActivity;
import com.jmv.codigociudadano.resistenciarte.R;
import com.jmv.codigociudadano.resistenciarte.fragments.PlaceholderFragment;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Escultura;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Foto;
import com.jmv.codigociudadano.resistenciarte.net.IRequester;
import com.jmv.codigociudadano.resistenciarte.net.RestClientResistenciarte;
import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.jmv.codigociudadano.resistenciarte.utils.Generador;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;

public class HighlightsSectionFragment extends PlaceholderFragment {

	private ArrayList<Foto> fotos;
	private Escultura localReg;
	private ImageView img;
	private TextView textView;
	private View mDeatailedView;
	

	public HighlightsSectionFragment() {
		super();
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
		codeName = getClass().getName();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		if (img == null){
			img = (ImageView) rootView.findViewById(R.id.img);
		}
		textView = (TextView) rootView.findViewById(R.id.title);
		mDeatailedView = rootView.findViewById(R.id.login_form_detailed);
		return rootView;
	}

	@Override
	public String getRequestURI() {
		Integer numero = Generador.generarNumeroAleatorio(1, 52);
		return Constants.BASE_URL+"/api/v1/node?page=" + numero;
	}

	@Override
	public void onResponse(String response) {
		getLocker().adquireLock(codeName);
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(response);
			int index = Generador.generarNumeroAleatorio(0,
					jsonArray.length() - 1);
			JSONObject jsonObject = jsonArray.optJSONObject(index);

			localReg = new Escultura();
			
			Utils.extractFromResponseToObject(localReg, jsonObject);

			getPhotos(localReg);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getPhotos(final Escultura localReg) {
		RestClientResistenciarte internalCall = new RestClientResistenciarte(photosRquester);
		internalCall.makeJsonRestRequest();
	}
	
	private IRequester photosRquester = new IRequester() {
		
		@Override
		public void onResponse(String response) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONArray fotosArrays = jsonObject.getJSONObject("field_fotos").getJSONArray("und");
				
				fotos = new ArrayList<Foto>();
				for (int i = 0; i < fotosArrays.length(); i++) {
					Foto foto = new Foto();
					JSONObject jsonObjectFoto = fotosArrays.getJSONObject(i);
					Utils.extractFromResponseToObject(foto, jsonObjectFoto);
					fotos.add(foto);
				}
				
				  // Image url
		        String image_url = Constants.BASE_URL+"/sites/default/files/"+fotos.get(0).getUri().replaceFirst("public://", "");;
		        
		        Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
					@Override
					public Void apply(Bitmap bmap) {
				        textView.setText(localReg.getTitle());
						HomeActivity.getInstance().getLocker().releaseLock(codeName);
						
						DisplayMetrics metrics = new DisplayMetrics();
						HomeActivity.getInstance().getWindowManager().getDefaultDisplay().getMetrics(metrics);
						int height = metrics.heightPixels;
						int width = metrics.widthPixels;
						 
						float bmapWidth = bmap.getWidth();
						float bmapHeight = bmap.getHeight();
						 
						float wRatio = width / bmapWidth;
						float hRatio = height / bmapHeight;
						 
						float ratioMultiplier = wRatio;
						// Untested conditional though I expect this might work for landscape mode
						if (hRatio < wRatio) {
							ratioMultiplier = hRatio;
						}
						 
						int newBmapWidth = (int) (bmapWidth*ratioMultiplier);
						int newBmapHeight = (int) (bmapHeight*ratioMultiplier);
						 
						img.setLayoutParams(new LinearLayout.LayoutParams(newBmapWidth, newBmapHeight));
						showProgress(false);
						mDeatailedView.setVisibility(View.VISIBLE);
						return null;
					}
				};
				
		        getImageLoaderService().DisplayImage(image_url, img, afterLogin);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getRequestURI() {
			return localReg.getUri();
		}

		@Override
		public void onResponse(InputStream result) {
		}
	};

	@Override
	public void onResponse(InputStream result) {		
	}
	

}
