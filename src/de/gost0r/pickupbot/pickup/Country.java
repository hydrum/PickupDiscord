package de.gost0r.pickupbot.pickup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

public class Country {

	private static HashMap<String, String> CountryToContinentMap = new HashMap<String, String>();
	
	public static void initCountryCodes() throws IOException {
		String fileName = Paths.get("country-and-continent-codes-list.json").toString();
		InputStream is = new FileInputStream(fileName);
		String jsonTxt = IOUtils.toString(is, "UTF-8");

		JSONArray arr = new JSONArray(jsonTxt);
		
		for(int i = 0; i < arr.length(); i++) {
			String continent = arr.getJSONObject(i).get("Continent_Code").toString();
			String country = arr.getJSONObject(i).get("Two_Letter_Country_Code").toString();
			
			CountryToContinentMap.put(country, continent);
		}
	}
	
	public static String getContinent(String country) {
		return CountryToContinentMap.get(country);
	}
	
	public static Boolean isValid(String country) {
		Object o = CountryToContinentMap.get(country);

		return o != null;
	}
	
	public static String getCountryFlag(String country) {
		String msg;
		
		if( country.equalsIgnoreCase("NOT_DEFINED")) {
			msg = "";
		}
		else {
			msg = ":flag_" + country.toLowerCase() + ":";
		}
		
		return msg;
	}
	
}
