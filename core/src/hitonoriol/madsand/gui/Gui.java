package hitonoriol.madsand.gui;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static hitonoriol.madsand.MadSand.getRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.inventory.CraftDialog;
import hitonoriol.madsand.gui.dialogs.OkDialog;
import hitonoriol.madsand.gui.stages.Overlay;
import hitonoriol.madsand.util.BooleanTally;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;

public class Gui {
	public static final float DEFAULT_WIDTH = Gdx.graphics.getWidth() / 4;
	public static final int BTN_WIDTH = 150, BTN_HEIGHT = 30;
	public final static int FONT_XXS = 12, FONT_XS = 14, FONT_S = 16, FONT_M = 20, FONT_L = 24, FONT_XL = 28;
	public final static float DELAY = 0.05f;

	private static BooleanTally gameFocus = new BooleanTally();
	private static BooleanTally gameDialogs = new BooleanTally();

	public static final Skin skin = GuiSkin.get();

	public static Overlay overlay;
	private static ScreenViewport viewport = new ScreenViewport();
	private static GlyphLayout glyphLayout = new GlyphLayout();

	public static void fitChildren(WidgetGroup group) {
		group.setSize(group.getPrefWidth(), group.getPrefHeight());
	}

	public static float screenHeight() {
		return Gdx.graphics.getHeight();
	}

	public static float screenWidth() {
		return Gdx.graphics.getWidth();
	}

	public static float screenHeight(float coef) {
		return Gdx.graphics.getHeight() * coef;
	}

	public static float screenWidth(float coef) {
		return Gdx.graphics.getWidth() * coef;
	}

	public static void screenSize(Actor actor, float widthCoef, float heightCoef) {
		actor.setSize(
			screenWidth(widthCoef),
			screenHeight(heightCoef)
		);
	}

	public static ScreenViewport viewport() {
		return viewport;
	}

	public static void openCraftMenu(int id) {
		new CraftDialog(id).show();
	}

	private static OkDialog drawOkDialog(String title, String msg, Stage stage) {
		var dialog = new OkDialog(title, msg, stage);
		TimeUtils.scheduleTask(() -> dialog.show(), DELAY);
		return dialog;
	}

	public static OkDialog drawOkDialog(String title, String msg) {
		return drawOkDialog(title, msg, null);
	}

	public static OkDialog drawOkDialog(String msg) {
		return drawOkDialog(OkDialog.DEFAULT_TITLE, msg);
	}

	public static void doLater(Runnable action) {
		Gdx.app.postRunnable(action);
	}

	public static <T extends Group> void seamlessRefresh(T container, Runnable populator, float duration) {
		container.addAction(
			sequence(
				fadeOut(duration),
				run(() -> {
					container.clearChildren();
					populator.run();
				}),
				fadeIn(duration)
			)
		);
	}

	public static <T extends Group> void seamlessRefresh(T container, Runnable populator) {
		seamlessRefresh(container, populator, 0.1f);
	}

	public static void openDialog() {
		overlay.hideTooltip();
		overlay.getContextMenu().close();
		gameDialogs.action();
	}

	public static void closeDialog() {
		if (gameDialogs.reverseAction())
			overlay.showTooltip();
		Utils.dbg("[Dialog closed (all dialogs closed = %b)]", gameDialogs.isNeutral());
	}

	public static boolean isDialogActive() {
		return !gameDialogs.isNeutral();
	}

	public static void unfocusGame() {
		gameFocus.action();
		overlay.hideTooltip();
	}

	public static void resumeGameFocus() {
		if (gameFocus.reverseAction() && !isDialogActive())
			overlay.showTooltip();
	}

	public static void resetGameFocus() {
		gameFocus.reset();
	}

	public static boolean isGameUnfocused() {
		return !gameFocus.isNeutral() || isDialogActive();
	}

	public static void refreshOverlay() {
		MadSand.player().doAfterMovement(() -> overlay.refresh());
	}

	public static float horizontalCenter(Actor actor) {
		return (Gdx.graphics.getWidth() / 2) - actor.getWidth();
	}

	public static <T extends Drawable> T setMinSize(T drawable, float width, float height) {
		drawable.setMinWidth(width);
		drawable.setMinHeight(height);
		if (width == 0) {
			drawable.setLeftWidth(width);
			drawable.setRightWidth(width);
		}
		if (height == 0) {
			drawable.setTopHeight(height);
			drawable.setBottomHeight(height);
		}
		return drawable;
	}

	public static <T extends Drawable> T clearMinSize(T drawable) {
		return setMinSize(drawable, 0, 0);
	}

	public static <T extends Drawable> T setMinSize(T drawable, float size) {
		return setMinSize(drawable, size, size);
	}

	public static ClickListener setClickAction(Actor actor, int button, Runnable action) {
		ClickListener listener;
		actor.addListener(listener = new ClickListener(button) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				action.run();
			}
		});
		return listener;
	}

	public static ClickListener setClickAction(Actor actor, Runnable action) {
		return setClickAction(actor, Buttons.LEFT, action);
	}

	public static Actor setAction(Actor actor, Runnable action) {
		actor.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				action.run();
			}
		});
		return actor;
	}

	public static BitmapFont getFont(int size) {
		return GuiSkin.getLabelStyle(size).font;
	}

	public static Label setFontSize(Label label, int size) {
		label.setStyle(GuiSkin.getLabelStyle(size));
		return label;
	}

	private static GlyphLayout modifyGlyph(CharSequence text, int fontSize) {
		glyphLayout.setText(getFont(fontSize), text);
		return glyphLayout;
	}

	private static GlyphLayout modifyGlyph(CharSequence text) {
		return modifyGlyph(text, FONT_S);
	}

	public static float getTextWidth(CharSequence text, int fontSize) {
		return modifyGlyph(text, fontSize).width;
	}

	public static float getTextWidth(CharSequence text) {
		return getTextWidth(text, FONT_S);
	}

	public static float getTextHeight(String text) {
		return modifyGlyph(text).height;
	}

	public static float getTextHeight(String text, int fontSize) {
		return modifyGlyph(text, fontSize).height;
	}

	public static Cell<Label> skipLine(Table table) {
		Cell<Label> cell = table.add(new Label("", skin));
		cell.row();
		return cell;
	}

	public static void removeActor(Table table, Actor actor) {
		Cell<?> cell = table.getCell(actor);
		actor.remove();
		table.getCells().removeValue(cell, true);
		table.invalidate();
	}

	/*
	 * Horizontally center object `B` relatively to object `A` (that may be a world object, so camera's zoom may be applied)
	 * Resulting value is the `x` coordinate to be set for object `B`
	 */
	public static float relativeCenterX(float aX, float aWidth, float bWidth, boolean projectedFromWorld) {
		return (aX + aWidth * 0.5f * (projectedFromWorld ? getRenderer().getCamZoom() : 1f)) - bWidth * 0.5f;
	}

	public static float relativeCenterX(float aX, float aWidth, float bWidth) {
		return relativeCenterX(aX, aWidth, bWidth, true);
	}
}
