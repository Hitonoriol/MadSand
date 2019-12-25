package ru.bernarder.fallenrisefromdust;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetUtils {
	public static final String USER_AGENT_STR = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.75 Safari/537.36";
	public static final String USER_AGENT_PROPERTY = "User-Agent";

	public static String getResponse(String host) throws Exception {
		String codePage = "utf-8";
		StringBuilder sb = new StringBuilder();
		URL pageURL = new URL(host);
		HttpURLConnection uc = (HttpURLConnection) pageURL.openConnection();
		uc.addRequestProperty(USER_AGENT_PROPERTY, USER_AGENT_STR);
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