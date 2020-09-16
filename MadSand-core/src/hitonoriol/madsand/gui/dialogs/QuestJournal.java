package hitonoriol.madsand.gui.dialogs;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.properties.QuestList;

public class QuestJournal extends Dialog {

	static float TABLE_WIDTH = 500;
	static float TABLE_HEIGHT = 300;
	static float TITLE_YPADDING = 20;
	static float CLOSE_BUTTON_YPADDING = 6;
	static float HEADER_YPADDING = 10;
	static float SCROLL_YPADDING = 25;

	static float ENTRY_HEIGHT = 20;

	static float STATUS_LABEL_WIDTH = 125;
	static float REQUIREMENT_LABEL_WIDTH = TABLE_WIDTH - STATUS_LABEL_WIDTH;
	static float HEADER_SCALE = 1.12f;

	Skin skin;
	static String titleString = "Quest Journal";
	static String statusString = "Status";
	static String reqString = "Requirements";
	static String closeText = "Close";
	static String inProgressString = "In Progress";
	static String completedString = "Completed";
	static String emptyJournalString = "Your journal is empty";

	QuestWorker quests;

	AutoFocusScrollPane questScroll;
	Table questTable;
	TextButton closeButton;
	Label statusLabel, reqLabel;
	Label emptyJournalLabel;

	public QuestJournal(String title, Skin skin) {
		super(title, skin);
		this.skin = skin;

	}

	public QuestJournal(QuestWorker quests) {
		this(titleString, Gui.skin);
		this.quests = quests;
		super.getTitleTable().padTop(TITLE_YPADDING).align(Align.center);
		super.getTitleLabel().setAlignment(Align.center);
		super.row();

		statusLabel = new Label(statusString, skin);
		reqLabel = new Label(reqString, skin);
		emptyJournalLabel = new Label(emptyJournalString, skin);

		statusLabel.setFontScale(HEADER_SCALE);
		reqLabel.setFontScale(HEADER_SCALE);

		questTable = new Table();
		questTable.setSize(TABLE_WIDTH, TABLE_HEIGHT);
		questTable.setBackground(Gui.darkBackgroundSizeable);
		questTable.align(Align.top);

		questScroll = new AutoFocusScrollPane(questTable);
		questScroll.setOverscroll(false, false);
		questScroll.setScrollingDisabled(true, false);

		closeButton = new TextButton(closeText, skin);
		super.add(questScroll).size(TABLE_WIDTH, TABLE_HEIGHT).padTop(SCROLL_YPADDING).row();
		super.add(closeButton).size(GameDialog.BTN_WIDTH, GameDialog.BTN_HEIGHT).padBottom(CLOSE_BUTTON_YPADDING).row();

		closeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}
		});
	}

	public void refresh() {
		questTable.clear();
		questTable.setSkin(skin);
		questTable.add(statusLabel).size(STATUS_LABEL_WIDTH, ENTRY_HEIGHT).padBottom(HEADER_YPADDING);
		questTable.add(reqLabel).size(REQUIREMENT_LABEL_WIDTH, ENTRY_HEIGHT).padBottom(HEADER_YPADDING).row();

		Label requirements;
		Quest quest;

		List<Integer> allQuests = Stream.of(quests.questsInProgress, quests.completedQuests)
				.flatMap(x -> x.stream())
				.collect(Collectors.toList());

		if (allQuests.size() == 0) {
			questTable.add();
			questTable.add(emptyJournalLabel).align(Align.left).padTop(questScroll.getHeight() / 2);
			return;
		}

		for (int id : allQuests) {
			quest = QuestList.quests.get(id);
			requirements = new Label(quest.journalText, skin);
			requirements.setWrap(true);

			questTable.add(getStatusLabel(id)).size(STATUS_LABEL_WIDTH, ENTRY_HEIGHT);
			questTable.add(requirements).size(REQUIREMENT_LABEL_WIDTH, ENTRY_HEIGHT).row();
		}

	}

	private Label getStatusLabel(int id) {
		return quests.isQuestInProgress(id) ? new Label(inProgressString, skin) : new Label(completedString, skin);
	}

	public void show() {
		Gui.gameUnfocused = Gui.dialogActive = true;
		refresh();
		super.show(Gui.overlay);
	}

	public boolean remove() {
		Gui.gameUnfocused = Gui.dialogActive = false;
		Gui.overlay.getTooltip().setVisible(true);
		return super.remove();
	}

}
