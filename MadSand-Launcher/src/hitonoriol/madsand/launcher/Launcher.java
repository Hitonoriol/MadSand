package hitonoriol.madsand.launcher;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Launcher extends JFrame {
	private static final long serialVersionUID = 8629061487833976448L;
	static final String HOST = "github.com";
	static final String RELEASES_URL = "https://" + HOST + "/Hitonoriol/MadSand/releases/";
	static final String LATEST_RELEASE_URL = RELEASES_URL + "latest";

	static final String GAME_FILE = "madsand.jar";
	static final String VER_FILE = "version.dat";

	static final String RUN_CONF = "java -jar -Xmx1024m -Xms256m " + GAME_FILE;

	static String noConnectionMsg = "Oops! Either GitHub is down, or there's no network connection.";

	static int CHANGELOG_PADDING = 55;

	JLabel infoLabel = new JLabel("");
	JLabel changelogLabel = new JLabel("");

	JScrollPane changelogScroll = new JScrollPane();
	JButton launchButton = new JButton("Launch");
	JButton forceUpdateButton = new JButton("Force update");
	JPanel bottomPanel = new JPanel();

	JPanel containerPanel = new JPanel();

	String latestVersion, gameLink;
	Document releasePage;
	String changelogTemplate, infoTemplate = "<html><h3>%s</h3></html>";

	public Launcher() {
		super("MadSand Launcher " + Main.VERSION);
		setBounds(100, 100, 500, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		launchButton.setEnabled(false);
		forceUpdateButton.setEnabled(false);

		containerPanel.setLayout(new BorderLayout());
		bottomPanel.setLayout(new GridLayout(2, 1));
		bottomPanel.add(launchButton);
		bottomPanel.add(forceUpdateButton);

		containerPanel.add(infoLabel, "North");

		changelogScroll.setVerticalScrollBarPolicy(22);
		changelogScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		changelogScroll.getVerticalScrollBar().setUnitIncrement(16);
		changelogScroll.add(changelogLabel);
		changelogScroll.setViewportView(changelogLabel);
		containerPanel.add(changelogScroll);
		containerPanel.add(bottomPanel, BorderLayout.PAGE_END);
		add(containerPanel, BorderLayout.CENTER);
		changelogLabel.setVerticalAlignment(JLabel.TOP);
		changelogTemplate = "<html><div width = " + (super.getWidth() - CHANGELOG_PADDING) + ">"
				+ "<h1>Version %s</h1>%s</html>";

		if (new File(GAME_FILE).exists())
			launchButton.setEnabled(true);

		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				launchGame();
			}
		});

		forceUpdateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkConnection()) {
					try {
						refreshChangelog();
						new File(GAME_FILE).delete();
						new Thread(() -> updateGame()).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
	}

	void launchGame() {
		try {
			Runtime.getRuntime().exec(RUN_CONF);
			Main.launcher.dispose();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void parseReleasePage() {
		releasePage = Jsoup.parse(NetUtils.getResponse(LATEST_RELEASE_URL));
	}

	String getLatestVersion() {
		String link = "";
		Elements links = releasePage.select("a");
		for (Element element : links) {
			link = element.attr("href");
			if (link.indexOf("releases/download/") != -1)
				return link.split("/")[5]; // github.com/Hitonoriol/MadSand/releases/download/<version>/...
		}
		return null;
	}

	String getChangelog() {
		String changelog = "";
		changelog = releasePage.getElementsByClass("markdown-body").html();
		Element time = releasePage.select("relative-time").first();
		//String date = time.ownText();
		return time.text() + changelog;
	}

	void printInfo(String arg) {
		infoLabel.setText(String.format(infoTemplate, arg));
	}

	void refreshChangelog() {
		latestVersion = getLatestVersion();
		gameLink = RELEASES_URL + "download/" + latestVersion + "/" + GAME_FILE;
		changelogLabel.setText(String.format(changelogTemplate, latestVersion, getChangelog()));
	}

	void checkForUpdates() {
		parseReleasePage();
		printInfo("Checking for updates...");
		if (checkConnection()) {
			refreshChangelog();
			try {
				update();
			} catch (Exception e) {
				e.printStackTrace();
				printInfo("Failed to check for updates.");
			}
		} else {
			printInfo(noConnectionMsg);
			if (!new File(GAME_FILE).exists()) {
				launchButton.setEnabled(false);
			}
		}
	}

	void updateGame() {
		launchButton.setEnabled(false);
		forceUpdateButton.setEnabled(false);

		NetUtils.downloadFile(gameLink, GAME_FILE);
		Main.writeFile(new File(VER_FILE), latestVersion);

		launchButton.setEnabled(true);
		forceUpdateButton.setEnabled(true);
		printInfo("Done!");
	}

	void update() throws Exception {
		File gameFile = new File(GAME_FILE);
		File versionFile = new File(VER_FILE);

		if (gameFile.exists() && versionFile.exists()) {
			launchButton.setEnabled(true);
			forceUpdateButton.setEnabled(true);

			BufferedReader brTest = new BufferedReader(new FileReader(VER_FILE));
			String text = brTest.readLine();
			brTest.close();

			if (text.equals(latestVersion))
				printInfo("Your game version is up-to-date!");
			else {
				printInfo("Newer Version (" + latestVersion + ") found, downloading.\n");
				Thread.sleep(1000L);
				updateGame();
			}
		} else {
			printInfo("No game and version file. Downloading for the first time.");
			Thread.sleep(1000L);
			updateGame();
		}
	}

	boolean checkConnection() {
		printInfo("Connecting...");
		return NetUtils.pingHost(HOST, 80, 10000);
	}
}
