package de.sab.church;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class Start
{
	protected JFrame				myFrame;

	public static void main(String[] args)
	{
		boolean weekList=false;
		int rows=0;
		int tab=0;
		for(String arg:args)
		{
			int idx=-1;
			
			if(arg==null || arg.length()==0)
			{
				continue;
			}
			else
			{
				idx=arg.indexOf('=')+1;
			}
			if("week=list".equalsIgnoreCase(arg))
			{
				weekList=true;
			}
			else if(arg.startsWith("rows="))
			{
				try
				{
					rows=Integer.parseInt(arg.substring(idx));
				}
				catch(Exception ex)
				{
				}
			}
			else if(arg.startsWith("tab="))
			{
				try
				{
					tab=Integer.parseInt(arg.substring(idx))-1;					
				}
				catch(Exception ex)
				{
				}
			}
		}
		new Start(weekList,rows,tab);
	}
	
	public Start(boolean weekList, int rows, int tab)
	{
		myFrame=new JFrame("Datumsberechnung");
		JTabbedPane pane=new JTabbedPane();
//		JPanel panel=new JPanel(new ColLayout(10));
		pane.addTab("Monatskalender",new CalendarTable().getView());
		pane.addTab("Altersberechnung",new AgeCalculation().getView());
		pane.addTab("Kirchentag",new Kirchentage(weekList,rows).getView());
		if(tab>=0 && tab<pane.getTabCount()) pane.setSelectedIndex(tab);
		
		myFrame.getContentPane().add(new JScrollPane(pane));
//		myFrame.getContentPane().add(panel);
		myFrame.pack();
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame.setVisible(true);
	}

	
}
