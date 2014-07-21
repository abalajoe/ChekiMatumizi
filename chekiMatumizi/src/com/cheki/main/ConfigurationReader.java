package com.cheki.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * This class is used to load our configuration file.
 * 
 * @author jabala
 * 
 */
public class ConfigurationReader {
	// logging
	private String LOG = "ConfigurationReader";

	// declare application context & properties
	private Context context;
	private Properties properties;

	/**
	 * Initialize
	 * 
	 * @param context
	 */
	public ConfigurationReader(Context context) {
		this.context = context;

		// construct new properties object
		properties = new Properties();
	}

	/**
	 * This method opens and reads contents of properties file.
	 * 
	 * @param FileName
	 *            - properties file
	 * @return
	 */
	public Properties getProperties(String FileName) {

		try {

			// access the application's raw asset files
			AssetManager assetManager = context.getAssets();

			// open asset
			InputStream inputStream = assetManager.open(FileName);

			// load properties file from specified stream
			properties.load(inputStream);
			Log.i(LOG, "configuration file loaded!");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(LOG, e.toString());
		}
		return properties;

	}

}