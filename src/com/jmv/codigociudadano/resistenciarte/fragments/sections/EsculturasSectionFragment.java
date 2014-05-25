package com.jmv.codigociudadano.resistenciarte.fragments.sections;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jmv.codigociudadano.resistenciarte.R;
import com.jmv.codigociudadano.resistenciarte.fragments.PlaceholderFragment;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Escultura;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Foto;
import com.jmv.codigociudadano.resistenciarte.net.IRequester;
import com.jmv.codigociudadano.resistenciarte.net.RestClientResistenciarte;
import com.jmv.codigociudadano.resistenciarte.utils.Generador;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;

public class EsculturasSectionFragment extends PlaceholderFragment {

	private TextView textView;
	private ArrayList<Foto> fotos;

	public EsculturasSectionFragment() {
		super();
		RestClientResistenciarte client = new RestClientResistenciarte(this);
		client.makeJsonRestRequest();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		textView = (TextView) rootView.findViewById(R.id.section_label);
		return rootView;
	}

	@Override
	public String getRequestURI() {
		Integer numero = Generador.generarNumeroAleatorio(1, 52);
		return "http://dev.resistenciarte.org/api/v1/node?page=" + numero;
	}

	@Override
	public void onResponse(String response) {
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(response);
			int index = Generador.generarNumeroAleatorio(0,
					jsonArray.length() - 1);
			JSONObject jsonObject = jsonArray.optJSONObject(index);

			final Escultura localReg = new Escultura();
			
			Utils.extractFromResponseToObject(localReg, jsonObject);

			RestClientResistenciarte internalCall = new RestClientResistenciarte(
					new IRequester() {
						
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
								
							} catch (Exception e) {
								e.printStackTrace();
							}
							showProgress(false);
						}

						@Override
						public String getRequestURI() {
							return localReg.getUri();
						}

						@Override
						public void onResponse(InputStream result) {
							// TODO Auto-generated method stub
							
						}
					});
			
			internalCall.makeJsonRestRequest();

			textView.setText(localReg.getTitle());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub
		
	}

}
