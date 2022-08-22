package hitonoriol.madsand.util;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class CameraShaker {
	private float time = 0;
	private float currentTime = 0;
	private float intensity = 0;
	private float currentIntensity = 0;
	private Vector3 position = new Vector3();
	private OrthographicCamera camera;

	public CameraShaker(OrthographicCamera camera) {
		this.camera = camera;
	}

	public void shake(float intensity, float duration) {
		if (time == 0)
			this.intensity = intensity;
		else
			this.intensity += intensity * 0.5f;
		time = duration;
		currentTime = 0;
		Utils.dbg("Starting camera shake: intesity = %f, duration = %f", intensity, duration);
	}

	public void tick(float delta) {
		if (currentTime > time)
			time = 0;
		else {
			currentIntensity = intensity * ((time - currentTime) / time);
			position.x = (Utils.random.nextFloat() - 0.5f) * 2 * currentIntensity;
			position.y = (Utils.random.nextFloat() - 0.5f) * 2 * currentIntensity;
			currentTime += delta;
		}

		if (time > 0)
			camera.translate(position);
	}
}
