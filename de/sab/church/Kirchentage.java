package de.sab.church;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.sab.util.ColLayout;
import de.sab.util.DecimalField;
import de.sab.util.ListComboBoxModel;
import de.sab.util.MinimumLayout;
import de.sab.util.RowLayout;

public class Kirchentage implements ActionListener, ChangeListener, CaretListener, FocusListener, ListSelectionListener
{
	protected static final String	CHURCH_DAY_FILE	= "kirchentage.txt";

	protected static final String[]	WEEK_DAYS		= {"Tag", "Montag", "Dienstag", "Mittwoch",
			"Donnerstag", "Freitag", "Samstag", "Sonntag",};

	protected static final String[]	PRE_POST		= {"vor", "genau", "nach",};

	protected JSpinner		myWeekSpinner;
	protected JList<Integer>myWeekList;
	protected JList<String> myDayOfWeek;
	protected JList<String> myPrePost;
	protected JList<ChurchDay>myChurchDay;
	protected DecimalField	myYear;

	protected JTextField	myJulianDate;
	protected JTextField	myGregorianDate;
	
	protected JTextField 	myJulianDoW;
	protected JTextField	myGregorianDoW;
	
	protected JComponent 	myView;
	protected int 			myRows;

	public Kirchentage(boolean weekList, int rows)
	{
		myRows=rows;
		initView(weekList);
		loadChurchDays();
	}
	
	public JComponent getView()
	{
		return myView;
	}

	protected void initView(boolean weekList)
	{
		myView= new JPanel(new MinimumLayout(MinimumLayout.HORIZONTAL,10));
		myView.setBorder(new EmptyBorder(10,10,10,10));
		
		if(weekList) 
		{
			Integer[] weeks=new Integer[53];
			for(int i=0;i<weeks.length;weeks[i++]=i);
			myWeekList=new JList<>(weeks);
			myWeekList.setSelectedIndex(0);
			myWeekList.addListSelectionListener(this);
			
			myView.add(myWeekList);
		}
		else
		{
			myWeekSpinner = new JSpinner(new SpinnerNumberModel(1,1,53,1));
			myWeekSpinner.addChangeListener(this);
//			myWeek.addFocusListener(this);
			((DefaultEditor)myWeekSpinner.getEditor()).getTextField().addCaretListener(this);
			((DefaultEditor)myWeekSpinner.getEditor()).getTextField().addFocusListener(this);
			
			myView.add(myWeekSpinner);
		}
		myView.add(new JLabel(". "));

		myDayOfWeek = new JList<>(WEEK_DAYS);
		myDayOfWeek.setSelectedIndex(0);
		myDayOfWeek.addListSelectionListener(this);
		myView.add(myDayOfWeek);
//		AutoCompletion.enable(myDayOfWeek);
		
		myPrePost = new JList<>(PRE_POST);
		myPrePost.setSelectedIndex(1);
		myPrePost.addListSelectionListener(this);
		myView.add(myPrePost);
//		AutoCompletion.enable(myPrePost);

		myChurchDay = new JList<>();
		myChurchDay.addListSelectionListener(this);
		myChurchDay.setLayoutOrientation(JList.VERTICAL_WRAP);
		myView.add(new JScrollPane(myChurchDay));
//		AutoCompletion.enable(myChurchDay);

		myYear = new DecimalField(2009,5,0);
		myYear.addChangeListener(this);
//		myYear.addFocusListener(this);
		myView.add(myYear);

		myJulianDate = new JTextField(6);
		myJulianDate.setEditable(false);
		myJulianDate.setDragEnabled(true);
		myJulianDate.addFocusListener(this);
		
		myGregorianDate = new JTextField(6);
		myGregorianDate.setEditable(false);
		myGregorianDate.setDragEnabled(true);
		myGregorianDate.addFocusListener(this);

		myJulianDoW = new JTextField(6);
		myJulianDoW.setEditable(false);
		myJulianDoW.setDragEnabled(true);
		myJulianDoW.addFocusListener(this);
		
		myGregorianDoW = new JTextField(6);
		myGregorianDoW.setEditable(false);
		myGregorianDoW.setDragEnabled(true);
		myGregorianDoW.addFocusListener(this);

		
		JPanel date = new JPanel(new ColLayout(5));
		JPanel julian = new JPanel(new RowLayout(5));
		julian.add(new JLabel("julianisch:"));
		julian.add(myJulianDate);
		julian.add(myJulianDoW);
		date.add(julian);
		JPanel gregorian = new JPanel(new RowLayout(5));
		gregorian.add(new JLabel("gregorianisch:"));
		gregorian.add(myGregorianDate);
		gregorian.add(myGregorianDoW);
		date.add(gregorian);

		myView.add(date);
	}

