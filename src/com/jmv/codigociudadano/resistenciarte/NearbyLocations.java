package com.jmv.codigociudadano.resistenciarte;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Function;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Autor;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Foto;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.GeoEscultura;
import com.jmv.codigociudadano.resistenciarte.net.IRequester;
import com.jmv.codigociudadano.resistenciarte.net.RestClientResistenciarte;
import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NearbyLocations extends LocatorActivity implements IRequester {

	private View mLoginStatusView;
	private View mLoginFormView;

	private LinearLayout myLinearLayout;

	private ProgressBar progressBarStreet;
	private Location myLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby_locations);

		mLoginFormView = findViewById(R.id.home_form);
		mLoginStatusView = findViewById(R.id.login_status);

		myLinearLayout = (LinearLayout) findViewById(R.id.text_view_place);

		mAddress = (TextView) findViewById(R.id.cuurent_location);

		mAddressGPS = (TextView) findViewById(R.id.cuurent_location_by_gps);

		progressBarStreet = (ProgressBar) findViewById(R.id.progress_street);

		mAddress.setVisibility(View.GONE);

		Utils.addTouchEffectoToButtons(mLoginFormView);

		showProgress(true);
		init();
	}

	public static void showHome(Context home) {
		Intent intent = new Intent(home, NearbyLocations.class);
		home.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nearby_locations, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		if (location != null) {
			myLocation = location;
			RestClientResistenciarte client = new RestClientResistenciarte(this);
			client.makeJsonRestRequest();
		}
	}

	@Override
	protected void onAdressGet(String address) {
		if (!address.contains("error")) {
			mAddress.setVisibility(View.VISIBLE);
			mAddress.setText(address);
			progressBarStreet.setVisibility(View.GONE);
		} else {
			mAddress.setVisibility(View.GONE);
			progressBarStreet.setVisibility(View.VISIBLE);
		}

	}

	@Override
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	@Override
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/closest_nodes_by_coord?lat="
				+ myLocation.getLatitude() + "&lon="
				+ myLocation.getLongitude() + "&qty_nodes=10&dist=12000";
	}

	@Override
	public void onResponse(String response) {
		ArrayList<GeoEscultura> listaEsculturas = new ArrayList<GeoEscultura>();

		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(response);
			int max = jsonArray.length();

			for (int i = 0; i < max; i++) {
				JSONObject jsonObject = jsonArray.optJSONObject(i);

				GeoEscultura localReg = new GeoEscultura();

				Utils.extractFromResponseToObject(localReg, jsonObject);

				listaEsculturas.add(localReg);
			}

		} catch (Exception e) {

		}
		myLinearLayout.removeAllViewsInLayout();

		if (!listaEsculturas.isEmpty()) {
			Collections.sort(listaEsculturas);

			int number = 1;

			for (GeoEscultura distancias2 : listaEsculturas) {

				View v = View.inflate(NearbyLocations.this,
						R.layout.fragment_escultura_nearby, null);

				final LatLng lt = new LatLng(distancias2.getNode_latitude(),
						distancias2.getNode_longitude());

				addIMage(v, distancias2);

				final TextView texto = (TextView) v.findViewById(R.id.tittle);
				texto.setText(" " + number + " - "
						+ distancias2.getNode_title());
				number++;
				final Button textoUbic = (Button) v
						.findViewById(R.id.ubicacion);
				textoUbic.setText((Utils.toDecimalFormat(distancias2
						.getDistance() * 1000)) + " metros aprox.");

				textoUbic.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								android.content.Intent.ACTION_VIEW,
								Uri.parse("http://maps.google.com/maps?   saddr="
										+ currentLocation.latitude
										+ ","
										+ currentLocation.longitude
										+ "&daddr="
										+ lt.latitude + "," + lt.longitude));
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.setClassName("com.google.android.apps.maps",
								"com.google.android.maps.MapsActivity");
						startActivity(intent);
					}
				});

				myLinearLayout.addView(v);

			}

			showProgress(false);
		}
	}

	private void addIMage(final View view, final GeoEscultura distancias2) {
		IRequester r = new IRequester() {

			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONArray fotosArrays = jsonObject.getJSONObject(
							"field_fotos").getJSONArray("und");

					addAutorName(jsonObject,
							(Button) view.findViewById(R.id.author));

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

	private void addAutorName(JSONObject response, final Button textAuthor) {
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
							final int nid = autorId;
							textAuthor
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {

											AutorActivity.showHome(
													HomeActivity.getInstance(),
													nid);
										}
									});
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

	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

}
