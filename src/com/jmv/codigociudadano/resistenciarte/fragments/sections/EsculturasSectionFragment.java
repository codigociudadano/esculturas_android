package com.jmv.codigociudadano.resistenciarte.fragments.sections;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.gms.internal.au;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Function;
import com.jmv.codigociudadano.resistenciarte.HomeActivity;
import com.jmv.codigociudadano.resistenciarte.NearbyLocations;
import com.jmv.codigociudadano.resistenciarte.R;
import com.jmv.codigociudadano.resistenciarte.comps.lists.ListViewAdapter;
import com.jmv.codigociudadano.resistenciarte.fragments.PlaceholderFragment;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Autor;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Escultura;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Foto;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.GeoEscultura;
import com.jmv.codigociudadano.resistenciarte.net.IRequester;
import com.jmv.codigociudadano.resistenciarte.net.RestClientResistenciarte;
import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.jmv.codigociudadano.resistenciarte.utils.Generador;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;
import com.jmv.codigociudadano.resistenciarte.utils.XMLParser;

public class EsculturasSectionFragment extends PlaceholderFragment {

	ProgressDialog pDialog;
	private LinearLayout myLinearLayout;
	private int currentPage = 0;
	private Button button2;
	private ArrayList<EsculturaItem> listaEsculturas = new ArrayList<EsculturaItem>();
	private Button button;


	public EsculturasSectionFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setFragmentId(R.layout.fragment_home2);

		super.onCreateView(inflater, container, savedInstanceState);
	
		myLinearLayout = (LinearLayout) rootView.findViewById(R.id.text_view_place);
		
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
				button2.setVisibility(View.VISIBLE);
				new loadMoreListView().execute();
			}
		});
		button2.setVisibility(View.GONE);

		return rootView;
	}

	@Override
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/node?page="+currentPage+"&pagesize=5&parameters[type]=escultura";
	}

	@Override
	public void onResponse(String response) {
		JSONArray jsonArray;
		
		ArrayList<Escultura> esculturas = new ArrayList<Escultura>();
		try {
			jsonArray = new JSONArray(response);
			int max = jsonArray.length();
			
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


		for (Escultura item : esculturas) {

			Escultura distancias2 = item;
			
			View v = View.inflate(HomeActivity.getInstance(),
					R.layout.fragment_escultura, null);

			addIMage(v, item);

			final TextView texto = (TextView) v.findViewById(R.id.tittle);
			texto.setText(distancias2.getTitle());

			myLinearLayout.addView(v);

		}

		if (esculturas.isEmpty()){
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

					addAutorName(jsonObject,
							(Button) view.findViewById(R.id.author));

					final Button textoUbic = (Button) view.findViewById(R.id.ubicacion);
					setUbicacion(jsonObject, textoUbic);
					
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

			private void setUbicacion(JSONObject jsonObject, Button ubic_main) {
				/*
				 * "":{"und":[{"lat":"-27.4492586","lon":"-58.9922044",
				 * "map_width":null,"map_height":null,"zoom":"17","name":""}]}
				 * 
				 * field_ubicacion":{"und":[{"value":"Perón, Juan Domingo Nº 454\t\t\t\t",
				 * "safe_value":"Perón, Juan Domingo Nº 454\t\t\t\t","format":null}]}
				 */
				JSONArray aYs;
				try {
					if (jsonObject.isNull("field_mapa")) {
						ubic_main.setVisibility(View.GONE);
						return;
					}
					JSONObject auhtorObject;
					auhtorObject = jsonObject.getJSONObject("field_mapa");

					aYs = auhtorObject.isNull("und") ? null : auhtorObject
							.getJSONArray("und");

					if (aYs == null) {
						ubic_main.setVisibility(View.GONE);
					} else {
						final double lat = Double.valueOf(aYs.getJSONObject(0).getInt("lat"));
						final double lon = Double.valueOf(aYs.getJSONObject(0).getInt("lon"));
						ubic_main.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								
								Intent intent = new Intent(
										android.content.Intent.ACTION_VIEW,
										Uri.parse("http://maps.google.com/maps?&daddr="
												+ lat + "," + lon));
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addCategory(Intent.CATEGORY_LAUNCHER);
								intent.setClassName("com.google.android.apps.maps",
										"com.google.android.maps.MapsActivity");
								startActivity(intent);
							}
						});
						
						if (jsonObject.isNull("field_ubicacion")) {
							ubic_main.setText("Ver en el Mapa");
							return;
						}
						JSONObject ubiObject;
						ubiObject = jsonObject.getJSONObject("field_ubicacion");

						JSONArray aYs2 = ubiObject.isNull("und") ? null : ubiObject
								.getJSONArray("und");
						if (aYs2 == null) {
							ubic_main.setText("Ver en el Mapa");
							return;
						} else {
							final String value = String.valueOf(aYs2.getJSONObject(0).getString("value")).trim();
							ubic_main.setText(value);
						}
						
					}
				} catch (JSONException e) {
					// the author is null
					ubic_main.setVisibility(View.GONE);
					return;
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
	
	private void addAutorName(JSONObject response, final Button textAuthor) {
		JSONArray aYs;
		try {
			if (response.isNull("field_autor")){
				textAuthor.setText(Constants.ANONIMO);
				return;
			}
			
			JSONObject auhtorObject;
			
			try{
				auhtorObject = response.getJSONObject("field_autor");
			} catch (JSONException e){
				//the author is null
				textAuthor.setText(Constants.ANONIMO);
				return;
			}
			
			aYs = auhtorObject.isNull("und")? null:auhtorObject.getJSONArray("und");
			
			if (aYs == null){
				textAuthor.setText(Constants.ANONIMO);
			} else {
				final int autorId = aYs.getJSONObject(0).getInt("target_id");

				final Autor author = new Autor();

				IRequester authorRequest = new IRequester() {

					@Override
					public void onResponse(String response) {
						try {
							JSONArray jsonArray;
							jsonArray = new JSONArray(response);
							JSONObject jsonObject = jsonArray.optJSONObject(0);
							Utils.extractFromResponseToObject(author, jsonObject);
							textAuthor.setText(author.getTitle().trim());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public String getRequestURI() {
						return Constants.BASE_URL + "/api/v1/node?parameters[nid]="
								+ autorId;
					}

					@Override
					public void onResponse(InputStream result) {
					}
				};

				RestClientResistenciarte internalCall = new RestClientResistenciarte(
						authorRequest);
				internalCall.makeJsonRestRequest();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
						return Constants.BASE_URL + "/api/v1/node?page="+currentPage+"&pagesize=5&parameters[type]=escultura";
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
						

						for (Escultura item : esculturas) {

							Escultura distancias2 = item;
							
							View v = View.inflate(HomeActivity.getInstance(),
									R.layout.fragment_escultura, null);


							final ImageView imgView = (ImageView) v.findViewById(R.id.image);

							addIMage(v, item);

							final TextView texto = (TextView) v.findViewById(R.id.tittle);
							texto.setText(distancias2.getTitle());

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
								}, 1500);
						    }
						}, 2000);
						
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
