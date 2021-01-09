package hitonoriol.madsand.minigames;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.world.World;

public class CardGameUI extends GameDialog {

	protected int currency = Globals.getCurrency().id;
	protected String currencyName = ItemProp.getItemName(currency);

	protected Label betLabel = new Label("", Gui.skin);
	protected MapObject object;
	protected TextButton closeButton;

	public CardGameUI(MapObject object) {
		super(Gui.overlay);
		Card.loadTextures();
		this.object = object;
	}

	protected void showBetDialog(Consumer<Integer> confirmAction) {
		if (!World.player.inventory.hasItem(currency, 1)) {
			Gui.drawOkDialog("You don't have any money!", Gui.overlay);
			endGame();
			return;
		}
		new SliderDialog(World.player.inventory.getItem(currency).quantity)
				.setSliderTitle("Place your bet:")
				.setOnUpdateText(currencyName + "s")
				.setConfirmAction(confirmAction)
				.setTitle("Bet")
				.show();
	}

	public void setBetText(int bet) {
		betLabel.setText("Bet: " + bet + " " + currencyName + "s");
	}

	protected void startGame() {
		closeButton.setVisible(false);
		object.takeFullDamage();
		if (object.id == MapObject.NULL_OBJECT_ID) {
			remove();
			Gui.drawOkDialog("Oops",
					"As you were about to press one of the machine's buttons it exploded into pieces!",
					Gui.overlay);
			Gui.overlay.processActionMenu();
			return;
		}
	}

	protected void endGame() {
		closeButton.setVisible(true);
	}

	@Override
	public Cell<TextButton> addCloseButton() {
		Cell<TextButton> cell = super.addCloseButton(Gui.BTN_WIDTH, Gui.BTN_HEIGHT);
		closeButton = cell.getActor();
		return cell;
	}

}
