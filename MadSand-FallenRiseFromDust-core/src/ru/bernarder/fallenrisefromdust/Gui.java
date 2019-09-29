package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Random;

public class Gui {
	static final float DEFWIDTH = 250f;
	static TextButton resumeButton;
	public static Label verlbl;
	static NinePatchDrawable transparency;
	static int LOG_LENGTH = 20;

	public static void createBasicSkin() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
				Gdx.files.local(MadSand.SAVEDIR + MadSand.FONT_PATH));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.characters = MadSand.FONT_CHARS;
		parameter.size = 16;
		parameter.borderWidth = 0.9f;
		parameter.borderColor = Color.BLACK;
		parameter.color = Color.WHITE;
		BitmapFont font = generator.generateFont(parameter);
		font.getData().markupEnabled = true;
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
	}

	public static ThreadedUtils tr;
	static Dialog dialog;

	static int ACCUR, CONSTITUTION, ATK, STAMINA, DEX, LUCK, INT;
	static final int STAT_SUM = 25;
	static final int STAT_RAND_MAX = 4;

	static Label CNT;
	static Label STM;
	static Label ATKl;
	static Label ACC;
	static Label IN;
	static Label LK;
	static Label DX;

	static void refreshOverlay() {
		overlayStatLabels[0].setText("HP: " + World.player.stats.hp + "/" + World.player.stats.mhp);
		overlayStatLabels[1].setText("Level: " + World.player.stats.lvl);
		overlayStatLabels[2].setText("Experience: " + World.player.stats.exp + "/" + World.player.stats.requiredexp);
		overlayStatLabels[3].setText("Food: " + World.player.stats.food + " / " + World.player.stats.maxFood);
		overlayStatLabels[4].setText("Hand: " + ItemProp.name.get(World.player.stats.hand));
	}

	public static void showStatsWindow() {
		final Dialog dialog = new Dialog("", Gui.skin);
		dialog.text(World.player.stats.name);
		TextButton ok = new TextButton("Close", Gui.skin);
		MadSand.charcrt = true;
		ok.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.charcrt = false;
				dialog.remove();
			}

		});
		dialog.setBackground(bck);
		dialog.setMovable(true);
		dialog.add(new Label("", Gui.skin));
		ATKl.setText("Strength: " + World.player.stats.str);
		STM.setText("Stamina: " + World.player.stats.maxstamina / 10 + " (" + World.player.stats.stamina + "/"
				+ World.player.stats.maxstamina + ")");
		ACC.setText("Accuracy: " + World.player.stats.accur);
		CNT.setText("Constitution: " + World.player.stats.mhp / 10 + " (" + World.player.stats.hp + "/"
				+ World.player.stats.mhp + ")");
		IN.setText("Intelligence: " + World.player.stats.intelligence);
		LK.setText("Luck: " + World.player.stats.luck);
		DX.setText("Dexterity: " + World.player.stats.dexterity);
		dialog.row();
		dialog.add(new Label(
				"Level: " + World.player.stats.lvl + " (" + World.player.stats.exp + "/"
						+ World.player.stats.requiredexp + ")",
				Gui.skin))/* .width(Gdx.graphics.getWidth() / 4).row() */;
		dialog.row();
		dialog.add(new Label("", Gui.skin)).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(new Label("Stats:", Gui.skin)).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(CNT).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(STM).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(ATKl).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(ACC).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(IN).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(LK).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(DX).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(new Label("", Gui.skin)).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(new Label("Skills:", Gui.skin)).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(new Label("Woodcutting: " + World.player.stats.woodcutterskill[0], Gui.skin))
		/* .width(Gdx.graphics.getWidth() / 4).row() */;
		dialog.row();
		dialog.add(new Label("Harvesting: " + World.player.stats.harvestskill[0],
				Gui.skin))/*
							 * .width(Gdx.graphics.getWidth() / 4) .row()
							 */;
		dialog.row();
		dialog.add(new Label("Mining: " + World.player.stats.miningskill[0],
				Gui.skin))/*
							 * .width(Gdx.graphics.getWidth() / 4) .row()
							 */;
		dialog.row();
		dialog.add(new Label("Survival: " + World.player.stats.survivalskill[0],
				Gui.skin))/*
							 * .width(Gdx.graphics.getWidth() / 4) .row()
							 */;
		dialog.row();
		dialog.add(ok).width(Gdx.graphics.getWidth() / 4).row();
		dialog.show(Gui.overlay);
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

	static void drawOkDialog(String msg) {
		final Dialog dialog = new Dialog(" ", Gui.skin);
		dialog.text(msg);
		dialog.row();
		TextButton cbtn = new TextButton("Ok", Gui.skin);
		dialog.add(cbtn).width(Gdx.graphics.getWidth() / 4).row();
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				dialog.remove();
			}

		});
		dialog.show(menu);
	}

	static void setStats() {
		World.player.stats.str = ATK;
		World.player.stats.stamina = STAMINA * 5;
		World.player.stats.maxstamina = STAMINA * 5;
		World.player.stats.accur = ACCUR;
		World.player.stats.hp = CONSTITUTION * 10;
		World.player.stats.mhp = CONSTITUTION * 10;
		World.player.stats.luck = LUCK;
		World.player.stats.dexterity = DEX;
		World.player.stats.intelligence = INT;
	}

	static void rollStats() {
		ACCUR = Utils.rand(1, STAT_RAND_MAX);
		CONSTITUTION = Utils.rand(1, STAT_RAND_MAX);
		ATK = Utils.rand(1, STAT_RAND_MAX);
		STAMINA = Utils.rand(1, STAT_RAND_MAX);
		LUCK = Utils.rand(1, STAT_RAND_MAX);
		INT = Utils.rand(1, STAT_RAND_MAX);
		DEX = Utils.rand(1, STAT_RAND_MAX);
		setStats();
	}

	static void setStatLabels() {
		ATKl.setText("Strength: " + ATK);
		STM.setText("Stamina: " + STAMINA);
		ACC.setText("Accuracy: " + ACCUR);
		CNT.setText("Constitution: " + CONSTITUTION);
		IN.setText("Intelligence: " + INT);
		LK.setText("Luck: " + LUCK);
		DX.setText("Dexterity: " + DEX);
	}

	static void createCharDialog() {
		rollStats();
		MadSand.charcrt = true;

		String msg = "Character creation";
		final Dialog dialog = new Dialog(" ", Gui.skin);
		dialog.setMovable(true);
		dialog.text(msg);
		dialog.row();
		final TextField nameField = new TextField("Johnny", Gui.skin);
		setStatLabels();
		dialog.add(new Label("Character name:", Gui.skin)).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(nameField).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(CNT).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(STM).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(ATKl).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(ACC).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(IN).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(LK).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(DX).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		TextButton rbtn = new TextButton("Reroll", Gui.skin);
		TextButton cbtn = new TextButton("Create", Gui.skin);
		dialog.add(rbtn).width(Gdx.graphics.getWidth() / 4).row();
		dialog.row();
		dialog.add(cbtn).width(Gdx.graphics.getWidth() / 4).row();
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				if (!nameField.getText().trim().equals("")) {
					World.player.setName(nameField.getText());
					MadSand.charcrt = false;
					setStats();
					World.player.reinit();
					dialog.remove();
				}
			}

		});
		rbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				rollStats();
				setStatLabels();
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
		dialog.add(renderslide).width(Gdx.graphics.getWidth() / 4).row();

		dialog.add(cbtn).width(Gdx.graphics.getWidth() / 4).row();
		dialog.add(cancel).width(Gdx.graphics.getWidth() / 4).row();

		renderslide.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderv.setText("Render radius (" + (int) renderslide.getValue() + ")");
			}
		});
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.setParams(Math.round(renderslide.getValue()));
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
						drawOkDialog("Unable to load world " + sa);

				}
			});
			i++;
		}
		TextButton cbtn = new TextButton("Cancel", Gui.skin);
		if (slots == 0)
			ldialog.add(new TextButton("No game sessions to load", Gui.skin)).width(Gdx.graphics.getWidth() / 4).row();
		ldialog.add(cbtn).width(Gdx.graphics.getWidth() / 4).row();
		ldialog.add(new Label("\n", Gui.skin)).width(Gdx.graphics.getWidth() / 4).row();
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

					MadSand.state = GameState.WORLDGEN;
					new ThreadedUtils().initialWorldGen.start();
					Gdx.input.setInputProcessor(Gui.overlay);
					World.player.x = new Random().nextInt(World.MAPSIZE);
					World.player.y = new Random().nextInt(World.MAPSIZE);
					World.player.globalPos.x = (World.player.x * MadSand.TILESIZE);
					World.player.globalPos.y = (World.player.y * MadSand.TILESIZE);
					Gui.exitButton.setVisible(false);
					Gui.craftButton.setVisible(false);
					Utils.invent = false;
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
		if (!GameSaver.getExternal("MadSandData/version.dat").equals(""))
			MadSand.VER = "[GREEN]b-" + (GameSaver.getExternal(MadSand.VERFILE));
		else
			MadSand.VER = "[GREEN]Version file not found";
		verlbl = new Label(MadSand.VER, Gui.skin);
	}

	static final int OVSTAT_COUNT = 6;

	static void initmenu() {
		Gui.overlayStatLabels = new Label[OVSTAT_COUNT];

		CNT = new Label("Constitution: " + CONSTITUTION, Gui.skin);
		STM = new Label("Stamina: " + STAMINA, Gui.skin);
		ATKl = new Label("Strength: " + ATK, Gui.skin);
		ACC = new Label("Accuracy: " + ACCUR, Gui.skin);
		IN = new Label("Intelligence: " + INT, Gui.skin);
		LK = new Label("Luck: " + LUCK, Gui.skin);
		DX = new Label("Dexterity: " + DEX, Gui.skin);

		Gui.gamecontext = new Table(Gui.skin);
		Gui.contextMenuBtn = new TextButton[5];
		Gui.mousemenu = new Table(Gui.skin);
		Gui.mousemenu.setVisible(true);
		Gui.mouselabel = new Label[5];
		int cc = 0;
		Gui.contextMenuBtn[0] = new TextButton("Interact", Gui.skin);
		Gui.contextMenuBtn[0].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				lookAtMouse();
				World.player.interact(World.player.stats.look);
			}

		});
		Gui.contextMenuBtn[3] = new TextButton("Use item", Gui.skin);
		Gui.contextMenuBtn[3].addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
			}

		});
		Gui.contextMenuBtn[4] = new TextButton("Free hands", Gui.skin);
		Gui.contextMenuBtn[4].addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.stats.hand = 0;
				MadSand.print("You freed your hands.");
				Gui.equip[4].setDrawable(new SpriteDrawable(new Sprite(Utils.cursor)));
			}

		});
		Gui.contextMenuBtn[1] = new TextButton("Fight", Gui.skin);
		Gui.contextMenuBtn[1].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				lookAtMouse();
			}
		});
		Gui.contextMenuBtn[2] = new TextButton("Turn", Gui.skin);
		Gui.contextMenuBtn[2].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				lookAtMouse();
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
		Gui.overlay = new Stage();
		// overlay.setDebugAll(true);
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
			ovstatTbl.add(Gui.overlayStatLabels[count]).width(170);
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
				if (!MadSand.dontlisten) {
					if (MadSand.state == GameState.GAME) {
						if (Gui.mousemenu.isVisible()) {
							Gui.mousemenu.setVisible(false);
							Gui.gamecontext.setVisible(true);
							Gui.gamecontext.setPosition(MadSand.mx + 50, MadSand.my - 30);
							MadSand.wclickx = MadSand.wmx;
							MadSand.wclicky = MadSand.wmy;
							MadSand.contextopened = true;
						} else {
							Gui.mousemenu.setVisible(true);
							Gui.gamecontext.setVisible(false);
							MadSand.contextopened = false;
						}
					}
				}
			}
		});
		Gui.exitButton = new TextButton("Exit to menu", Gui.skin);
		Gui.craftButton = new TextButton("Crafting", Gui.skin);
		TextButton back = new TextButton("Back", Gui.skin);
		final TextButton newGameButton = new TextButton("New game", Gui.skin);
		resumeButton = new TextButton("Resume game", Gui.skin);
		resumeButton.setVisible(false);
		TextButton settingsButton = new TextButton("Settings", Gui.skin);
		TextButton exitButton = new TextButton("Exit", Gui.skin);
		TextButton loadGame = new TextButton("Load game", Gui.skin);

		Label title = new Label("MadSand: Fallen. Rise From Dust\n", Gui.skin);
		title.setPosition(Gdx.graphics.getWidth() / 2 - title.getWidth() / 2.0F, Gdx.graphics.getHeight() / 2 + 100);

		int cg = 0;
		Gui.craftbtn = new TextButton[MadSand.CRAFTABLES];

		while (cg < MadSand.CRAFTABLES) {
			Gui.craftbtn[cg] = new TextButton(ItemProp.name.get(MadSand.craftableid[cg]), Gui.skin);
			Gui.craftbl.add(Gui.craftbtn[cg]).width(250.0F);
			Gui.craftbl.add(new Label(" " + Item.queryToName(ItemProp.recipe.get(MadSand.craftableid[cg])),
					Gui.skin))/* .align(8) */;
			Gui.craftbl.row();
			final int ssa = cg;
			Gui.craftbtn[ssa].addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					CraftUtils.craftItem(MadSand.craftableid[ssa]);
					Utils.out("Craft request for " + MadSand.craftableid[ssa]);
				}
			});
			cg++;
		}

		Gui.craftbl.add(back).width(250.0F);
		Gui.scroll = new ScrollPane(Gui.craftbl);
		Gui.scroll.setSize(1280.0F, 720.0F);
		Gui.craft.addActor(Gui.scroll);
		Gui.craftButton.setHeight(82.0F);
		Gui.craftButton.align(16);
		Gui.craftButton.setWidth(250.0F);
		ovtbl.row();
		ovtbl.row();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add(Gui.craftButton).width(200.0F).row();
		Gui.craftButton.setVisible(false);
		ovtbl.add();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add(Gui.exitButton).width(200.0F).row();
		int aa = 0;
		while (aa < 4) {
			ovtbl.add();
			ovtbl.add();
			ovtbl.add();
			ovtbl.add(Gui.equip[aa]).width(80.0F).align(Align.right).row();
			aa++;
		}
		ovtbl.add();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add(Gui.equip[4]).width(80.0F).align(Align.right).row();

		Gui.exitButton.setVisible(false);

		TextButton RespawnButton = new TextButton("Respawn", Gui.skin);
		Table tab = new Table();
		Label dieLabel = new Label("You died", Gui.skin);
		dieLabel.setAlignment(Align.center);
		tab.add(dieLabel).width(500.0F);
		tab.row();
		tab.add(new Label("", Gui.skin)).width(500.0F);
		tab.row();
		tab.add(RespawnButton).width(500.0F).row();
		Gui.dead = new Stage();
		Gui.dead.addActor(darkness);
		tab.setFillParent(true);
		Gui.dead.addActor(tab);

		getVersion();
		Table menut = new Table();
		menut.setFillParent(true);
		menut.setBackground(bck);
		menut.add(new Label("MadSand: Fallen. Rise From Dust\n", Gui.skin));
		menut.row();
		menut.add(resumeButton).width(DEFWIDTH);
		menut.row();
		menut.add(newGameButton).width(DEFWIDTH);
		menut.row();
		menut.add(loadGame).width(DEFWIDTH);
		menut.row();
		menut.add(settingsButton).width(DEFWIDTH);
		menut.row();
		menut.add(exitButton).width(DEFWIDTH);
		menut.row();
		menut.add(verlbl).width(DEFWIDTH);
		Gui.menu.addActor(menut);
		RespawnButton.addListener(new ChangeListener() {
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
		exitButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Utils.out("Bye!");
				System.exit(0);
			}
		});
		Gui.acceptD.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.dialogflag = true;
				MadSand.dialogresult = 0;
				MadSand.maindialog.setVisible(false);
			}
		});
		Gui.refuseD.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.dialogflag = true;
				MadSand.dialogresult = 1;
				MadSand.maindialog.setVisible(false);
			}

		});
		newGameButton.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				createWorldDialog();
			}

		});
		resumeButton.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.state = GameState.GAME;
				Gdx.input.setInputProcessor(Gui.overlay);
			}

		});
		loadGame.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				loadWorldDialog();
			}

		});
		Gui.craftButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gdx.input.setInputProcessor(Gui.craft);
				Gui.craft.setScrollFocus(Gui.scroll);
				MadSand.state = GameState.CRAFT;
			}
		});
		Gui.exitButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gui.resumeButton.setVisible(true);
				Gdx.input.setInputProcessor(Gui.menu);
				MadSand.state = GameState.NMENU;
			}
		});
		back.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gdx.input.setInputProcessor(Gui.overlay);
				MadSand.state = GameState.INVENTORY;
			}
		});
		initLaunchMenu();
		initWmenu();
		Gdx.input.setInputProcessor(menu);
	}

	static Direction lookAtMouse() {
		if (MadSand.wclickx > World.player.x) {
			World.player.stats.look = Direction.RIGHT;
			Utils.turn(World.player.stats.look);
			Utils.isInFront();
		} else if (MadSand.wclickx < World.player.x) {
			World.player.stats.look = Direction.LEFT;
			Utils.turn(World.player.stats.look);
			Utils.isInFront();
		} else if (MadSand.wclicky > World.player.y) {
			World.player.stats.look = Direction.UP;
			Utils.turn(World.player.stats.look);
			Utils.isInFront();
		} else if (MadSand.wclicky < World.player.y) {
			World.player.stats.look = Direction.DOWN;
			Utils.turn(World.player.stats.look);
			Utils.isInFront();
		}
		return World.player.stats.look;
	}

	static Image[] equip;
	static Table darkness;
	static com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scroll;
	static Table gamecontext;
	static TextButton[] contextMenuBtn;
	static Table mousemenu;
	static Label[] mouselabel;
	public static TextButton craftButton;
	static TextButton acceptD;
	static TextButton refuseD;
	static Label dialMSG;
	static Stage dead;
	static TextField inputField;
	static Label[] overlayStatLabels;
	static Label[] log;
	static Stage overlay;
	static Table craftbl;
	static Skin skin;
	static Stage craft;
	static TextButton[] craftbtn;
	static Stage menu;
	public static BitmapFont font;
	public static BitmapFont font1;
	public static TextButton exitButton;
}