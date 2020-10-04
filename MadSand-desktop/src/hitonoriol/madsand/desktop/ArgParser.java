package hitonoriol.madsand.desktop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hitonoriol.madsand.Utils;

public class ArgParser {

	private List<String> argsList = new ArrayList<>();
	private HashMap<String, String> options = new HashMap<>();

	public ArgParser(String[] args) {
		parse(args);
	}

	public ArgParser parse(String[] args) {

		for (int i = 0; i < args.length; i++) {

			switch (args[i].charAt(0)) {
			case '-':
				if (args[i].length() < 2)
					Utils.die("Invalid argument [" + args[i] + "]");

				if (args.length - 1 == i)
					Utils.die("Flag must be followed by an argument [" + args[i] + "]");

				options.put(args[i], args[i + 1]);
				i++;
				break;

			default:
				argsList.add(args[i]);
				break;
			}

		}
		return this;
	}

	public boolean argExists(String arg) {
		return argsList.contains(arg);
	}

	public boolean optionExists(String option) {
		return options.containsKey(option);
	}

	public boolean getBooleanOption(String option) {
		return Boolean.parseBoolean(options.get(option));
	}
}
