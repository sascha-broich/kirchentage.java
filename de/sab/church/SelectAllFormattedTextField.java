package de.sab.church;

import java.awt.event.FocusEvent;
import java.text.Format;

import javax.swing.JFormattedTextField;

public class SelectAllFormattedTextField extends JFormattedTextField
{
	private static final long	serialVersionUID	= 1L;

	public SelectAllFormattedTextField()
	{
		super();
	}

	public SelectAllFormattedTextField(AbstractFormatter formatter)
	{
		super(formatter);
	}

	public SelectAllFormattedTextField(AbstractFormatterFactory factory, Object currentValue)
	{
		super(factory, currentValue);
	}

	public SelectAllFormattedTextField(AbstractFormatterFactory factory)
	{
		super(factory);
	}

	public SelectAllFormattedTextField(Format format)
	{
		super(format);
	}

	public SelectAllFormattedTextField(Object value)
	{
		super(value);
	}

	@Override
	protected void processFocusEvent(FocusEvent e)
	{
		super.processFocusEvent(e);
		if(e.getID()==FocusEvent.FOCUS_GAINED)
		{
			selectAll();
		}
	}
}