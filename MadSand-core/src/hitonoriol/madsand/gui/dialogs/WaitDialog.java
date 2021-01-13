package hitonoriol.madsand.gui.dialogs;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.world.World;

public class WaitDialog extends SliderDialog {

	private int worldSeconds;

	public WaitDialog(int maxTimeSkip) {
		super(maxTimeSkip);
		super.setTitle("Skip Time");
		super.setSliderTitle("How much time to skip:");
		super.setSliderAction(
				ticks -> super.setSliderText(Utils.timeString(worldSeconds = MadSand.world.toWorldTimeSeconds(ticks))));
		super.setConfirmAction(ticks -> {
			World.player.skipTime(ticks);
			World.player.inventory.delItem(Globals.getInt(Globals.TIMESKIP_ITEM), ticks);
			MadSand.notice("Skipped " + Utils.timeString(worldSeconds));
		});
	}
}
