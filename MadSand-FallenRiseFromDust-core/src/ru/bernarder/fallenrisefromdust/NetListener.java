package ru.bernarder.fallenrisefromdust;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetListener {
	public static String getRespond(String pageAddress) throws Exception {
		String codePage = "utf-8";
		StringBuilder sb = new StringBuilder();
		URL pageURL = new URL(pageAddress);
		HttpURLConnection uc = (HttpURLConnection) pageURL.openConnection();
		uc.addRequestProperty("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.75 Safari/537.36");
		BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream(), codePage));
		try {
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				sb.append(inputLine);
			}
		} finally {
			br.close();
		}
		return sb.toString();
	}
}