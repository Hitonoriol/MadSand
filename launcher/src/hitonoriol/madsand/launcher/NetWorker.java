package hitonoriol.madsand.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import hitonoriol.madsand.commons.exception.Exceptions;

public class NetWorker {
	private String url;

	public NetWorker(String url) {
		this.url = url;
	}

	public CompletableFuture<File> downloadFile(String localPath, Consumer<Double> progressConsumer) {
		return CompletableFuture.supplyAsync(
				Exceptions.asUnchecked(() -> {
					URL url = getURL();
					BufferedInputStream in = new BufferedInputStream(url.openStream());
					FileOutputStream out = new FileOutputStream(localPath);
					URLConnection connection = url.openConnection();
					int totalSize = connection.getContentLength(), currentSize = 0;
					byte data[] = new byte[1024];
					int bytesRead;
					while ((bytesRead = in.read(data, 0, 1024)) != -1) {
						out.write(data, 0, bytesRead);
						currentSize += bytesRead;
						if (totalSize > 0)
							progressConsumer.accept((double) currentSize / totalSize);
					}
					in.close();
					out.close();
					return new File(localPath);
				}));
	}

	public CompletableFuture<String> sendGETRequest() {
		return CompletableFuture.supplyAsync(
				Exceptions.asUnchecked(() -> {
					URL url = getURL();
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
					String response = new BufferedReader(
							new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
									.lines()
									.collect(Collectors.joining(System.lineSeparator()));
					connection.disconnect();
					return response;
				}));
	}

	private URL getURL() throws MalformedURLException {
		return new URL(url);
	}
}