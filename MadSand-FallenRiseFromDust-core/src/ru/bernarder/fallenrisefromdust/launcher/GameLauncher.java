package ru.bernarder.fallenrisefromdust.launcher;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class GameLauncher extends JFrame {
	private static final long serialVersionUID = 8629061487833976448L;

	static final String DATADIR = "MadSandData";
	static final String SAVEDIR = "MadSand_Saves";

	static final String USERFILE = SAVEDIR + "/user.dat";
	static final String GAMEFILE = DATADIR + "/game.jar";
	static final String UPDATERFILE = "updater.jar";
	static final String VERFILE = DATADIR + "/version.dat";
	static final String TMPDATA = SAVEDIR + "/data.zip";

	static final String NETHOST = "http://applang.tk/";

	static final String NETVER = NETHOST + "games/madsand/ver";
	static final String NETNEWS = NETHOST + "games/madsand/madnews";
	static final String NETGAME = NETHOST + "games/madsand/game.jar";
	static final String NETUPDATER = NETHOST + "updater.jar";
	static final String NETDATA = NETHOST + "games/madsand/data.zip";
	static final String NETLAUNCHERVER = NETHOST + "cver";

	static final String RUNCONF = "java -jar -Xmx1024m -Xms256m MadSandData/game.jar fromUpdater";
	static final String UPDATERCONF = "java -jar updater.jar";

	static String nointernet = "Oops! MadSand server is down, or there's no network connection.";

	static String VER = "R13";
	static boolean shutdown = false;
	static GameLauncher laun;
	static String name = getExternal(USERFILE);

	static void p(String arg) {
		text.setText("<html><h3>" + arg + "</html>");
	}

	static void refreshNews() {
		news.setText("<html><div width=470>" + NetUtils.getResponse(NETNEWS));
	}

	@SuppressWarnings("resource")
	static void check() throws Exception {
		refreshNews();
		File f = new File(GAMEFILE);
		File f1 = new File(VERFILE);
		if (!new File(DATADIR).exists()) {
			new File(DATADIR).mkdirs();
		}
		if ((f.exists()) && (!f.isDirectory()) && (f1.exists()) && (!f1.isDirectory())) {
			p("Game and version log exists.");
			start.setEnabled(true);
			fupd.setEnabled(true);
			BufferedReader brTest = new BufferedReader(new FileReader(VERFILE));
			String text = brTest.readLine();
			if (text.equals(NetUtils.getResponse(NETVER))) {
				p("Your game version is up-to-date!");
			} else {
				p("Newer Version detected, downloading.\n" + text + "/" + NetUtils.getResponse(NETVER));
				Thread.sleep(1000L);
				start.setEnabled(false);
				fupd.setEnabled(false);
				NetUtils.downloadFile(NETGAME, GAMEFILE);
				File file = new File(VERFILE);
				PrintWriter pw = new PrintWriter(file);
				pw.println((NetUtils.getResponse(NETVER)));
				pw.close();
				new File(SAVEDIR).mkdirs();
				NetUtils.downloadFile(NETDATA, TMPDATA);
				p("Unpacking data...");
				UnzipUtility.unzip(TMPDATA, SAVEDIR);
				new File(TMPDATA).delete();
				start.setEnabled(true);
				fupd.setEnabled(true);
				p("Done!");
			}
		} else {
			p("No game and version file. Downloading for the first time.");
			start.setEnabled(false);
			fupd.setEnabled(false);
			Thread.sleep(1000L);
			File file = new File(VERFILE);
			PrintWriter pw = new PrintWriter(file);
			pw.println((NetUtils.getResponse(NETVER)));
			pw.close();
			NetUtils.downloadFile(NETGAME, GAMEFILE);
			new File(SAVEDIR).mkdirs();
			NetUtils.downloadFile(NETDATA, TMPDATA);
			p("Unpacking data...");
			UnzipUtility.unzip(TMPDATA, SAVEDIR);
			new File(TMPDATA).delete();
			start.setEnabled(true);
			fupd.setEnabled(true);
			p("Done!");
		}
		if (!VER.equalsIgnoreCase(NetUtils.getResponse(NETLAUNCHERVER))) {
			p("Wow! There's an update for launcher!");
			Thread.sleep(1000);
			p("Starting update...");
			Thread.sleep(1000);
			NetUtils.downloadFile(NETUPDATER, UPDATERFILE);
			@SuppressWarnings("unused")
			Process proc = Runtime.getRuntime().exec(UPDATERCONF);
			System.exit(0);
		}
	}

	static boolean isApiUp() {
		return NetUtils.pingHost(NETHOST, 80, 2000);
	}

	static JLabel text = new JLabel("");
	static JLabel news = new JLabel("");
	static JScrollPane scroll = new JScrollPane();
	static JButton start = new JButton("Launch");
	static JButton fupd = new JButton("Force update");
	static JPanel bottomPane = new JPanel();
	static JPanel optPane = new JPanel();

	static JPanel bbPanel = new JPanel();
	static JPanel launcherPanel = new JPanel();

	void upd() {
		if (!isApiUp())
			return;

		try {
			refreshNews();
			fupd.setEnabled(false);
			start.setEnabled(false);
			p("Deleted game file. Updating...");
			Thread.sleep(1000);
			File file1 = new File(VERFILE);
			PrintWriter pw = new PrintWriter(file1);
			pw.println((NetUtils.getResponse(NETVER)));
			pw.close();
			NetUtils.downloadFile(NETGAME, GAMEFILE);
			new File(SAVEDIR).mkdirs();
			NetUtils.downloadFile(NETDATA, TMPDATA);
			p("Unpacking data...");
			UnzipUtility.unzip(TMPDATA, SAVEDIR);
			new File(TMPDATA).delete();
			start.setEnabled(true);
			fupd.setEnabled(true);
			p("Done force updating!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getExternal(String name) {
		try {
			File file = new File(name);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line1 = br.readLine();
			br.close();
			return line1;
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}

	public static void saveToExternal(String name, String text) {
		try {
			File file = new File(name);
			PrintWriter pw = new PrintWriter(file);
			pw.print(text);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GameLauncher() {
		super("BerNardEr Games Launcher " + VER);
		setBounds(100, 100, 500, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		start.setEnabled(false);
		fupd.setEnabled(false);

		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					@SuppressWarnings("unused")
					Process proc = Runtime.getRuntime().exec(RUNCONF);
					System.exit(0);
					GameLauncher.laun.dispose();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		});
		fupd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (isApiUp()) {
					try {
						refreshNews();
						File file = new File(GAMEFILE);
						file.delete();
						new Thread(new Runnable() {
							public void run() {
								upd();
							}
						}).start();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
		launcherPanel.setLayout(new BorderLayout());
		bottomPane.setLayout(new GridLayout(2, 1));
		bottomPane.add(start);
		optPane.setLayout(new GridLayout(2, 1));

		optPane.add(bbPanel);
		bottomPane.add(fupd);

		launcherPanel.add(text, "North");

		if (isApiUp())
			refreshNews();
		else {
			if (new File(GAMEFILE).exists())
				start.setEnabled(true);
			fupd.setEnabled(true);
		}

		scroll.setVerticalScrollBarPolicy(22);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.add(news);
		scroll.setViewportView(news);
		launcherPanel.add(scroll);
		launcherPanel.add(bottomPane, BorderLayout.PAGE_END);
		add(launcherPanel, BorderLayout.CENTER);
	}

	public static void main(String[] args) {

		laun = new GameLauncher();
		laun.setResizable(false);
		laun.setVisible(true);

		p("Checking for updates...");
		if (isApiUp()) {
			try {
				check();
			} catch (Exception e) {
				e.printStackTrace();
				p("Failed to check for updates.");
			}
		} else {
			p(nointernet);
			if (!new File(GAMEFILE).exists()) {
				start.setEnabled(false);
			}
		}
	}
}
