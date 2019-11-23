package de.sab.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * <p>Überschrift: Layout-Manager für Minimal-Größe der Komponenten</p>
 * <p>Beschreibung: </p>
 * <p>Copyright (c) 2002</p>
 * <p>Organisation: Fiedler Optoelektronik GmbH</p>
 * @author Sascha Broich
 * @version 1.0
 */
public class MinimumLayout implements LayoutManager2
{
	/** ordnet die Elemente horizontal an */
	public static final int HORIZONTAL = 0;

	/** ordnet die Elemente vertikal an */
	public static final int VERTICAL = 1;

	/** die Ausrichtung */
	protected int myDirection;

	/** die Lücke zwischen den Elementen */
	protected int myGap;

	/**
	 * Erzeugt ein neues horizontales Minimum-Layout 
	 */
	public MinimumLayout()
	{
		this(HORIZONTAL, 0);
	}

	/**
	 * Erzeugt ein neues Minimum-Layout mit der Ausrichtung <code>direction</code>
	 * @param direction die Ausrichtung:
	 * <ul>
	 * <li>HORIZONTAL</li>
	 * <li>VERTICAL</li>
	 * </ul>
	 */
	public MinimumLayout(int direction)
	{
		this(direction, 0);
	}

	/**
	 * Erzeugt ein neues Minimum-Layout mit der Ausrichtung <code>direction</code>
	 * @param direction die Ausrichtung:
	 * <ul>
	 * <li>HORIZONTAL</li>
	 * <li>VERTICAL</li>
	 * </ul>
	 * @param gap die Lücke zwischen den Elementen in Pixeln
	 */
	public MinimumLayout(int direction, int gap)
	{
		setDirection(direction);
		setGap(gap);

	}

	/**
	 * setzt die Ausrichtung
	 * @param direction die Ausrichtung:
	 * <ul>
	 * <li>HORIZONTAL</li>
	 * <li>VERTICAL</li>
	 * </ul>
	 */
	public void setDirection(int direction)
	{
		switch (direction)
		{
			default :
			case HORIZONTAL :
				myDirection = HORIZONTAL;
				break;
			case VERTICAL :
				myDirection = VERTICAL;
				break;
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#addLayoutComponent(String, Component)
	 */
	public void addLayoutComponent(String name, Component comp)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#removeLayoutComponent(Component)
	 */
	public void removeLayoutComponent(Component comp)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#preferredLayoutSize(Container)
	 */
	public Dimension preferredLayoutSize(Container parent)
	{
		Dimension dim = new Dimension(0, 0);

		for (int i = 0; i<parent.getComponentCount();i++)
		{
			Component c=parent.getComponent(i);
			if(!c.isVisible()) continue;
			
			Dimension cdim =c.getPreferredSize();
			if (myDirection == VERTICAL)
			{
				dim.width = Math.max(dim.width, cdim.width);
				dim.height += cdim.height + myGap;
			}
			else
			{
				dim.width += cdim.width + myGap;
				dim.height = Math.max(cdim.height, dim.height);
			}
		}
		
		Insets insets=parent.getInsets();
		dim.width+=insets.left+insets.right;
		dim.height+=insets.top+insets.bottom;

		return dim;
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#minimumLayoutSize(Container)
	 */
	public Dimension minimumLayoutSize(Container parent)
	{
		return preferredLayoutSize(parent);
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#layoutContainer(Container)
	 */
	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();

		int x0 = insets.left;
		int y0 = insets.top;

		int x = x0;
		int y = y0;

		for (int i = 0; i < parent.getComponentCount(); i++)
		{
			Component c = parent.getComponent(i);
			if(!c.isVisible()) continue;
			Dimension d = c.getPreferredSize();
			if (myDirection == VERTICAL)
			{
				c.setBounds(x, y, d.width, d.height);
				y += d.height + myGap;
			}
			else
			{
				c.setBounds(x, y, d.width, d.height);
				x += d.width + myGap;
			}
		}

	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#addLayoutComponent(Component, Object)
	 */
	public void addLayoutComponent(Component comp, Object constraints)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#getLayoutAlignmentX(Container)
	 */
	public float getLayoutAlignmentX(Container target)
	{
		return Component.LEFT_ALIGNMENT;
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#getLayoutAlignmentY(Container)
	 */
	public float getLayoutAlignmentY(Container target)
	{
		return Component.TOP_ALIGNMENT;
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#invalidateLayout(Container)
	 */
	public void invalidateLayout(Container target)
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#maximumLayoutSize(Container)
	 */
	public Dimension maximumLayoutSize(Container target)
	{
		return preferredLayoutSize(target);
	}

	/**
	 * @return int
	 */
	public int getGap()
	{
		return myGap;
	}

	/**
	 * Sets the gap.
	 * @param gap The gap to set
	 */
	public void setGap(int gap)
	{
		myGap = gap;
	}

}
