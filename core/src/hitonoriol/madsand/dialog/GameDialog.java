package hitonoriol.madsand.dialog;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TypingLabel;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.screens.AbstractScreen;
import hitonoriol.madsand.util.Utils;

public class GameDialog extends Dialog {
	private static final float TITLE_YPAD = 18, TITLE_XPAD = 3;
	private static final float TEXT_YPAD = 15, BTN_TEXT_XPAD = 30;
	public static final float PADDING = 10;
	public static final float FADE_DURATION = 0.3f;
	private final static String STYLE_NAME = "dialog";

	private AutoFocusScrollPane textScroll;
	private Table dialogContainer = Widgets.table();
	protected TypingLabel textLbl;
	protected TextButton proceedButton;
	protected Stage stage;
	private boolean hasNext = false;
	private float fadeInDuration = FADE_DURATION;
	private float fadeOutDuration = HIDE_DURATION;

	public GameDialog(String title, String text, Stage stage) {
		super(title, Gui.skin, STYLE_NAME);
		this.stage = stage;
		setMovable(false);

		// Set up title
		Table titleTbl = getTitleTable();
		titleTbl.getCell(getTitleLabel());
		titleTbl.padTop(TITLE_YPAD).padLeft(TITLE_XPAD);
		row();

		// Set up main text label
		textLbl = Widgets.typingLabel(text);
		textLbl.setAlignment(Align.topLeft);
		textLbl.setWrap(true);

		// Set up the layout
		dialogContainer.add(textLbl).width(defaultWidth());
		dialogContainer.align(Align.topLeft);
		add(textScroll = new AutoFocusScrollPane(dialogContainer))
			.height(defaultHeight())
			.pad(PADDING)
			.padTop(TEXT_YPAD).row();
		Gui.setClickAction(this, () -> textLbl.skipToTheEnd());
		getButtonTable().defaults().size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT);
	}

	public GameDialog(String text, Stage stage) {
		this("", text, stage);
	}

	public GameDialog(Stage stage) {
		this("", "", stage);
		Cell<AutoFocusScrollPane> cell = getCell(textScroll);
		textScroll.remove();
		getCells().removeValue(cell, true);
		invalidate();
	}

	public GameDialog() {
		this((Stage) null);
	}

	public GameDialog(AbstractScreen<? extends Stage> screen) {
		this(screen.getStage());
	}

	public GameDialog(String title, String text, Stage stage, String okText) {
		this(title, text, stage);
		addOkButton(okText);
	}

	public Cell<Label> skipLine() {
		Cell<Label> cell = add("");
		cell.row();
		return cell;
	}

	public GameDialog appendText(String text) {
		Utils.dbg("Appending `%s` to `%s`", text, textLbl.storedText);
		setText(textLbl.storedText += text);
		return this;
	}

	public GameDialog newLine(int repeat) {
		return appendText(StringUtils.repeat(Resources.LINEBREAK, repeat));
	}

	public GameDialog newLine() {
		return newLine(1);
	}

	public void setText(String text) {
		sizeChanged();
		textLbl.restart(TextSubstitutor.replace(text));
	}

	public GameDialog setTitle(String text) {
		super.getTitleLabel().setText(text);
		return this;
	}

	public void centerTitle() {
		super.getTitleLabel().setAlignment(Align.center);
	}

	@Override
	public boolean remove() {
		boolean ret = super.remove();
		Utils.dbg("Closing %X", hashCode());
		Gui.closeDialog();
		Gui.overlay.refreshActionButton();
		return ret;
	}

	private final static float HIDE_SCALE = 0.05f, HIDE_DURATION = 0.25f;

	@Override
	public void hide() {
		setOrigin(Align.center);
		if (!hasNext)
			hide(
				Actions.parallel(
					fadeOut(fadeOutDuration, Interpolation.fade),
					Actions.scaleTo(HIDE_SCALE, HIDE_SCALE, HIDE_DURATION, Interpolation.smoother)
				)
			);
		else
			hide(fadeOut(fadeOutDuration, Interpolation.fade));
	}

	@Override
	public Dialog show(Stage stage) {
		Utils.dbg("Opening %X", hashCode());
		Gui.openDialog();
		setScale(1);
		if (fadeInDuration == 0)
			super.show(stage, null);
		else
			show(
				stage, sequence(
					Actions.alpha(0),
					Actions.fadeIn(fadeInDuration, Interpolation.fade),
					Actions.run(() -> Gui.overlay.hideActionBtn())
				)
			);
		centerOnStage();
		return this;
	}

	public void centerOnStage(boolean animated) {
		addAction(
			Actions.moveTo(
				Math.round((stage.getWidth() - getWidth()) / 2),
				Math.round((stage.getHeight() - getHeight()) / 2),
				animated ? 0.125f : 0f
			)
		);
	}

	public void centerOnStage() {
		centerOnStage(false);
	}

	protected void chainReply(TextButton replyButton, final GameDialog nextDialog) {
		if (!hasNext)
			hasNext = true;

		Gui.setAction(replyButton, () -> {
			nextDialog.show(stage);
			hide();
		});
	}

	public void chainReply(String btnText, GameDialog nextDialog) {
		TextButton nextBtn = Widgets.button(btnText);
		addButton(nextBtn);
		chainReply(nextBtn, nextDialog);
	}

	public void addOkButton(String text) {
		TextButton okBtn = Widgets.button(text);
		Gui.setAction(okBtn, () -> hide());
		addButton(okBtn);
	}

	public void addLuaButton(String buttonText, String luaCode) {
		TextButton button = Widgets.button(buttonText);
		Gui.setAction(button, () -> Lua.execute(luaCode));
		addButton(button);
	}

	public Cell<TextButton> addButton(TextButton button, boolean breakRow) {
		if (proceedButton == null)
			proceedButton = button;

		Cell<TextButton> cell = add(button)
			.width(Math.max(Gui.BTN_WIDTH, Gui.getTextWidth(button.getText()) + BTN_TEXT_XPAD))
			.height(Gui.BTN_HEIGHT)
			.padBottom(PADDING / 2);
		if (breakRow)
			cell.row();
		return cell;
	}

	public Cell<TextButton> addButton(TextButton button) {
		return addButton(button, true);
	}

	public Cell<TextButton> addButton(String text, Runnable action) {
		Cell<TextButton> cell = addButton(Widgets.button(text));
		Gui.setAction(cell.getActor(), action);
		return cell;
	}

	public TextButton getProceedButton() {
		return proceedButton;
	}

	public void show() {
		if (stage == null)
			stage = MadSand.getStage();

		show(stage);
	}

	protected TextButton createCloseButton() {
		TextButton closeButton = Widgets.button("Close");
		Gui.setAction(closeButton, () -> hide());
		return closeButton;
	}

	public Cell<TextButton> addCloseButton(float width, float height) {
		return add(createCloseButton()).size(width, height);
	}

	public Cell<TextButton> addCloseButton() {
		return addCloseButton(Gui.BTN_WIDTH, Gui.BTN_HEIGHT);
	}

	@Override
	public <T extends Actor> Cell<T> add(T actor) {
		if (bordered)
			return dialogContainer.add(actor);
		else
			return super.add(actor);
	}

	public void setStageBackgroundOpacity(float opacity) {
		var style = new WindowStyle(getStyle());
		var color = Color.BLACK;
		color.a = opacity;
		style.stageBackground = GuiSkin.getColorDrawable(color);
		setStyle(style);
	}

	public void setBackgroundOpacity(float opacity) {
		var color = Color.BLACK;
		color.a = opacity;
		background(GuiSkin.getTintedDrawable("dialog", color));
	}

	private boolean bordered = false;
	private final static int TITLE_PAD = 20, BORDER_PAD = 5;

	protected final void makeBordered() {
		clear();
		dialogContainer.clear();
		setBackground(GuiSkin.getColorDrawable(Color.DARK_GRAY));
		dialogContainer.setBackground(GuiSkin.getColorDrawable(Color.LIGHT_GRAY));
		add(dialogContainer).pad(BORDER_PAD).padTop(TITLE_PAD);
		dialogContainer.pad(BORDER_PAD);
		dialogContainer.row();
		bordered = true;
	}

	public void setFadeOutDuration(float duration) {
		fadeOutDuration = duration;
	}

	public void setFadeInDuration(float duration) {
		fadeInDuration = duration;
	}

	public TypingLabel getTextLabel() {
		return textLbl;
	}

	public static GameDialog generateDialogChain(String text, Stage stage) {
		return new DialogChainGenerator(text).generate(stage);
	}
	
	public static float defaultWidth() {
		return Gui.screenWidth(0.45f);
	}
	
	public static float defaultHeight() {
		return Gui.screenHeight(0.4f);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof GameDialog))
			return false;

		return getClass().getSimpleName().equals(obj.getClass().getSimpleName());
	}
}
