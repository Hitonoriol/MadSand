package ru.bernarder.fallenrisefromdust.desktop;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class NetListener {
	public static String getRespond(String pageAddress) throws Exception {
		String codePage = "utf-8";
		StringBuilder sb = new StringBuilder();
		URL pageURL = new URL(pageAddress);
		URLConnection uc = pageURL.openConnection();
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