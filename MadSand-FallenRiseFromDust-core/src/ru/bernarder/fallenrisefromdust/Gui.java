package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import ru.bernarder.fallenrisefromdust.containers.Pair;
import ru.bernarder.fallenrisefromdust.dialog.GameDialog;
import ru.bernarder.fallenrisefromdust.entities.Npc;
import ru.bernarder.fallenrisefromdust.entities.Player;
import ru.bernarder.fallenrisefromdust.entities.Stats;
import ru.bernarder.fallenrisefromdust.entities.inventory.Item;
import ru.bernarder.fallenrisefromdust.enums.GameState;
import ru.bernarder.fallenrisefromdust.enums.ItemType;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.map.Map;
import ru.bernarder.fallenrisefromdust.map.MapObject;
import ru.bernarder.fallenrisefromdust.map.Tile;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;
import ru.bernarder.fallenrisefromdust.world.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.security.MessageDigest;

/*
 * If you opened this file by accident, immediately close it, i don't want to be responsible for consequences.
 * This file can cause eye bleeding and sudden but very painful death. Beware.
 */
public class Gui {
	public static boolean gameUnfocused = false;
	public static boolean inventoryActive = false;
	public static boolean dialogActive = false;

	public static final String LINEBREAK = "\n";
	static final float DEFWIDTH = 250f;
	static final int LOG_LENGTH = 20;
	public static final int EQ_SLOTS = 5;
	static float defLblWidth = Gdx.graphics.getWidth() / 4;
	public static final float ACTION_TBL_YPOS = Gdx.graphics.getHeight() / 6f;

	public static String noticeMsgColor = "[#16E1EA]";

	static NinePatchDrawable transparency;

	static Image[] equip;

	public static Table darkness;
	public static Table gamecontext;
	public static Table mousemenu;
	public static Table craftbl;
	public static Table actionTbl;

	static ScrollPane scroll;

	static Label[] overlayStatLabels;
	static Label[] log;
	public static Label mouselabel;
	public static Label verlbl;

	static Stage menu;
	public static Stage dead;
	public static Stage craft;
	public static Stage overlay;

	static TextField inputField;

	public static Skin skin;

	static TextButton[] craftbtn;
	public static TextButton exitToMenuBtn;
	public static TextButton craftBtn;
	static TextButton[] contextMenuBtn;
	static TextButton resumeBtn;

	public static TextButton interactBtn;

	static BitmapFont font;
	static BitmapFont fontMedium;
	static BitmapFont fontBig;

	public static InputListener inGameBtnListener;
	public static ChangeListener npcInteractListener;
	public static ChangeListener objInteractListener;

	public static NinePatchDrawable darkBackground;
	public static NinePatchDrawable darkBackgroundSizeable;
	public static NinePatchDrawable dialogBackground;

