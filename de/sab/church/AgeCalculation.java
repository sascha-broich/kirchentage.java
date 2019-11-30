package de.sab.church;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import de.sab.util.ColLayout;
import de.sab.util.RowLayout;

public class AgeCalculation
{
	protected JComponent myView;
	protected JTextField myBirthdateField;
	protected JTextField myDeathdateField;
	protected JTextField myYearsField;
	protected JTextField myMonthsField;
	protected JTextField myWeeksField;
	protected JTextField myDaysField;
	
	final SimpleDateFormat dateFormat=new SimpleDateFormat("dd.MM.yyyy");
	
	protected CalendarSystemChooser myCalendarSystem;
	
	public AgeCalculation()
	{
		initView();
	}
	
	public JComponent getView()
	{
		return myView;
	}

	protected void initView()
	{
		myView=new JPanel(new RowLayout());
		myView.setBorder(new EmptyBorder(5,5,5,5));
		myBirthdateField=createDateField();
		myDeathdateField=createDateField();
		myYearsField=createNumberField(4);
		myMonthsField=createNumberField(2);
		myWeeksField=createNumberField(2);
		myDaysField=createNumberField(2);

		myView.add(new Label("*"));
		myView.add(myBirthdateField);
		JButton birthDate = new JButton(new AbstractAction("?")
		{
			private static final long	serialVersionUID	= 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				calculate(myBirthdateField);
			}
		});
		birthDate.setToolTipText("Berechne Geburtsdatum");
		myView.add(birthDate);
		
		myView.add(new Label(":"));
		myView.add(myYearsField);
		myView.add(new Label("Jahre"));
		myView.add(myMonthsField);
		myView.add(new Label("Monate"));
		myView.add(myWeeksField);
		myView.add(new Label("Wochen"));
		myView.add(myDaysField);
		myView.add(new Label("Tage"));
		myView.add(new Label(":"));
		myView.add(new Label("+"));
		myView.add(myDeathdateField);
		JButton deathDate = new JButton(new AbstractAction("?")
		{
			private static final long	serialVersionUID	= 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				calculate(myDeathdateField);
			}
		});
		deathDate.setToolTipText("Berechne Sterbedatum");
		myView.add(deathDate);
		myView.add(new Label(" "));
		myCalendarSystem=new CalendarSystemChooser();
		myView.add(myCalendarSystem.getView());
		
		JPanel col=new JPanel(new ColLayout());
		col.add(myView);
		myView=col;
	}
	
	private JTextField createNumberField(int length)
	{
		DecimalFormat format=new DecimalFormat("0");
		format.setMaximumIntegerDigits(length);
		format.setMaximumFractionDigits(0);
		JFormattedTextField field=new SelectAllFormattedTextField(format);
		field.setHorizontalAlignment(JTextField.RIGHT);
		field.setColumns(length);
		return field;
	}
	
	private JTextField createDateField()
	{
		JFormattedTextField field = new SelectAllFormattedTextField(dateFormat);
		field.setColumns(10);
		return field;
	}

	protected void calculate(Object target)
	{
		int years=getInt(myYearsField);
		int months=getInt(myMonthsField);
		int weeks=getInt(myWeeksField);
		int days=getInt(myDaysField);
		
		if(target==myBirthdateField)
		{
			GregorianCalendar date=getCalendar(myDeathdateField.getText());
			if(date==null) return;
			
			date.add(Calendar.YEAR, -years);
			date.add(Calendar.MONTH, -months);
			date.add(Calendar.WEEK_OF_YEAR, -weeks);
			date.add(Calendar.DAY_OF_MONTH, -days);
			myBirthdateField.setText(dateFormat.format(date.getTime()));
		}
		else if(target==myDeathdateField)
		{
			GregorianCalendar date=getCalendar(myBirthdateField.getText());
			if(date==null) return;
			
			date.add(Calendar.YEAR, years);
			date.add(Calendar.MONTH, months);
			date.add(Calendar.WEEK_OF_YEAR, weeks);
			date.add(Calendar.DAY_OF_MONTH, days);
			myDeathdateField.setText(dateFormat.format(date.getTime()));
		}
		
		
	}

	private int getInt(JTextField field)
	{
		String text=field.getText();
		try
		{
			return Integer.parseInt(text);
		}
		catch(Exception ex)
		{
			return 0;
		}
	}

	private GregorianCalendar getCalendar(String text)
	{
		try
		{
			return myCalendarSystem.getCalendar(dateFormat.parse(text));
		}
		catch(Exception ex)
		{
			
		}
		return null;
	}
}
