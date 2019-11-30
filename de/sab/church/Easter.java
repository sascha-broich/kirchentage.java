package de.sab.church;

import java.util.GregorianCalendar;

public class Easter extends ChurchDay
{
	public Easter()
	{
		super("Ostern", 7,0, null);
	}

	@Override
	protected GregorianCalendar getDate(int year, boolean julian)
	{
		int g=year%19;
		int i,j;
		
		if(julian)
		{
			i=(19*g+15)%30;
			j=(year+(year/4)+i)%7;
		}
		else
		{
			int c=year/100;
			int h=(c-(c/4)-(((8*c)+13)/25)+(19*g)+15)%30;
			i=h-((h/28)*(1-(29/(h+1)))*((21-g)/11));
			j=(year+(year/4)+i+2-c+(c/4))%7;
		}
		
		int m=i-j;
		int month=3+((m+40)/44);
		int day=m+28-(31*(month/4));

		GregorianCalendar cal=getCalendar(julian);
		cal.set(year,month-1,day);
		return cal;
	}
}
