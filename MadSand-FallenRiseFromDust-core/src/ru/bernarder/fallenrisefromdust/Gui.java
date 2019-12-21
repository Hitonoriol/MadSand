package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.security.MessageDigest;

public class Gui {
	static final float DEFWIDTH = 250f;
	static final int LOG_LENGTH = 20;
	public static final int EQ_SLOTS = 5;
	static float defLblWidth = Gdx.graphics.getWidth() / 4;

	static NinePatchDrawable transparency;

	static Image[] equip;

	static Table darkness;
	static Table gamecontext;
	static Table mousemenu;
	static Table craftbl;

	static ScrollPane scroll;

	static Label[] overlayStatLabels;
	static Label[] log;
	static Label[] mouselabel;
	static Label dialMSG;
	public static Label verlbl;

	static Stage menu;
	static Stage dead;
	static Stage craft;
	static Stage overlay;

	static TextField inputField;

	static Skin skin;

	static TextButton[] craftbtn;
	public static TextButton exitToMenuBtn;
	public static TextButton craftBtn;
	static TextButton acceptD;
	static TextButton refuseD;
	static TextButton[] contextMenuBtn;
	static TextButton resumeBtn;

	static BitmapFont font;
	static BitmapFont fontBig;

	public static void createBasicSkin() {
		font = createFont(16);
		fontBig = createFont(24);
		Gui.skin = new Skin();
		Gui.skin.add("default", font);
		Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 12, Pixmap.Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		Gui.skin.add("background", new Texture(pixmap));

		Slider.SliderStyle slst = new Slider.SliderStyle();
		slst.background = Gui.skin.newDrawable("background", Color.DARK_GRAY);
		Drawable knob = Gui.skin.newDrawable("background", Color.GRAY);
		knob.setMinWidth(20);
		slst.knob = knob;
		Gui.skin.add("default-horizontal", slst);
		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = Gui.skin.newDrawable("background", Color.GRAY);
		textButtonStyle.down = Gui.skin.newDrawable("background", Color.DARK_GRAY);
		textButtonStyle.over = Gui.skin.newDrawable("background", Color.LIGHT_GRAY);
		textButtonStyle.font = Gui.skin.getFont("default");
		textButtonStyle.disabled = Gui.skin.newDrawable("background", Color.BLACK);
		Gui.skin.add("default", textButtonStyle);
		Label.LabelStyle labelStyle = new Label.LabelStyle();
		labelStyle.font = font;
		labelStyle.fontColor = Color.WHITE;
		Gui.skin.add("default", labelStyle);
		transparency = new NinePatchDrawable(
				new NinePatch(new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/transparency.png"))));
		Window.WindowStyle ws = new Window.WindowStyle();
		ws.background = Gui.skin.newDrawable("background", Color.LIGHT_GRAY);
		ws.stageBackground = transparency;
		ws.titleFontColor = Color.BLUE;
		ws.titleFont = font;
		Gui.skin.add("default", ws);
		TextField.TextFieldStyle tx = new TextField.TextFieldStyle();
		tx.font = font;
		tx.fontColor = Color.WHITE;
		tx.background = Gui.skin.newDrawable("background", Color.DARK_GRAY);
		tx.background.setMinHeight(35.0F);
		tx.selection = Gui.skin.newDrawable("background", Color.LIGHT_GRAY);
		tx.cursor = Gui.skin.newDrawable("background", Color.GRAY);
		tx.cursor.setMinWidth(1.0F);
		tx.cursor.setMinHeight(tx.background.getMinHeight());
		Gui.skin.add("default", tx);
		ScrollPane.ScrollPaneStyle spx = new ScrollPane.ScrollPaneStyle();
		Gui.skin.add("default", spx);
		
		Gui.overlay = new Stage();
	}

	static Dialog dialog;

	static Label statsLbl, skillsLbl;
	static Label conStatLbl, strStatLbl, accStatLbl, intStatLbl, luckStatLbl, dexStatLbl, statSumLbl;
	static Label hpStatLbl, staminaStatLbl;

	static int ITEM_DISPLAY_HOLDING = 4;
	static int ITEM_DISPLAY_SLOTS = 5;

	public static void setHandDisplay(int id) {
		Drawable img = Resources.noEquip;
		if (id != 0)
			img = new TextureRegionDrawable(Resources.item[id]);
		equip[ITEM_DISPLAY_HOLDING].setDrawable((Drawable) img);
	}

	static void refreshOverlay() {
		overlayStatLabels[0].setText("HP: " + World.player.stats.hp + "/" + World.player.stats.mhp);
		overlayStatLabels[1].setText("Level: " + World.player.stats.skills.getLvl(Skill.Level));
		overlayStatLabels[2].setText("Experience: " + World.player.stats.skills.getExpString(Skill.Level));
		overlayStatLabels[3].setText("Food: " + World.player.stats.food + " / " + World.player.stats.maxFood);
		overlayStatLabels[4].setText("Hand: " + World.player.stats.hand.name);
	}

	public static void showStatsWindow() {
		final Dialog statWindow = new Dialog("", Gui.skin);
		statWindow.text(World.player.stats.name);
		TextButton ok = new TextButton("Close", Gui.skin);
		MadSand.charcrt = true;
		ok.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.charcrt = false;
				statWindow.remove();
				statWindow.clearActions();
			}

		});
		statWindow.setBackground(bck);
		statWindow.setMovable(true);
		statWindow.add(new Label("", Gui.skin));
		refreshStatLabels();
		statWindow.row();
		statWindow.add(new Label("Level: " + World.player.stats.skills.getLvl(Skill.Level) + " ("
				+ World.player.stats.skills.getExpString(Skill.Level) + ")", Gui.skin));
		statWindow.row();
		statWindow.add(new Label("", Gui.skin)).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(statsLbl).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(hpStatLbl).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(staminaStatLbl).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(strStatLbl).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(accStatLbl).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(intStatLbl).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(luckStatLbl).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(dexStatLbl).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(skillsLbl).width(defLblWidth).row();
		statWindow.row();
		Skill skill;
		Label skillLbl;

