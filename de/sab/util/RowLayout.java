/*
 * Created on 30.04.2004
 */
package de.sab.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

public class RowLayout implements LayoutManager
{
	protected int myGap;
	
	public RowLayout()
	{
		super();
	}
	
	public RowLayout(int gap)
	{
		setGap(gap);
	}

	public void addLayoutComponent(String name, Component comp)
	{
	}
	public void removeLayoutComponent(Component comp)
	{
	}

	public Dimension minimumLayoutSize(Container parent)
	{
		return getLayoutSize(parent, false);
	}
	public Dimension preferredLayoutSize(Container parent)
	{
		return getLayoutSize(parent,true);
	}
	
	public int[] getWidths(Container parent, boolean preferred)
	{
		int count=parent.getComponentCount();
		int[] widths=new int[count];
		for(int i=0;i<count;i++)
		{
			Component c=parent.getComponent(i);
			if(c!=null && c.isVisible()) 
			{
				widths[i]=getSize(c, preferred).width;
			}
		}
		return widths;
	}
	
	public int[] getHeights(Container parent, boolean preferred)
	{
		int count=parent.getComponentCount();
		int[][] heights=new int[count][];
		int max=0;
		for(int i=0;i<count;i++)
		{
			Component c=parent.getComponent(i);
			if(c==null || !c.isVisible()) continue;
			if(c instanceof Container)
			{
				LayoutManager lm=((Container)c).getLayout();
				if(lm instanceof ColLayout)
				{
					heights[i]=((ColLayout)lm).getHeights((Container)c,preferred);
					if(heights[i]!=null)
					{
						max=Math.max(max,heights[i].length);
					}
				}
			}			
		}
		int[] res=new int[max];
		for(int i=0;i<heights.length;i++)
		{
			if(heights[i]==null) continue;
			for(int j=0;j<heights[i].length;j++)
			{
				res[j]=Math.max(res[j],heights[i][j]);
			}
		}
		return res;
	}
	
	protected Dimension getSize(Component c, boolean preferred)
	{
		if(c==null || !c.isVisible()) return new Dimension();
		if(preferred) return c.getPreferredSize();
		else return c.getMinimumSize();
	}
	
	protected Dimension getLayoutSize(Container parent, boolean preferred)
	{
		// Wenn parent in einem ColLayout steckt, 
		// von diesem die Breiten holen und die Größen anpassen
		Dimension max=new Dimension();

		Container c=parent.getParent();
		LayoutManager lm=(c==null)?null:c.getLayout();
		if(lm!=null && (lm instanceof ColLayout))
		{
			int[] widths=((ColLayout)lm).getWidths(c, preferred);
			for(int i=0;i<parent.getComponentCount();i++)
			{
				Dimension d=getSize(parent.getComponent(i), preferred);
				max.width+=widths[i];
				if(i>0) max.width+=getGap();
				max.height=Math.max(max.height,d.height);
			}
		}
		else
		{
			for(int i=0;i<parent.getComponentCount();i++)
			{
				Dimension d=getSize(parent.getComponent(i), preferred);
				max.width+=d.width;
				if(i>0) max.width+=getGap();
				max.height=Math.max(max.height,d.height);
			}
		}
		Insets insets=parent.getInsets();
		max.width+=insets.left+insets.right;
		max.height+=insets.top+insets.bottom;
		return max;
	}
	
	protected Rectangle getInnerRect(Container c)
	{
		Dimension d=c.getSize();
		Insets i=c.getInsets();
		d.width-=i.left+i.right;
		d.height-=i.top+i.bottom;
		return new Rectangle(i.left,i.top,d.width,d.height);
	}
	

	public void layoutContainer(Container parent)
	{
		Rectangle r=getInnerRect(parent);
		boolean preferred=true;//(d.width<=r.width &&  d.height<=r.height);		
		
		int[] widths;

		Container c=parent.getParent();
		LayoutManager lm=(c==null)?null:c.getLayout();
		if(lm!=null && (lm instanceof ColLayout))
		{
			widths=((ColLayout)lm).getWidths(c, preferred);
		}
		else
		{
			widths=getWidths(parent, preferred);
		}

		int x=r.x;
		int y=r.y;
		int h=r.height;
		for(int i=0;i<parent.getComponentCount();i++)
		{
			Component comp=parent.getComponent(i);
			if(comp!=null) comp.setBounds(x, y, widths[i], h);
			x+=widths[i]+getGap();
			
		}
	}	
	
	public int getGap()
	{
		return myGap;
	}
	public void setGap(int gap)
	{
		myGap = gap;
	}
}