	public static void createBasicSkin() { // TODO: Remove this shit and move skin to json for fucks sake
		font = createFont(16);
		fontMedium = createFont(20);
		fontBig = createFont(24);

		skin = new Skin();
		skin.add("default", font);

		Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 12, Pixmap.Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("background", new Texture(pixmap));

		Slider.SliderStyle slst = new Slider.SliderStyle();
		slst.background = skin.newDrawable("background", Color.DARK_GRAY);

		Drawable knob = skin.newDrawable("background", Color.GRAY);
		knob.setMinWidth(20);
		slst.knob = knob;
		skin.add("default-horizontal", slst);

		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("background", Color.GRAY);
		textButtonStyle.down = skin.newDrawable("background", Color.DARK_GRAY);
		textButtonStyle.over = skin.newDrawable("background", Color.LIGHT_GRAY);
		textButtonStyle.font = skin.getFont("default");
		textButtonStyle.disabled = skin.newDrawable("background", Color.BLACK);
		skin.add("default", textButtonStyle);

		Label.LabelStyle labelStyle = new Label.LabelStyle();
		labelStyle.font = font;
		labelStyle.fontColor = Color.WHITE;
		skin.add("default", labelStyle);

		transparency = new NinePatchDrawable(
				new NinePatch(new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/transparency.png"))));

		Window.WindowStyle ws = new Window.WindowStyle();
		NinePatch patch = new NinePatch(new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/bg.png")));
		dialogBackground = new NinePatchDrawable(patch);
		dialogBackground.setMinHeight(50);
		dialogBackground.setMinWidth(100);
		ws.background = skin.newDrawable("background", Color.LIGHT_GRAY);
		;
		ws.stageBackground = transparency;
		ws.titleFontColor = Color.WHITE;
		ws.titleFont = fontMedium;
		skin.add("default", ws);

		TextField.TextFieldStyle tx = new TextField.TextFieldStyle();
		tx.font = font;
		tx.fontColor = Color.WHITE;
		tx.background = skin.newDrawable("background", Color.DARK_GRAY);
		tx.background.setMinHeight(35.0F);
		tx.selection = skin.newDrawable("background", Color.LIGHT_GRAY);
		tx.cursor = skin.newDrawable("background", Color.GRAY);
		tx.cursor.setMinWidth(1.0F);
		tx.cursor.setMinHeight(tx.background.getMinHeight());
		skin.add("default", tx);

		ScrollPane.ScrollPaneStyle spx = new ScrollPane.ScrollPaneStyle();
		skin.add("default", spx);

		NinePatch ptc = new NinePatch(new Texture(Gdx.files.local(MadSand.SAVEDIR + "misc/darkness.png")), 3, 3, 3, 3);
		darkBackground = new NinePatchDrawable(ptc);

		darkBackgroundSizeable = new NinePatchDrawable(ptc);
		darkBackgroundSizeable.setMinHeight(0);
		darkBackgroundSizeable.setMinWidth(0);

		TextTooltip.TextTooltipStyle txtool = new TextTooltip.TextTooltipStyle();
		txtool.background = darkBackground;
		txtool.label = labelStyle;
		skin.add("default", txtool);

		overlay = new Stage();
	}

	static Dialog dialog;

	static Label statsLbl, skillsLbl;
	static Label conStatLbl, strStatLbl, accStatLbl, intStatLbl, luckStatLbl, dexStatLbl, statSumLbl;
	static Label hpStatLbl, staminaStatLbl;

	static int ITEM_DISPLAY_HEAD = 0;
	static int ITEM_DISPLAY_CHEST = 1;
	static int ITEM_DISPLAY_LEGS = 2;
	static int ITEM_DISPLAY_SHIELD = 3;
	static int ITEM_DISPLAY_HOLDING = 4;
	static int ITEM_DISPLAY_SLOTS = 5;

	public static void setHandDisplay(int id) {
		Utils.out("Setting hand display to " + id);
		Drawable img = Resources.noEquip;
		if (id != 0)
			img = new TextureRegionDrawable(Resources.item[id]);
		equip[ITEM_DISPLAY_HOLDING].setDrawable((Drawable) img);
	}

	public static void refreshEquipDisplay() {
		Stats stats = World.player.stats;
		Drawable img = Resources.noEquip;

		if (stats.headEquip != Item.nullItem)
			img = new TextureRegionDrawable(Resources.item[stats.headEquip.id]);
		equip[ITEM_DISPLAY_HEAD].setDrawable((Drawable) img);

		img = Resources.noEquip;
		if (stats.chestEquip != Item.nullItem)
			img = new TextureRegionDrawable(Resources.item[stats.chestEquip.id]);
		equip[ITEM_DISPLAY_CHEST].setDrawable((Drawable) img);

		img = Resources.noEquip;
		if (stats.legsEquip != Item.nullItem)
			img = new TextureRegionDrawable(Resources.item[stats.legsEquip.id]);
		equip[ITEM_DISPLAY_LEGS].setDrawable((Drawable) img);

	}

	public static void refreshOverlay() {
		overlayStatLabels[0].setText("HP: " + World.player.stats.hp + "/" + World.player.stats.mhp);
		overlayStatLabels[1].setText("LVL: " + World.player.stats.skills.getLvl(Skill.Level));
		overlayStatLabels[2].setText("XP: " + World.player.stats.skills.getExpString(Skill.Level));
		overlayStatLabels[3].setText("Food: " + World.player.stats.food + " / " + World.player.stats.maxFood);
		overlayStatLabels[4].setText("Hand: " + World.player.stats.hand.name);
	}

	private final static float headerLeftPadding = -45f;
	private final static float headerBottomPadding = 5f;
	private final static float headerScale = 1.11f;

	public static GameDialog statWindow;

	public static void showStatsWindow() {
		if (statWindow != null) {
			statWindow.remove();
			statWindow = null;
			return;
		}

		statWindow = new GameDialog(overlay);
		Label nameLbl = new Label(World.player.stats.name, skin);
		Label levelLbl = new Label("Level: " + World.player.stats.skills.getLvl(Skill.Level) + " ("
				+ World.player.stats.skills.getExpString(Skill.Level) + ")", skin);

		levelLbl.setFontScale(headerScale);
		nameLbl.setFontScale(headerScale);
		statWindow.add(nameLbl).row();
		TextButton ok = new TextButton("Close", skin);
		ok.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				statWindow.remove();
				statWindow.clearActions();
			}

		});
		statWindow.setBackground(darkBackground);
		statWindow.setMovable(true);
		statWindow.add(new Label("", skin));
		refreshStatLabels();
		statWindow.row();
		statWindow.add(levelLbl);
		statWindow.row();
		statWindow.add(new Label("", skin)).width(defLblWidth).row();
		statWindow.row();
		statWindow.add(statsLbl).width(defLblWidth).padLeft(headerLeftPadding).padBottom(headerBottomPadding).row();
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
		statWindow.add(skillsLbl).width(defLblWidth).padLeft(headerLeftPadding).padBottom(headerBottomPadding).row();
		statWindow.row();
		Skill skill;
		Label skillLbl;

		for (int i = 1; i < Skill.len(); ++i) {
			skill = Skill.get(i);
			if (skill == Skill.Level)
				continue;
			skillLbl = new Label(skill + ": " + World.player.stats.skills.getLvlString(skill), skin);
			statWindow.add(skillLbl).width(defLblWidth).row();
			statWindow.row();
		}

		statWindow.add(ok).width(defLblWidth).padTop(35).padBottom(5).row();
		statWindow.show(overlay);
	}

