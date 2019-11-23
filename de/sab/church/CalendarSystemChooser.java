package de.sab.church;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CalendarSystemChooser
{
	protected class ChangeListenerImplementation implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
			if(myCalendarSystem.isSelected())
			{
				myCalendarSystem.setText("Julianisch");
			}
			else
			{
				myCalendarSystem.setText("Gregorianisch");
			}
			fireStateChanged();
		}
	}

	protected JToggleButton myCalendarSystem;
	protected HashSet<ChangeListener> myListeners;
	
	public CalendarSystemChooser()
	{
		this(false);
	}
	
	public CalendarSystemChooser(boolean julian)
	{
		myCalendarSystem=new JToggleButton("Gregorianisch");
		myCalendarSystem.addChangeListener(new ChangeListenerImplementation());
		myCalendarSystem.setSelected(julian);
	}
	
	public JComponent getView()
	{
		return myCalendarSystem;
	}
	
	public GregorianCalendar getCalendar(Date date)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		if(date!=null) calendar.setTime(date);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		if(myCalendarSystem.isSelected())
		{
			calendar.setGregorianChange(new Date(Long.MAX_VALUE));
		}
		return calendar;
	}

	public synchronized void addChangeListener(ChangeListener listener)
	{
		if(listener==null) return;
		if(myListeners==null) myListeners=new HashSet<ChangeListener>();
		myListeners.add(listener);
	}
	
	public synchronized void removeChangeListener(ChangeListener listener)
	{
		if(listener==null) return;
		if(myListeners==null) return;
		myListeners.remove(listener);
	}
	
	protected synchronized void fireStateChanged()
	{
		if(myListeners==null) return;
		ChangeEvent event=new ChangeEvent(this);
		for(ChangeListener listener:myListeners)
		{
			listener.stateChanged(event);
		}
	}
	
	private boolean isSelected()
	{
		return myCalendarSystem.isSelected();
	}
	
	public boolean isJulian()
	{
		return isSelected();
	}
	
	public boolean isGregorian()
	{
		return !isSelected();
	}
	
	public String getText()
	{
		return myCalendarSystem.getText();
	}
}
