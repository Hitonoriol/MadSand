package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;

public class ItemFactoryUI extends GameDialog {

	final float WIDTH = 500;
	final float HEIGHT = 400;
	final float PAD_RIGHT = 10;
	final float TITLE_PAD = 50;
	final float PAD_VERTICAL = 25;

	final float ENTRY_WIDTH = WIDTH / 2;
	final float ENTRY_HEIGHT = 30;

	final float BUTTON_WIDTH = 80;
	final float BUTTON_HEIGHT = 40;

	final String closeBtnString = "Close";

	final String upgradeString = "Upgrade with ";
	final String addConsumableString = "Add ";
	final String takeProductString = "Take ";
	final String maxLvlString = "Max level reached";
	final String lvlString = " Lvl. ";

	String consumedMaterial, producedMaterial;
	String stationName;

	ItemProducer station;
	Player player;

	TextButton closeButton;

	TextButton upgradeButton;

	TextButton addConsumableButton;
	TextButton takeProductButton;
	Slider consumableSlider;
	Slider productSlider;

	String produceString = "Produces: ";
	String consumeString = "Consumes: ";

	String upgradeLblString = "Upgrade:";
	String storageString = " in storage: ";

	Label productStorageLabel;
	Label consumableStorageLabel;

	Label produceLabel;
	Label consumeLabel;

	Timer.Task refreshTask = TimeUtils.createTask(() -> refresh());

	private ItemFactoryUI(Stage stage) {
		super(stage);
	}

	public ItemFactoryUI(ItemProducer producer) {
		this(Gui.overlay);
		this.station = producer;
		this.player = MadSand.player();
		consumedMaterial = ItemProp.getItemName(producer.getConsumedMaterialId());
		producedMaterial = ItemProp.getItemName(producer.getProductId());
		int id = producer.getId();

		if (id > 0)
			stationName = ObjectProp.getName(id);
		else
			stationName = NpcProp.npcs.get(-id).name;

		closeButton = new TextButton(closeBtnString, Gui.skin);

		upgradeButton = new TextButton("", Gui.skin);
		addConsumableButton = new TextButton("", Gui.skin);
		takeProductButton = new TextButton("", Gui.skin);

		consumableSlider = new Slider(0, 1, 1, false, Gui.skin);
		productSlider = new Slider(0, 1, 1, false, Gui.skin);
		consumableSlider.setSize(ENTRY_WIDTH, ENTRY_HEIGHT);
		productSlider.setSize(ENTRY_WIDTH, ENTRY_HEIGHT);

		productStorageLabel = new Label("", Gui.skin);
		consumableStorageLabel = new Label("", Gui.skin);
		produceLabel = new Label("", Gui.skin);
		consumeLabel = new Label("", Gui.skin);

		refresh();
		initSliderListeners();
		initButtonListeners();
		createLayout();
	}

	private void createLayout() {
		boolean endless = station.isEndless();

		if (endless) {
			produceLabel.setWrap(true);
			produceLabel.setAlignment(Align.center);
		}

		add(produceLabel).width(ENTRY_WIDTH).padTop(TITLE_PAD / 3).row();
		if (!endless)
			add(consumeLabel).width(ENTRY_WIDTH).row();

		add(productStorageLabel).colspan(2).padTop(PAD_VERTICAL - 5).row();
		if (!endless)
			add(consumableStorageLabel).colspan(2).padBottom(PAD_VERTICAL * 2).row();

		add(new Label(takeProductString + producedMaterial, Gui.skin));
		if (!endless)
			add(new Label(addConsumableString + consumedMaterial, Gui.skin));
		row();

		add(productSlider).size(ENTRY_WIDTH, ENTRY_HEIGHT);
		if (!endless)
			add(consumableSlider).size(ENTRY_WIDTH, ENTRY_HEIGHT);
		row();

		add(takeProductButton).size(ENTRY_WIDTH, ENTRY_HEIGHT);
		if (!endless)
			add(addConsumableButton).size(ENTRY_WIDTH, ENTRY_HEIGHT);
		row();

		if (!endless) {
			/*add(new Label(upgradeLblString, Gui.skin)).colspan(2).align(Align.center).row();*/
			add(upgradeButton).size(ENTRY_WIDTH, ENTRY_HEIGHT).padBottom(PAD_VERTICAL * 2).colspan(2)
					.align(Align.center).row();
		} else
			add().padBottom(PAD_VERTICAL * 2).row();

		add(closeButton).colspan(2).size(BUTTON_WIDTH, BUTTON_HEIGHT).padTop(PAD_VERTICAL / 2)
				.align(Align.center);
		setSize(WIDTH, HEIGHT);
		pack();
	}

