package hitonoriol.madsand.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.world.World;

public class QuestArrow extends Image {
	static float RADIUS = 100;
	static float ANIM_TIME = 0.2f;

	public Quest quest;
	Vector2 screenCoords = new Vector2((Gdx.graphics.getWidth() / 2 - (MadSand.TILESIZE / 2)),
			Gdx.graphics.getHeight() / 2);

	public QuestArrow(Quest quest) {
		super(Resources.questArrow);
		super.setOrigin(Align.center);
		this.quest = quest;
	}

	private void update(int pX, int pY, int objX, int objY) {
		float angle = (float) Math.atan2(objY - pY, objX - pX);
		float x = screenCoords.x, y = screenCoords.y;

		x += (float) (RADIUS * Math.cos((angle)));
		y += (float) (RADIUS * Math.sin((angle)));

		super.addAction(Actions.moveTo(x, y, ANIM_TIME));
		super.addAction(Actions.rotateTo((float) Math.toDegrees(angle), ANIM_TIME));
	}

	public void update() {
		Npc npc = quest.getNpc();
		if (!npc.equals(Map.nullNpc))
			update(World.player.x, World.player.y, npc.x, npc.y);
		else {
			Pair curWCoords = MadSand.world.getCurWPos();
			Pair npcWCoords = quest.npcWorldPos;
			update(curWCoords.x, curWCoords.y, npcWCoords.x, npcWCoords.y);
		}
	}
}