	protected void action(Object o)
	{
		calculate();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e != null) action(e.getSource());
	}

	public void stateChanged(ChangeEvent e)
	{
		if (e != null) action(e.getSource());
	}

	protected void calculate()
	{
		int week = 0;
		if(myWeekList==null)
		{
			week=((Number)myWeekSpinner.getValue()).intValue();
		}
		else
		{
			week=myWeekList.getSelectedIndex()+1;
		}
		int dayOfWeek = myDayOfWeek.getSelectedIndex();
		int prePost = myPrePost.getSelectedIndex() - 1;
		ChurchDay churchDay = (ChurchDay) myChurchDay.getSelectedValue();

		int year = ((Number) myYear.getValue()).intValue();

		Day day;
		
		if(prePost == 0) day= churchDay;
		else if(dayOfWeek==0) day=new Day(week*prePost, churchDay);
		else day=new Day(dayOfWeek, week * prePost, churchDay);

		setDate(myGregorianDate, day.getGregorianDate(year), myGregorianDoW);

		setDate(myJulianDate, day.getJulianDate(year),myJulianDoW);
	}

	protected void setDate(JTextField field, Calendar date,JTextField dowText)
	{
		StringBuilder sb = new StringBuilder(10);
		sb.append(date.get(Calendar.DAY_OF_MONTH));
		if (sb.length() < 2) sb.insert(0, "0");
		sb.append('.');
		sb.append(date.get(Calendar.MONTH) + 1);
		if (sb.length() < 5) sb.insert(3, "0");
		sb.append('.');
		sb.append(date.get(Calendar.YEAR));
		field.setText(sb.toString());
		
		int dow=date.get(Calendar.DAY_OF_WEEK)-1;
		if(dow==0) dow=7;
		dowText.setText(WEEK_DAYS[dow]);		
	}

	@SuppressWarnings("unchecked")
	protected void loadChurchDays()
	{
		Easter easter = new Easter();
		Advent advent = new Advent();

		List<ChurchDay> myDays = new ArrayList<ChurchDay>();
		myDays.add(easter);
		myDays.add(advent);

		try
		{
			File file = new File(CHURCH_DAY_FILE);
			if (!file.exists())
			{
				URL url=Kirchentage.class.getResource("/");
				if(url==null) throw new FileNotFoundException(CHURCH_DAY_FILE);
				String dir = url.getPath();
				file = new File(dir, CHURCH_DAY_FILE);
			}
			try(LineNumberReader reader = new LineNumberReader(new FileReader(file)))
			{
				String line = null;			
				while ((line = reader.readLine()) != null)
				{
					try
					{
						String[] split = line.split("\t");
						for (int i = 0; i < split.length; i++)
						{
							split[i] = split[i].trim();
						}					
						if (split.length >= 2 && split[0].contains("."))
						{
							// Datum lesen: dd.MM.
							String[] date = split[0].split("[.]");
							int dayOfMonth = Integer.parseInt(date[0]);
							int month = Integer.parseInt(date[1]);
							StringBuilder name = new StringBuilder();
							for(int i=1;i<split.length;i++)
							{
								if(i>1) name.append(' ');
								name.append(split[i]);
							}
							myDays.add(new ChurchDay(name.toString(), dayOfMonth, month));
						}
						else
						{
							StringBuilder name = new StringBuilder();
							int week = 0;
							int dow = 7;
							ChurchDay ref = easter;
							int month=-1;
	
							int len = -1;
							// Offset
							if (split.length > ++len)
							{
								week = Integer.parseInt(split[len]);
							}
							// Wochentag
							if (split.length > ++len)
							{
								for (int i = 1; i < WEEK_DAYS.length; i++)
								{
									if (WEEK_DAYS[i].toLowerCase().startsWith(split[len].toLowerCase()))
									{
										dow = i;
										break;
									}
								}
							}
							// Referenz-Tag
							if (split.length > ++len)
							{
								// Auf Monat prüfen
								if(split[len].matches("\\d+"))
								{
									month=Integer.parseInt(split[len]);								
								}
								else if (easter.getName().toLowerCase().startsWith(split[len].toLowerCase()))
								{
									ref = easter;
								}
								else if (advent.getName().toLowerCase().startsWith(
										split[len].toLowerCase()))
								{
									ref = advent;
								}
								else if("advent".startsWith(
										split[len].toLowerCase()))
								{
									ref = advent;
								}
							}
							// Name
							while (split.length > ++len)
							{
								if(name.length()>0) name.append(' ');
								name.append(split[len]);
							}
	
							if (name.length() > 0)
							{
								if(month>0) myDays.add(new MonthDay(week,dow,month,name.toString()));							
								else myDays.add(new ChurchDay(name.toString(), dow, week, ref));
							}
						}
					}
					catch (Exception ex)
					{
						StringBuilder message=new StringBuilder();
						message.append("<html>").append(ex.getClass().getSimpleName());
						message.append("<br>").append(ex.getMessage());
						message.append("<br>in Zeile ").append(reader.getLineNumber());
						message.append(": &quot;").append(line).append("&quot;");
						message.append("</html>");
						JOptionPane.showMessageDialog(myView,message,"Fehler",JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			catch(Exception ex)
			{
				throw ex;
			}
		}
		catch (Exception ex)
		{
			StringBuilder message=new StringBuilder();
			message.append("<html>").append(ex.getClass().getName());
			message.append("<br>").append(ex.getMessage());
			message.append("</html>");
			JOptionPane.showMessageDialog(myView,message,"Fehler",JOptionPane.ERROR_MESSAGE);
		}

		Collections.sort(myDays, new Comparator<ChurchDay>()
		{
			public int compare(ChurchDay o1, ChurchDay o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		myChurchDay.setModel(new ListComboBoxModel(myDays));
		myChurchDay.setSelectedValue(easter,true);
		if(myRows<0 && myDays.size()>40)
		{
			myChurchDay.setLayoutOrientation(JList.VERTICAL);
			myChurchDay.setVisibleRowCount(20);
		}
		else
		{
			myChurchDay.setVisibleRowCount((myRows<1)?(myDays.size()+1)/2:myRows);
		}
	}

	public void caretUpdate(CaretEvent e)
	{
		if(e!=null) action(e.getSource());
	}

	public void focusGained(FocusEvent e)
	{
		if(e.getComponent() instanceof JTextField)
		{
			((JTextField)e.getComponent()).selectAll();
		}
	}

	public void focusLost(FocusEvent e)
	{
		if(e!=null) action(e);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if(e!=null) action(e);
	}
}
