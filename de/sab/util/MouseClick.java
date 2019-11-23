package de.sab.util;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public abstract class MouseClick extends MouseAdapter
{
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if(e==null) return;
		switch(e.getClickCount())
		{
			case 1:
			{
				if(SwingUtilities.isRightMouseButton(e))
				{
					rightSingleClick(e);
				}
				else if(SwingUtilities.isLeftMouseButton(e))
				{
					leftSingleClick(e);					
				}
				else if(SwingUtilities.isMiddleMouseButton(e))
				{
					middleSingleClick(e);					
				}
				break;
			}
			case 2:
			{
				if(SwingUtilities.isRightMouseButton(e))
				{
					rightDoubleClick(e);
				}
				else if(SwingUtilities.isLeftMouseButton(e))
				{
					leftDoubleClick(e);					
				}
				else if(SwingUtilities.isMiddleMouseButton(e))
				{
					middleDoubleClick(e);					
				}
				break;
			}
			default:break;			
		}
	}
	
	protected void leftSingleClick(MouseEvent e)
	{
		
	}
	
	protected void rightSingleClick(MouseEvent e)
	{
		
	}
		
	protected void leftDoubleClick(MouseEvent e)
	{
		
	}
	
	protected void rightDoubleClick(MouseEvent e)
	{
		
	}
	
	protected void middleSingleClick(MouseEvent e)
	{
		
	}
	
	protected void middleDoubleClick(MouseEvent e)
	{
		
	}
	
	public static void showPopup(Component view, boolean below, Component... components)
	{
		JPopupMenu popupMenu=new JPopupMenu();
		for(Component component:components)
		{
			popupMenu.add(component);
		}
		popupMenu.show(view, view.getX(), view.getY()+(below?view.getHeight():0));		
	}	

	public static void showPopup(MouseEvent e, Component... components)
	{
		JPopupMenu popupMenu=new JPopupMenu();
		for(Component component:components)
		{
			popupMenu.add(component);
		}
		popupMenu.show(e.getComponent(), e.getX(),e.getY());		
	}	

	public static void showPopup(MouseEvent e, Collection<? extends Component> components)
	{
		showPopup(e, components.toArray(new Component[components.size()]));
	}

	public static void showPopup(Component view, boolean below, Collection<? extends Component> components)
	{
		showPopup(view,below, components.toArray(new Component[components.size()]));
	}
}
