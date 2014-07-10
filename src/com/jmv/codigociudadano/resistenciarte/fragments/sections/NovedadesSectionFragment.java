package com.jmv.codigociudadano.resistenciarte.fragments.sections;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.bt;
import com.google.android.gms.internal.di;
import com.jmv.codigociudadano.resistenciarte.HomeActivity;
import com.jmv.codigociudadano.resistenciarte.R;
import com.jmv.codigociudadano.resistenciarte.comps.TextViewEx;
import com.jmv.codigociudadano.resistenciarte.fragments.PlaceholderFragment;
import com.jmv.codigociudadano.resistenciarte.logic.esculturas.Evento;

import com.jmv.codigociudadano.resistenciarte.utils.CalendarUtil;
import com.jmv.codigociudadano.resistenciarte.utils.Constants;
import com.jmv.codigociudadano.resistenciarte.utils.Utils;

public class NovedadesSectionFragment extends PlaceholderFragment {

	private SharedPreferences prefs;

	public static final String CALENDAR_ID = "CALENDAR_ID";
	public static final String CALENDAR_TITLE = "CALENDAR_TITLE";
	public static final String CALENDAR_START_DATE = "CALENDAR_START_DATE";
	public static final String CALENDAR_END_DATE = "CALENDAR_END_DATE";

	ProgressDialog pDialog;
	private LinearLayout myLinearLayout;

	private LinearLayout mOpps;

	private ArrayList<Evento> esculturas;
	private Button button;

	private boolean isAModification;
	private Long eventIDForModification;

	private ProgressBar progressBar1;

	private TextView loading_status_message;

	public NovedadesSectionFragment(Context context) {
		super(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setFragmentId(R.layout.fragment_home3);

		super.onCreateView(inflater, container, savedInstanceState);

		showProgress(true);

		myLinearLayout = (LinearLayout) rootView
				.findViewById(R.id.text_view_place);

		mOpps = (LinearLayout) rootView.findViewById(R.id.no_novedades);
		mOpps.setVisibility(View.GONE);
		

		progressBar1 = (ProgressBar) rootView.findViewById(R.id.progressBar1);
		loading_status_message = (TextView) rootView.findViewById(R.id.loading_status_message);
		
		new DownloadTask().execute();

		return rootView;
	}

	
	
	@Override
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onResponse(String response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResponse(InputStream result) {
		// TODO Auto-generated method stub

	}

	// usually, subclasses of AsyncTask are declared inside the activity class.
	// that way, you can easily modify the UI thread from here
	private class DownloadTask extends
			AsyncTask<String, Integer, ArrayList<Evento>> {

		public DownloadTask() {

		}

		@Override
		protected void onPreExecute() {
			// Get the Drawable custom_progressbar                     
		    Drawable draw=context.getResources().getDrawable(R.drawable.customprogressbar);
		// set the drawable as progress drawable
		    progressBar1.setProgressDrawable(draw);
			progressBar1.setMax(100);
		}
		
		@Override
		protected ArrayList<Evento> doInBackground(String... sUrl) {

			esculturas = esculturas == null ? Utils
					.getWebLocations(NovedadesSectionFragment.this.context)
					: esculturas;

			if (myLinearLayout.getChildCount() == 0) {
				myLinearLayout.removeAllViewsInLayout();

				String currentDate = "";

				Collections.sort(esculturas);

				int number = 0;
				for (Evento item : esculturas) {

					final Evento distancias2 = item;

					number++;
					publishProgress((int) ((number / (float) esculturas.size()) * 100));
					
					View v = View.inflate(HomeActivity.getInstance(),
							R.layout.fragment_novedades, null);

					View heView = View.inflate(HomeActivity.getInstance(),
							R.layout.header, null);

					final TextView texto = (TextView) v
							.findViewById(R.id.tittle);
					texto.setText(distancias2.getNombre());

					StringBuilder builder = new StringBuilder();
					builder.append("Duracion: " + distancias2.getHora_inicio());
					builder.append(" a ");
					builder.append(distancias2.getHora_fin());

					final TextView descEx = (TextView) v
							.findViewById(R.id.date);
					descEx.setText(builder.toString());

					final TextView descEx1 = (TextView) v
							.findViewById(R.id.lugar);
					descEx1.setText("Lugar: " + distancias2.getLugar());

					final TextView descEx2 = (TextView) v
							.findViewById(R.id.organizador);
					descEx2.setText("Organiza: " + distancias2.getOrganizador());

					Button shareButton = (Button) v.findViewById(R.id.share);
					shareButton.setEnabled(true);
					shareButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Utils.shareNovedad(HomeActivity.getInstance(),
									distancias2);
						}
					});

					DateTimeFormatter dateStringFormat = DateTimeFormat
							.forPattern("dd-MM-yyyy HH:mm");
					DateTime time = dateStringFormat.parseDateTime(String.valueOf(
							distancias2.getDate().replaceAll("\\s+", "")
									+ " "
									+ distancias2.getHora_inicio().replaceAll("\\s+",
											"")).trim());

					Calendar cal = Calendar.getInstance();

					cal.setTime(time.toDate());

					DateTime timeEnd = dateStringFormat.parseDateTime(String.valueOf(
							distancias2.getDate().replaceAll("\\s+", "") + " "
									+ distancias2.getHora_fin().replaceAll("\\s+", ""))
							.trim());

					Calendar untilCal = Calendar.getInstance();
					untilCal.setTime(timeEnd.toDate());
					
					final boolean alreadyInCalendar = CalendarUtil.isAlreadyAtCalendar(context, cal.getTimeInMillis(), untilCal.getTimeInMillis(), distancias2.getNombre());
					
					final Button calendarBtn = (Button) v.findViewById(R.id.asistir);
					
					if (alreadyInCalendar){
						calendarBtn.setText(context.getString(R.string.asistirOK));
						Drawable img = context.getResources().getDrawable( R.drawable.ic_assits_ok );
						img.setBounds( 0, 0, 60, 60 );
						calendarBtn.setCompoundDrawables( img, null, null, null );
					}
					
					
					calendarBtn.setEnabled(true);
					calendarBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (alreadyInCalendar){
								Toast.makeText(
										HomeActivity.getInstance().getApplicationContext(),
										R.string.calendar_event_already_added_message,
										Toast.LENGTH_LONG).show();
							} else {
								calendarBtn.setText(context.getString(R.string.asistirOK));
								Drawable img = context.getResources().getDrawable( R.drawable.ic_assits_ok );
								img.setBounds( 0, 0, 60, 60 );
								calendarBtn.setCompoundDrawables( img, null, null, null );
								addToCalendar(distancias2);
							}
						}
					});

