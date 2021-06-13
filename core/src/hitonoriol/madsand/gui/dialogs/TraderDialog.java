package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.npc.Trader;
import hitonoriol.madsand.entities.quest.ProceduralQuest;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.Location;

public class TraderDialog extends GameDialog {

	Trader npc;

	public TraderDialog(Player player, Trader npc) {
		super(npc.stats.name, Utils.randElement(Globals.values().traderGreetings), Gui.overlay);
		this.npc = npc;

		TextButton tradeButton = new TextButton("What do you have for sale?", Gui.skin);
		TextButton helpButton = new TextButton("Do you need any help?", Gui.skin);
		TextButton closeButton = new TextButton("Bye", Gui.skin);

		super.addButton(tradeButton);

		if (!MadSand.world().inEncounter && MadSand.world().curLayer() == Location.LAYER_OVERWORLD) {
			super.addButton(helpButton);
			helpButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					ProceduralQuest quest = player.getQuestWorker().startProceduralQuest(npc.uid);
					if (quest != ProceduralQuest.timeoutQuest)
						addQuestReward(quest);
					remove();
				}
			});
		}

		super.addButton(closeButton);

		tradeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				player.interact(npc);
				remove();
			}
		});

		closeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}
		});
	}

	private void addQuestReward(ProceduralQuest quest) {
		int currency = Globals.values().currencyId;
		int curQuantity = npc.rollTraderCurrency();
		String currencyName = ItemProp.getItemName(currency);
		quest.endMsg += Resources.LINEBREAK +
				DialogChainGenerator.LBRACKET + "Trader gets +" + curQuantity + " " + currencyName + "s"
				+ DialogChainGenerator.RBRACKET;
		quest.execOnCompletion = "world:getCurLoc():getNpc(" + npc.uid + "):addItem(" + currency + ", " + curQuantity
				+ ");";
	}

}
