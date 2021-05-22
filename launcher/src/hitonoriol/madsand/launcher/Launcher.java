package hitonoriol.madsand.launcher;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class Launcher extends JFrame {
	static final String GAME_FILE = "madsand.jar";
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
	String changelogTemplate, infoTemplate = "<html><h3>%s</h3></html>";

	ReleaseParser parser = new ReleaseParser();

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
		changelogTemplate = "<html><div width = " + (super.getWidth() - CHANGELOG_PADDING) + ">%s</div></html>";

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

	String getGameVersion() {
		try (JarInputStream jarStream = new JarInputStream(new FileInputStream(GAME_FILE))) {
			return jarStream.getManifest().getMainAttributes().getValue("Implementation-Version");
		} catch (Exception e) {
			e.printStackTrace();
			return "-";
		}
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

	void printInfo(String arg) {
		infoLabel.setText(String.format(infoTemplate, arg));
	}

	void refreshChangelog() {
		String contents = String.format(changelogTemplate, parser.getChangelog());
		changelogLabel.setText(contents);
	}

	void checkForUpdates() {
		printInfo("Checking for updates...");
		parser.refresh();
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

		parser.downloadGame();

		launchButton.setEnabled(true);
		forceUpdateButton.setEnabled(true);
		printInfo("Done!");
	}

	void update() throws Exception {
		File gameFile = new File(GAME_FILE);
		String latestVersion = parser.getLatestVersion();

		if (gameFile.exists()) {
			launchButton.setEnabled(true);
			forceUpdateButton.setEnabled(true);

			if (latestVersion == null)
				return;

			if (getGameVersion().equals(latestVersion))
				printInfo("Your game version is up-to-date!");
			else {
				printInfo("Newer Version (" + latestVersion + ") found, downloading.\n");
				Thread.sleep(1000L);
				updateGame();
			}
		} else {
			printInfo("No game file. Downloading for the first time.");
			Thread.sleep(1000L);
			updateGame();
		}
	}

	boolean checkConnection() {
		printInfo("Connecting...");
		return NetUtils.pingHost(NetUtils.HOST, 80, 10000);
	}
}
