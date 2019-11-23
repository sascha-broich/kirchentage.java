package de.sab.church;


public class ChurchDay extends Day
{
	protected String myName;
	
	public ChurchDay(String name, int dayOfWeek, int week, ChurchDay reference)
	{
		super(dayOfWeek,week,reference);
		myName=name;
	}
	
	public ChurchDay(String name, int day, int month)
	{
		super(day,month);
		myName=name;
	}

	@Override
	public String toString()
	{
		return getName();
	}
	
	public String getName()
	{
		return myName;
	}
	
}
