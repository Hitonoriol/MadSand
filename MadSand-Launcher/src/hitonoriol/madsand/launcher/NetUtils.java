package hitonoriol.madsand.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class NetUtils {

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
					Main.launcher.printInfo("Downloading: " + (int) Math.round((sumCount / size * 100.0)) + "%");
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
			Main.launcher.printInfo(Launcher.noConnectionMsg);
			e.printStackTrace();
			return false;
		}
	}

	public static String getResponse(String pageAddress) {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
}