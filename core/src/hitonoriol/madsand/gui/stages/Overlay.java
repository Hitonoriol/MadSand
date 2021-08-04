package hitonoriol.madsand.gui.stages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.gui.widgets.gametooltip.GameTooltip;
import hitonoriol.madsand.gui.widgets.overlay.ActionButton;
import hitonoriol.madsand.gui.widgets.overlay.EquipmentSidebar;
import hitonoriol.madsand.gui.widgets.overlay.GameContextMenu;
import hitonoriol.madsand.gui.widgets.overlay.GameLog;
import hitonoriol.madsand.gui.widgets.overlay.Hotbar;
import hitonoriol.madsand.gui.widgets.overlay.OverlayBottomMenu;
import hitonoriol.madsand.gui.widgets.stats.StatProgressBar;
import hitonoriol.madsand.gui.widgets.waypoint.WaypointArrow;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

/*
 * Main in-game overlay (GameStage.GAME) 
 */

public class Overlay extends Stage {
	Skin skin;
	static float SIDEBAR_XPADDING = 5;

	Table overlayTable = new Table();

	private Table topTable = new Table();
	public GameTooltip gameTooltip;
	public GameContextMenu gameContextMenu;
	public ActionButton actionButton;
	public GameLog gameLog;
	public OverlayBottomMenu bottomMenu;
	public Hotbar hotbar;
	public EquipmentSidebar equipmentSidebar;

	public StatProgressBar hpBar;
	public StatProgressBar foodBar;
	public StatProgressBar staminaBar;
	public StatProgressBar expBar;

	private List<WaypointArrow> waypointArrows = new ArrayList<>();

	Label overlayStatLabel;
	Label timeLabel;
	static final int OVSTAT_COUNT = 6;

	public Overlay() {
		super(Gui.uiViewport);
		skin = Gui.skin;

		actionButton = new ActionButton();
		gameTooltip = GameTooltip.instance();
		gameContextMenu = new GameContextMenu();
		gameLog = new GameLog();
		bottomMenu = new OverlayBottomMenu(this);
		equipmentSidebar = new EquipmentSidebar();
		hotbar = new Hotbar();

		initOverlayTable();
		updateWidgetPositions();

		Mouse.tooltipContainer = gameTooltip;

		super.addActor(equipmentSidebar);
		super.addActor(bottomMenu);
		super.addActor(hotbar);
		super.addActor(gameTooltip);
		super.addActor(gameContextMenu);
		super.addActor(actionButton);
	}

	public void setPlayer(Player player) {
		expBar.setSkill(player.stats.skills.get(Skill.Level));
	}

	public void updateWidgetPositions() {
		equipmentSidebar.setPosition(this.getWidth() - SIDEBAR_XPADDING,
				equipmentSidebar.getHeight() + (this.getHeight() - equipmentSidebar.getHeight()) * 0.5f,
				Align.topRight);
		overlayTable.getCell(topTable).width(this.getWidth());
	}

	float ENTRY_PAD = 5;

	private void initOverlayTable() {
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

	String prevConsoleInput;

	public void pollGameConsole() {
		if (!Globals.debugMode)
			return;

		if (getKeyboardFocus() != gameLog.inputField)
			return;

		if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			String cmd = gameLog.inputField.getText().trim();

			try {
				Lua.execute(cmd);
			} catch (Exception e) {
				MadSand.print("Couldn't execute user input");
				e.printStackTrace();
			} finally {
				prevConsoleInput = gameLog.inputField.getText();
				gameLog.inputField.setText("");
				gameLog.inputField.setVisible(!gameLog.inputField.isVisible());
				unfocus(gameLog.inputField);
				Gui.gameResumeFocus();
			}
		}

		if (Gdx.input.isKeyJustPressed(Keys.UP)) {
			String tmp = gameLog.inputField.getText();
			gameLog.inputField.setText(prevConsoleInput);
			prevConsoleInput = tmp;
			gameLog.inputField.setCursorPosition(gameLog.inputField.getText().length());
		}
	}

	public TextField getConsoleField() {
		return gameLog.inputField;
	}

	public boolean isConsoleFocused() {
		return super.getKeyboardFocus() == gameLog.inputField;
	}

	public Label[] getLogLabels() {
		return gameLog.logLabels;
	}

	public void refreshActionButton() {
		actionButton.refresh();
	}

	public void closeGameContextMenu() {
		gameContextMenu.closeGameContextMenu();
	}

	public void hideActionBtn() {
		actionButton.hideButton();
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

	public WaypointArrow getWaypointArrow(int destX, int destY) {
		return waypointArrows.stream()
				.filter(arrow -> arrow.getDestination().equals(destX, destY))
				.findFirst()
				.orElse(null);
	}

	public void addWaypointArrow(WaypointArrow arrow) {
		Utils.dbg("[Overlay] Added waypoint arrow {%s}", arrow);
		waypointArrows.add(arrow);
		addActor(arrow);
		refreshWaypointArrows();
	}

	private void refreshWaypointArrows() {
		Iterator<WaypointArrow> it = waypointArrows.iterator();
		WaypointArrow arrow;
		while (it.hasNext()) {
			arrow = it.next();
			if (!arrow.hasParent()) {
				it.remove();
				Utils.dbg("[Overlay] Removed waypoint arrow {%s}", arrow);
			} else
				arrow.update();
		}

		QuestWorker quests = MadSand.player().getQuestWorker();
		quests.questsInProgress.stream()
				.filter(quest -> quest.isComplete() && !quest.hasQuestArrow())
				.forEach(quest -> {
					quest.completionNotice();
					addWaypointArrow(quest.questArrow());
				});
	}

	public void closeAllDialogs() {
		Functional.with(getActors(), actors -> {
			Actor actor;
			for (int i = 0; i < actors.size; ++i)
				if ((actor = actors.get(i)) instanceof GameDialog)
					actor.remove();
		});
	}

	public void refresh() {
		Player player = MadSand.player();
		PlayerStats stats = player.stats;

		hpBar.setRange(0, stats.mhp).setValue(stats.hp);
		foodBar.setRange(0, stats.maxFood).setValue(stats.food);
		staminaBar.setRange(0, stats.maxstamina).setValue(stats.stamina);
		expBar.setStatText("LVL " + stats.skills.getLvl())
				.update();

		String info = (MadSand.world().getLocation().name +
				", Cell (" + player.x + ", " + player.y + ")" + getSectorString());

		timeLabel.setText(getTimeString());
		overlayStatLabel.setText(info);

		hotbar.refreshVisibility();
		refreshWaypointArrows();
	}

	private String getTimeString() {
		World world = MadSand.world();
		String hour = fixTime(Utils.str(world.getWorldTimeHour()));
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
		return (MadSand.world().inEncounter()
				? " @ Random Encounter"
				: " @ Sector (" + MadSand.world().getCurWPos() + ")");

	}

}
