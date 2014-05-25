package com.jmv.codigociudadano.resistenciarte.fragments.sections;

import java.util.ArrayList;
import java.util.Locale;

import com.jmv.codigociudadano.resistenciarte.R;
import com.jmv.codigociudadano.resistenciarte.fragments.PlaceholderFragment;
import com.jmv.codigociudadano.resistenciarte.net.ImageLoader;
import com.jmv.codigociudadano.resistenciarte.net.WaveLocker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

	public static final String ARG_SECTION_NUMBER = "section_number";

	private ArrayList<PlaceholderFragment> fragments;
	private Context context;
	private WaveLocker locker;

	private ImageLoader imageLoaderService;

	public SectionsPagerAdapter(FragmentManager fm, Context context, WaveLocker locker, ImageLoader imageLoaderService) {
		super(fm);
		this.context = context;
		fragments = new ArrayList<PlaceholderFragment>(3);
		this.locker = locker;
		this.imageLoaderService = imageLoaderService;
	}

	@Override
	public Fragment getItem(int position) {
		PlaceholderFragment p = position < fragments.size() ? fragments
				.get(position) : null;
		if (p == null) {
			switch (position) {
			case 0:
				p = new HighlightsSectionFragment();
				p.setFragmentId(R.layout.fragment_home);
				break;
			case 1:
				p = new EsculturasSectionFragment();
				p.setFragmentId(R.layout.fragment_home2);
				break;
			case 2:
				p = new EsculturasSectionFragment();
				p.setFragmentId(R.layout.fragment_home3);
				break;
			}
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, position);
			p.setArguments(args);
			p.setLocker(locker);
			p.setImageLoaderService(imageLoaderService);
			fragments.add(position, p);
		}
		return p;
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return context.getString(R.string.title_section1).toUpperCase(l);
		case 1:
			return context.getString(R.string.title_section2).toUpperCase(l);
		case 2:
			return context.getString(R.string.title_section3).toUpperCase(l);
		}
		return null;
	}

}
