package uc.ap.war;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class TextFieldChangeHandler implements DocumentListener {

	@Override
	public void changedUpdate(DocumentEvent e) {
		docChanged(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		docChanged(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		docChanged(e);
	}

	abstract protected void docChanged(DocumentEvent e);

}
