package hitonoriol.madsand.gui.dialogs;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.properties.QuestList;

public class QuestListDialog extends GameDialog {

	float BUTTON_WIDTH = 250;
	float BUTTON_HEIGHT = 35;
	float SCROLL_WIDTH = 300;
	float SCROLL_HEIGHT = 400;
	float BUTTON_PAD = 5;

	Table scrollTable;
	AutoFocusScrollPane scroll;
	Table container;
	TextButton closeButton;

	QuestWorker quests;
	ArrayList<Integer> questList;

	private QuestListDialog(Stage stage) {
		super(stage);
	}

	public QuestListDialog(QuestWorker quests, ArrayList<Integer> questList, String npcName) {
		this(Gui.overlay);
		this.quests = quests;
		this.questList = questList;

		super.setTitle(npcName + "'s Quests");
		container = new Table();
		scrollTable = new Table();
		scroll = new AutoFocusScrollPane(scrollTable);
		container.add(scroll).size(BUTTON_WIDTH, SCROLL_HEIGHT).row();
		super.add(container);
		closeButton = new TextButton("Close", Gui.skin);

		refresh();
		container.add(closeButton).size(BUTTON_WIDTH / 2, BUTTON_HEIGHT).row();

		closeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}
		});
	}

	private void refresh() {
		Quest quest;
		TextButton questButton;
		String buttonString;
		for (int id : questList)
			if (quests.isQuestAvailable(id)) {
				quest = QuestList.quests.get(id);
				buttonString = quest.name;

				if (quests.isQuestInProgress(id))
					buttonString += Resources.LINEBREAK + "(In Progress)";

				questButton = new TextButton(buttonString, Gui.skin);
				scrollTable.add(questButton).width(BUTTON_WIDTH).pad(BUTTON_PAD).row();

				questButton.addListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						scroll.remove();
						remove();
						quests.processQuest(id);
					}
				});
			}

	}

}
