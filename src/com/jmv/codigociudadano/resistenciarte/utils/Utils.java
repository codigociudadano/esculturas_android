package com.jmv.codigociudadano.resistenciarte.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.jmv.codigociudadano.resistenciarte.R;
import com.jmv.codigociudadano.resistenciarte.fragments.sections.EsculturaItem;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Escultura;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

public class Utils {

	public static final String LAT1 = "lat1";
	public static final String LAT2 = "lat2";
	public static final String LONG1 = "long1";
	public static final String LONG2 = "long2";

	public static final int GPS_NOT_TURNED_FF = 3;
	public static final String TARJEBUS_APP = "com.jmv.tarje_bus_nea.";
	public static final String FIRST_TIME = TARJEBUS_APP + "first_time";
	public static final String LAST_UPDATE = "last_update";
	public static final String LAST_PAGE = "last_page";
	public static final String FILE_NAME = "systemTB";
	public static final String CONTENTS = "contents";

	public static String GOOGLE_ACCOUNT_USERNAME = "tarjebusapp@gmail.com";
	public static String GOOGLE_ACCOUNT_PASSWORD = "cajetaquemada123";
	public static String EMPRESA_SELECTED = "empresa_selected";
	public static String CURRENT_ESCULTURA = "escultura_actual";
	public static final int GPS_NOT_TURNED_ON = 2;
	public static final String LAST_NID = "last_nid";

	
	
	@SuppressWarnings("unchecked")
	public static <T> T tranformAccordingType(Class<T> type, Object object) {

        if (type.isAssignableFrom(String.class)) {
            return (T) object;
        } else if (type.isAssignableFrom(Date.class)) {
            String[] date = String.valueOf(object).split(Constants.DATE_SEPARATOR);
            int year = Integer.parseInt(date[2].trim());
            int month = Integer.parseInt(date[1].trim());
            int day = Integer.parseInt(date[0].trim());
            Date d;
            Calendar cal = GregorianCalendar.getInstance();
            cal.set( year, month, day);
            d = cal.getTime();
            return (T) d;
        }  else if (type.isAssignableFrom(double.class) || (type.isAssignableFrom(Double.class))) {
            return (T) Double.valueOf(String.valueOf(object).trim().replaceAll(Constants.COMMA, Constants.DOT));
        } else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
            return (T) Double.valueOf(String.valueOf(object).trim().replaceAll(Constants.COMMA, Constants.DOT));
        } else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
            //takeout all spaces
            return (T) Integer.valueOf(String.valueOf(object).trim());
        }
        return null;
        //To change body of generated methods, choose Tools | Templates.
    }
    
    public static <T> T[] copyArray(T[] vector){
        T[] another = (T[]) Array.newInstance(vector.getClass().getComponentType(), vector.length);
        System.arraycopy(vector, 0, another, 0, vector.length);
        return another;
    }

    public static String getSetMethod(String fieldName) {
        // TODO Auto-generated method stub
        String firstWithCapitalLetter = fieldName.toUpperCase().substring(0, 1);
        String restOfMethodName = fieldName.substring(1, fieldName.length());
        return Constants.SET + firstWithCapitalLetter + restOfMethodName;
    }
    
    public static String getGetMethod(String fieldName) {
        // TODO Auto-generated method stub
        String firstWithCapitalLetter = fieldName.toUpperCase().substring(0, 1);
        String restOfMethodName = fieldName.substring(1, fieldName.length());
        String methodName = Constants.GET + firstWithCapitalLetter + restOfMethodName;
        return methodName;
    }
    
    public void shareEscultura(Activity activity, EsculturaItem escultura) {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "http://goo.gl/x8w50A";
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				activity.getString(R.string.share_tittle).replaceAll(Constants.PATTERN_REPLACE, escultura.getEscultura().getTitle()));
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		activity.startActivity(Intent.createChooser(sharingIntent, "Compartilo en..."));
	}
    
    public static String toDecimalFormat(Double d){
    	return new DecimalFormat("##.##").format(d);
    }

    static void handleException(Exception ex) {
        System.out.println(Constants.EXCEPCION_OCURRIDA_ + ex.getClass().getName()+" "+ex.getMessage());
    }

	public static long getDaysCountFromLastUpdate(String lastUpdate) {
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
		Date startDate;
		try {
			startDate = formatter.parse(lastUpdate);
		} catch (ParseException e) {
			return 1;
		}
		Date today = new Date();

		long startTime = startDate.getTime();
		long endTime = today.getTime();

		long diffTime = endTime - startTime;

		long diffDays = diffTime / (1000 * 60 * 60 * 24);

		return diffDays;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	
	public static String getDateAsString() {
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
		return formatter.format(new Date());
	}

	public static void addTouchEffectoToButtons(View container) {
		ArrayList<View> touchables = container.getTouchables();

		final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

		for (View view : touchables) {
			if (view instanceof Button) {
				view.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN: {
							v.startAnimation(buttonClick);
							Drawable drw = v.getBackground();
							if (drw != null) {
								drw.setColorFilter(0xe0f47521,
										PorterDuff.Mode.SRC_ATOP);
							}
							v.invalidate();
							break;
						}
						case MotionEvent.ACTION_UP: {
							Drawable drw = v.getBackground();
							if (drw != null) {
								drw.clearColorFilter();
							}
							v.invalidate();
							break;
						}
						}
						return false;
					}

				});
			}
		}
	}

	public static  <T> void extractFromResponseToObject(T localReg, JSONObject jsonObject) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, NoSuchMethodException {
		Class<?> classToUse = localReg.getClass();
		Field[] fields = classToUse.getDeclaredFields();
		if (fields.length == 0){
			classToUse = localReg.getClass().getSuperclass();
			fields = classToUse.getDeclaredFields();
		}
		for (Field f : fields) {
			Method method;
			method = classToUse.getDeclaredMethod(
					Utils.getSetMethod(f.getName()), f.getType());
			method.invoke(
					localReg,
					Utils.tranformAccordingType(f.getType(),
							jsonObject.get(f.getName())));
		}
	}
}
