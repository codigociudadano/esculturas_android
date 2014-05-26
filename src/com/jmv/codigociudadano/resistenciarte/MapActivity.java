package com.jmv.codigociudadano.resistenciarte;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Escultura;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.GeoEscultura;
import com.jmv.codigociudadano.resistenciarte.net.IRequester;
import com.jmv.codigociudadano.resistenciarte.net.RestClientResistenciarte;
import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.jmv.codigociudadano.resistenciarte.utils.Generador;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MapActivity extends ActionBarCustomActivity implements IRequester {

	protected View mLoginFormView;
	protected View mLoginStatusView;

	// Google Map
	private GoogleMap googleMap;
	private Location myLocation;
	private ArrayList<GeoEscultura> listaEsculturas;

	public MapActivity() {
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		try {

			mLoginFormView = findViewById(R.id.login_form);
			mLoginStatusView = findViewById(R.id.login_status);
			showProgress(true);
			initilizeMap();
		} catch (Exception e) {
		}

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	public static void showHome(Context home) {
		Intent intent = new Intent(home, MapActivity.class);
		home.startActivity(intent);
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * 
	 * @throws Exception
	 * */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initilizeMap() throws Exception {
		if (googleMap == null) {

			FragmentManager myFragmentManager = getSupportFragmentManager();
			SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
					.findFragmentById(R.id.map);
			googleMap = mySupportMapFragment.getMap();

			googleMap.setMyLocationEnabled(true);
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);

			// Getting LocationManager object from System Service
			// LOCATION_SERVICE
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// ---set the criteria for best location provider---
			Criteria c = new Criteria();
			c.setAccuracy(Criteria.ACCURACY_FINE);
			// ---OR---
			// c.setAccuracy(Criteria.ACCURACY_COARSE);
			c.setAltitudeRequired(false);
			c.setBearingRequired(false);
			c.setCostAllowed(true);
			c.setPowerRequirement(Criteria.POWER_HIGH);
			// ---get the best location provider---
			String bestProvider = locationManager.getBestProvider(c, true);

			// Getting Current Location
			if (bestProvider != null) {
				myLocation = locationManager.getLastKnownLocation(bestProvider);

				if (myLocation != null) {
					// Getting latitude of the current location
					double latitude = myLocation.getLatitude();

					// Getting longitude of the current location
					double longitude = myLocation.getLongitude();

					LatLng myPosition = new LatLng(latitude, longitude);
					googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
							myPosition, 14.0f));

				}
			}

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.map_fragment, container,
					false);
			return rootView;
		}
	}

	@Override
	public String getRequestURI() {
		String url = Constants.BASE_URL
				+ "/api/v1/closest_nodes_by_coord?lat=-27.454528&lon=-58.976896&qty_nodes=100";
		return url;
	}

	@Override
	public void onResponse(String response) {
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(response);
			int max = jsonArray.length();

			listaEsculturas = new ArrayList<GeoEscultura>();

			for (int i = 0; i < max; i++) {
				JSONObject jsonObject = jsonArray.optJSONObject(i);

				GeoEscultura localReg = new GeoEscultura();

				Utils.extractFromResponseToObject(localReg, jsonObject);

				listaEsculturas.add(localReg);
			}

			final LatLngBounds.Builder builder = new LatLngBounds.Builder();

			for (GeoEscultura locationStore : listaEsculturas) {
				final LatLng pos = new LatLng(locationStore.getNode_latitude(),
						locationStore.getNode_longitude());
				builder.include(pos);
				LatLng pos2 = new LatLng(
						locationStore.getNode_latitude() + 0.001,
						locationStore.getNode_longitude() + 0.001);
				builder.include(pos2);

				int image = R.drawable.ic_action_pin;

				googleMap.addMarker(new MarkerOptions()
						.position(pos)
						.title(locationStore.getNode_title())
						.snippet(
								"Distancia actual:"
										+ locationStore.getDistance() + "(KM)")
						.icon(BitmapDescriptorFactory.fromResource(image)));

			}

			showProgress(false);

			googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

				public void onCameraChange(CameraPosition arg0) {
					googleMap.animateCamera(CameraUpdateFactory
							.newLatLngBounds(builder.build(), 50));

					googleMap.setOnCameraChangeListener(null);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

}
