package com.jmv.codigociudadano.resistenciarte.fragments.sections;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Function;
import com.jmv.codigociudadano.resistenciarte.HomeActivity;
import com.jmv.codigociudadano.resistenciarte.R;
import com.jmv.codigociudadano.resistenciarte.fragments.PlaceholderFragment;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Autor;
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
	private Button textAuthor;
	private Button ubic_main;
	
	private SharedPreferences prefs;

	private int currentPage = 0;
	
	public HighlightsSectionFragment() {
		super();
		codeName = getClass().getName();
		prefs = HomeActivity.getInstance().getSharedPreferences(Utils.TARJEBUS_APP, 0);
		checkNidUpdate();
	}

	private void checkNidUpdate() {
		String lastUpdate = prefs.getString(Utils.LAST_UPDATE, "null");
		if (lastUpdate.equalsIgnoreCase("null")) {
			currentPage = 0;
			saveCurrentPage(currentPage);
		} else {
			int lastPage = prefs.getInt(Utils.LAST_PAGE, 0);
			if (Utils.getDaysCountFromLastUpdate(lastUpdate) > 6){
				currentPage++;
				saveCurrentPage(lastPage);
			}
			currentPage = lastPage;
		}
	}

	private void saveCurrentPage(int currentPage2) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Utils.LAST_UPDATE, Utils.getDateAsString());
		editor.putInt(Utils.LAST_PAGE, currentPage2);
		editor.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setFragmentId(R.layout.fragment_home);
		
		super.onCreateView(inflater, container, savedInstanceState);
		
		img = (ImageView) rootView.findViewById(R.id.img);
		
		
		textView = (TextView) rootView.findViewById(R.id.title);
		textAuthor = (Button) rootView.findViewById(R.id.author_main);
		ubic_main = (Button) rootView.findViewById(R.id.ubic_main);

		mDeatailedView = rootView.findViewById(R.id.login_form_detailed);
		
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
		
		Utils.addTouchEffectoToButtons(rootView);
		return rootView;
	}

	@Override
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/node?page="+currentPage+"&pagesize=1&parameters[type]=escultura";
	}

	@Override
	public void onResponse(String response) {
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(response);
			int index = 0;
			JSONObject jsonObject = jsonArray.optJSONObject(index);

			localReg = new Escultura();

			Utils.extractFromResponseToObject(localReg, jsonObject);

			getPhotos(localReg);

		} catch (Exception e) {
			//retrying
			currentPage = 0;
			saveCurrentPage(currentPage);
			RestClientResistenciarte client = new RestClientResistenciarte(this);
			client.makeJsonRestRequest();
		}
	}

	private void getPhotos(final Escultura localReg) {
		RestClientResistenciarte internalCall = new RestClientResistenciarte(
				photosRquester);
		internalCall.makeJsonRestRequest();
	}

	private IRequester photosRquester = new IRequester() {

		@Override
		public void onResponse(String response) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONArray fotosArrays = jsonObject.getJSONObject("field_fotos")
						.getJSONArray("und");

				setUbicacion(jsonObject, ubic_main);

				setAuthor(jsonObject, textAuthor);

				fotos = new ArrayList<Foto>();
				for (int i = 0; i < fotosArrays.length(); i++) {
					Foto foto = new Foto();
					JSONObject jsonObjectFoto = fotosArrays.getJSONObject(i);
					Utils.extractFromResponseToObject(foto, jsonObjectFoto);
					fotos.add(foto);
				}

				// Image url
				String image_url = Constants.BASE_URL + "/sites/default/files/"
						+ fotos.get(0).getFilename();

				Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
					@Override
					public Void apply(Bitmap bmap) {
						textView.setText(localReg.getTitle());

						DisplayMetrics metrics = new DisplayMetrics();
						HomeActivity.getInstance().getWindowManager()
								.getDefaultDisplay().getMetrics(metrics);
						int height = metrics.heightPixels;
						int width = metrics.widthPixels;

						float bmapWidth = bmap.getWidth();
						float bmapHeight = bmap.getHeight();

						float wRatio = width / bmapWidth;
						float hRatio = height / bmapHeight;

						float ratioMultiplier = wRatio;
						// Untested conditional though I expect this might work
						// for landscape mode
						if (hRatio < wRatio) {
							ratioMultiplier = hRatio;
						}

						int newBmapWidth = (int) (bmapWidth * ratioMultiplier);
						int newBmapHeight = (int) (bmapHeight * ratioMultiplier);

						img.setLayoutParams(new LinearLayout.LayoutParams(
								newBmapWidth, newBmapHeight));
						showProgress(false);
						mDeatailedView.setVisibility(View.VISIBLE);
						return null;
					}
				};

				getImageLoaderService()
						.DisplayImage(image_url, img, afterLogin);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void setUbicacion(JSONObject jsonObject, Button ubic_main) {
			/*
			 * "":{"und":[{"lat":"-27.4492586","lon":"-58.9922044",
			 * "map_width":null,"map_height":null,"zoom":"17","name":""}]}
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
				}
			} catch (JSONException e) {
				// the author is null
				ubic_main.setVisibility(View.GONE);
				return;
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

	private void setAuthor(JSONObject response, final Button textAuthor) {
		JSONArray aYs;
		try {
			if (response.isNull("field_autor")) {
				textAuthor.setText(Constants.ANONIMO);
				return;
			}

			JSONObject auhtorObject;

			try {
				auhtorObject = response.getJSONObject("field_autor");
			} catch (JSONException e) {
				// the author is null
				textAuthor.setText(Constants.ANONIMO);
				return;
			}

			aYs = auhtorObject.isNull("und") ? null : auhtorObject
					.getJSONArray("und");

			if (aYs == null) {
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
							Utils.extractFromResponseToObject(author,
									jsonObject);
							textAuthor.setText(author.getTitle().trim());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public String getRequestURI() {
						return Constants.BASE_URL
								+ "/api/v1/node?parameters[nid]=" + autorId;
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

}
