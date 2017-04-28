package controllers;

import play.mvc.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImportSthlmData extends Controller {

	public Result getParks() {
		
		String output = "";
		String tempLine = "";
		
		try {

			URL url = new URL("http://api.stockholm.se/ServiceGuideService/ServiceUnitTypes/9da341e4-bdc6-4b51-9563-e65ddc2f7434/ServiceUnits?apikey=56010af30b114502bfbf8db404ef41a4");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/xml");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			output = "Output from Server .... \n";
			
			while ((tempLine = br.readLine()) != null) {
				output += tempLine;
			}
			
			conn.disconnect();

		} catch (MalformedURLException e) { e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		
		return ok("All is well!\n\n" + output);

	}

}
