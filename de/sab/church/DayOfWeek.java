package de.sab.church;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.sab.util.RowLayout;

public class DayOfWeek implements ActionListener, FocusListener
{
	protected JComponent				myView;

	protected JTextField				myInput;
	protected JTextField				myWeekDay;
	protected JTextField				myOppositeDate;

	protected HashMap<Integer, String>	myWeekDays;
	protected SimpleDateFormat			myDateFormat	= new SimpleDateFormat("dd.MM.yyyy");

	protected CalendarSystemChooser	myCalendarSystem;
	protected CalendarSystemChooser	myOppositeSystem;
	protected JLabel	myOpposite;

	public DayOfWeek()
	{
		initWeekDays();
		initView();
	}
	
	public JComponent getView()
	{
		return myView;
	}

	protected void initWeekDays()
	{
		myWeekDays = new HashMap<Integer, String>();
		myWeekDays.put(Calendar.SUNDAY, "Sonntag");
		myWeekDays.put(Calendar.MONDAY, "Montag");
		myWeekDays.put(Calendar.TUESDAY, "Dienstag");
		myWeekDays.put(Calendar.WEDNESDAY, "Mittwoch");
		myWeekDays.put(Calendar.THURSDAY, "Donnerstag");
		myWeekDays.put(Calendar.FRIDAY, "Freitag");
		myWeekDays.put(Calendar.SATURDAY, "Samstag");
	}

	protected void initView()
	{
		myView = new JPanel(new RowLayout(5));
		myView.setBorder(new EmptyBorder(5,5,5,5));

		myInput = new JTextField(10);
		initInput();
		myView.add(myInput);
		
		myWeekDay = new JTextField(10);
		myWeekDay.setEditable(false);
		myWeekDay.setDragEnabled(true);
		myWeekDay.addFocusListener(this);
		
		myOppositeDate=new JTextField(10);
		myOppositeDate.setEditable(false);
		myOppositeDate.setDragEnabled(true);
		myOppositeDate.addFocusListener(this);

		myCalendarSystem=new CalendarSystemChooser(false);
		myOppositeSystem=new CalendarSystemChooser(true);
		myCalendarSystem.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				myOppositeSystem=new CalendarSystemChooser(myCalendarSystem.isGregorian());
				myOpposite.setText(myOppositeSystem.getText());
				calculate();
			}			
		});
		
		JPanel julian = new JPanel(new RowLayout(5));
		julian.add(myCalendarSystem.getView());
		julian.add(myWeekDay);
		julian.add(new JLabel("entspricht "));
		myOpposite = new JLabel(myOppositeSystem.getText());
		julian.add(myOpposite);		
		julian.add(myOppositeDate);

		myView.add(julian);
	}

	protected void initInput()
	{
//		myInput.addFocusListener(this);
		myInput.setDragEnabled(true);
		myInput.setDropTarget(new DropTarget(myInput, new DropTargetAdapter()
		{
			public void drop(java.awt.dnd.DropTargetDropEvent dtde)
			{
				try
				{
					Transferable tf=dtde.getTransferable();
					DataFlavor[] flavors=tf.getTransferDataFlavors();
					for(DataFlavor flavor:flavors)
					{
						if(!flavor.isFlavorTextType()) continue;
						dtde.acceptDrop(dtde.getSourceActions());
						Object o = tf.getTransferData(flavor);
						if (o instanceof String)
						{
							myInput.setText((String) o);
							calculate();
							
						}
					}
				}
				catch (Exception e)
				{
				}

			}
		}));
		myInput.addActionListener(this);
		myInput.addCaretListener(new CaretListener()
		{
			public void caretUpdate(javax.swing.event.CaretEvent e)
			{
				calculate();
			}
		});
	}

	public void actionPerformed(ActionEvent e)
	{
		calculate();
	}

	protected void calculate()
	{
		String text = myInput.getText();
		String doW = null;
		String oppositeDate=null;
		try
		{
			String[] split = text.split("\\.");
			int day = Integer.parseInt(split[0]);
			int month = Integer.parseInt(split[1]);
			int year = Integer.parseInt(split[2]);
			GregorianCalendar calendar=myCalendarSystem.getCalendar(null);
			calendar.set(year, month-1, day);

			doW = myWeekDays.get(calendar.get(Calendar.DAY_OF_WEEK));
			
			Date date=calendar.getTime();
			
			oppositeDate=toString(myOppositeSystem.getCalendar(date));
		}
		catch (Exception ex)
		{
		}

		myWeekDay.setText(doW);
		myOppositeDate.setText(oppositeDate);
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
		if(e!=null) calculate();
	}
	
	public String toString(Calendar calendar)
	{
		StringBuilder sb=new StringBuilder(10);
		
		int day=calendar.get(Calendar.DAY_OF_MONTH);
		int month=calendar.get(Calendar.MONTH)+1;
		int year=calendar.get(Calendar.YEAR);
		
		if(day<10) sb.append('0');
		sb.append(day);
		sb.append('.');				
		if(month<10) sb.append('0');
		sb.append(month);
		sb.append('.');
		sb.append(year);		
		
		return sb.toString();
	}
}
