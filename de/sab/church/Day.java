package de.sab.church;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Day
{
	protected Day myReference;
	protected int myWeekOffset;
	protected int myDayOfWeek;
	protected int myDayOfMonth;
	protected int myMonth;
	protected int myOffset;
	
	protected boolean myAbsolute=false;
	protected boolean myRelative=false;
	
	public Day(int dayOfWeek, int week, Day reference)
	{
		myReference=reference;
		myDayOfWeek=dayOfWeek;
		myWeekOffset=week;
		myRelative=true;
	}
	
	public Day(int offset, Day reference)
	{
		myReference=reference;
		myOffset=offset;
	}

	public Day(int day, int month)
	{
		myReference=null;
		myDayOfMonth=day;
		myMonth=month-1; // Umrechnung für Calendar
		myAbsolute=true;
	}

	protected int getOffset(Calendar calendar)
	{
		if(myAbsolute) // Sollte nicht vorkommen
		{
			return 0;
		}
		else if(myRelative) // Wochentag 
		{
			int refDayOfWeek=(myReference==null)?0:myReference.getDayOfWeek(calendar);
			int dd=myDayOfWeek-refDayOfWeek;
			int week=myWeekOffset;
			
			if(dd<0 && week <0) week++;
			else if(dd>0 && week>0) week--;

			return (7*week)+dd;
		}
		else // Einfach mit Offset
		{
			return myOffset;
		}	
	}	

	public final Calendar getJulianDate(int year)
	{
		return getDate(year,true);
	}

	public final Calendar getGregorianDate(int year)
	{
		return getDate(year,false);
	}
	
	protected Calendar getDate(int year, boolean julian)
	{
		if(myReference==null)
		{
			GregorianCalendar calendar=getCalendar(julian);
			calendar.set(year,myMonth,myDayOfMonth);
			return calendar;
		}
		else
		{
			return getDate(myReference.getDate(year,julian));
		}		
	}
	

	protected Calendar getDate(Calendar cal)
	{
		cal.add(Calendar.DAY_OF_YEAR,getOffset(cal));		
		
		return cal;
	}
	
	public int getDayOfWeek(Calendar calendar)
	{
		if(myRelative) // Wochentag schon vorhanden
		{
			return myDayOfWeek;
		}
		else if(myAbsolute)
		{
			Calendar cal=((Calendar)calendar.clone());
			cal.set(calendar.get(Calendar.YEAR),myMonth,myDayOfMonth);
			int dow=cal.get(Calendar.DAY_OF_WEEK)-1;// Einen Tag abziehen
			if(dow==0) dow=7;//Sonntag			
			return dow;
		}
		else // Sollte nicht vorkommen
		{
			return 0;
		}
	}
	
	protected GregorianCalendar getCalendar(boolean julian)
	{
		GregorianCalendar calendar=new GregorianCalendar();
		if(julian) calendar.setGregorianChange(new Date(Long.MAX_VALUE));
		return calendar;
	}

}
