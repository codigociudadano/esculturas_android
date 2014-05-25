package com.jmv.codigociudadano.resistenciarte;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Function;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.GeoEscultura;
import com.jmv.codigociudadano.resistenciarte.net.IRequester;
import com.jmv.codigociudadano.resistenciarte.net.RestClientResistenciarte;
import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
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
		} else if (id == R.id.home) {
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
				+ myLocation.getLongitude() + "&qty_nodes=100&dist=12000";
	}

	@Override
	public void onResponse(String response) {
		ArrayList<GeoEscultura> listaEsculturas = null;

		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(response);
			int max = jsonArray.length();

			listaEsculturas = new ArrayList<GeoEscultura>();

			for (int i = 0; i < 21; i++) {
				JSONObject jsonObject = jsonArray.optJSONObject(i);

				GeoEscultura localReg = new GeoEscultura();

				Utils.extractFromResponseToObject(localReg, jsonObject);

				listaEsculturas.add(localReg);
			}

		} catch (Exception e) {

		}
		myLinearLayout.removeAllViewsInLayout();

		Collections.sort(listaEsculturas);

		int number = 1;

		for (GeoEscultura distancias2 : listaEsculturas) {
			final TextView rowTextView = new TextView(NearbyLocations.this);

			// set some properties of rowTextView or something
			rowTextView.setText("    " + number + " - "
					+ distancias2.getNode_title() + "; "
					+ (Utils.toDecimalFormat(distancias2.getDistance() / 1000)) + " metros aprox.");

			number++;

			final LatLng pos = new LatLng(distancias2.getNode_latitude(),
					distancias2.getNode_longitude());

			LinearLayout LL = new LinearLayout(NearbyLocations.this);
			LL.setOrientation(LinearLayout.HORIZONTAL);
			LL.setBackgroundColor(number % 2 == 0 ? Color.parseColor("#bfbfbf")
					: Color.parseColor("#d8d8d8"));

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;

			LL.setLayoutParams(params);
			LL.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(
							android.content.Intent.ACTION_VIEW,
							Uri.parse("http://maps.google.com/maps?   saddr="
									+ currentLocation.latitude + ","
									+ currentLocation.longitude + "&daddr="
									+ pos.latitude + "," + pos.longitude));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setClassName("com.google.android.apps.maps",
							"com.google.android.maps.MapsActivity");
					startActivity(intent);
				}
			});

			LinearLayout linearForText = new LinearLayout(NearbyLocations.this);
			linearForText.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams paramsT = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsT.gravity = Gravity.CENTER | Gravity.LEFT;
			linearForText.setLayoutParams(paramsT);

			linearForText.addView(rowTextView);

			// creo la imagen

			LinearLayout linearForIMG = new LinearLayout(NearbyLocations.this);
			linearForIMG.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams paramsIMG = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsIMG.gravity = Gravity.CENTER | Gravity.LEFT;
			linearForIMG.setLayoutParams(paramsIMG);

			final ImageView imgView = new ImageView(NearbyLocations.this);

			imgView.setVisibility(View.VISIBLE);

			linearForIMG.addView(imgView);

			LL.addView(linearForIMG);

			// Image url
			String image_url = Constants.BASE_URL
					+ "/sites/default/files/esculturas/imagefield_VSgeIF.jpeg";

			Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
				@Override
				public Void apply(Bitmap bmap) {

					int height = 36;
					int width = 36;

					float bmapWidth = bmap.getWidth();
					float bmapHeight = bmap.getHeight();

					float wRatio = width / bmapWidth;
					float hRatio = height / bmapHeight;

					float ratioMultiplier = wRatio;
					// Untested conditional though I expect this might work for
					// landscape mode
					if (hRatio < wRatio) {
						ratioMultiplier = hRatio;
					}

					int newBmapWidth = (int) (bmapWidth * ratioMultiplier);
					int newBmapHeight = (int) (bmapHeight * ratioMultiplier);

					imgView.setLayoutParams(new LinearLayout.LayoutParams(
							newBmapWidth, newBmapHeight));
					imgView.setVisibility(View.VISIBLE);
					return null;
				}
			};

			HomeActivity.getInstance().getImageLoaderService()
					.DisplayImage(image_url, imgView, afterLogin);

			LL.addView(linearForText);

			// add the textview to the linearlayout
			float scale = getResources().getDisplayMetrics().density;
			int dpAsPixels = (int) (5*scale + 0.5f);
			
			LL.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
			myLinearLayout.addView(LL);

		}

		showProgress(false);
	}

	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

}
