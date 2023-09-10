package hitonoriol.madsand.gui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiColors;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.RowColorTable;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.map.Map;

public class QuestJournal extends GameDialog {

	static float TABLE_WIDTH = 600;
	static float TABLE_HEIGHT = 300;
	static float TITLE_YPADDING = 20;
	static float CLOSE_BUTTON_YPADDING = 6;
	static float HEADER_YPADDING = 10;
	static float SCROLL_YPADDING = 25;

	static float ENTRY_HEIGHT = 20;
	static float PAD_BOTTOM = 5;

	static float STATUS_LABEL_WIDTH = 125;
	static float NAME_LABEL_WIDTH = 170;
	static float OBJECTIVE_LABEL_WIDTH = 200;
	static float NPC_INFO_LABEL_WIDTH = 200;

	QuestWorker quests;

	AutoFocusScrollPane questScroll;
	RowColorTable questTable;
	Label statusLabel, nameLabel, reqLabel, npcLocationLabel;
	Label emptyJournalLabel;

	public QuestJournal(QuestWorker quests) {
		this.quests = quests;
		setTitle("Quest Journal");
		super.getTitleTable().padTop(TITLE_YPADDING).align(Align.center);
		super.getTitleLabel().setAlignment(Align.center);

		statusLabel = createTitle("Status");
		nameLabel = createTitle("Quest");
		reqLabel = createTitle("Objectives");
		npcLocationLabel = createTitle("Turn in to");
		emptyJournalLabel = createTitle("Your journal is empty");

		questTable = new RowColorTable();
		questTable.align(Align.top);

		questScroll = new AutoFocusScrollPane(questTable);
		questScroll.setOverscroll(false, false);
		questScroll.setScrollingDisabled(true, false);

		super.add(questScroll).minSize(TABLE_WIDTH, TABLE_HEIGHT).padTop(SCROLL_YPADDING).row();
		super.add(createCloseButton()).size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).padBottom(CLOSE_BUTTON_YPADDING).row();
		pack();
	}

	private Label createTitle(String text) {
		return Widgets.label(text, Gui.FONT_M);
	}

	public void refresh() {
		questTable.clear();
		questTable.align(Align.topLeft);
		questTable.setSkin(Gui.skin);
		questTable.setRowBackground(GuiColors.TRANSPARENT);
		questTable.add(statusLabel).size(STATUS_LABEL_WIDTH, ENTRY_HEIGHT).padBottom(HEADER_YPADDING);
		questTable.add(nameLabel).size(NAME_LABEL_WIDTH, ENTRY_HEIGHT).padBottom(HEADER_YPADDING);
		questTable.add(reqLabel).size(OBJECTIVE_LABEL_WIDTH, ENTRY_HEIGHT).padBottom(HEADER_YPADDING);
		questTable.add(npcLocationLabel).size(NPC_INFO_LABEL_WIDTH, ENTRY_HEIGHT).padBottom(HEADER_YPADDING);
		questTable.row();

		List<Quest> allQuests = Stream.of(quests.questsInProgress, quests.completedQuests)
			.flatMap(ArrayList::stream)
			.collect(Collectors.toList());

		if (allQuests.size() == 0) {
			questTable.add(emptyJournalLabel).align(Align.center).colspan(4).padTop(questScroll.getHeight() / 2);
			return;
		}

		Label questName, questObjective, npcInfo;
		AbstractNpc npc;
		for (Quest quest : allQuests) {
			quest.setPlayer(MadSand.player());
			questName = Widgets.label(quest.name);
			questName.setWrap(true);
			questName.setAlignment(Align.topLeft);
			questObjective = Widgets.label(quest.getObjectiveString());
			questObjective.setWrap(true);
			npcInfo = Widgets.label("");
			npcInfo.setWrap(true);

			npc = quest.getNpc();
			if (npc == Map.nullNpc)
				npcInfo.setText("NPC at sector (" + quest.npcWorldPos + ")");
			else
				npcInfo.setText(npc.stats.name + " at cell (" + npc.x + ", " + npc.y + ") of current sector");

			if (quest.isComplete)
				questObjective.setText("[LIME]" + questObjective.getText());

			questTable.add(getStatusLabel(quest.id())).size(STATUS_LABEL_WIDTH, ENTRY_HEIGHT).align(Align.topLeft)
				.padBottom(PAD_BOTTOM);
			questTable.add(questName).width(NAME_LABEL_WIDTH).align(Align.topLeft).padBottom(PAD_BOTTOM);
			questTable.add(questObjective).width(OBJECTIVE_LABEL_WIDTH).padBottom(PAD_BOTTOM).align(Align.topLeft);
			questTable.add(npcInfo).width(NPC_INFO_LABEL_WIDTH).padBottom(PAD_BOTTOM).align(Align.topLeft);
			questTable.row();
		}

	}

	private Label createProgressLabel(boolean inProgress) {
		var label = inProgress
			? Widgets.label("[ORANGE]In Progress[]")
			: Widgets.label("[LIME]Complete[]");
		label.setAlignment(Align.topLeft);
		return label;
	}

	private Label getStatusLabel(int id) {
		return createProgressLabel(quests.isQuestInProgress(id));
	}

	@Override
	public void show() {
		refresh();
		super.show();
	}

}
