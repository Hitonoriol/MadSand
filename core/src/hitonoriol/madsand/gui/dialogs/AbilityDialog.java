package hitonoriol.madsand.gui.dialogs;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.ability.ActiveAbility;
import hitonoriol.madsand.entities.ability.PassiveAbility;
import hitonoriol.madsand.properties.Prefs;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;

public class AbilityDialog extends GameDialog {

	private List<Ability> abilities;
	static float BTN_HEIGHT = 50;
	static float PAD = 30;
	Table container = new Table(Gui.skin);

	public AbilityDialog(List<Ability> abilities) {
		super.setTitle("Abilities");
		super.skipLine();
		super.skipLine().padBottom(PAD);
		this.abilities = abilities;
		super.add(container).row();
		super.skipLine().padTop(PAD);
		super.addCloseButton();

		refresh();
	}

	private void refresh() {
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
							Prefs prefs = Prefs.values();

							addButton(abstrAbility,
									() -> {
										ability.apply();
										Utils.scheduleTask(() -> remove(), 0.1f);
									}).padRight(5).padLeft(5);

							addButton(ability.getBindKeyString(),
									() -> new KeyDialog(key -> {
										prefs.bindAbility(key, ability.id);
										refresh();
									}).show())
											.size(75, BTN_HEIGHT)
											.padRight(5)
											.row();
						});

		abstrAbility.as(PassiveAbility.class)
				.ifPresent(ability -> Functional.with(addButton(abstrAbility, () -> {}), cell -> {
					cell.getActor().setDisabled(true);
					cell.row();
				}));
	}

	private Cell<TextButton> addButton(Ability ability, Runnable action) {
		return addButton(ability.toString(), action);
	}

	private Cell<TextButton> addButton(String text, Runnable action) {
		Cell<TextButton> cell = container.add(new TextButton(text, Gui.skin))
				.size(300, BTN_HEIGHT);
		Gui.setAction(cell.getActor(), action);
		return cell;
	}

}
