package hitonoriol.madsand;

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

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.gui.dialogs.CharacterCreationDialog;
import hitonoriol.madsand.gui.dialogs.CharacterInfoWindow;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.security.MessageDigest;

public class Gui {
	static final float DEFWIDTH = 250f;
	static final int LOG_LENGTH = 20;
	
	public static final float defLblWidth = Gdx.graphics.getWidth() / 4;
	public static final float ACTION_TBL_YPOS = Gdx.graphics.getHeight() / 6f;

	public static String noticeMsgColor = "[#16E1EA]";

	public static boolean gameUnfocused = false;
	public static boolean inventoryActive = false;
	public static boolean dialogActive = false;

	public static Overlay overlay;

	static NinePatchDrawable transparency;

	public static Table darkness;
	public static Table gamecontext;
	public static Table mousemenu;

	static Label[] log;
	public static Label mouselabel;
	public static Label verlbl;

	public static Stage menu;
	public static Stage dead;

	static TextField inputField;

	public static Skin skin;

	static TextButton[] contextMenuBtn;
	static TextButton resumeBtn;

	static BitmapFont font;
	static BitmapFont fontMedium;
	static BitmapFont fontBig;

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

		overlay = new Overlay();
	}

	static Dialog dialog;

	static void initLaunchMenu() {
		MadSand.state = GameState.NMENU;
		Gdx.input.setInputProcessor(menu);
	}

	static Stage worldg;
	static Stage loadg;
	static Stage gotodg;

	private static void createTransitionScreen(Stage stage, String text) {
		stage = new Stage();
		Label label = new Label("Generating your world...", skin);
		label.setFontScale(1.5f);
		label.setAlignment(Align.center);
		Table tbl = new Table();
		tbl.setFillParent(true);
		tbl.add(label).width(Gdx.graphics.getWidth()).row();
		stage.addActor(tbl);
	}

	static void initWmenu() {
		createTransitionScreen(worldg, "Generating your world...");
		createTransitionScreen(loadg, "Loading...");
		createTransitionScreen(gotodg, "Saving current sector and going to the next one...");
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

	static void getVersion() {
		String ver = "MadSandData/version.dat";
		if (!GameSaver.getExternal(ver).equals(""))
			MadSand.VER = "\n[GREEN]b-" + (GameSaver.getExternal(ver));
		else
			MadSand.VER = "\n[GREEN]Version file not found";
		verlbl = new Label(MadSand.VER, skin);
		verlbl.setAlignment(Align.center);
	}

	static Label dieLabel;

	public static void setDeadText(String str) {
		dieLabel.setText(str);
	}

	static void initmenu() {
		

		

		//TODO: init stat labels 
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
		inputField.setWidth(DEFWIDTH + 50);
		inputField.setMessageText("");
		inputField.setFocusTraversal(true);
		inputField.setTextFieldListener(new TextField.TextFieldListener() {
			public void keyTyped(TextField textField, char key) {
				if (key == Keys.ESCAPE || key == Keys.GRAVE) {
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
		logtbl.add(inputField).width(DEFWIDTH + 50).align(Align.left).pad(3).height(30);
		inputField.debug();
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

	static final String FONT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"'<>";
	static final String FONT_PATH = "fonts/8bitoperator.ttf";

}
