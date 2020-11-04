package hitonoriol.madsand.gui.stages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Mouse;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.enums.EquipSlot;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.gui.dialogs.BuildDialog;
import hitonoriol.madsand.gui.dialogs.CharacterCreationDialog;
import hitonoriol.madsand.gui.dialogs.CharacterInfoWindow;
import hitonoriol.madsand.gui.dialogs.LevelupDialog;
import hitonoriol.madsand.gui.dialogs.QuestJournal;
import hitonoriol.madsand.gui.widgets.ActionButton;
import hitonoriol.madsand.gui.widgets.EquipmentSidebar;
import hitonoriol.madsand.gui.widgets.GameContextMenu;
import hitonoriol.madsand.gui.widgets.GameLog;
import hitonoriol.madsand.gui.widgets.GameTooltip;
import hitonoriol.madsand.gui.widgets.OverlayBottomMenu;
import hitonoriol.madsand.gui.widgets.QuestArrow;
import hitonoriol.madsand.gui.widgets.StatProgressBar;
import hitonoriol.madsand.world.World;

/*
 * Main in-game overlay (GameStage.GAME) 
 */

public class Overlay extends Stage {
	Skin skin;
	static float SIDEBAR_PADDING = 100;
	static float SIDEBAR_XPADDING = 5;

	Table overlayTable = new Table();

	public CharacterInfoWindow statWindow;
	public CharacterCreationDialog charCreateDialog;

	public GameTooltip gameTooltip;
	public GameContextMenu gameContextMenu;
	public ActionButton actionButton;
	public GameLog gameLog;
	public OverlayBottomMenu bottomMenu;
	public EquipmentSidebar equipmentSidebar;

	public StatProgressBar hpBar;
	public StatProgressBar foodBar;
	public StatProgressBar staminaBar;
	public StatProgressBar expBar;

	List<QuestArrow> questArrows = new ArrayList<>();

	Label overlayStatLabel;
	Label timeLabel;
	static final int OVSTAT_COUNT = 6;

	public Overlay() {
		super();
		skin = Gui.skin;
		initMouseListeners();

		actionButton = new ActionButton();
		gameTooltip = new GameTooltip();
		gameContextMenu = new GameContextMenu();
		gameLog = new GameLog();
		bottomMenu = new OverlayBottomMenu(this);

		initOverlayTable();

		equipmentSidebar = new EquipmentSidebar();
		equipmentSidebar.setPosition(this.getWidth() - SIDEBAR_XPADDING, equipmentSidebar.getHeight() + SIDEBAR_PADDING,
				Align.topRight);

		Mouse.tooltipContainer = gameTooltip;

		super.addActor(equipmentSidebar);
		super.addActor(bottomMenu);
		super.addActor(gameTooltip);
		super.addActor(gameContextMenu);
		super.addActor(actionButton);
	}

	private void initMouseListeners() {

		super.addListener(new ClickListener(Buttons.LEFT) {
			private boolean ignoreClick = false;

			public void clicked(InputEvent event, float x, float y) {
				if (ignoreClick) {
					ignoreClick = false;
					return;
				}

				if (!Gui.gameUnfocused && !Gui.dialogActive && MadSand.state.equals(GameState.GAME)) {
					Mouse.justClicked = true;
					Mouse.mouseClickAction();
				}
			}

			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				super.touchDown(event, x, y, pointer, button);
				if (event.getType().equals(InputEvent.Type.touchDown))
					ignoreClick = Gui.isGameUnfocused() || !MadSand.state.equals(GameState.GAME);
				return true;
			}
		});

