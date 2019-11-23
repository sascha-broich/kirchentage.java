/*
 * Created on 04.03.2005
 */
package de.sab.util;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class FileDropTarget extends DropTargetAdapter
{
	public static interface Listener
	{
		void loadFile(File file);
	}
	public static interface MultiListener extends Listener
	{
		void loadFiles(File...files);
	}
	
	protected Listener myListener;
		

	public FileDropTarget(Listener listener)
	{
		myListener=listener;
	}

	public FileDropTarget(Component component, Listener listener)
	{
		this(listener);
		component.setDropTarget(new DropTarget(component,this));		
	}

	public void drop(DropTargetDropEvent dtde)
	{
		try
		{
			dtde.acceptDrop(DnDConstants.ACTION_LINK);
			Transferable t=dtde.getTransferable();
			DataFlavor df=DataFlavor.javaFileListFlavor;
			if(t.isDataFlavorSupported(df))
			{
				List<?> list=(List<?>)t.getTransferData(df);
				if(list.size()>0)
				{
					if(myListener instanceof MultiListener)
					{
						List<File> files=new ArrayList<File>(list.size());
						for(Object o:list)
						{
							if(o instanceof File)
							{
								files.add((File)o);
							}
						}
						((MultiListener)myListener).loadFiles(files.toArray(new File[files.size()]));
					}
					else
					{
						Object o=list.get(0);
						if(o instanceof File)
						{
							myListener.loadFile((File)o);
						}
					}
				}
			}
		}
		catch (Throwable ex)
		{
			JOptionPane.showMessageDialog(null,ex,"Fehler",JOptionPane.ERROR_MESSAGE);
		}
	}

}
