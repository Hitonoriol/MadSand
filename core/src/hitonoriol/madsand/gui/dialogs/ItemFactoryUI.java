package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gamecontent.Npcs;
import hitonoriol.madsand.gamecontent.Objects;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.map.ItemProducer;
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

	private final static String addConsumableStr = "Add ", takeProductStr = "Take ";
	private final static String storageStr = " in storage: ";

	private final static int PRECISION = 4;

	private String producerName;
	private String consumedMaterial, producedMaterial;

	private ItemProducer producer;
	private Player player;

	private TextButton upgradeButton = new TextButton("", Gui.skin);
	private TextButton addConsumableButton = new TextButton("", Gui.skin),
			takeProductButton = new TextButton("", Gui.skin);
	private Slider consumableSlider = new Slider(0, 1, 1, false, Gui.skin),
			productSlider = new Slider(0, 1, 1, false, Gui.skin);
	private Label productStorageLabel = new Label("", Gui.skin), consumableStorageLabel = new Label("", Gui.skin);
	private Label produceLabel = new Label("", Gui.skin), consumeLabel = new Label("", Gui.skin);

	Timer.Task refreshTask = TimeUtils.createTask(() -> refresh());

	public ItemFactoryUI(ItemProducer producer) {
		super(Gui.overlay);
		this.producer = producer;
		this.player = MadSand.player();
		consumedMaterial = Items.all().getName(producer.getConsumedMaterialId());
		producedMaterial = Items.all().getName(producer.getProductId());
		int id = producer.getId();

		if (id > 0)
			producerName = Objects.all().getName(id);
		else
			producerName = Npcs.all().get(-id).name;

		consumableSlider.setSize(ENTRY_WIDTH, ENTRY_HEIGHT);
		productSlider.setSize(ENTRY_WIDTH, ENTRY_HEIGHT);

		refresh();
		initSliderListeners();
		initButtonListeners();
		createLayout();
	}

	private void createLayout() {
		boolean endless = producer.isEndless();

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

		add(new Label(takeProductStr + producedMaterial, Gui.skin));
		if (!endless)
			add(new Label(addConsumableStr + consumedMaterial, Gui.skin));
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

		add(createCloseButton())
				.colspan(2)
				.size(BUTTON_WIDTH, BUTTON_HEIGHT)
				.padTop(PAD_VERTICAL / 2)
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
				.setText("Produces: " + producedMaterial
						+ " (+" + round(producer.getProductionRate() / tickRate) + " per second)");
		consumeLabel
				.setText("Consumes: " + consumedMaterial
						+ " (-" + round(producer.getConsumptionRate() / tickRate) + " per second)");
	}

	private void refreshStorageLabels() {
		productStorageLabel
				.setText(producedMaterial + storageStr + Utils.round(producer.getProductStorage()));

		consumableStorageLabel
				.setText(consumedMaterial + storageStr + Utils.round(producer.getConsumedMaterialStorage()));
	}

	private void initButtonListeners() {
		Gui.setAction(upgradeButton, () -> {
			if (!producer.upgrade())
				Gui.drawOkDialog(
						"There's not enough " + consumedMaterial + " in storage for upgrade!");
			else
				refresh();
		});

		Gui.setAction(addConsumableButton, () -> {
			int quantity = (int) consumableSlider.getValue();
			if (!player.inventory.delItem(producer.getConsumedMaterialId(), quantity))
				Gui.drawOkDialog("You don't have this many " + consumedMaterial);
			else {
				producer.addRawMaterial(quantity);
				refresh();
			}
		});

		Gui.setAction(takeProductButton, () -> {
			player.addItem(producer.getProduct((int) productSlider.getValue()));
			refresh();
		});
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
		super.setTitle(producerName + " Lvl. " + producer.getLvl());
	}

	private void refreshSliders() {
		Item consumable = player.inventory.getItem(producer.getConsumedMaterialId());
		consumableSlider.setRange(0, consumable.quantity);
		productSlider.setRange(0, producer.getProductStorage());
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
		return addConsumableStr + quantity + " " + consumedMaterial;
	}

	private String getTakeBtnString(int quantity) {
		return takeProductStr + quantity + " " + producedMaterial;
	}

	private String getUpgradeString() {
		if (producer.getLvl() < producer.getMaxLvl())
			return "Upgrade with " + Utils.round(producer.getUpgradeRequirement()) + " " + consumedMaterial;
		else
			return "Max level reached";
	}

	private static String round(float rate) {
		return Utils.round(rate, PRECISION);
	}
}
