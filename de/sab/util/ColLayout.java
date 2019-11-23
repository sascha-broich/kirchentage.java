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

public class ColLayout implements LayoutManager
{
	protected int myGap;
	
	public ColLayout(int gap)
	{
		setGap(gap);
	}
	
	public ColLayout()
	{
		super();
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

	public int[] getHeights(Container parent, boolean preferred)
	{
		int count=parent.getComponentCount();
		int[] heights=new int[count];
		for(int i=0;i<count;i++)
		{
			Component c=parent.getComponent(i);
			if(c!=null && c.isVisible()) 
			{
				heights[i]=getSize(c, preferred).height;
			}
		}
		return heights;
	}

	public int[] getWidths(Container parent, boolean preferred)
	{
		int count=parent.getComponentCount();
		int[][] widths=new int[count][];
		int max=0;
		for(int i=0;i<count;i++)
		{
			Component c=parent.getComponent(i);
			if(!c.isVisible()) continue;
			if(c instanceof Container)
			{
				LayoutManager lm=((Container)c).getLayout();
				if(lm instanceof RowLayout)
				{
					widths[i]=((RowLayout)lm).getWidths((Container)c,preferred);
					if(widths[i]!=null)
					{
						max=Math.max(max,widths[i].length);
					}
				}
			}			
		}
		int[] res=new int[max];
		for(int i=0;i<widths.length;i++)
		{
			if(widths[i]==null) continue;
			for(int j=0;j<widths[i].length;j++)
			{
				res[j]=Math.max(res[j],widths[i][j]);
			}
		}
		return res;
	}

	
	protected Dimension getSize(Component c, boolean preferred)
	{
		if(preferred) return c.getPreferredSize();
		else return c.getMinimumSize();
	}
	
	protected Dimension getLayoutSize(Container parent, boolean preferred)
	{
		// Wenn parent in einem RowLayout steckt, 
		// von diesem die Breiten holen und die Größen anpassen

		Dimension max=new Dimension();

		Container c=parent.getParent();
		LayoutManager lm=(c==null)?null:c.getLayout();
		if(lm!=null && (lm instanceof RowLayout))
		{
			int[] heights=((RowLayout)lm).getHeights(c, preferred);
			for(int i=0;i<parent.getComponentCount();i++)
			{
				Dimension d=getSize(parent.getComponent(i), preferred);
				max.width=Math.max(max.width,d.width);
				max.height+=heights[i];
				if(i>0) max.height+=getGap();				
			}
		}
		else
		{
			for(int i=0;i<parent.getComponentCount();i++)
			{
				Dimension d=getSize(parent.getComponent(i), preferred);
				max.width=Math.max(max.width,d.width);
				max.height+=d.height;
				if(i>0) max.height+=getGap();				
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
		
		int[] heights;

		Container c=parent.getParent();
		LayoutManager lm=(c==null)?null:c.getLayout();
		if(lm!=null && (lm instanceof RowLayout))
		{
			heights=((RowLayout)lm).getHeights(c, preferred);
		}
		else
		{
			heights=getHeights(parent, preferred);
		}

		int x=r.x;
		int y=r.y;
		int w=r.width;
		for(int i=0;i<parent.getComponentCount();i++)
		{
			Component comp=parent.getComponent(i);
			if(comp!=null) comp.setBounds(x, y, w, heights[i]);
			y+=heights[i]+getGap();
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
