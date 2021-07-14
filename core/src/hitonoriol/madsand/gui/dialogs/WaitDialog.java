package hitonoriol.madsand.gui.dialogs;

import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.util.Utils;

public class WaitDialog extends SliderDialog {

	private int worldSeconds, realtimeTicks;
	private int timeSkipItemSpent;

	public WaitDialog(int maxTimeSkip) {
		super((int) Player.TIMESKIP_COEF, maxTimeSkip);
		super.setStep((int) Player.TIMESKIP_COEF);
		super.setTitle("Skip Time");
		super.setSliderTitle("How much time to skip:");
		super.getBottomLabel().setAlignment(Align.center);

		String timeSkipItem = ItemProp.getItemName(Globals.values().timeSkipItem);
		super.setSliderAction(
				ticks -> {
					timeSkipItemSpent = (int) Math.max((float) ticks / Player.TIMESKIP_COEF, 1);
					worldSeconds = MadSand.world().toWorldTimeSeconds(ticks);
					realtimeTicks = (int) (timeSkipItemSpent * Player.REALTIME_SKIP_COEF);
					super.setSliderText("+" + Utils.timeString(worldSeconds) + " [[World time]"
							+ Resources.LINEBREAK

							+ "+" + Utils.timeString((long) (realtimeTicks * MadSand.world().getRealtimeActionSeconds()))
							+ " [[Realtime]"
							+ Resources.LINEBREAK

							+ "-" + timeSkipItemSpent + " " + timeSkipItem);
				});

		super.setConfirmAction(ticks -> {
			MadSand.player().inventory.delItem(Globals.values().timeSkipItem, timeSkipItemSpent);
			MadSand.player().skipTicks(ticks);
			MadSand.world().skipActionTicks(realtimeTicks);
			MadSand.notice("Skipped " + Utils.timeString(worldSeconds));
		});
	}
}