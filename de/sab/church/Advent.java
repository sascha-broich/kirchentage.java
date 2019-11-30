package de.sab.church;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Advent extends ChurchDay
{
	public Advent()
	{
		super("1. Advent",7,0,null);
	}

	@Override
	protected GregorianCalendar getDate(int year, boolean julian)
	{
		GregorianCalendar calendar=getCalendar(julian);
		calendar.set(year,11,24);		// Heilig Abend

		calendar.add(Calendar.DAY_OF_YEAR,-3*7); // 3 Wochen zurück
		int off=calendar.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY;
		calendar.add(Calendar.DAY_OF_YEAR,-off); // 1.Advent 

		return calendar;
	}
	
	

}
