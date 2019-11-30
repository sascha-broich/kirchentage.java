package de.sab.util;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * <p>
 * Ein Eingabefeld für Double-Werte
 * </p>
 * <p>
 * Copyright (c) 2003
 * </p>
 * <p>
 * Organisation: Fiedler Optoelektronik GmbH
 * </p>
 * 
 * @author Sascha Broich
 * @version 1.0
 */
public class DecimalField extends JTextField
{
	private static final long	serialVersionUID	= 1L;

	/** das benutzte Number-Format */
	protected DecimalFormat	myFormat;

	/** der zuletzt eingegebene Wert */
	protected double		myValue;

	protected Color			myBadColor;

	protected Color			myGoodColor;
	
	protected boolean myIgnore=false;

	/**
	 * Erzeugt ein neues DecimalField mit dem Wert <code>value</code>, der
	 * Breite von <code>columns</code> Zeichen und dem Format <code>f</code>
	 * 
	 * @param value der Anfangs-Wert
	 * @param columns die Darstellungs-Breite
	 * @param f das Format
	 */
	public DecimalField(double value, int columns, DecimalFormat format)
	{
		super(columns);
		myGoodColor = getBackground();
		myBadColor = new Color(255, 192, 192);

		setDocument(new DecimalDocument());
		if(format==null) format=new DecimalFormat();
		myFormat=format;
		myFormat.setGroupingUsed(false);
		myValue = Double.NaN;
		setValue(value);
		setHorizontalAlignment(JTextField.RIGHT);
	}

	/**
	 * Erzeugt ein neues DecimalField mit dem Wert <code>value</code>, der
	 * Breite von <code>columns</code> Zeichen und dem Standard-Format
	 * 
	 * @param value der Anfangs-Wert
	 * @param columns die Darstellungs-Breite
	 */
	public DecimalField(double value, int columns)
	{
		this(value, columns, new DecimalFormat());
	}

	/**
	 * Erzeugt ein neues DecimalField mit dem Wert <code>0</code>, der Breite
	 * von <code>columns</code> Zeichen und dem Standard-Format
	 * 
	 * @param columns die Darstellungs-Breite
	 */
	public DecimalField(int columns)
	{
		this(Double.NaN, columns);
	}

	/**
	 * Erzeugt ein neues DecimalField mit dem Wert <code>0</code>, der Breite
	 * von <code>0</code> Zeichen und dem Standard-Format
	 */
	public DecimalField()
	{
		this(0);
	}

	public DecimalField(double value, int columns, int fractions)
	{
		this(value, columns);
		myFormat.setMaximumFractionDigits(fractions);
	}

	public void setFraction(int fraction)
	{
		myFormat.setMaximumFractionDigits(fraction);
	}

	/**
	 * gibt den Wert des Feldes zurück
	 * 
	 * @return double der Wert
	 */
	public double getValue()
	{
		double retVal = myValue;
		/*
		 * try { retVal = format.parse(getText()).doubleValue(); } catch
		 * (ParseException e) { }
		 */
		return retVal;
	}

	/**
	 * setzt den Inhalt des Feldes auf <code>value</code>
	 * 
	 * @param value der neue Wert
	 */
	public void setValue(double value)
	{
		if (getValue() != value)
		{
			setMyValue(value);
			myIgnore=true;
			setText(value == value ? myFormat.format(value) : "");
			myIgnore=false;
			super.fireActionPerformed();
		}
	}

	/**
	 * setzt den Wert auf <code>value</code>
	 * 
	 * @param value der neue Wert
	 */
	protected void setMyValue(double value)
	{
		if(myValue==value || myIgnore) return; 
		myValue = value;
		fireStateChanged();
	}

	public void addChangeListener(ChangeListener listener)
	{
		listenerList.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener)
	{
		listenerList.remove(ChangeListener.class, listener);
	}

	protected void fireStateChanged()
	{
		Object[] listeners = listenerList.getListeners(ChangeListener.class);
		if (listeners == null) return;
		ChangeEvent e = new ChangeEvent(this);
		for (int i = 0; i < listeners.length; i++)
		{
			((ChangeListener) listeners[i]).stateChanged(e);
		}
	}

	/**
	 * <p>
	 * Innere Klasse für ein Document, das Double-Werte prüft
	 * </p>
	 * <p>
	 * Copyright (c) 2003
	 * </p>
	 * <p>
	 * Organisation: Fiedler Optoelektronik GmbH
	 * </p>
	 * 
	 * @author Sascha Broich
	 * @version 1.0
	 */
	protected class DecimalDocument extends PlainDocument
	{
		private static final long	serialVersionUID	= 1L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.text.Document#insertString(int, java.lang.String,
		 *      javax.swing.text.AttributeSet)
		 */
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
		{

			String currentText = getText(0, getLength());
			String beforeOffset = currentText.substring(0, offs);
			String afterOffset = currentText.substring(offs, currentText.length());
			String proposedResult = beforeOffset + str + afterOffset;

			try
			{
				double value = myFormat.parse(proposedResult).doubleValue();
				setMyValue(value);
				setBackground(myGoodColor);
			}
			catch (ParseException e)
			{
				setBackground(myBadColor);
			}
			super.insertString(offs, str, a);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.text.Document#remove(int, int)
		 */
		public void remove(int offs, int len) throws BadLocationException
		{
			String currentText = getText(0, getLength());
			String beforeOffset = currentText.substring(0, offs);
			String afterOffset = currentText.substring(len + offs, currentText.length());
			String proposedResult = beforeOffset + afterOffset;

			try
			{
				if (proposedResult.length() != 0)
				{
					setMyValue(myFormat.parse(proposedResult).doubleValue());
				}
				setBackground(myGoodColor);
			}
			catch (ParseException e)
			{
				setBackground(myBadColor);
			}
			super.remove(offs, len);
		}
	}

	public void setFormat(DecimalFormat format)
	{
		if(myFormat!=null && myFormat.equals(format)) return;
		myFormat=format;
	}

}