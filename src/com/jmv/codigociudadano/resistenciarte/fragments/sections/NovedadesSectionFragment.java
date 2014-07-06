package com.jmv.codigociudadano.resistenciarte.fragments.sections;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.jmv.codigociudadano.resistenciarte.HomeActivity;
import com.jmv.codigociudadano.resistenciarte.ObraActivity;
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


	public NovedadesSectionFragment(Context context) {
		super(context);
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
			
			if (max < Constants.MAX_NUMBER_ITEMS){
				button.setVisibility(View.GONE);
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
					
					setDescription(jsonObject, (Button) view.findViewById(R.id.description));
					
					if (!jsonObject.isNull("field_fotos")) {
						try {
							jsonObject.getJSONArray("field_fotos");
							ImageView viewImg = (ImageView) view
									.findViewById(R.id.image);
							viewImg.setBackgroundResource(R.drawable.ic_launcher_custom);
							setInvisibleToDefaultImages();
							return;
						} catch (Exception e) {
							//everything goes well!
						}
					} else {
						ImageView viewImg = (ImageView) view
								.findViewById(R.id.image);
						viewImg.setBackgroundResource(R.drawable.ic_launcher_custom);
						setInvisibleToDefaultImages();
						return;
					}
					
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
							setInvisibleToDefaultImages();
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
			
			private void setDescription(JSONObject jsonObject,
					Button descriptionBtn) {
				/*
				 * "":{"und":[{"lat":"-27.4492586","lon":"-58.9922044",
				 * "map_width":null,"map_height":null,"zoom":"17","name":""}]}
				 * 
				 * field_ubicacion":{"und":[{"value":"Perón, Juan Domingo Nº
				 * 454\t\t\t\t",
				 * "safe_value":"Perón, Juan Domingo Nº 454\t\t\t\t"
				 * ,"format":null}]}
				 */
				JSONArray aYs;
				try {
					if (jsonObject.isNull("body")) {
						descriptionBtn.setVisibility(View.GONE);
						return;
					}
					JSONObject auhtorObject;
					auhtorObject = jsonObject.getJSONObject("body");

					aYs = auhtorObject.isNull("und") ? null : auhtorObject
							.getJSONArray("und");

					if (aYs == null) {
						descriptionBtn.setVisibility(View.GONE);
					} else {
						final String value = String
								.valueOf(
										aYs.getJSONObject(0).getString("value"))
								.trim().substring(0, 50).concat("... <b>[Leer mas..]</b>");
						descriptionBtn.setText(value.contains("Fuente :")?Html.fromHtml("<b>[Leer mas...]</b>"):Html.fromHtml(value));
						descriptionBtn.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ObraActivity.showHome(HomeActivity.getInstance(), distancias2.getNid(), distancias2.getTitle().trim());
							}
						});
					}
				} catch (JSONException e) {
					// the author is null
					descriptionBtn.setVisibility(View.GONE);
					return;
				}

			}
			
			private void setInvisibleToDefaultImages() {
				View p = view.findViewById(R.id.progress);
				p.setVisibility(View.GONE);
				View iV = view.findViewById(R.id.default_image);
				iV.setVisibility(View.GONE);
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
			showProgress(true);
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
								if (currentPage == 0){
									button2.setVisibility(View.GONE);
								}
								button.setVisibility(View.GONE);
								showProgress(false);
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
									R.layout.fragment_novedades, null);

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
								scroll.smoothScrollTo(0, 0);
								scroll.fullScroll(ScrollView.FOCUS_UP);
								showProgress(false);
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
		
		}
	}
}
