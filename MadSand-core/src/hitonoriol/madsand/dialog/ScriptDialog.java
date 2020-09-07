package hitonoriol.madsand.dialog;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.containers.Tuple;

public class ScriptDialog {
	private GameDialog dialog;
	private final String okString = "Proceed";

	/*
	 * Scriptable dialogs for quests n stuff
	 */

	@SafeVarargs
	public ScriptDialog(String title, String text, Stage stage, Tuple<String, String>... responses) {
		dialog = new GameDialog(title, text, stage);

		if (responses.length > 0)
			addResponses(responses);
		else
			dialog.addOkButton(okString);
	}

	@SuppressWarnings("unchecked")
	public void addResponses(Tuple<String, String>... tuples) {
		for (Tuple<String, String> button : tuples) {
			final String buttonText = button.l;
			final String onClick = button.r;

			TextButton responseButton = new TextButton(buttonText, Gui.skin);

			responseButton.addListener(new ChangeListener() {
				public void changed(ChangeListener.ChangeEvent event, Actor actor) {
					LuaUtils.execute(onClick);
					dialog.remove();
				}
			});

			dialog.addButton(responseButton);
		}
	}

	public void show() {
		dialog.show();
	}
}