	static void initLaunchMenu() {
		MadSand.state = GameState.NMENU;
		Gdx.input.setInputProcessor(menu);
	}

	static Stage worldg;
	static Stage loadg;
	static Stage gotodg;
	static Label wlbl;

	static void initWmenu() {
		worldg = new Stage();
		wlbl = new Label("Generating your world...", skin);
		wlbl.setAlignment(1);
		Table tbl = new Table();
		tbl.setFillParent(true);
		tbl.add(wlbl).width(Gdx.graphics.getWidth()).row();
		worldg.addActor(tbl);

		loadg = new Stage();
		Label wlbl = new Label("Loading...", skin);
		wlbl.setAlignment(1);
		Table tbl1 = new Table();
		tbl1.setFillParent(true);
		tbl1.add(wlbl).width(Gdx.graphics.getWidth()).row();
		loadg.addActor(tbl1);

		gotodg = new Stage();
		Label gotolbl = new Label("Saving current sector and going to the next one...", skin);
		gotolbl.setAlignment(1);
		Table gototbl = new Table();
		gototbl.setFillParent(true);
		gototbl.add(gotolbl).width(Gdx.graphics.getWidth()).row();
		gotodg.addActor(gototbl);
	}

	public static void drawOkDialog(String msg, Stage stage) {
		final Dialog dialog = new Dialog(" ", skin);
		int linesToSkip = 2;
		dialog.text(msg).pad(25);
		dialog.row();
		for (int i = 0; i < linesToSkip; ++i) {
			dialog.add(" ");
			dialog.row();
		}
		TextButton cbtn = new TextButton("Ok", skin);
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
		strStatLbl.setText("Strength: " + s.strength);
		accStatLbl.setText("Accuracy: " + s.accuracy);
		conStatLbl.setText("Constitution: " + s.constitution);
		intStatLbl.setText("Intelligence: " + s.intelligence);
		luckStatLbl.setText("Luck: " + s.luck);
		dexStatLbl.setText("Dexterity: " + s.dexterity);
		statSumLbl.setText("\nStat sum: " + s.getSum());

		hpStatLbl.setText("HP: " + s.hp + "/" + s.mhp);
		staminaStatLbl.setText("Stamina: " + s.stamina + "/" + s.maxstamina);
	}

