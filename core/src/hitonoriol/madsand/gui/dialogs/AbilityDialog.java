package hitonoriol.madsand.gui.dialogs;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.ability.ActiveAbility;
import hitonoriol.madsand.entities.ability.PassiveAbility;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.TimeUtils;

public class AbilityDialog extends GameDialog {

	private List<Ability> abilities;
	static final float BTN_HEIGHT = 50, MIN_BTN_WIDTH = 75;
	static float PAD = 30;
	Table container = Widgets.table();

	public AbilityDialog(List<Ability> abilities) {
		super.setTitle("Abilities");
		super.skipLine();
		super.skipLine().padBottom(PAD);
		this.abilities = abilities;
		super.add(container).row();
		super.skipLine().padTop(PAD);
		super.addCloseButton();

		container.defaults().padBottom(5);
		refresh();
	}

	private float maxBindCellWidth;

	private void refresh() {
		maxBindCellWidth = MIN_BTN_WIDTH;
		container.clear();

		if (abilities.isEmpty())
			container.add("You don't have any abilities").height(250).align(Align.center).row();
		else {
			createAbilityBlock("Active", ActiveAbility.class);
			createAbilityBlock("Passive", PassiveAbility.class);
		}
	}

	private <T extends Ability> void createAbilityBlock(String title, Class<T> abilityType) {
		if (!hasAbilities(abilityType))
			return;

		Gui.setFontSize(container.add(title).padBottom(7).getActor(), Gui.FONT_M);
		container.row();
		createAbilityEntries(abilityType, ability -> addAbilityButton(ability.as(abilityType).get()));

	}

	private <T extends Ability> boolean hasAbilities(Class<T> abilityType) {
		return filterAbilities(abilityType).findAny().isPresent();
	}

	private <T extends Ability> Stream<Ability> filterAbilities(Class<T> abilityType) {
		return abilities.stream()
			.filter(ability -> ability.is(abilityType));
	}

	private <T extends Ability> void createAbilityEntries(Class<T> abilityType, Consumer<Ability> action) {
		filterAbilities(abilityType)
			.forEach(action);
	}

	private void addAbilityButton(Ability abstrAbility) {
		abstrAbility.as(ActiveAbility.class)
			.ifPresent(
				ability -> {
					var player = MadSand.player();
					int currentKey = player.getAbilityKey(ability.id);

					addAbilityButton(
						abstrAbility,
						() -> {
							ability.apply();
							TimeUtils.scheduleTask(this::remove, 0.1f);
						}
					).padRight(5).padLeft(5);

					var bindString = ability.getBindKeyString();
					addAbilityButton(
						bindString,
						() -> new KeyDialog(key -> {
							player.bindAbility(key, ability.id);
							refresh();
						}, currentKey).setRemoveBindingAction(
							() -> {
								player.unbindAbility(currentKey);
								refresh();
							}
						)
							.show()
					)
						.size(
							maxBindCellWidth = Math.max(
								maxBindCellWidth,
								Gui.getTextWidth(bindString) + 10
							), BTN_HEIGHT
						)
						.fill()
						.padRight(5)
						.row();
				}
			);

		abstrAbility.as(PassiveAbility.class)
			.ifPresent(ability -> Functional.with(addAbilityButton(abstrAbility, () -> {}), cell -> {
				cell.getActor().setDisabled(true);
				cell.row();
			}));
	}

	private Cell<TextButton> addAbilityButton(Ability ability, Runnable action) {
		return addAbilityButton(ability.toString(), action);
	}

	private Cell<TextButton> addAbilityButton(String text, Runnable action) {
		var cell = container.add(Widgets.button(text))
			.size(300, BTN_HEIGHT);
		Gui.setAction(cell.getActor(), action);
		return cell;
	}

}
