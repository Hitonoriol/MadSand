package hitonoriol.madsand.launcher.gui;

import static hitonoriol.madsand.launcher.GameLauncher.gameFile;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.FileInputStream;
import java.util.jar.JarInputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import hitonoriol.madsand.launcher.GameLauncher;
import hitonoriol.madsand.launcher.Main;
import hitonoriol.madsand.launcher.NetUtils;
import hitonoriol.madsand.launcher.ReleaseParser;

public class LauncherFrame extends JFrame {

	private static final int CHANGELOG_PADDING = 55;

	private JLabel infoLabel = new JLabel("");
	private JLabel changelogLabel = new JLabel("");

	private JScrollPane changelogScroll = new JScrollPane();
	private JButton launchButton = new JButton("Launch");
	private JButton forceUpdateButton = new JButton("Force update");
	private JPanel bottomPanel = new JPanel();

	JPanel containerPanel = new JPanel();
	String changelogTemplate, infoTemplate = "<html><h3>%s</h3></html>";

	ReleaseParser parser = new ReleaseParser();

	public LauncherFrame() {
		super("MadSand Launcher " + Main.VERSION);
		initLayout();
		initActionListeners();
	}

	private void initLayout() {
		setBounds(100, 100, 500, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		launchButton.setEnabled(false);
		forceUpdateButton.setEnabled(false);

		bottomPanel.setLayout(new GridLayout(2, 1));
		bottomPanel.add(launchButton);
		bottomPanel.add(forceUpdateButton);

		changelogScroll.setVerticalScrollBarPolicy(22);
		changelogScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		changelogScroll.getVerticalScrollBar().setUnitIncrement(16);
		changelogScroll.add(changelogLabel);
		changelogScroll.setViewportView(changelogLabel);
		changelogLabel.setVerticalAlignment(JLabel.TOP);
		changelogTemplate = "<html><div width = " + (super.getWidth() - CHANGELOG_PADDING) + ">%s</div></html>";

		containerPanel.setLayout(new BorderLayout());
		containerPanel.add(infoLabel, "North");
		containerPanel.add(changelogScroll);
		containerPanel.add(bottomPanel, BorderLayout.PAGE_END);
		add(containerPanel, BorderLayout.CENTER);

		if (gameFile().exists())
			launchButton.setEnabled(true);
	}

	private void initActionListeners() {
		launchButton.addActionListener(event -> new GameLauncher().launch());
		forceUpdateButton.addActionListener(event -> {
			if (!checkConnection())
				return;
			refreshChangelog();
			gameFile().delete();
			try {
				new Thread(() -> updateGame()).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void printInfo(String arg) {
		infoLabel.setText(String.format(infoTemplate, arg));
	}

	void refreshChangelog() {
		String contents = String.format(changelogTemplate, parser.getChangelog());
		changelogLabel.setText(contents);
	}

	public void checkForUpdates() {
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
			printInfo(NetUtils.noConnectionMsg);
			if (!gameFile().exists()) {
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
		String latestVersion = parser.getLatestVersion();

		if (gameFile().exists()) {
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

	String getGameVersion() {
		try (JarInputStream jarStream = new JarInputStream(new FileInputStream(gameFile()))) {
			return jarStream.getManifest().getMainAttributes().getValue("Implementation-Version");
		} catch (Exception e) {
			e.printStackTrace();
			return "-";
		}
	}

	boolean checkConnection() {
		printInfo("Connecting...");
		return NetUtils.pingHost(NetUtils.API_HOST, 80, 10000);
	}
}