	static void createCharDialog() {
		gameUnfocused = true;
		rollStats();

		String msg = "Character creation";
		final GameDialog dialog = new GameDialog(overlay);
		Label title = dialog.getTitleLabel();
		title.setText(msg);
		title.setAlignment(Align.center);
		dialog.setMovable(true);
		final TextField nameField = new TextField("Player", skin);
		refreshStatLabels();
		dialog.add(new Label("\nCharacter name:", skin)).width(defLblWidth).row();
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
		TextButton rbtn = new TextButton("Reroll", skin);
		TextButton cbtn = new TextButton("Create", skin);
		dialog.add(rbtn).width(defLblWidth).row();
		dialog.row();
		dialog.add(cbtn).width(defLblWidth).row();
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				if (!nameField.getText().trim().equals("")) {
					World.player.setName(nameField.getText());
					World.player.reinit();
					dialog.remove();
					gameUnfocused = false;
				}
			}

		});
		rbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				rollStats();
			}

		});
		dialog.show(overlay);
	}

	static void drawSettingsDialog() {
		int radius = 12;

		final Dialog dialog = new Dialog(" ", skin);
		dialog.text("\nSettings");
		dialog.row();
		dialog.align(Align.center);
		final Label renderv = new Label("", skin);
		final Slider renderslide = new Slider(5, 125, 5, false, skin);
		if (new File("MadSand_Saves/lastrend.dat").exists())
			radius = (Integer.parseInt(getExternal("lastrend.dat")));
		renderslide.setValue(radius);
		renderv.setText("Render radius (" + (int) renderslide.getValue() + ")");
		TextButton cbtn = new TextButton("Set", skin);
		TextButton cancel = new TextButton("Cancel", skin);

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
				saveToExternal("lastrend.dat", Math.round(renderslide.getValue()) + "");
				// TODO
				dialog.remove();
			}

		});
		cancel.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				dialog.remove();
			}

		});
		dialog.show(menu);
	}

	static void loadWorldDialog() {
		File file = new File(MadSand.MAPDIR);
		String[] dirs = file.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		final Dialog ldialog = new Dialog(" ", skin);
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
			ldbtn[i] = new TextButton(dirs[i], skin);
			ldialog.add(ldbtn[i]).width(Gdx.graphics.getWidth() / 2).row();
			final String sa = dirs[i];
			ldbtn[i].addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					MadSand.WORLDNAME = sa;
					if (GameSaver.loadWorld(sa)) {
						MadSand.state = GameState.GAME;
					}

				}
			});
			i++;
		}
		TextButton cbtn = new TextButton("Cancel", skin);
		if (slots == 0)
			ldialog.add(new TextButton("No worlds to load", skin)).width(defLblWidth).row();
		ldialog.add(cbtn).width(defLblWidth).row();
		ldialog.add(new Label("\n", skin)).width(defLblWidth).row();
		cbtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				ldialog.remove();

			}

		});
		ldialog.show(menu);
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
		dialog = new Dialog(" ", skin);
		final TextField worldtxt = new TextField("World #" + (++slots), skin);
		dialog.text("New game");
		TextButton okbtn = new TextButton("Proceed", skin);
		TextButton nobtn = new TextButton("Cancel", skin);
		if (slots == MadSand.MAXSAVESLOTS) {
			worldtxt.setText("No free slots left!");
			worldtxt.setDisabled(true);
			okbtn.setDisabled(true);
		}
		nobtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				dialog.remove();
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

					MadSand.switchStage(GameState.GAME, overlay);
					if (!MadSand.justStarted)
						MadSand.world.generate();
					// World.player.x = new Random().nextInt(World.MAPSIZE);
					// World.player.y = new Random().nextInt(World.MAPSIZE);
					World.player.updCoords();
					exitToMenuBtn.setVisible(false);
					craftBtn.setVisible(false);
					inventoryActive = false;
					dialog.remove();
					Gdx.graphics.setContinuousRendering(false);
					MadSand.ZOOM = MadSand.DEFAULT_ZOOM;
					createCharDialog();
				}

			}
		});
		worldtxt.setTextFieldListener(new TextField.TextFieldListener() {

			public void keyTyped(TextField textField, char key) {
			}

		});
		dialog.row();
		dialog.add(new Label("\n\n", skin)).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(new Label("\n\nWorld name:\n", skin)).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(worldtxt).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(okbtn).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(nobtn).width(Gdx.graphics.getWidth() / 2).row();
		dialog.add(new Label("\n\n", skin)).width(Gdx.graphics.getWidth() / 2).row();
		dialog.show(menu);
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
			MadSand.VER = "\n[GREEN]b-" + (GameSaver.getExternal(ver));
		else
			MadSand.VER = "\n[GREEN]Version file not found";
		verlbl = new Label(MadSand.VER, skin);
		verlbl.setAlignment(Align.center);
	}

	static final int OVSTAT_COUNT = 6;

	static Label dieLabel;

	public static void setDeadText(String str) {
		dieLabel.setText(str);
	}

	private static float CRAFT_BTN_WIDTH = 250;
	private static float CRAFT_ENTRY_PADDING = 30;

	static void refreshCraftMenu() {
		Utils.out("Refreshing craft menu...");
		craftbl.remove();
		craftbl = new Table();
		Player player = World.player;
		// player.refreshAvailableRecipes();
		int craftSz = player.craftRecipes.size();
		Utils.out("Total unlocked recipes: " + craftSz + " out of " + Resources.CRAFTABLES);

		if (craftSz == 0) {
			craftbl.add(new Label("You don't know any craft recipes.", skin));
			Utils.out("No unlocked recipes.");
		}

		craftbtn = new TextButton[craftSz];

		int i = 0;
		int perRow = 3, id;
		int quantity;
		String craftString;
		while (i < craftSz) {
			craftString = "";
			id = player.craftRecipes.get(i);
			quantity = ItemProp.craftQuantity.get(id);
			if (quantity > 1)
				craftString = quantity + " ";
			craftString += ItemProp.name.get(id);
			craftbtn[i] = new TextButton(craftString, skin);
			craftbl.add(craftbtn[i]).width(CRAFT_BTN_WIDTH);
			craftbl.add(new Label(" " + Item.queryToName(ItemProp.recipe.get(id)), skin)).padRight(CRAFT_ENTRY_PADDING);

			if ((i + 1) % perRow == 0)
				craftbl.row();

			final int j = i, fid = id;
			Utils.out("Creating a button for item " + j + " craft recipe...");

			craftbtn[j].addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					World.player.craftItem(fid);
				}
			});

			i++;
		}
		craftbl.row();

		craftbl.setBackground(darkBackgroundSizeable);
		scroll = new ScrollPane(craftbl);
		scroll.setSize(MadSand.XDEF, MadSand.YDEF);
		craft.addActor(scroll);

		Table backTable = new Table();
		TextButton backBtn = new TextButton("Back", skin);

		backTable.align(Align.bottom);
		backTable.add(backBtn).fillY().expandY();
		backTable.setWidth(Gdx.graphics.getWidth());
		backBtn.align(Align.center);
		backBtn.setOrigin(Align.center);
		backBtn.pad(10);
		backBtn.setWidth(250);
		backBtn.setHeight(50);

		backBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				MadSand.switchStage(GameState.INVENTORY, overlay);
			}
		});

		craft.addActor(backTable);
	}

	static void initmenu() {
		equip = new Image[EQ_SLOTS];
		for (int i = 0; i < EQ_SLOTS; ++i) {
			equip[i] = new Image();
			equip[i].setDrawable(Resources.noEquip);
		}

		overlayStatLabels = new Label[OVSTAT_COUNT];

		statsLbl = new Label("Stats:", skin);
		skillsLbl = new Label("\nSkills:", skin);
		skillsLbl.setFontScale(headerScale);
		statsLbl.setFontScale(headerScale);

		conStatLbl = new Label("", skin);
		strStatLbl = new Label("", skin);
		accStatLbl = new Label("", skin);
		intStatLbl = new Label("", skin);
		luckStatLbl = new Label("", skin);
		dexStatLbl = new Label("", skin);
		statSumLbl = new Label("", skin);
		hpStatLbl = new Label("", skin);
		staminaStatLbl = new Label("", skin);
		refreshStatLabels();

		gamecontext = new Table(skin);
		contextMenuBtn = new TextButton[5];
		mousemenu = new Table(skin);
		actionTbl = new Table(skin);

		inGameBtnListener = new InputListener() {
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				gameUnfocused = true;
				mouselabel.setVisible(false);
			}

			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (!dialogActive) {
					gameUnfocused = false;
					mouselabel.setVisible(true);
				}
			}
		};

		npcInteractListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				World.player.interact(World.player.stats.look);
				actionTbl.setVisible(false);
			}
		};

		objInteractListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				World.player.interact(World.player.stats.look);
				gameUnfocused = true;
				processActionMenu();
			}
		};

		overlay.addActor(actionTbl);
		interactBtn = new TextButton("", skin);
		actionTbl.setVisible(false);
		actionTbl.setPosition(horizontalCenter(actionTbl), ACTION_TBL_YPOS);
		actionTbl.add(interactBtn).width(DEFWIDTH).row();

		mousemenu.setVisible(true);
		mouselabel = new Label("", skin);
		int cc = 0;
		contextMenuBtn[0] = new TextButton("Interact", skin);
		contextMenuBtn[0].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse(Mouse.wclickx, Mouse.wclicky);
				World.player.interact(World.player.stats.look);
			}

		});
		contextMenuBtn[3] = new TextButton("Use item", skin);
		contextMenuBtn[3].addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.useItem();
			}

		});
		contextMenuBtn[4] = new TextButton("Put held item to backpack", skin);
		contextMenuBtn[4].addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.freeHands();
				closeGameContextMenu();
			}

		});
		contextMenuBtn[1] = new TextButton("Attack", skin);
		contextMenuBtn[1].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse(Mouse.wx, Mouse.wy);
				World.player.attack();
			}
		});
		contextMenuBtn[2] = new TextButton("Turn", skin);
		contextMenuBtn[2].addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				World.player.lookAtMouse(Mouse.wx, Mouse.wy);
			}
		});

		while (cc < 5) {
			gamecontext.add(contextMenuBtn[cc]).width(DEFWIDTH).height(20.0F);
			gamecontext.row();
			cc++;
		}
		gamecontext.setVisible(false);

		cc = 0;

		mousemenu.add(mouselabel).width(100.0F);
		mousemenu.row();

		menu = new Stage();
		craft = new Stage();
		craftbl = new Table();
		inputField = new TextField("", skin);
		inputField.setMessageText("");
		inputField.setFocusTraversal(true);
		inputField.setTextFieldListener(new TextField.TextFieldListener() {
			public void keyTyped(TextField textField, char key) {
				if (key == Keys.ESCAPE) {
					inputField.setText("");
					overlay.unfocus(inputField);
				}

			}
		});

		// Setting up game log
		Table logtbl = new Table(skin).align(Align.topLeft);
		logtbl.setFillParent(true);
		int tpm = 0;
		log = new Label[LOG_LENGTH];
		int cxxc = 0;
		while (cxxc < LOG_LENGTH) {
			log[cxxc] = new Label(" ", skin);
			cxxc++;
		}
		while (tpm < LOG_LENGTH) {
			log[tpm].setWrap(true);
			logtbl.add(log[tpm]).width(DEFWIDTH + 50).pad(3);
			logtbl.row();
			tpm++;
		}
		logtbl.add(inputField).width(200).height(30);
		inputField.setVisible(false);

		tpm = 0;
		Table ovtbl = new Table(skin).align(18);

		// logtbl.setBackground(bck);
		// Overlay stat labels
		Table ovstatTbl = new Table();
		ovtbl.setFillParent(true);
		ovstatTbl.setFillParent(true);
		ovstatTbl.align(Align.topRight);
		int count = 0;
		while (count < OVSTAT_COUNT) {
			overlayStatLabels[count] = new Label(" ", skin);
			overlayStatLabels[count].setWrap(false);
			ovstatTbl.add(overlayStatLabels[count]).width(165);
			count++;
		}
		overlay.addActor(ovstatTbl);
		darkness = new Table();
		darkness.setBackground(darkBackground);
		darkness.setFillParent(true);
		darkness.setVisible(false);
		overlay.addActor(darkness);

		overlay.addActor(ovtbl);
		overlay.addActor(logtbl);
		overlay.addActor(mousemenu);
		overlay.addActor(gamecontext);

		overlay.addListener(new ClickListener(Buttons.RIGHT) {
			public void clicked(InputEvent event, float x, float y) {
				if (dialogActive)
					return;
				if (MadSand.state == GameState.GAME) {
					if (mousemenu.isVisible()) {
						openGameContextMenu();
						gameUnfocused = true;
					} else {
						closeGameContextMenu();
					}
				}
			}
		});

		overlay.addListener(new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				Mouse.justClicked = true;
			}
		});
		exitToMenuBtn = new TextButton("Exit to menu", skin);
		craftBtn = new TextButton("Crafting", skin);
		final TextButton newGameBtn = new TextButton("New game", skin);
		resumeBtn = new TextButton("Resume game", skin);
		resumeBtn.setVisible(false);
		TextButton settingsButton = new TextButton("Settings", skin);
		TextButton exitBtn = new TextButton("Exit", skin);
		TextButton loadGameBtn = new TextButton("Load game", skin);

		Label title = new Label("MadSand: Fallen. Rise From Dust\n", skin);
		title.setPosition(Gdx.graphics.getWidth() / 2 - title.getWidth() / 2.0F, Gdx.graphics.getHeight() / 2 + 100);

		refreshCraftMenu();
		// craft.setDebugAll(true);
		craftBtn.setHeight(82.0F);
		craftBtn.align(16);
		craftBtn.setWidth(250.0F);
		ovtbl.row();
		ovtbl.row();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add(craftBtn).width(200.0F).row();
		craftBtn.setVisible(false);
		ovtbl.add();
		ovtbl.add();
		ovtbl.add();
		ovtbl.add(exitToMenuBtn).width(200.0F).row();
		int aa = 0;
		while (aa < ITEM_DISPLAY_SLOTS) {
			ovtbl.add();
			ovtbl.add();
			ovtbl.add();
			ovtbl.add(equip[aa]).width(80.0F).align(Align.right).row();
			aa++;
		}

		exitToMenuBtn.setVisible(false);

		TextButton respawnButton = new TextButton("Respawn", skin);
		Table tab = new Table();
		dieLabel = new Label("", skin);
		dieLabel.setAlignment(Align.center);
		tab.add(dieLabel).width(500.0F);
		tab.row();
		tab.add(new Label("", skin)).width(500.0F);
		tab.row();
		tab.add(respawnButton).width(500.0F).row();
		dead = new Stage();
		dead.addActor(darkness);
		tab.setFillParent(true);
		dead.addActor(tab);

		getVersion();

		Table menuTbl = new Table();
		menuTbl.setFillParent(true);
		menuTbl.setBackground(darkBackground);
		menuTbl.add(new Label("MadSand: Fallen. Rise From Dust\n", skin));
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
		menu.addActor(menuTbl);

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

		newGameBtn.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				createWorldDialog();
			}

		});

		resumeBtn.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				Gdx.graphics.setContinuousRendering(false);
				MadSand.state = GameState.GAME;
				Gdx.input.setInputProcessor(overlay);
			}

		});

		loadGameBtn.addListener(new ChangeListener() {

			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				loadWorldDialog();
			}

		});

		craftBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				refreshCraftMenu();
				craft.setScrollFocus(scroll);
				MadSand.switchStage(GameState.CRAFT, craft);
			}
		});

		exitToMenuBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				resumeBtn.setVisible(true);
				MadSand.switchStage(GameState.NMENU, menu);
			}
		});

		initLaunchMenu();
		initWmenu();
		Gdx.input.setInputProcessor(menu);
	}

	public static void closeGameContextMenu() {
		mousemenu.setVisible(true);
		gamecontext.setVisible(false);
		gameUnfocused = false;
	}

	private final static int CONTEXT_USE_BTN = 3;

	public static void openGameContextMenu() {
		contextMenuBtn[CONTEXT_USE_BTN].setText("Use " + World.player.stats.hand.name);
		mousemenu.setVisible(false);
		gamecontext.setVisible(true);
		gamecontext.setPosition(Mouse.x + 50, Mouse.y - 30);
		Mouse.wclickx = Mouse.wx;
		Mouse.wclicky = Mouse.wy;
	}

	static BitmapFont createFont(int size) {
		BitmapFont font = new BitmapFont();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local(MadSand.SAVEDIR + Gui.FONT_PATH));
		FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
		param.characters = Gui.FONT_CHARS;
		param.size = size;
		param.color = Color.WHITE;
		param.borderWidth = 0.9f;
		param.borderColor = Color.BLACK;
		font = generator.generateFont(param);
		generator.dispose();
		font.getData().markupEnabled = true;
		return font;
	}

	public static float horizontalCenter(Actor actor) {
		return (Gdx.graphics.getWidth() / 2) - actor.getWidth();
	}

	private static void activateInteractBtn(TextButton btn, String text, ChangeListener listener) {
		actionTbl.setVisible(true);
		btn.setVisible(true);
		btn.setText(text);
		btn.addListener(listener);
	}

	public static void processActionMenu() {
		Map loc = MadSand.world.getCurLoc();

		Player player = World.player;
		Pair coords = new Pair(player.x, player.y);

		Tile tile = loc.getTile(coords.x, coords.y);
		coords.addDirection(player.stats.look);

		int tileItem = MapObject.getTileAltItem(tile.id, player.stats.hand.type.get());
		MapObject object = loc.getObject(coords.x, coords.y);
		Npc npc = loc.getNpc(coords.x, coords.y);
		String tileAction = TileProp.oninteract.get(tile.id);
		String objAction = ObjectProp.interactAction.get(object.id);

		if (tileAction == "-1" && npc == Map.nullNpc && object == Map.nullObject && tileItem == -1) {
			actionTbl.setVisible(false);
			return;
		}

		actionTbl.removeActor(interactBtn);
		interactBtn = new TextButton("", skin);
		actionTbl.add(interactBtn).width(DEFWIDTH + 50).row();
		actionTbl.addListener(inGameBtnListener);
		boolean holdsShovel = player.stats.hand.type == ItemType.Shovel;

		String tileMsg = "Interact with ";
		if (tile != Map.nullTile && (tileAction != "-1" || (tileItem != -1 && holdsShovel))) {
			if (tileItem != -1 && holdsShovel)
				tileMsg = "Dig ";
			activateInteractBtn(interactBtn, tileMsg + TileProp.name.get(tile.id), new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (!holdsShovel)
						BuildScript.execute(tileAction);
					else
						player.useItem();
					gameUnfocused = false;
					mouselabel.setVisible(true);
				}
			});
		} else if (npc != Map.nullNpc && !dialogActive)
			activateInteractBtn(interactBtn, "Talk to " + npc.stats.name, npcInteractListener);
		else if (object != Map.nullObject && objAction != "-1")
			activateInteractBtn(interactBtn, "Interact with " + object.name, objInteractListener);
		else {
			actionTbl.removeActor(interactBtn);
			actionTbl.setVisible(false);
		}
	}

	static final String FONT_CHARS = "АБВГДЕЁЖЗИЙКЛМНОПРСТФХЦЧШЩЪЬЫЭЮЯабвгдеёжзийклмнопрстфхцчшщыъьэюяabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";

}
