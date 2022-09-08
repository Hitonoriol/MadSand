package hitonoriol.madsand.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class NetUtils {
	public static final String API_HOST = "api.github.com";
	public static final String noConnectionMsg = "Oops! Either GitHub is down, or there's no network connection.";

	public static void downloadFile(String remotePath, String localPath) {
		BufferedInputStream in = null;
		FileOutputStream out = null;

		try {
			URL url = new URL(remotePath);
			URLConnection conn = url.openConnection();
			int size = conn.getContentLength();

			in = new BufferedInputStream(url.openStream());
			out = new FileOutputStream(localPath);
			byte data[] = new byte[1024];
			int count;
			double sumCount = 0.0;

			while ((count = in.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);

				sumCount += count;
				if (size > 0)
					Main.getWindow().printInfo("Downloading: " + (int) Math.round((sumCount / size * 100.0)) + "%");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static boolean pingHost(String host, int port, int timeout) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), timeout);
			socket.close();
			return true;
		} catch (IOException e) {
			Main.getWindow().printInfo(noConnectionMsg);
			e.printStackTrace();
			return false;
		}
	}

	public static String getResponse(String pageAddress) {
		try {
			URL url = new URL(pageAddress);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
			String response = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
					.lines()
					.collect(Collectors.joining(System.lineSeparator()));
			connection.disconnect();
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
}