package de.vandermeer.asciitable;

import java.util.ArrayList;
import java.util.List;

public class Table extends AsciiTable {
	private List<Object> currentRow;

	public Table() {
		row();
	}

	public Table add(Object obj) {
		currentRow.add(obj);
		return this;
	}

	public void expand() {
		int colDiff = getColNumber() - currentRow.size();
		if (colDiff > 0)
			for (int i = 0; i < colDiff; ++i)
				currentRow.add(null);
	}

	public Table row() {
		if (currentRow != null) {
			expand();
			super.addRow(currentRow);
		}
		currentRow = new ArrayList<>();
		return this;
	}
}