	public void show() {
		super.show();
		float tickRate = MadSand.world().getRealtimeActionSeconds();
		Timer.instance().scheduleTask(refreshTask, tickRate, tickRate);
	}

	public boolean remove() {
		boolean ret = super.remove();
		refreshTask.cancel();
		return ret;
	}

	private void refreshRateLabels() {
		float tickRate = MadSand.world().getRealtimeActionSeconds();
		produceLabel
				.setText(produceString + producedMaterial + " (+" + Utils.round(station.getProductionRate() / tickRate)
						+ " per second)");
		consumeLabel
				.setText(consumeString + consumedMaterial + " (-" + Utils.round(station.getConsumptionRate() / tickRate)
						+ " per second)");
	}

	private void refreshStorageLabels() {
		productStorageLabel
				.setText(producedMaterial + storageString + Utils.round(station.getProductStorage()));

		consumableStorageLabel
				.setText(consumedMaterial + storageString + Utils.round(station.getConsumedMaterialStorage()));
	}

	private void initButtonListeners() {
		Gui.setAction(upgradeButton, () -> {
			if (!station.upgrade())
				Gui.drawOkDialog(
						"There's not enough " + consumedMaterial + " in storage for upgrade!");
			else
				refresh();
		});

		Gui.setAction(addConsumableButton, () -> {
			int quantity = (int) consumableSlider.getValue();
			if (!player.inventory.delItem(station.getConsumedMaterialId(), quantity))
				Gui.drawOkDialog("You don't have this many " + consumedMaterial);
			else {
				station.addRawMaterial(quantity);
				refresh();
			}
		});

		Gui.setAction(takeProductButton, () -> {
			player.addItem(station.getProduct((int) productSlider.getValue()));
			refresh();
		});
		Gui.setAction(closeButton, () -> remove());
	}

	private void initSliderListeners() {
		consumableSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				refreshAddButton((int) consumableSlider.getValue());
			}
		});

		productSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				refreshTakeButton((int) productSlider.getValue());
			}
		});
	}

	private void refresh() {
		refreshTitle();
		refreshSliders();
		refreshAddButton((int) consumableSlider.getValue());
		refreshTakeButton((int) productSlider.getValue());
		refreshUpgradeButton();
		refreshStorageLabels();
		refreshRateLabels();
	}

	private void refreshTitle() {
		super.setTitle(stationName + lvlString + station.getLvl());
	}

	private void refreshSliders() {
		Item consumable = player.inventory.getItem(station.getConsumedMaterialId());
		consumableSlider.setRange(0, consumable.quantity);
		productSlider.setRange(0, station.getProductStorage());
	}

	private void refreshAddButton(int quantity) {
		addConsumableButton.setText(getAddBtnString(quantity));
	}

	private void refreshTakeButton(int quantity) {
		takeProductButton.setText(getTakeBtnString(quantity));
	}

	private void refreshUpgradeButton() {
		upgradeButton.setText(getUpgradeString());
	}

	private String getAddBtnString(int quantity) {
		return addConsumableString + quantity + " " + consumedMaterial;
	}

	private String getTakeBtnString(int quantity) {
		return takeProductString + quantity + " " + producedMaterial;
	}

	private String getUpgradeString() {
		if (station.getLvl() < station.getMaxLvl())
			return upgradeString + Utils.round(station.getUpgradeRequirement()) + " " + consumedMaterial;
		else
			return maxLvlString;
	}

}
