/*
 * Created on 14.05.2004
 */
package de.sab.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MinMaxPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	protected JLabel myTitle;
	protected MinMaxToggle myMinMaxToggle;
	protected JComponent myMinComponent;
	protected JComponent myMaxComponent;
	protected JPanel myTitlePanel;
	protected ChangeEvent myChangeEvent=new ChangeEvent(this);
	protected List<ChangeListener> myChangeListeners=new ArrayList<ChangeListener>();

	public MinMaxPanel(String title)
	{
		this();
		setTitle(title);
	}

	public MinMaxPanel(JComponent maxComponent)
	{
		this();
		setMaxComponent(maxComponent);
	}

	public MinMaxPanel(JComponent maxComponent, JComponent minComponent)
	{
		this();
		setMaxComponent(maxComponent);
		setMinComponent(minComponent);
	}

	public MinMaxPanel(String title, JComponent maxComponent)
	{
		this();
		setTitle(title);
		setMaxComponent(maxComponent);
	}
	
	public MinMaxPanel(String title, JComponent maxComponent,JComponent minComponent)
	{
		this();
		setTitle(title);
		setMaxComponent(maxComponent);
		setMinComponent(minComponent);
	}

	public MinMaxPanel(String title, JComponent maxComponent,boolean maximized)
	{
		this();
		setTitle(title);
		setMaxComponent(maxComponent);
		setMinimized(!maximized);
	}
	
	public MinMaxPanel()
	{
		super(new BorderLayout());
		myTitle=new JLabel();
		myMinMaxToggle=new MinMaxToggle();
		myTitlePanel=new JPanel(new BorderLayout());
		myTitlePanel.add(myMinMaxToggle,BorderLayout.EAST);
		myTitlePanel.add(myTitle,BorderLayout.WEST);
		add(myTitlePanel,BorderLayout.NORTH);
		Color titleColor=UIManager.getColor("ToggleButton.select");
		myTitle.setOpaque(false);
		setFrameColor(titleColor);
		myMinMaxToggle.addActionListener(this);
		myTitlePanel.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(!isEnabled()) return;
				if(e.getClickCount()==1) toggle();
			}
		});
	}
	
	public void setTitleColor(Color color)
	{
		myTitle.setForeground(color);
	}

	public void setFrameColor(Color color)
	{
		myTitle.setBackground(color);
		myTitlePanel.setBackground(color);
		myTitlePanel.setBorder(new LineBorder(color));
		setBorder(new LineBorder(color));
	}
	
	protected void setState()
	{
		if(myMinComponent!=null) remove(myMinComponent);
		if(myMaxComponent!=null) remove(myMaxComponent);
		if(isMinimized() && myMinComponent!=null)
		{
			add(myMinComponent,BorderLayout.CENTER);
		}
		else if(isMaximized() && myMaxComponent!=null)
		{
			add(myMaxComponent,BorderLayout.CENTER);
		}
		revalidate();
		fireStateChanged();
	}

	public void toggle()
	{
		myMinMaxToggle.toggle();
	}
	
	public void setMinimized(boolean minimized)
	{
		if(isMaximized()==minimized) myMinMaxToggle.toggle();
	}
	
	public boolean isMinimized()
	{
		return !isMaximized();
	}

	public boolean isMaximized()
	{
		return myMinMaxToggle.isSelected();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e!=null && e.getSource()==myMinMaxToggle)
		{
			setState();
		}
	}
	public JComponent getMaxComponent()
	{
		return myMaxComponent;
	}
	
	public JComponent getMinComponent()
	{
		return myMinComponent;
	}

	public void setMaxComponent(JComponent maxComponent)
	{
		if(myMaxComponent!=null) remove(myMaxComponent);
		myMaxComponent=maxComponent;
		setState();
	}

	public void setMinComponent(JComponent minComponent)
	{
		if(myMinComponent!=null) remove(myMinComponent);
		myMinComponent=minComponent;
		setState();
	}
	
	public String getTitle()
	{
		return myTitle.getText();
	}
	
	public void setTitle(String title)
	{
		myTitle.setText(title);
	}
	
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if(myMaxComponent!=null) myMaxComponent.setEnabled(enabled);
		if(myMinComponent!=null) myMinComponent.setEnabled(enabled);
	}

	protected class MinMaxToggle extends JButton
	{
		private static final long serialVersionUID = 1L;
		protected Icon mySelectedIcon;
		protected Icon myUnselectedIcon;
		protected HashSet<ActionListener> myListeners;
		protected boolean mySelected;	
		
		public MinMaxToggle()
		{
			initIcons();
			setIconTextGap(0);
			myListeners=new HashSet<ActionListener>();
			setOpaque(true);
			setBorder(new EmptyBorder(1,1,1,1));
			addMouseListener(new Mouse());
			mySelected=true;
			setSelected(false);
		}
		
		protected void initIcons()
		{
			mySelectedIcon=UIManager.getIcon("InternalFrame.minimizeIcon");
			myUnselectedIcon=UIManager.getIcon("InternalFrame.maximizeIcon");
		}
		
		public boolean isSelected()
		{
			return mySelected;
		}
		public void setSelected(boolean selected)
		{
			if(mySelected!=selected)
			{
				mySelected = selected;
				setIcon(mySelected?mySelectedIcon:myUnselectedIcon);
				setDisabledIcon(mySelected?mySelectedIcon:myUnselectedIcon);
				fireActionEvent();
			}
		}
		
		public void toggle()
		{
			setSelected(!isSelected());		
		}
		
		public void addActionListener(ActionListener listener)
		{
			myListeners.add(listener);
		}
		
		public void removeActionListener(ActionListener listener)
		{
			myListeners.remove(listener);
		}

		protected void fireActionEvent()
		{
			ActionEvent e=new ActionEvent(this,0,null);
			for (ActionListener listener : myListeners)
			{
				listener.actionPerformed(e);			
			}
		}
		
		protected class Mouse extends MouseAdapter
		{		
			public void mouseClicked(MouseEvent e)
			{
				if(!isEnabled()) return;
				toggle();
			}
		}
	}
	
	public void addChangeListener(ChangeListener listener)
	{
		if(listener!=null && !myChangeListeners.contains(listener))
		{
			myChangeListeners.add(listener);
		}
	}
	
	public void removeChangeListener(ChangeListener listener)
	{
		if(listener!=null)
		{
			myChangeListeners.remove(listener);
		}
	}
	
	protected void fireStateChanged()
	{
		for(ChangeListener listener:myChangeListeners)
		{
			listener.stateChanged(myChangeEvent);
		}
	}
}