		super.addListener(new ClickListener(Buttons.RIGHT) {

			public void clicked(InputEvent event, float x, float y) {

				if (Gui.dialogActive)
					return;

				if (!MadSand.state.equals(GameState.GAME))
					return;

				if (gameTooltip.isVisible()) {
					gameContextMenu.openGameContextMenu();
					Gui.gameUnfocused = true;
				} else
					gameContextMenu.closeGameContextMenu();

			}

		});
	}

	float ENTRY_PAD = 5;

	private void initOverlayTable() {
		Table topTable = new Table();
		topTable.setSize(Gdx.graphics.getWidth(), StatProgressBar.HEIGHT);
		topTable.setBackground(Gui.darkBackgroundSizeable);
		topTable.align(Align.left);

		expBar = StatProgressBar.createLevelBar();
		hpBar = StatProgressBar.createHpBar();
		staminaBar = StatProgressBar.createStaminaBar();
		foodBar = new StatProgressBar("Food").setStyle(Color.ORANGE);

		timeLabel = new Label(" ", skin);
		overlayStatLabel = new Label(" ", skin);
		overlayStatLabel.setAlignment(Align.center);

		topTable.add(expBar).padLeft(ENTRY_PAD).padRight(ENTRY_PAD);
		topTable.add(hpBar).padRight(ENTRY_PAD);
		topTable.add(foodBar).padRight(ENTRY_PAD);
		topTable.add(staminaBar).padRight(ENTRY_PAD);
		topTable.add(overlayStatLabel).align(Align.center).expandX().height(StatProgressBar.HEIGHT + 5);
		topTable.add(timeLabel).align(Align.center).expandX().row();

		overlayTable.align(Align.topLeft);
		overlayTable.add(topTable).width(Gdx.graphics.getWidth()).row();
		overlayTable.add(gameLog).height(GameLog.HEIGHT).padTop(ENTRY_PAD).padLeft(ENTRY_PAD).align(Align.topLeft);

		overlayTable.setFillParent(true);
		super.addActor(overlayTable);
	}

	public void pollGameConsole() {
		if (!Utils.debugMode)
			return;

		if (getKeyboardFocus() != gameLog.inputField)
			return;

		if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			String cmd = gameLog.inputField.getText().trim();

			try {
				LuaUtils.execute(cmd);
			} catch (Exception e) {
				MadSand.print("Couldn't execute user input");
				e.printStackTrace();
			} finally {
				gameLog.inputField.setText("");
				gameLog.inputField.setVisible(!gameLog.inputField.isVisible());
				unfocus(gameLog.inputField);
			}
		}
	}

	public void levelUpDialog() {
		new LevelupDialog().show();
	}

	public TextField getConsoleField() {
		return gameLog.inputField;
	}

	public Label[] getLogLabels() {
		return gameLog.logLabels;
	}

	public void processActionMenu() {
		actionButton.processActionMenu();
	}

	public void closeGameContextMenu() {
		gameContextMenu.closeGameContextMenu();
	}

	public Label getTooltip() {
		return gameTooltip.tooltipLabel;
	}

	public void hideActionBtn() {
		actionButton.hideActionBtn();
	}

	public void toggleStatsWindow() {
		if (statWindow != null) {
			statWindow.remove();
			statWindow = null;
			return;
		}

		statWindow = new CharacterInfoWindow();
		statWindow.show();
	}

	public void createCharDialog() {
		if (charCreateDialog == null)
			charCreateDialog = new CharacterCreationDialog();
		charCreateDialog.show();
	}

	public void showJournal() {
		new QuestJournal(World.player.quests).show();
	}

	public void showBuildMenu() {
		new BuildDialog().show();
	}

	public void setHandDisplay(Item item) {
		equipmentSidebar.equipItem(EquipSlot.MainHand, item);
	}

	public void hideTooltip() {
		gameTooltip.hide();
	}

	public void showTooltip() {
		gameTooltip.show();
	}

	private void refreshQuestArrows() {
		Player player = World.player;
		Iterator<QuestArrow> it = questArrows.iterator();
		QuestArrow arrow;
		boolean objectiveDone;
		HashSet<Integer> hasArrow = new HashSet<>();
		
		while (it.hasNext()) {
			arrow = it.next();
			objectiveDone = arrow.quest.isComplete();
			if (!player.quests.isQuestInProgress(arrow.quest.id) || !objectiveDone) {
				Utils.out("removin.. not in progr");
				arrow.remove();
				it.remove();
			} else if (objectiveDone){
				hasArrow.add(arrow.quest.id);
				arrow.update();
				Utils.out("updatin");
			}
		}

		for (Quest quest : player.quests.questsInProgress) {
			if (quest.isComplete() && !hasArrow.contains(quest.id)) {
				arrow = new QuestArrow(quest);
				questArrows.add(arrow);
				super.addActor(arrow);
				arrow.update();
			}
		}
	}

	public void refreshOverlay() {
		Player player = World.player;
		Stats stats = player.stats;

		hpBar.setRange(0, stats.mhp).setValue(stats.hp);
		foodBar.setRange(0, stats.maxFood).setValue(stats.food);
		staminaBar.setRange(0, stats.maxstamina).setValue(stats.stamina);
		expBar.setRange(0, stats.skills.get(Skill.Level).requiredExp).setStatText("LVL " + stats.skills.getLvl())
				.setValue(stats.skills.getExp());

		String info = ("Location: Cell (" + player.x + ", " + player.y + ")" + getSectorString());

		timeLabel.setText(getTimeString());
		overlayStatLabel.setText(info);

		refreshQuestArrows();
	}

	private String getTimeString() {
		World world = MadSand.world;
		String hour = fixTime(Utils.str(world.worldtime));
		String minute = fixTime(Utils.str(world.getWorldTimeMinute()));

		String time = "Day " + world.getWorldTimeDay();
		time += ", " + hour + ":" + minute;
		return time;
	}

	private String fixTime(String timeStr) {
		if (timeStr.length() < 2)
			return "0" + timeStr;
		return timeStr;
	}

	private String getSectorString() {
		return (MadSand.world.inEncounter
				? " @ Random Encounter"
				: " @ Sector (" + MadSand.world.getCurWPos() + ")");

	}
}
