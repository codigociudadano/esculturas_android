package com.jmv.codigociudadano.resistenciarte.fragments.sections;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.common.base.Function;
import com.jmv.codigociudadano.resistenciarte.HomeActivity;
import com.jmv.codigociudadano.resistenciarte.R;
import com.jmv.codigociudadano.resistenciarte.comps.TextViewEx;
import com.jmv.codigociudadano.resistenciarte.fragments.PlaceholderFragment;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Escultura;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Foto;
import com.jmv.codigociudadano.resistenciarte.net.IRequester;
import com.jmv.codigociudadano.resistenciarte.net.RestClientResistenciarte;
import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;

public class NovedadesSectionFragment extends PlaceholderFragment {

	ProgressDialog pDialog;
	private LinearLayout myLinearLayout;
	private int currentPage = 0;
	private Button button2;
	private Button button;
	private LinearLayout mOpps;


	public NovedadesSectionFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setFragmentId(R.layout.fragment_home3);

		super.onCreateView(inflater, container, savedInstanceState);
	
		showProgress(true);
		
		myLinearLayout = (LinearLayout) rootView.findViewById(R.id.text_view_place);
		
		mOpps = (LinearLayout) rootView.findViewById(R.id.no_novedades);
		mOpps.setVisibility(View.GONE);
		
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
		
		
		button = (Button) rootView.findViewById(R.id.btn_load);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentPage++;
				new loadMoreListView().execute();
			}
		});
		
		button2 = (Button) rootView.findViewById(R.id.btn_load_before);
		button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentPage--;
				if (currentPage < 0){
					currentPage = 0;
				}
				new loadMoreListView().execute();
			}
		});
		button2.setVisibility(View.GONE);

		return rootView;
	}

	@Override
	public String getRequestURI() {
		//http://dev.resistenciarte.org/api/v1/node?page=0&parameters[type]=eventos
		return Constants.BASE_URL + "/api/v1/node?page="+currentPage+"&pagesize=5&parameters[type]=eventos";
	}

	@Override
	public void onResponse(String response) {
		JSONArray jsonArray;
		
		ArrayList<Escultura> esculturas = new ArrayList<Escultura>();
		try {
			jsonArray = new JSONArray(response);
			int max = jsonArray.length();
			
			if(max == 0){
				//no more results
				mOpps.setVisibility(View.VISIBLE);
				button.setVisibility(View.GONE);
				showProgress(false);
				return;
			}
			
			for (int i = 0; i < max; i++) {
				JSONObject jsonObject = jsonArray.optJSONObject(i);

				Escultura localReg = new Escultura();

				EsculturaItem item = new EsculturaItem();
				Utils.extractFromResponseToObject(localReg, jsonObject);

				item.setEscultura(localReg);
				
				esculturas.add(localReg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		myLinearLayout.removeAllViewsInLayout();


		int number = 1;
		for (Escultura item : esculturas) {

			Escultura distancias2 = item;
			
			View v = View.inflate(HomeActivity.getInstance(),
					R.layout.fragment_novedades, null);

			addIMage(v, distancias2);
			
			final TextViewEx texto = (TextViewEx) v.findViewById(R.id.tittle);
			texto.setText(distancias2.getTitle(), true);

			final Button btn = (Button) v.findViewById(R.id.novedad);
			btn.setText(Constants.NOVEDAD+" "+(currentPage + 1)+" - "+number);
			number++;
			myLinearLayout.addView(v);

		}

		if (esculturas.isEmpty() && currentPage == 0){
			RestClientResistenciarte client = new RestClientResistenciarte(this);
			client.makeJsonRestRequest();
		} else {
			showProgress(false);
		}
	}
	
	private void addIMage(final View view, final Escultura distancias2) {
		IRequester r = new IRequester() {

			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONArray fotosArrays = jsonObject.getJSONObject(
							"field_fotos").getJSONArray("und");

					
					ArrayList<Foto> fotos = new ArrayList<Foto>();
					for (int i = 0; i < fotosArrays.length(); i++) {
						Foto foto = new Foto();
						JSONObject jsonObjectFoto = fotosArrays
								.getJSONObject(i);
						Utils.extractFromResponseToObject(foto, jsonObjectFoto);
						fotos.add(foto);
					}

					// Image url
					String image_url = Constants.BASE_URL
							+ "/sites/default/files/"
							+ fotos.get(0).getUri()
									.replaceFirst("public://", "");
					;

					Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
						@Override
						public Void apply(Bitmap bmap) {
							View p = view.findViewById(R.id.progress);
							p.setVisibility(View.GONE);
							View iV = view.findViewById(R.id.default_image);
							iV.setVisibility(View.GONE);
							return null;
						}
					};

					HomeActivity
							.getInstance()
							.getImageLoaderService()
							.DisplayImage(image_url,
									(ImageView) view.findViewById(R.id.image),
									afterLogin);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public String getRequestURI() {
				return Constants.BASE_URL + "/api/v1/node/"
						+ distancias2.getNid();
			}

			@Override
			public void onResponse(InputStream result) {
			}
		};

		RestClientResistenciarte internalCall = new RestClientResistenciarte(r);
		internalCall.makeJsonRestRequest();
	}
	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

	/**
	 * Async Task that send a request to url Gets new list view data Appends to
	 * list view
	 * */
	private class loadMoreListView extends AsyncTask<Void, Void,  ArrayList<EsculturaItem> > {

		@Override
		protected void onPreExecute() {
			// Showing progress dialog before sending http request
			pDialog = new ProgressDialog(HomeActivity.getInstance());
			pDialog.setMessage("Cargando Datos del Servidor..");
			pDialog.setIndeterminate(true);
			pDialog.setCancelable(false);
			pDialog.show();
			if (currentPage == 0){
				button2.setVisibility(View.GONE);
			} else {
				button2.setVisibility(View.VISIBLE);
			}
			
		}

		protected ArrayList<EsculturaItem> doInBackground(Void... unused) {
			try {
				IRequester r = new IRequester() {
					
					@Override
					public void onResponse(InputStream result) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public String getRequestURI() {
						return Constants.BASE_URL + "/api/v1/node?page="+currentPage+"&pagesize=5&parameters[type]=eventos";
					}

					@Override
					public void onResponse(String response) {
						JSONArray jsonArray;
						
						ArrayList<Escultura> esculturas = new ArrayList<Escultura>();
						try {
							jsonArray = new JSONArray(response);
							int max = jsonArray.length();
											
							if(max == 0){
								//no more results
								currentPage--;
								button.setVisibility(View.GONE);
								return;
							}
							
							myLinearLayout.removeAllViewsInLayout();
							for (int i = 0; i < max; i++) {
								JSONObject jsonObject = jsonArray.optJSONObject(i);

								Escultura localReg = new Escultura();

								EsculturaItem item = new EsculturaItem();
								Utils.extractFromResponseToObject(localReg, jsonObject);

								item.setEscultura(localReg);
								
								esculturas.add(localReg);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						int number = 1;
						for (Escultura item : esculturas) {

							Escultura distancias2 = item;
							
							View v = View.inflate(HomeActivity.getInstance(),
									R.layout.fragment_escultura, null);

							final TextViewEx texto = (TextViewEx) v.findViewById(R.id.tittle);
							texto.setText(distancias2.getTitle(), true);
							
							final Button btn = (Button) v.findViewById(R.id.novedad);
							
							btn.setText(Constants.NOVEDAD+" "+(currentPage + 1)+" - "+number);

							number++;
							myLinearLayout.addView(v);

						}
						
						
						final ScrollView scroll = (ScrollView) mLoginFormView;
						scroll.postDelayed(new Runnable() {
						    @Override
						    public void run() {
						    	try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
								}
						    	scroll.smoothScrollTo(0, 0);
						        scroll.fullScroll(ScrollView.FOCUS_UP);
						        scroll.postDelayed(new Runnable() {
								    @Override
								    public void run() {
								        pDialog.dismiss();
								    }
								}, 1000);
						    }
						}, 600);
						
					}

				};
				RestClientResistenciarte client = new RestClientResistenciarte(r);
				client.makeJsonRestRequest();

			} catch (Exception e) {
				return null;
			}
			return new ArrayList<EsculturaItem>();
		}

		protected void onPostExecute( ArrayList<EsculturaItem>  unused) {
			pDialog.dismiss();
		}
	}
}
