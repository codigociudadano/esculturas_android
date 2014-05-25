package com.jmv.codigociudadano.resistenciarte.net;

import java.io.InputStream;

public interface IRequester {

	public String getRequestURI();
	public void onResponse(String response);
	public void onResponse(InputStream result);
	
}
