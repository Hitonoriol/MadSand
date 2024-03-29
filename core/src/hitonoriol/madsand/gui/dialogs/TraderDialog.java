package hitonoriol.madsand.gui.dialogs;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.npc.Trader;
import hitonoriol.madsand.entities.quest.ProceduralQuest;
import hitonoriol.madsand.gamecontent.Globals;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoSizeTooltip;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.Location;

public class TraderDialog extends GameDialog {
	private Trader npc;

	public TraderDialog(Player player, Trader npc) {
		super(npc.stats.name, Utils.randElement(Globals.values().traderGreetings), Gui.overlay);
		this.npc = npc;

		var tradeButton = Widgets.button("What do you have for sale?");
		var helpButton = Widgets.button("Do you need any help?");
		var closeButton = Widgets.button("Bye");

		helpButton.addListener(
			new AutoSizeTooltip("Completing tasks for this trader will increase their currency supply")
		);

		super.addButton(tradeButton);

		if (!MadSand.world().inEncounter() && MadSand.world().curLayer() == Location.LAYER_OVERWORLD) {
			super.addButton(helpButton);
			Gui.setAction(helpButton, () -> {
				var quest = player.getQuestWorker().startProceduralQuest(npc.uid());
				if (quest != ProceduralQuest.timeoutQuest)
					addQuestReward(quest);
				remove();
			});
		}

		super.addButton(closeButton);
		Gui.setAction(tradeButton, () -> player.tradeWith(npc));
		Gui.setAction(closeButton, this::remove);
	}

	private void addQuestReward(ProceduralQuest quest) {
		int currency = Globals.values().currencyId;
		int curQuantity = npc.rollTraderCurrency();
		var currencyName = Items.all().getName(currency);
		quest.endMsg += Resources.LINEBREAK +
			DialogChainGenerator.LBRACKET + "Trader gets +" + curQuantity + " " + currencyName + "s"
			+ DialogChainGenerator.RBRACKET;
		quest.execOnCompletion = "world:getCurLoc():getNpc(" + npc.uid() + "):addItem(" + currency + ", " + curQuantity
			+ ");";
	}
}
