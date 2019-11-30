package de.sab.church;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MonthDay extends ChurchDay
{
	public MonthDay(int offset, int dayOfWeek, int month, String name)
	{
		super(name,0,month);
		this.myDayOfWeek=dayOfWeek;
		this.myOffset=offset>0?offset-1:offset;
		this.myAbsolute=false;
		this.myRelative=true;
	}

	@Override
	protected Calendar getDate(int year, boolean julian)
	{		
		GregorianCalendar calendar=getCalendar(julian);
		calendar.set(year,myMonth,1);

		// Wenn von hinten gezählt wird
		if(myOffset<0) calendar.add(Calendar.MONTH, 1);

		int dow=calendar.get(Calendar.DAY_OF_WEEK)-1;// Einen Tag abziehen
		if(dow==0) dow=7;//Sonntag
		
		int days=(myOffset*7)+(myDayOfWeek-dow);
		calendar.add(Calendar.DAY_OF_MONTH, days);

		return calendar;
	}
}