					if (!currentDate.equalsIgnoreCase(distancias2.getDate())) {
						final Button btn = (Button) heView
								.findViewById(R.id.novedad);

						// First convert to Date. This is one of the many ways.
						String dateString = distancias2.getDate();
						Date date = null;
						try {
							date = new SimpleDateFormat("dd-MM-yyyy")
									.parse(dateString);
						} catch (ParseException e) {

						}

						// Then get the day of week from the Date based on
						// specific locale.
						String dayOfWeek = new SimpleDateFormat("EEEE",
								Locale.getDefault()).format(date);

						btn.setText(Utils.toCamelCase(dayOfWeek).trim() + ", "
								+ distancias2.getDate());
						currentDate = distancias2.getDate();
						number++;
						myLinearLayout.addView(heView);
					}

					myLinearLayout.addView(v);

				}

			}
			
			
			
			return esculturas;
			 
		 }

		 @Override
		 protected void onProgressUpdate(Integer... progress) {
		     //setProgressPercent(progress[0]);
			 progressBar1.setProgress(progress[0]);
			 if (progress[0] >=99){
				 loading_status_message.setText(context.getString(R.string.sync_finish));
			 }
		 }

		private void addToCalendar(Evento distancias2) {

			DateTimeFormatter dateStringFormat = DateTimeFormat
					.forPattern("dd-MM-yyyy HH:mm");
			DateTime time = dateStringFormat.parseDateTime(String.valueOf(
					distancias2.getDate().replaceAll("\\s+", "")
							+ " "
							+ distancias2.getHora_inicio().replaceAll("\\s+",
									"")).trim());

			Calendar cal = Calendar.getInstance();

			cal.setTime(time.toDate());

			DateTime timeEnd = dateStringFormat.parseDateTime(String.valueOf(
					distancias2.getDate().replaceAll("\\s+", "") + " "
							+ distancias2.getHora_fin().replaceAll("\\s+", ""))
					.trim());

			Calendar untilCal = Calendar.getInstance();
			untilCal.setTime(timeEnd.toDate());

			if (cal.getTimeInMillis() >= untilCal.getTimeInMillis()) {
				Toast.makeText(
						HomeActivity.getInstance().getApplicationContext(),
						R.string.calendar_event_wrong_message,
						Toast.LENGTH_LONG).show();
				return;
			}

			StringBuilder changeType = new StringBuilder();

			if (Build.VERSION.SDK_INT >= 8) {
				try {
					CalendarUtil.pushAppointmentsToCalender(
							HomeActivity.getInstance(),
							distancias2.getNombre(), distancias2.toString(),
							-1L, cal.getTimeInMillis(),
							untilCal.getTimeInMillis(), true, true, true);
				} catch (Exception e) {
					Toast.makeText(
							HomeActivity.getInstance().getApplicationContext(),
							R.string.calendar_event_undefined_error,
							Toast.LENGTH_LONG).show();
					return;
				}
			} else {
				Intent intent = new Intent(Intent.ACTION_EDIT);
				intent.setType("vnd.android.cursor.item/event");
				intent.putExtra("beginTime", cal.getTimeInMillis());
				intent.putExtra("allDay", false);
				intent.putExtra("rrule", "FREQ=DAILY;COUNT=1");
				intent.putExtra("endTime", untilCal.getTimeInMillis());
				intent.putExtra("title", distancias2.toString());
				startActivity(intent);
			}

			Toast.makeText(HomeActivity.getInstance().getApplicationContext(),
					R.string.calendar_event_added_message, Toast.LENGTH_LONG)
					.show();

		}

		@Override
		protected void onPostExecute(ArrayList<Evento> result) {
			progressBar1.setProgress(100);
			showProgress(false);

		}
	}

}
