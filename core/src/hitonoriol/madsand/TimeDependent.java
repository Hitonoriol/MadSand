package hitonoriol.madsand;

public interface TimeDependent {
	void update();

	default long getUpdateRate() {
		return MadSand.world().getRealtimeActionPeriod();
	}
}