		for (int i = 1; i < Skill.len(); ++i) {
			skill = Skill.get(i);
			if (skill == Skill.Level)
				continue;
			skillLbl = new Label(skill + ": " + World.player.stats.skills.getLvlString(skill), Gui.skin);
			statWindow.add(skillLbl).width(defLblWidth).row();
			statWindow.row();
		}

		statWindow.add(ok).width(defLblWidth).row();
		statWindow.show(Gui.overlay);
	}

	static void initLaunchMenu() {
		MadSand.state = GameState.NMENU;
		Gdx.input.setInputProcessor(Gui.menu);
	}

	static Stage worldg;
	static Stage loadg;
	static Stage gotodg;
	static Label wlbl;
	static NinePatchDrawable bck;

	static void initWmenu() {
		worldg = new Stage();
		wlbl = new Label("Generating your world...", Gui.skin);
		wlbl.setAlignment(1);
		Table tbl = new Table();
		tbl.setFillParent(true);
		tbl.add(wlbl).width(Gdx.graphics.getWidth()).row();
		worldg.addActor(tbl);

		loadg = new Stage();
		Label wlbl = new Label("Loading...", Gui.skin);
		wlbl.setAlignment(1);
		Table tbl1 = new Table();
		tbl1.setFillParent(true);
		tbl1.add(wlbl).width(Gdx.graphics.getWidth()).row();
		loadg.addActor(tbl1);

		gotodg = new Stage();
		Label gotolbl = new Label("Saving current sector and going to the next one...", Gui.skin);
		gotolbl.setAlignment(1);
		Table gototbl = new Table();
		gototbl.setFillParent(true);
		gototbl.add(gotolbl).width(Gdx.graphics.getWidth()).row();
		gotodg.addActor(gototbl);
	}

	static void drawOkDialog(String msg, Stage stage) {
		final Dialog dialog = new Dialog(" ", Gui.skin);
		int linesToSkip = 2;
		dialog.text(msg).pad(25);
		dialog.row();
		for (int i = 0; i < linesToSkip; ++i) {
			dialog.add(" ");
			dialog.row();
		}
		TextButton cbtn = new TextButton("Ok", Gui.skin);
		cbtn.align(Align.center);
		dialog.add(cbtn).width(defLblWidth).row();
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				dialog.remove();
			}

		});
		dialog.show(stage);
	}

	static void rollStats() {
		World.player.stats.roll();
		refreshStatLabels();
	}

	static void refreshStatLabels() {
		Stats s = World.player.stats;
		strStatLbl.setText("Strength: " + s.str);
		accStatLbl.setText("Accuracy: " + s.accur);
		conStatLbl.setText("Constitution: " + s.constitution);
		intStatLbl.setText("Intelligence: " + s.intelligence);
		luckStatLbl.setText("Luck: " + s.luck);
		dexStatLbl.setText("Dexterity: " + s.dexterity);
		statSumLbl.setText("\nStat sum: " + s.getSum());

		hpStatLbl.setText("HP: " + s.hp + "/" + s.mhp);
		staminaStatLbl.setText("Stamina: " + s.stamina + "/" + s.maxstamina);
	}

	static void createCharDialog() {
		rollStats();
		MadSand.charcrt = true;

		String msg = "Character creation";
		final Dialog dialog = new Dialog(" ", Gui.skin);
		dialog.setMovable(true);
		dialog.text(msg);
		dialog.row();
		final TextField nameField = new TextField("Player", Gui.skin);
		refreshStatLabels();
		dialog.add(new Label("Character name:", Gui.skin)).width(defLblWidth).row();
		dialog.row();
		dialog.add(nameField).width(defLblWidth).row();
		dialog.row();
		dialog.add(conStatLbl).width(defLblWidth).row();
		dialog.row();
		dialog.add(strStatLbl).width(defLblWidth).row();
		dialog.row();
		dialog.add(accStatLbl).width(defLblWidth).row();
		dialog.row();
		dialog.add(intStatLbl).width(defLblWidth).row();
		dialog.row();
		dialog.add(luckStatLbl).width(defLblWidth).row();
		dialog.row();
		dialog.add(dexStatLbl).width(defLblWidth).row();
		dialog.row();
		dialog.add(statSumLbl).width(defLblWidth).row();
		TextButton rbtn = new TextButton("Reroll", Gui.skin);
		TextButton cbtn = new TextButton("Create", Gui.skin);
		dialog.add(rbtn).width(defLblWidth).row();
		dialog.row();
		dialog.add(cbtn).width(defLblWidth).row();
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				if (!nameField.getText().trim().equals("")) {
					World.player.setName(nameField.getText());
					MadSand.charcrt = false;
					World.player.reinit();
					dialog.remove();
				}
			}

		});
		rbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				rollStats();
			}

		});
		dialog.show(Gui.overlay);
	}

	static void drawSettingsDialog() {
		int radius = 12;

		final Dialog dialog = new Dialog(" ", Gui.skin);
		dialog.text("\nSettings");
		dialog.row();
		dialog.align(Align.center);
		final Label renderv = new Label("", Gui.skin);
		final Slider renderslide = new Slider(5, 125, 5, false, Gui.skin);
		if (new File("MadSand_Saves/lastrend.dat").exists())
			radius = (Integer.parseInt(getExternal("lastrend.dat")));
		renderslide.setValue(radius);
		renderv.setText("Render radius (" + (int) renderslide.getValue() + ")");
		TextButton cbtn = new TextButton("Set", Gui.skin);
		TextButton cancel = new TextButton("Cancel", Gui.skin);

		dialog.add(renderv).row();
		dialog.add(renderslide).width(defLblWidth).row();

		dialog.add(cbtn).width(defLblWidth).row();
		dialog.add(cancel).width(defLblWidth).row();

		renderslide.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderv.setText("Render radius (" + (int) renderslide.getValue() + ")");
			}
		});
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.setRenderRadius(Math.round(renderslide.getValue()));
				Gui.saveToExternal("lastrend.dat", Math.round(renderslide.getValue()) + "");
				// TODO
				dialog.remove();
			}

		});
		cancel.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				dialog.remove();
			}

		});
		dialog.show(Gui.menu);
	}

	static void loadWorldDialog() {
		File file = new File(MadSand.MAPDIR);
		String[] dirs = file.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		final Dialog ldialog = new Dialog(" ", Gui.skin);
		ldialog.text("\nLoad game:\n");
		ldialog.row();
		int i = 0;
		int slots = 0;
		try {
			slots = dirs.length;
		} catch (Exception e) {

		}
		if (slots > MadSand.MAXSAVESLOTS)
			slots = MadSand.MAXSAVESLOTS;

		TextButton[] ldbtn = new TextButton[slots];
		while (i < slots) {
			ldbtn[i] = new TextButton(dirs[i], Gui.skin);
			ldialog.add(ldbtn[i]).width(Gdx.graphics.getWidth() / 2).row();
			final String sa = dirs[i];
			ldbtn[i].addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					MadSand.WORLDNAME = sa;
					if (GameSaver.loadWorld(sa)) {
						MadSand.state = GameState.GAME;
					} else
						drawOkDialog("Unable to load world " + sa, menu);

				}
			});
			i++;
		}
		TextButton cbtn = new TextButton("Cancel", Gui.skin);
		if (slots == 0)
			ldialog.add(new TextButton("No game sessions to load", Gui.skin)).width(defLblWidth).row();
		ldialog.add(cbtn).width(defLblWidth).row();
		ldialog.add(new Label("\n", Gui.skin)).width(defLblWidth).row();
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				ldialog.remove();

			}

		});
		ldialog.show(Gui.menu);
	}

	static void createWorldDialog() {
		File file = new File(MadSand.MAPDIR);
		String[] dirs = file.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		int slots = 0;
		try {
			slots = dirs.length;
		} catch (Exception e) {

		}
		if (slots > MadSand.MAXSAVESLOTS)
			slots = MadSand.MAXSAVESLOTS;
		dialog = new Dialog(" ", Gui.skin);
		final TextField worldtxt = new TextField("Save Slot #" + (++slots), Gui.skin);
		dialog.text("New game");
		TextButton okbtn = new TextButton("Proceed", Gui.skin);
		TextButton nobtn = new TextButton("Cancel", Gui.skin);
		if (slots == MadSand.MAXSAVESLOTS) {
			worldtxt.setText("No free slots left!");
			worldtxt.setDisabled(true);
			okbtn.setDisabled(true);
		}
		nobtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gui.dialog.remove();
			}

		});
		okbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				if (!worldtxt.getText().trim().equals("")) {
					MadSand.WORLDNAME = worldtxt.getText();
					File index = new File("MadSand_Saves/" + MadSand.WORLDNAME);
					String[] entries = index.list();
					try {
						String[] arrayOfString1;
						int j = (arrayOfString1 = entries).length;
						for (int i = 0; i < j; i++) {
							String s = arrayOfString1[i];
							File currentFile = new File(index.getPath(), s);
							currentFile.delete();
						}
						index.delete();
					} catch (Exception localException) {
					}

					MadSand.switchStage(GameState.GAME, Gui.overlay);
					MadSand.world.Generate();
					// World.player.x = new Random().nextInt(World.MAPSIZE);
					// World.player.y = new Random().nextInt(World.MAPSIZE);
					World.player.updCoords();
					Gui.exitToMenuBtn.setVisible(false);
					Gui.craftBtn.setVisible(false);
					Gui.inventoryActive = false;
					dialog.remove();
					Gui.createCharDialog();
				}

			}
		});
		worldtxt.setTextFieldListener(new TextField.TextFieldListener() {

			public void keyTyped(TextField textField, char key) {
			}

		});
		dialog.row();
		dialog.add(new Label("\n\n", Gui.skin)).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(new Label("\n\nSession name:\n", Gui.skin)).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(worldtxt).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(okbtn).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(nobtn).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(new Label("\n\n", Gui.skin)).width(Gdx.graphics.getWidth() / 2).row();
		dialog.show(Gui.menu);
	}

	static String sha1(String input) {
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			byte[] result = mDigest.digest(input.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString((result[i] & 0xFF) + 256, 16).substring(1));
			}

			return sb.toString();
		} catch (Exception e) {
		}
		return "";
	}

	public static String getExternal(String name) {
		try {
			File file = new File(MadSand.SAVEDIR + name);
			BufferedReader br = new BufferedReader(new java.io.FileReader(file));
			String line1 = br.readLine();
			br.close();
			return (line1);
		} catch (Exception e) {
		}
		return "";
	}

	public static void saveToExternal(String name, String text) {
		try {
			File file = new File(MadSand.SAVEDIR + name);
			PrintWriter pw = new PrintWriter(file);
			pw.print((text));
			pw.close();
		} catch (Exception localException) {
		}
	}

	static void getVersion() {
		String ver = "MadSandData/version.dat";
		if (!GameSaver.getExternal(ver).equals(""))
			MadSand.VER = "[GREEN]b-" + (GameSaver.getExternal(ver));
		else
			MadSand.VER = "[GREEN]Version file not found";
		verlbl = new Label(MadSand.VER, Gui.skin);
	}

	static final int OVSTAT_COUNT = 6;

	static Label dieLabel;

	public static void setDeadText(String str) {
		dieLabel.setText(str);
	}

	static void initmenu() {
		Gui.equip = new Image[EQ_SLOTS];
		for (int i = 0; i < EQ_SLOTS; ++i) {
			Gui.equip[i] = new Image();
			Gui.equip[i].setDrawable(Resources.noEquip);
		}
		
		Gui.overlayStatLabels = new Label[OVSTAT_COUNT];

		statsLbl = new Label("Stats:", Gui.skin);
		skillsLbl = new Label("\nSkills:", Gui.skin);

		conStatLbl = new Label("", Gui.skin);
		strStatLbl = new Label("", Gui.skin);
		accStatLbl = new Label("", Gui.skin);
		intStatLbl = new Label("", Gui.skin);
		luckStatLbl = new Label("", Gui.skin);
		dexStatLbl = new Label("", Gui.skin);
		statSumLbl = new Label("", Gui.skin);
		hpStatLbl = new Label("", Gui.skin);
		staminaStatLbl = new Label("", Gui.skin);
		refreshStatLabels();

		Gui.gamecontext = new Table(Gui.skin);
		Gui.contextMenuBtn = new TextButton[5];
		Gui.mousemenu = new Table(Gui.skin);
		Gui.mousemenu.setVisible(true);
		Gui.mouselabel = new Label[5];
		int cc = 0;
		Gui.contextMenuBtn[0] = new TextButton("Interact", Gui.skin);
		Gui.contextMenuBtn[0].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse();
				World.player.interact(World.player.stats.look);
			}

		});
		Gui.contextMenuBtn[3] = new TextButton("Use item", Gui.skin);
		Gui.contextMenuBtn[3].addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.useItem();
			}

		});
		Gui.contextMenuBtn[4] = new TextButton("Free hands", Gui.skin);
		Gui.contextMenuBtn[4].addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.freeHands();
			}

		});
		Gui.contextMenuBtn[1] = new TextButton("Fight", Gui.skin);
		Gui.contextMenuBtn[1].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse();
				//TODO fight
			}
		});
		Gui.contextMenuBtn[2] = new TextButton("Turn", Gui.skin);
		Gui.contextMenuBtn[2].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse();
			}
		});

		while (cc < 5) {
			Gui.gamecontext.add(Gui.contextMenuBtn[cc]).width(100.0F).height(20.0F);
			Gui.gamecontext.row();
			cc++;
		}
		Gui.gamecontext.setVisible(false);

		cc = 0;
		while (cc < 5) {
			Gui.mouselabel[cc] = new Label("!", Gui.skin);
			Gui.mousemenu.add(Gui.mouselabel[cc]).width(100.0F);
			Gui.mousemenu.row();
			cc++;
		}
		Gui.menu = new Stage();
		Gui.craft = new Stage();
		Gui.craftbl = new Table();
		Gui.inputField = new TextField("", Gui.skin);
		Gui.inputField.setMessageText("");
		Gui.inputField.setFocusTraversal(true);
		Gui.inputField.setTextFieldListener(new TextField.TextFieldListener() {
			public void keyTyped(TextField textField, char key) {
				if (key == Keys.ESCAPE) {
					Gui.inputField.setText("");
					Gui.overlay.unfocus(Gui.inputField);
				}

			}
		});
		Table logtbl = new Table(Gui.skin).align(10);
		logtbl.setFillParent(true);
		int tpm = 0;
		Gui.log = new Label[LOG_LENGTH];
		int cxxc = 0;
		while (cxxc < LOG_LENGTH) {
			Gui.log[cxxc] = new Label(" ", Gui.skin);
			cxxc++;
		}
		while (tpm < LOG_LENGTH) {
			logtbl.add(Gui.log[tpm]).width(200.0F);
			logtbl.row();
			tpm++;
		}
		logtbl.add(Gui.inputField).width(200).height(30);
		Gui.inputField.setVisible(false);
		tpm = 0;
		Table ovtbl = new Table(Gui.skin).align(18);
		MadSand.dialog = new Table(Gui.skin).align(1);
		Gui.acceptD = new TextButton("Accept", Gui.skin);
		Gui.refuseD = new TextButton("Refuse", Gui.skin);
		MadSand.maindialog = new Table(Gui.skin).align(4);
		NinePatch patch = new NinePatch(new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/bg.png")), 3, 3, 3, 3);
		NinePatchDrawable background = new NinePatchDrawable(patch);
		NinePatch ptc = new NinePatch(new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/darkness.png")), 3, 3, 3, 3);
		bck = new NinePatchDrawable(ptc);
		MadSand.maindialog.setFillParent(true);
		Gui.dialMSG = new Label("Test", Gui.skin);
		Gui.dialMSG.setWrap(true);
		MadSand.dialog.setBackground(background);
		MadSand.dialog.add(Gui.dialMSG).width(500.0F).height(200.0F);
		MadSand.dialog.row();
		MadSand.dialog.add(Gui.acceptD).width(500.0F).height(50.0F);
		MadSand.dialog.row();
		MadSand.dialog.add(Gui.refuseD).width(500.0F).height(50.0F);
		Table ovstatTbl = new Table();
		ovtbl.setFillParent(true);
		ovstatTbl.setFillParent(true);
		ovstatTbl.align(Align.topRight);
		int count = 0;
		while (count < OVSTAT_COUNT) {
			Gui.overlayStatLabels[count] = new Label(" ", skin);
			Gui.overlayStatLabels[count].setWrap(false);
			ovstatTbl.add(Gui.overlayStatLabels[count]).width(165);
			count++;
		}
		Gui.overlay.addActor(ovstatTbl);
		ScrollPane dsp = new ScrollPane(MadSand.dialog, Gui.skin);
		Gui.darkness = new Table();
		Gui.darkness.setBackground(bck);
		Gui.darkness.setFillParent(true);
		Gui.darkness.setVisible(false);
		MadSand.maindialog.add(dsp);
		MadSand.maindialog.setVisible(false);
		Gui.overlay.addActor(Gui.darkness);

		Gui.overlay.addActor(ovtbl);
		Gui.overlay.addActor(MadSand.maindialog);
		Gui.overlay.addActor(logtbl);
		Gui.overlay.addActor(Gui.mousemenu);
		Gui.overlay.addActor(Gui.gamecontext);
		Gui.overlay.addListener(new ClickListener(1) {
			public void clicked(InputEvent event, float x, float y) {
				if (MadSand.state == GameState.GAME) {
					if (Gui.mousemenu.isVisible()) {
						Gui.mousemenu.setVisible(false);
						Gui.gamecontext.setVisible(true);
						Gui.gamecontext.setPosition(MadSand.mx + 50, MadSand.my - 30);
						MadSand.wclickx = MadSand.wmx;
						MadSand.wclicky = MadSand.wmy;
						contextMenuActive = true;
					} else {
						Gui.mousemenu.setVisible(true);
						Gui.gamecontext.setVisible(false);
						contextMenuActive = false;
					}
				}
			}
		});
		exitToMenuBtn = new TextButton("Exit to menu", skin);
		craftBtn = new TextButton("Crafting", skin);
		TextButton backBtn = new TextButton("Back", skin);
		final TextButton newGameBtn = new TextButton("New game", skin);
		resumeBtn = new TextButton("Resume game", skin);
		resumeBtn.setVisible(false);
		TextButton settingsButton = new TextButton("Settings", skin);
		TextButton exitBtn = new TextButton("Exit", skin);
		TextButton loadGameBtn = new TextButton("Load game", skin);

		Label title = new Label("MadSand: Fallen. Rise From Dust\n", skin);
		title.setPosition(Gdx.graphics.getWidth() / 2 - title.getWidth() / 2.0F, Gdx.graphics.getHeight() / 2 + 100);

		int cg = 0;
		craftbtn = new TextButton[MadSand.CRAFTABLES];

		int perRow = 3;
		while (cg < MadSand.CRAFTABLES) {
			craftbtn[cg] = new TextButton(ItemProp.name.get(MadSand.craftableid[cg]), skin);
			craftbl.add(Gui.craftbtn[cg]).width(250.0F);
			craftbl.add(new Label(" " + Item.queryToName(ItemProp.recipe.get(MadSand.craftableid[cg])),
					skin))/* .align(8) */;
			if ((cg + 1) % perRow == 0)
				craftbl.row();
			final int ssa = cg;
			craftbtn[ssa].addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					World.player.craftItem(MadSand.craftableid[ssa]);
				}
			});
			cg++;
		}
		craftbl.row();
		// craft.setDebugAll(true);
		Table backTable = new Table();
		backTable.align(Align.bottom);
		backTable.add(backBtn).fillY().expandY();
		backTable.setWidth(Gdx.graphics.getWidth());
		backBtn.align(Align.center);
		backBtn.setOrigin(Align.center);
		backBtn.pad(10);
		backBtn.setWidth(250);
		backBtn.setHeight(50);
		scroll = new ScrollPane(Gui.craftbl);
		scroll.setSize(1280.0F, 720.0F);
		craft.addActor(Gui.scroll);
		craft.addActor(backTable);
		craftBtn.setHeight(82.0F);
		craftBtn.align(16);
		craftBtn.setWidth(250.0F);
		ovtbl.row();
		ovtbl.row();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add(Gui.craftBtn).width(200.0F).row();
		Gui.craftBtn.setVisible(false);
		ovtbl.add();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add(Gui.exitToMenuBtn).width(200.0F).row();
		int aa = 0;
		while (aa < ITEM_DISPLAY_SLOTS) {
			ovtbl.add();
			ovtbl.add();
			ovtbl.add();
			ovtbl.add(Gui.equip[aa]).width(80.0F).align(Align.right).row();
			aa++;
		}

		Gui.exitToMenuBtn.setVisible(false);

		TextButton respawnButton = new TextButton("Respawn", Gui.skin);
		Table tab = new Table();
		dieLabel = new Label("", Gui.skin);
		dieLabel.setAlignment(Align.center);
		tab.add(dieLabel).width(500.0F);
		tab.row();
		tab.add(new Label("", Gui.skin)).width(500.0F);
		tab.row();
		tab.add(respawnButton).width(500.0F).row();
		Gui.dead = new Stage();
		Gui.dead.addActor(darkness);
		tab.setFillParent(true);
		Gui.dead.addActor(tab);

		getVersion();

		Table menuTbl = new Table();
		menuTbl.setFillParent(true);
		menuTbl.setBackground(bck);
		menuTbl.add(new Label("MadSand: Fallen. Rise From Dust\n", Gui.skin));
		menuTbl.row();
		menuTbl.add(resumeBtn).width(DEFWIDTH);
		menuTbl.row();
		menuTbl.add(newGameBtn).width(DEFWIDTH);
		menuTbl.row();
		menuTbl.add(loadGameBtn).width(DEFWIDTH);
		menuTbl.row();
		menuTbl.add(settingsButton).width(DEFWIDTH);
		menuTbl.row();
		menuTbl.add(exitBtn).width(DEFWIDTH);
		menuTbl.row();
		menuTbl.add(verlbl).width(DEFWIDTH);
		Gui.menu.addActor(menuTbl);

		respawnButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				darkness.setVisible(false);
				World.player.respawn();
			}
		});

		settingsButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				drawSettingsDialog();
			}
		});

		exitBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Utils.out("Bye!");
				System.exit(0);
			}
		});

		Gui.acceptD.addListener(new ChangeListener() { // TODO remove this ass shit
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.dialogClosed = true;
				MadSand.dialogresult = 0;
				MadSand.maindialog.setVisible(false);
			}
		});

		Gui.refuseD.addListener(new ChangeListener() { // TODO same
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.dialogClosed = true;
				MadSand.dialogresult = 1;
				MadSand.maindialog.setVisible(false);
			}

		});

		newGameBtn.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				createWorldDialog();
			}

		});

		resumeBtn.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.state = GameState.GAME;
				Gdx.input.setInputProcessor(Gui.overlay);
			}

		});

		loadGameBtn.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				loadWorldDialog();
			}

		});

		craftBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gui.craft.setScrollFocus(Gui.scroll);
				MadSand.switchStage(GameState.CRAFT, Gui.craft);
			}
		});

		exitToMenuBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gui.resumeBtn.setVisible(true);
				MadSand.switchStage(GameState.NMENU, Gui.menu);
			}
		});

		backBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.switchStage(GameState.INVENTORY, Gui.overlay);
			}
		});

		initLaunchMenu();
		initWmenu();
		Gdx.input.setInputProcessor(menu);
	}



	static BitmapFont createFont(int size) {
		BitmapFont font = new BitmapFont();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
				Gdx.files.local(MadSand.SAVEDIR + MadSand.FONT_PATH));
		FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
		param.characters = MadSand.FONT_CHARS;
		param.size = size;
		param.color = Color.WHITE;
		param.borderWidth = 0.9f;
		param.borderColor = Color.BLACK;
		font = generator.generateFont(param);
		generator.dispose();
		font.getData().markupEnabled = true;
		return font;
	}

	public static boolean contextMenuActive = false;
	public static boolean inventoryActive = false;

}