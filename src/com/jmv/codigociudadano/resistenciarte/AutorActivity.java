package com.jmv.codigociudadano.resistenciarte;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.jmv.codigociudadano.resistenciarte.comps.TextViewEx;
import com.jmv.codigociudadano.resistenciarte.fragments.sections.EsculturaItem;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Escultura;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Foto;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.FotoObraAutor;
import com.jmv.codigociudadano.resistenciarte.net.IRequester;
import com.jmv.codigociudadano.resistenciarte.net.ImageLoader;
import com.jmv.codigociudadano.resistenciarte.net.RestClientResistenciarte;
import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AutorActivity extends ActionBarCustomActivity implements
		IRequester {

	protected View mLoginFormView;
	protected View mLoginStatusView;

	private ImageLoader imageLoaderService;
	private LinearLayout myLinearLayout;
	private int nid;
	private LinearLayout btn_loading_screen;
	private Button see_gallery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_autor);

		nid = getIntent().getIntExtra(Constants.NID, 0);

		if (nid == 0) {
			HomeActivity.showHome(this);
			finish();
			return;
		}
		
		myLinearLayout = (LinearLayout) findViewById(R.id.text_view_place);
		
		see_gallery = (Button)  findViewById(R.id.see_gallery);
		see_gallery.setVisibility(View.GONE);
		
		mLoginFormView = findViewById(R.id.home_form);
		mLoginStatusView = findViewById(R.id.login_status);

		btn_loading_screen = (LinearLayout) findViewById(R.id.btn_loading_screen);
		
		// initialize tthe image loader service
		// ImageLoader class instance
		imageLoaderService = ImageLoader.getInstance(getApplicationContext());

		showProgress(true);
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
	}

	private void addImages(final String autorName) {
		IRequester r = new IRequester() {

			@Override
			public void onResponse(String response) {
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(response);

					JSONArray fotosArrays = jsonObject.getJSONArray("data");

					ArrayList<FotoObraAutor> fotos = new ArrayList<FotoObraAutor>();
					for (int i = 0; i < fotosArrays.length(); i++) {
						FotoObraAutor foto = new FotoObraAutor();
						JSONObject jsonObjectFoto = fotosArrays
								.getJSONObject(i);
						Utils.extractFromResponseToObject(foto, jsonObjectFoto);
						fotos.add(foto);

					}

					final int terminoDeCargar = fotos.size();
					final int idx = 0;
					final ArrayList<Bitmap> bmDat = new ArrayList<Bitmap>();
					final ArrayList<String> bmStr = new ArrayList<String>();
					
					final CoverFlowData cv = new CoverFlowData();
					cv.setBm(bmDat);
					cv.setStr(bmStr);
					
					for (final FotoObraAutor fotoObraAutor : fotos) {
						// Image url
						final String image_url = fotoObraAutor.getImage();
						
						final View v = View.inflate(AutorActivity.this,
								R.layout.escultura_loading, null);
						
						final ImageView imSex = (ImageView) v.findViewById(R.id.image_to_load);
						
						final Button textView = (Button) v.findViewById(R.id.tittle);
						textView.setText(fotoObraAutor.getName().trim());
						
						
						final int nidObra = fotoObraAutor.getId();
						textView.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ObraActivity.showHome(AutorActivity.this, nidObra);
							}
						});
						
						myLinearLayout.addView(v);

						
						Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
							@Override
							public Void apply(Bitmap bmap) {
								btn_loading_screen.setVisibility(View.GONE);
								View p = v.findViewById(R.id.progress);
								p.setVisibility(View.GONE);
								View iV = v.findViewById(R.id.default_image);
								iV.setVisibility(View.GONE);
								
								bmStr.add(fotoObraAutor.getName());
								bmDat.add(bmap);
								if (bmDat.size() == terminoDeCargar){
									see_gallery.setVisibility(View.VISIBLE);
									see_gallery.setOnClickListener(new OnClickListener() {
										
										@Override
										public void onClick(View v) {
											CoverFlowExample.showHome(AutorActivity.this, cv, autorName);
										}
									});
									
								}
								return null;
							}
						};

						imageLoaderService.DisplayImage(image_url, imSex,
								afterLogin);
					}

				} catch (Exception e) {

				}
			}

			@Override
			public String getRequestURI() {
				return Constants.BASE_URL + "/api/v1/esculturas?autor=" + nid;
			}

			@Override
			public void onResponse(InputStream result) {
			}
		};

		RestClientResistenciarte internalCall = new RestClientResistenciarte(r);
		internalCall.makeJsonRestRequest();
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	protected void showProgress(final boolean show) {
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	public static void showHome(Context home, int nid) {
		Intent intent = new Intent(home, AutorActivity.class);
		intent.putExtra(Constants.NID, nid);
		home.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.autor, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public String getRequestURI() {
		return Constants.BASE_URL + "/api/v1/node/" + nid;
	}

	@Override
	public void onResponse(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);

			myLinearLayout.removeAllViews();
			
			final Button textName = (Button) findViewById(R.id.tittle);
			textName.setText(jsonObject.getString("title").trim());

			addImages(jsonObject.getString("title").trim());
			
			final TextViewEx textoUbic = (TextViewEx) findViewById(R.id.description);
			setDescription(jsonObject, textoUbic);

			if (!jsonObject.isNull("field_fotos")) {
				try {
					jsonObject.getJSONArray("field_fotos");
					ImageView viewImg = (ImageView) findViewById(R.id.image);
					viewImg.setBackgroundResource(R.drawable.ic_author_default);
					showProgress(false);
					return;
				} catch (Exception e) {
					// everything goes well!
				}
			} else {
				ImageView viewImg = (ImageView) findViewById(R.id.image);
				viewImg.setBackgroundResource(R.drawable.ic_author_default);
				
				showProgress(false);
				return;
			}
			JSONArray fotosArrays = jsonObject.getJSONObject("field_fotos")
					.getJSONArray("und");
			ArrayList<Foto> fotos = new ArrayList<Foto>();
			for (int i = 0; i < fotosArrays.length(); i++) {
				Foto foto = new Foto();
				JSONObject jsonObjectFoto = fotosArrays.getJSONObject(i);
				Utils.extractFromResponseToObject(foto, jsonObjectFoto);
				fotos.add(foto);
			}

			// Image url
			String image_url = Constants.BASE_URL + "/sites/default/files/"
					+ fotos.get(0).getUri().replaceFirst("public://", "");
			;

			Function<Bitmap, Void> afterLogin = new Function<Bitmap, Void>() {
				@Override
				public Void apply(Bitmap bmap) {
					View p = findViewById(R.id.progress);
					p.setVisibility(View.GONE);
					View iV = findViewById(R.id.default_image);
					iV.setVisibility(View.GONE);
					ImageView m = (ImageView) findViewById(R.id.image);
					m.setVisibility(View.VISIBLE);
					showProgress(false);
					return null;
				}
			};

			imageLoaderService.DisplayImage(image_url,
					(ImageView) findViewById(R.id.image), afterLogin);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setDescription(JSONObject jsonObject, TextViewEx descriptionBtn) {
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
				final String value = String.valueOf(
						aYs.getJSONObject(0).getString("value")).trim();
				descriptionBtn.setText(Html.fromHtml(value));
			}
		} catch (JSONException e) {
			// the author is null
			descriptionBtn.setVisibility(View.GONE);
			return;
		}

	}

	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

}
