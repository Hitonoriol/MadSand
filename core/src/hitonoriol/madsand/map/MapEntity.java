package hitonoriol.madsand.map;

import static hitonoriol.madsand.MadSand.getStage;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.entities.Damage;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.gui.animation.WorldAnimation;
import hitonoriol.madsand.gui.widgets.overlay.AnimatedWorldText;
import hitonoriol.madsand.gui.widgets.overlay.GameContextMenu;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.util.cast.DynamicallyCastable;

@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
@JsonSubTypes({ @Type(Entity.class), @Type(MapObject.class) })
public abstract class MapEntity implements DynamicallyCastable<MapEntity> {
	@JsonProperty
	private float luminosity = 0;

	protected abstract void damage(int damage);

	public void acceptDamage(Damage damage) {
		damage(damage.getValue());
		getStage().addActor(new AnimatedWorldText(this, damage.getValueString()));
	}

	public abstract String getName();

	@JsonIgnore
	public abstract Pair getPosition();
	
	@JsonIgnore
	public PairFloat getVisualPosition() {
		Pair position = getPosition().toScreen();
		return new PairFloat(position.x, position.y);
	}

	public abstract boolean add(Map map, Pair coords);

	protected void populateContextMenu(GameContextMenu menu) {}

	public void playAnimation(TextureRegion[] animation) {
		Pair worldPos = getPosition().toScreen();
		MadSand.getRenderer().queueAnimation(new WorldAnimation(animation).setCoords(worldPos.x, worldPos.y));
	}

	protected abstract void playDamageAnimation();
	
	@JsonIgnore
	public abstract TextureRegion getSprite();
	

	@JsonIgnore
	public abstract boolean isEmpty();

	public float getLuminosity() {
		return luminosity;
	}

	public boolean emitsLight() {
		return luminosity > 0;
	}

	protected void setLuminosity(float value) {
		luminosity = value;
	}
}
