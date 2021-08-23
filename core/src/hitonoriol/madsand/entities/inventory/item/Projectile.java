package hitonoriol.madsand.entities.inventory.item;

import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.FloatGenerator;

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

	@Override
	public Projectile copy() {
		return new Projectile(this);
	}

	@Override
	public void leftClickAction() {
		toggleEquipped();
	}

	static FloatGenerator dmgRangeGen = JRand.flt().range(0.4f, 1.25f);

	public int calcDamage() {
		if (Utils.percentRoll((float) lvl / 45f))
			return dmg * 2;

		return (int) Math.max(1, dmg * dmgRangeGen.gen());
	}

	static final float BASE_PROJECTILE_SPEED = 0.35f;

	public void launchProjectile(Pair from, Pair to, Consumer<MapEntity> impactAction) {
		final int imgSize = (int) (MadSand.TILESIZE * MadSand.getRenderer().getCamZoom());
		Image projectileImg = new Image(Resources.getItem(id));
		Vector3 screenCoords = new Vector3();
		projectileImg.setOrigin(Align.center);
		projectileImg.setSize(imgSize, imgSize);
		projectileImg
				.addAction(Actions.rotateTo((float) Math.toDegrees(Math.atan2(from.y - to.y, from.x - to.x)) + 90f));
		Gui.overlay.addActor(projectileImg);

		MadSand.getCamera().project(screenCoords.set(from.x, from.y, 0));
		projectileImg.setPosition(screenCoords.x, screenCoords.y);
		MadSand.getCamera().project(screenCoords.set(to.x, to.y, 0));

		to.toWorld();
		Map map = MadSand.world().getCurLoc();
		Utils.dbg("Projectile " + name + " will land on " + to.toString());
		Keyboard.ignoreInput();

		projectileImg.addAction(
				Actions.sequence(
						Actions.moveTo(screenCoords.x, screenCoords.y, BASE_PROJECTILE_SPEED),
						Actions.run(() -> {
							MapEntity target = map.getMapEntity(to);

							if (target != Map.nullNpc) {
								impactAction.accept(target);
								target.as(Entity.class)
										.ifPresent(entity -> entity.addActDelay(BASE_PROJECTILE_SPEED));
							} else
								map.putLoot(to.x, to.y, id, 1);
							Keyboard.resumeInput();
						}),
						Actions.run(() -> projectileImg.remove())));
	}

	@Override
	public EquipSlot getEquipSlot() {
		return EquipSlot.Offhand;
	}

	@Override
	public String getMiscInfo() {
		return "Projectile damage: [GREEN]" + dmg + Resources.COLOR_END + Resources.LINEBREAK;
	}
	
	@Override
	public void initCategory() {
		setCategory(ItemCategory.Projectiles, cost / 15);
	}
}
