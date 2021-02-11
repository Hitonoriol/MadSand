package hitonoriol.madsand.gui.dialogs;

import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.ability.ActiveAbility;
import hitonoriol.madsand.entities.ability.PassiveAbility;

public class AbilityDialog extends GameDialog {

	private List<Integer> abilities;

	public AbilityDialog(List<Integer> abilities) {
		super.setTitle("Abilities");
		super.skipLine();
		this.abilities = abilities;

		if (abilities.isEmpty())
			super.add("You don't have any abilities").height(250).align(Align.center).row();
		else {
			Gui.setFontSize(super.add("Active").getActor(), Gui.FONT_M);
			createAbilityEntries(ActiveAbility.class, ability -> addAbilityButton((ActiveAbility) ability));
			super.skipLine();

			Gui.setFontSize(super.add("Passive").getActor(), Gui.FONT_M);
			createAbilityEntries(PassiveAbility.class, ability -> addAbilityButton((PassiveAbility) ability));
		}

		super.addCloseButton();
	}

	private <T extends Ability> void createAbilityEntries(Class<T> abilityType, Consumer<Ability> action) {
		abilities.stream()
				.map(id -> Ability.get(id))
				.filter(ability -> ability.is(abilityType))
				.forEach(action);
	}

	private void addAbilityButton(ActiveAbility ability) {
		addButton(ability.name + " [" + ability.staminaCost + "]", () -> ability.apply());
	}

	private void addAbilityButton(PassiveAbility ability) {
		addButton(ability.name, () -> {
		}).setDisabled(true);
	}

	private TextButton addButton(String text, Runnable action) {
		TextButton button = new TextButton(text, Gui.skin);
		super.add(button).width(200).row();
		Gui.setAction(button, action);
		return button;
	}

}
