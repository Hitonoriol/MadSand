package hitonoriol.madsand.gui.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
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
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.entities.inventory.Item;
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
import hitonoriol.madsand.world.World;

/*
 * Main in-game overlay (GameStage.GAME) 
 */

public class Overlay extends Stage {
	Skin skin;
	static float SIDEBAR_PADDING = 100;
	static float SIDEBAR_XPADDING = 5;

	public CharacterInfoWindow statWindow;
	public CharacterCreationDialog charCreateDialog;

	public GameTooltip gameTooltip;
	public GameContextMenu gameContextMenu;
	public ActionButton actionButton;
	public GameLog gameLog;
	public OverlayBottomMenu bottomMenu;
	public EquipmentSidebar equipmentSidebar;

	static Label overlayStatLabel;
	static final int OVSTAT_COUNT = 6;

	public Overlay() {
		super();
		skin = Gui.skin;
		initMouseListeners();
		initOverlayStats();

		actionButton = new ActionButton();
		gameTooltip = new GameTooltip();
		gameContextMenu = new GameContextMenu();
		gameLog = new GameLog();
		bottomMenu = new OverlayBottomMenu(this);

		equipmentSidebar = new EquipmentSidebar();
		equipmentSidebar.setPosition(this.getWidth() - SIDEBAR_XPADDING, equipmentSidebar.getHeight() + SIDEBAR_PADDING,
				Align.topRight);

		Mouse.tooltipContainer = gameTooltip;

		super.addActor(equipmentSidebar);
		super.addActor(bottomMenu);
		super.addActor(gameTooltip);
		super.addActor(gameContextMenu);
		super.addActor(gameLog);
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

	private void initOverlayStats() {
		// Init top stat panel
		Table ovstatTbl = new Table();
		ovstatTbl.setFillParent(true);
		ovstatTbl.align(Align.topRight);
		overlayStatLabel = new Label(" ", skin);
		overlayStatLabel.setAlignment(Align.center);
		ovstatTbl.add(overlayStatLabel).width(Gdx.graphics.getWidth() - GameLog.INPUT_FIELD_WIDTH);
		super.addActor(ovstatTbl);
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
				gameLog.inputField.setVisible(!gameLog.inputField.isVisible());
			} catch (Exception e) {
				MadSand.print("Couldn't execute user input");
				e.printStackTrace();
			} finally {
				gameLog.inputField.setText("");
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

	public void refreshOverlay() {
		Player player = World.player;
		Stats stats = player.stats;
		String info = Resources.Tab;
		info += ("HP: " + stats.hp + "/" + stats.mhp) + Resources.Tab;
		info += ("LVL: " + stats.skills.getLvl(Skill.Level)) + Resources.Tab;
		info += ("XP: " + stats.skills.getExpString(Skill.Level)) + Resources.Tab;
		info += ("Food: " + stats.food + "/" + stats.maxFood + " (" + Utils.round(stats.getSatiationPercent()) + "%)")
				+ Resources.Tab;
		info += ("Location: Cell (" + player.x + ", " + player.y + ")" + getSectorString());

		info += Resources.LINEBREAK + Resources.Tab;

		info += "Stamina: " + Utils.round(stats.stamina) + "/" + Utils.round(stats.maxstamina);

		overlayStatLabel.setText(info);
	}

	private String getSectorString() {
		return (MadSand.world.inEncounter
				? " @ Random Encounter"
				: " @ Sector (" + MadSand.world.getCurWPos() + ")");

	}
}
