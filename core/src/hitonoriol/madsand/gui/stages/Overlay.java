package hitonoriol.madsand.gui.stages;

import static hitonoriol.madsand.MadSand.player;
import static hitonoriol.madsand.MadSand.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.widgets.gametooltip.GameTooltip;
import hitonoriol.madsand.gui.widgets.overlay.ActionButton;
import hitonoriol.madsand.gui.widgets.overlay.EquipmentSidebar;
import hitonoriol.madsand.gui.widgets.overlay.GameContextMenu;
import hitonoriol.madsand.gui.widgets.overlay.GameLog;
import hitonoriol.madsand.gui.widgets.overlay.Hotbar;
import hitonoriol.madsand.gui.widgets.overlay.InfoPanel;
import hitonoriol.madsand.gui.widgets.overlay.OverlayBottomMenu;
import hitonoriol.madsand.gui.widgets.stats.StatProgressBar;
import hitonoriol.madsand.gui.widgets.waypoint.WaypointArrow;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

/*
 * Main in-game overlay (GameStage.GAME) 
 */

public class Overlay extends Stage {
	static float SIDEBAR_XPADDING = 5;

	private Table overlayTable = new Table();
	private Table topTable = new Table();
	public GameTooltip gameTooltip = new GameTooltip();
	private GameContextMenu gameContextMenu = new GameContextMenu();
	public ActionButton actionButton = new ActionButton();
	private GameLog gameLog = new GameLog();
	private InfoPanel infoPanel = new InfoPanel("Info");
	public OverlayBottomMenu bottomMenu = new OverlayBottomMenu(this);
	private Hotbar hotbar = new Hotbar();
	public EquipmentSidebar equipmentSidebar = new EquipmentSidebar();

	public StatProgressBar hpBar;
	public StatProgressBar foodBar;
	public StatProgressBar staminaBar;
	public StatProgressBar expBar;

	private List<WaypointArrow> waypointArrows = new ArrayList<>();

	Label overlayStatLabel;
	Label timeLabel;
	static final int OVSTAT_COUNT = 6;

	public Overlay() {
		super(Gui.viewport());
	}

	public void setPlayer(Player player) {
		if (!layoutSetUp())
			initLayout();

		expBar.setSkill(player.stats.skills.get(Skill.Level));
	}

	private void initLayout() {
		initOverlayTable();
		initInfoPanel();
		updateWidgetPositions();

		addActor(equipmentSidebar);
		addActor(bottomMenu);
		addActor(hotbar);
		addActor(gameTooltip);
		addActor(gameContextMenu);
		addActor(actionButton);
	}

	private boolean layoutSetUp() {
		return !overlayTable.getCells().isEmpty();
	}

	public void updateWidgetPositions() {
		if (!layoutSetUp())
			return;

		equipmentSidebar.setPosition(this.getWidth() - SIDEBAR_XPADDING,
				equipmentSidebar.getHeight() + (this.getHeight() - equipmentSidebar.getHeight()) * 0.5f,
				Align.topRight);
		overlayTable.getCell(topTable).width(this.getWidth());
	}

	private void initInfoPanel() {
		infoPanel.addHeader("Status");
		infoPanel.addEntry(() -> "Health: " + player().getHealthState());
		infoPanel.addEntry(() -> player().stats().isSatiated() ? "Satiated" : "Hungry");
		infoPanel.addEntry(() -> player().isTargeted() ? "Under attack" : "No hostiles nearby");
		infoPanel.addEntry(() -> String.format("Encumbrance: %.1f%%", player().stats().getEncumbrancePercent() * 100f));

		Globals.debug(() -> {
			infoPanel.addHeader("Debug");
			infoPanel.addEntry(() -> Utils.memoryUsageString()).update(5);
			infoPanel.addEntry(() -> String.format("FPS: %2d (%-6.4f)",
					Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getDeltaTime())).update(1);
			infoPanel.addEntry(() -> {
				Map map = world().getCurLoc();
				return String.format("Map size: %dx%d", map.getWidth(), map.getHeight());
			});
			infoPanel.addEntry(() -> "Objects on map: " + world().getCurLoc().getObjectCount());
			infoPanel.addEntry(() -> "NPCs on map: " + world().getCurLoc().getNpcCount());
			infoPanel.addEntry(() -> "Item textures: " + Item.dynamicTextureCacheSize());
		});
		infoPanel.toggleEnabled();
	}

	float ENTRY_PAD = 5;

	private void initOverlayTable() {
		topTable.setSize(Gdx.graphics.getWidth(), StatProgressBar.HEIGHT);
		topTable.setBackground(GuiSkin.darkBackgroundSizeable);
		topTable.align(Align.left);

		expBar = StatProgressBar.createLevelBar();
		hpBar = StatProgressBar.createHpBar();
		staminaBar = StatProgressBar.createStaminaBar();
		foodBar = new StatProgressBar("Food").setStyle(Color.ORANGE);

		timeLabel = new Label(" ", Gui.skin);
		overlayStatLabel = new Label(" ", Gui.skin);
		overlayStatLabel.setAlignment(Align.center);

		topTable.add(expBar).padLeft(ENTRY_PAD).padRight(ENTRY_PAD);
		topTable.add(hpBar).padRight(ENTRY_PAD);
		topTable.add(foodBar).padRight(ENTRY_PAD);
		topTable.add(staminaBar).padRight(ENTRY_PAD);
		topTable.add(overlayStatLabel).align(Align.center).expandX().height(StatProgressBar.HEIGHT + 5);
		topTable.add(timeLabel).align(Align.center).expandX().row();

		overlayTable.align(Align.topLeft);
		overlayTable.add(topTable).width(Gdx.graphics.getWidth()).row();

		overlayTable.defaults()
				.padTop(ENTRY_PAD)
				.padLeft(ENTRY_PAD)
				.align(Align.topLeft);
		overlayTable.add(gameLog).padBottom(ENTRY_PAD).row();
		overlayTable.add(infoPanel).padTop(-getGameLog().getConsoleField().getHeight() + 3).row();
		gameLog.toFront();

		overlayTable.setFillParent(true);
		addActor(overlayTable);
	}

	public void toggleGameConsole() {
		gameLog.toggleConsole();
	}

	public void refreshActionButton() {
		actionButton.refresh();
	}

	public GameContextMenu getContextMenu() {
		return gameContextMenu;
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
	
	public GameTooltip getTooltip() {
		return gameTooltip;
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
		infoPanel.refresh();
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

	public Hotbar getHotbar() {
		return hotbar;
	}

	public GameLog getGameLog() {
		return gameLog;
	}

	public InfoPanel getInfoPanel() {
		return infoPanel;
	}
}
