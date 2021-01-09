package hitonoriol.madsand.gui.dialogs;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;

public class SliderDialog extends GameDialog {

	private static final float TITLE_YPADDING = 18;
	private static final float TITLE_XPADDING = 3;

	private static float BUTTON_WIDTH = 100;
	private static float BUTTON_HEIGHT = 35;
	private static float BUTTON_PADDING = 10;

	private static float WIDTH = 400;
	private static float DEFAULT_HEIGHT = 75;

	private static float BUTTONS_PAD_TOP = DEFAULT_HEIGHT / 3;
	private static float LABEL_PAD_BOTTOM = 15;

	private static float SLIDER_WIDTH = WIDTH;
	private static float SLIDER_HEIGHT = DEFAULT_HEIGHT / 2;

	private static int SLIDER_STEP = 1;
	private static String cancelText = "Cancel";
	private static String confirmText = "Confirm";

	private Label titleLabel; // Dialog title

	private Label topLabel; // Label above the slider
	private Label bottomLabel; // Label with item quantity and total cost (under the slider)
	protected Slider slider; // Item quantity slider

	private Table buttonTable;
	private TextButton cancelButton;
	private TextButton confirmButton;

	protected int minValue, maxValue;
	protected int currentQuantity;

	private SliderDialog(Stage stage) {
		super(stage);
		super.row();
		super.setWidth(WIDTH);
		Table titleTbl = super.getTitleTable();
		titleLabel = super.getTitleLabel();
		titleLabel.setAlignment(Align.center);

		titleTbl.padTop(TITLE_YPADDING).padLeft(TITLE_XPADDING);

		cancelButton = new TextButton(cancelText, Gui.skin);
		confirmButton = new TextButton(confirmText, Gui.skin);
		buttonTable = new Table();
		buttonTable.add(confirmButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(BUTTON_PADDING);
		buttonTable.add(cancelButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(BUTTON_PADDING);

		topLabel = new Label("", Gui.skin);
		bottomLabel = new Label("", Gui.skin);
	}

	public SliderDialog(int min, int max) {
		this(Gui.overlay);
		minValue = min;
		maxValue = max;
		topLabel.setWrap(true);
		topLabel.setAlignment(Align.bottomLeft);
		slider = new Slider(minValue, maxValue, SLIDER_STEP, false, Gui.skin);
		slider.setHeight(SLIDER_HEIGHT);

		super.add(topLabel).size(WIDTH, DEFAULT_HEIGHT).align(Align.left).padBottom(LABEL_PAD_BOTTOM).row();
		super.add(slider).size(SLIDER_WIDTH, DEFAULT_HEIGHT).align(Align.top).row();
		super.add(bottomLabel).align(Align.center).row();
		super.add(buttonTable).width(WIDTH).align(Align.center).padTop(BUTTONS_PAD_TOP).row();

		setCancelListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}
		});
	}
	
	public SliderDialog(int max) {
		this(1, max);
	}

	public SliderDialog setConfirmAction(Consumer<Integer> action) {
		confirmButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				action.accept(getSliderValue());
				remove();
			}
		});
		return this;
	}

	public SliderDialog setSliderListener(ChangeListener listener) {
		slider.addListener(listener);
		return this;
	}

	public SliderDialog setOnUpdateText(String text) { // Overrides previous listener
		setSliderListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setSliderText(getSliderValue() + " " + text);
			}
		});
		// Refresh this label by triggering change event
		slider.setValue(slider.getMaxValue());
		slider.setValue(minValue);
		return this;
	}
	
	public SliderDialog setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public SliderDialog setSliderTitle(String text) { // Set text above the slider
		topLabel.setText(text);
		return this;
	}

	public void setCancelListener(ChangeListener listener) {
		cancelButton.addListener(listener);
	}

	public int getSliderValue() {
		return (int) slider.getValue();
	}

	public void setSliderText(String text) { // Set text under the slider
		bottomLabel.setText(text);
	}

}
