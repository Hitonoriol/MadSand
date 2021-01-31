package hitonoriol.madsand.entities.inventory.item;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Pair;

public class Projectile extends LevelBoundItem {
	public int dmg;
	public boolean thrownByHand = false;

	public Projectile(Projectile protoItem) {
		super(protoItem);
		dmg = protoItem.dmg;
		thrownByHand = protoItem.thrownByHand;
	}
	
	public Projectile() {
		super();
	}
	
	static final float BASE_PROJECTILE_SPEED = 0.35f;

	public void launchProjectile(Pair from, Pair to, Runnable impactAction) {
		final int imgSize = (int) (MadSand.TILESIZE * MadSand.gameWorld.getCamZoom());
		Image projectileImg = new Image(Resources.item[id]);
		Vector3 screenCoords = new Vector3();
		projectileImg.setOrigin(Align.center);
		projectileImg.setSize(imgSize, imgSize);
		projectileImg
				.addAction(Actions.rotateTo((float) Math.toDegrees(Math.atan2(from.y - to.y, from.x - to.x)) + 90f));
		Gui.overlay.addActor(projectileImg);

		MadSand.getCamera().project(screenCoords.set(from.x, from.y, 0));
		projectileImg.setPosition(screenCoords.x, screenCoords.y);

		MadSand.getCamera().project(screenCoords.set(to.x, to.y, 0));
		projectileImg.addAction(
				Actions.sequence(
						Actions.moveTo(screenCoords.x, screenCoords.y, BASE_PROJECTILE_SPEED),
						Actions.run(impactAction),
						Actions.run(() -> projectileImg.remove())));
	}

	@Override
	public String getMiscInfo() {
		return "Projectile damage: [GREEN]" + dmg + Resources.COLOR_END + Resources.LINEBREAK;
	}
}
