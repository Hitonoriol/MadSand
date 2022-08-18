package hitonoriol.madsand.gui.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hitonoriol.madsand.entities.Entity;

public class EntityAnimation extends WorldAnimation {
	private Entity entity;

	public EntityAnimation(Entity entity, TextureRegion[] strip) {
		super(strip);
		this.entity = entity;
	}

	@Override
	public float x() {
		return entity.worldX();
	}

	@Override
	public float y() {
		return entity.worldY();
	}
}
