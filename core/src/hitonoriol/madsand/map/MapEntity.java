package hitonoriol.madsand.map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.DynamicallyCastable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.AnimationContainer;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.map.object.MapObject;

@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
@JsonSubTypes({ @Type(Entity.class), @Type(MapObject.class) })
public abstract class MapEntity implements DynamicallyCastable<MapEntity> {
	@JsonProperty
	private float luminosity = 0;

	public abstract void damage(int amt);

	public abstract String getName();

	@JsonIgnore
	public abstract Pair getPosition();

	public abstract boolean add(Map map, Pair coords);

	public void playAnimation(AnimationContainer animation) {
		Pair worldPos = getPosition().toScreen();
		MadSand.getRenderer().queueAnimation(animation, worldPos.x, worldPos.y);
	}

	public abstract void playDamageAnimation();

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
