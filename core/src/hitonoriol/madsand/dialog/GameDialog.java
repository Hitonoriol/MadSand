package hitonoriol.madsand.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.screens.AbstractScreen;
import hitonoriol.madsand.util.TimeUtils;

public class GameDialog extends Dialog {
	public static final float BTN_WIDTH = Gdx.graphics.getWidth() / 4;
	public static final float BTN_HEIGHT = 35;

	private static final float TITLE_YPADDING = 18;
	private static final float TITLE_XPADDING = 3;
	private static final float TEXT_YPADDING = 15;

	public static final float WIDTH = 500;
	public static final float HEIGHT = 250;
	public static final float PADDING = 10;

	private AutoFocusScrollPane textScroll;
	protected Table dialogContainer = new Table(Gui.skin);
	protected Label textLbl;
	protected TextButton proceedButton;
	protected Stage stage;
	private float cWidth = -1, cHeight = -1;

	public GameDialog(String title, String text, Stage stage) {
		super(title, Gui.skin);
		Table titleTbl = super.getTitleTable();
		Label titleLbl = super.getTitleLabel();
		titleTbl.getCell(titleLbl);
		titleTbl.padTop(TITLE_YPADDING).padLeft(TITLE_XPADDING);

		row();
		textLbl = new Label("", Gui.skin);
		setText(text);
		textLbl.setAlignment(Align.topLeft);
		textLbl.setWrap(true);
		dialogContainer.add(textLbl).size(WIDTH, HEIGHT);
		dialogContainer.align(Align.topLeft);
		add(textScroll = new AutoFocusScrollPane(dialogContainer)).size(WIDTH, HEIGHT)
				.pad(PADDING)
				.padTop(TEXT_YPADDING).row();
		this.stage = stage;
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

	public void setText(String text) {
		textLbl.setText(GameTextSubstitutor.replace(text));
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
		TimeUtils.scheduleTask(() -> {
			Gui.gameResumeFocus(this);
			Gui.overlay.refreshActionButton();
		}, Gui.DELAY);
		return ret;
	}

	@Override
	public Dialog show(Stage stage) {
		Dialog ret = super.show(stage);
		Gui.overlay.hideTooltip();
		Gui.overlay.gameContextMenu.setVisible(false);
		TimeUtils.scheduleTask(() -> Gui.gameUnfocus(), Gui.DELAY);
		return ret;
	}

	public void chainReply(TextButton replyButton, final GameDialog nextDialog) {
		replyButton.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				hide();
				nextDialog.show(stage);
			}
		});
	}

	public void chainReply(String btnText, GameDialog nextDialog) {
		TextButton nextBtn = new TextButton(btnText, Gui.skin);
		addButton(nextBtn);
		chainReply(nextBtn, nextDialog);
	}

	public void addOkButton(String text) {
		TextButton okBtn = new TextButton(text, Gui.skin);
		okBtn.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent event, Actor actor) {
				hide();
			}
		});
		addButton(okBtn);
	}

	public void addLuaButton(String buttonText, String luaCode) {
		TextButton button = new TextButton(buttonText, Gui.skin);
		button.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Lua.execute(luaCode);
			}
		});
		addButton(button);
	}

	public Cell<TextButton> addButton(TextButton button) {
		if (proceedButton == null)
			proceedButton = button;

		Cell<TextButton> cell = add(button).width(BTN_WIDTH).height(BTN_HEIGHT).padBottom(PADDING / 2);
		cell.row();
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

	public TextButton createCloseButton() {
		TextButton closeButton = new TextButton("Close", Gui.skin);
		Gui.setAction(closeButton, () -> remove());
		return closeButton;
	}

	public Cell<TextButton> addCloseButton(float width, float height) {
		return add(createCloseButton()).size(width, height);
	}

	public Cell<TextButton> addCloseButton() {
		return addCloseButton(Gui.BTN_WIDTH, Gui.BTN_HEIGHT);
	}

	public void setPrefSize(float width, float height) {
		setPrefHeight(height);
		setPrefWidth(width);
	}

	public void setPrefHeight(float height) {
		cHeight = height;
	}

	public void setPrefWidth(float width) {
		cWidth = width;
	}

	@Override
	public float getPrefWidth() {
		if (cWidth <= 0)
			return super.getPrefWidth();
		else
			return cWidth;
	}

	@Override
	public float getPrefHeight() {
		if (cHeight <= 0)
			return super.getPrefHeight();
		else
			return cHeight;
	}

	@Override
	public <T extends Actor> Cell<T> add(T actor) {
		if (bordered)
			return dialogContainer.add(actor);
		else
			return super.add(actor);
	}

	private boolean bordered = false;
	private final static int TITLE_PAD = 20, BORDER_PAD = 5;

	protected final void makeBordered() {
		clear();
		dialogContainer.clear();
		setBackground(Gui.getColorDrawable(Color.DARK_GRAY));
		dialogContainer.setBackground(Gui.getColorDrawable(Color.LIGHT_GRAY));
		add(dialogContainer).pad(BORDER_PAD).padTop(TITLE_PAD);
		dialogContainer.pad(BORDER_PAD);
		bordered = true;
	}

	public static GameDialog generateDialogChain(String text, Stage stage) {
		return new DialogChainGenerator(text).generate(stage);
	}

	public boolean isOnlyDialog() { // If this dialog is the only one in stage
		return !Gui.hasDialogs(stage, this);
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
