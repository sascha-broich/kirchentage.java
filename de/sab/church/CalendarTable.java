package de.sab.church;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import de.sab.util.ColLayout;
import de.sab.util.RowLayout;

public class CalendarTable
{

	protected JPanel view;
	protected JTable table;
	protected JComboBox<String> month;
	protected JSpinner year;
	protected CalendarSystemChooser systemChooser;
	
	
	
	public CalendarTable()
	{
		initView();		
	}
	
	
	
	protected void initView()
	{
		view=new JPanel(new ColLayout(10));
		view.setBorder(new EmptyBorder(10,10,10,10));
		table=new JTable();
		month=new JComboBox<>(new MonthModel());
		year=new JSpinner(new SpinnerNumberModel(1,1,3000,1));
		year.setEditor(new JSpinner.NumberEditor(year,"###0"));
		
		systemChooser=new CalendarSystemChooser(false);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setPreferredSize(new Dimension(7*20,6*16));
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setCellSelectionEnabled(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel row=new JPanel(new RowLayout(10));
		view.add(row);
		row.add(month);
		row.add(year);
		row.add(systemChooser.getView());
		
		row=new JPanel(new RowLayout(10));
		view.add(row);
		row.add(new JScrollPane(table));

		GregorianCalendar calendar=new GregorianCalendar();
		month.setSelectedIndex(calendar.get(Calendar.MONTH));
		year.setValue(calendar.get(Calendar.YEAR));
		updateTable();
		
		Listener listener=new Listener(); 
		
		month.addItemListener(listener);
		year.addChangeListener(listener);
		systemChooser.addChangeListener(listener);
		
	}

	public JPanel getView()
	{
		return view;
	}

	protected void set(int year, int month, boolean julian)
	{
		int col = table.getSelectedColumn();
		int row = table.getSelectedRow();
		Object selected=(col<0|| row<0)?null:table.getValueAt(row, col);
		
		GregorianCalendar calendar=new GregorianCalendar();
		if(julian) calendar.setGregorianChange(new Date(Long.MAX_VALUE));
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setMinimalDaysInFirstWeek(1);
		calendar.set(year,month,1);
		int last=calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		Integer[][] data=new Integer[6][7];
		int offset=(calendar.get(Calendar.DAY_OF_WEEK)+5)%7;
		
		for(int i=0;i<last;i++)
		{
			Integer day=i+1;
			int dow=(i+offset)%7;
			int wom=(i+offset)/7;			
			data[wom][dow]=day;
			if(selected!=null && day.equals(selected))
			{
				col=dow;
				row=wom;
			}
		}
		
		table.setModel(new CalendarModel(data));
		
		if(selected!=null)
		{
	        table.getColumnModel().getSelectionModel().setSelectionInterval(col,col);
	        table.getSelectionModel().setSelectionInterval(row,row);
		}
	}
	
	protected void updateTable()
	{
		int y=((Number)year.getValue()).intValue();
		int m=month.getSelectedIndex();
		boolean julian=systemChooser.isJulian();
		
		set(y,m,julian);
		
	}

	protected static class CalendarModel extends AbstractTableModel
	{
		private static final long	serialVersionUID	= 1L;
		protected Integer[][]	days;
		protected int weeksOfMonth;
		protected static final String[] header={"Mo","Di","Mi","Do","Fr","Sa","So"};

		public CalendarModel(Integer[][] data)
		{
			days = data;
			Integer[] lastWeek=data[data.length-1];
			boolean last=false;
			for(Integer day:lastWeek)
			{
				if(day!=null) last=true;
			}
			weeksOfMonth=(last)?data.length:data.length-1;
		}

		@Override
		public int getColumnCount()
		{
			return 7;
		}

		@Override
		public int getRowCount()
		{
			return weeksOfMonth;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			return days[rowIndex][columnIndex];
		}

		@Override
		public Class< ? > getColumnClass(int columnIndex)
		{
			return String.class;
		}

		@Override
		public String getColumnName(int column)
		{
			return header[column];
		}		
	}
	
	protected static class MonthModel extends AbstractListModel<String> implements ComboBoxModel<String>
	{
		private static final long	serialVersionUID	= 1L;
		
		protected String[] months;
		protected Object selectedItem;
		protected int size;
		
		public MonthModel()
		{
			SimpleDateFormat format=new SimpleDateFormat();
			months=format.getDateFormatSymbols().getMonths();
			for(int i=0;i<months.length;i++)
			{
				if(months[i]!=null && months[i].length()>0) size=i+1;
			}
		}

		@Override
		public String getElementAt(int index)
		{
			return months[index];
		}

		@Override
		public int getSize()
		{
			return size;
		}

		@Override
		public Object getSelectedItem()
		{
			return selectedItem;
		}

		@Override
		public void setSelectedItem(Object anItem)
		{
			selectedItem = anItem;
		}
		
	}
	protected class Listener implements ChangeListener, ItemListener
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
			updateTable();
		}

		@Override
		public void itemStateChanged(ItemEvent e)
		{
			updateTable();
		}
	}
	
}
