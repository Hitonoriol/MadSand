package hitonoriol.madsand.minigames;

import java.util.Optional;
import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;

public class CardGameUI extends GameDialog {

	protected final int currency = Globals.getCurrency().id;
	protected final String currencyName = ItemProp.getItemName(currency);

	protected Label betLabel = new Label("", Gui.skin);
	protected Optional<MapEntity> gameMachine;
	protected TextButton closeButton, betButton = new TextButton("Place Bet", Gui.skin);

	public CardGameUI(MapEntity gameMachine) {
		super(Gui.overlay);
		this.gameMachine = Optional.ofNullable(gameMachine);
	}

	public CardGameUI() {
		this(null);
	}

	protected void showBetDialog(Consumer<Integer> confirmAction) {
		if (!MadSand.player().inventory.hasItem(currency, 1)) {
			Gui.drawOkDialog("You don't have any money!");
			//endGame();
			return;
		}
		new SliderDialog(MadSand.player().inventory.getItem(currency).quantity)
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
		betButton.setVisible(false);
		closeButton.setVisible(false);
		gameMachine.ifPresent(machine -> {
			machine.as(MapObject.class).ifPresent(object -> {
				object.takeFullDamage();
				if (object.id == MapObject.NULL_OBJECT_ID) {
					remove();
					Gui.drawOkDialog("Oops",
							"As you were about to press one of the machine's buttons it exploded into pieces!");
					Gui.overlay.refreshActionButton();
					return;
				}
			});
		});
	}

	protected void endGame() {
		closeButton.setVisible(true);
		betButton.setVisible(true);
	}

	@Override
	public Cell<TextButton> addCloseButton() {
		Cell<TextButton> cell = super.addCloseButton(Gui.BTN_WIDTH, Gui.BTN_HEIGHT);
		closeButton = cell.getActor();
		return cell;
	}

}
