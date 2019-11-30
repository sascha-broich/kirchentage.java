/*
 * @(#)DefaultComboBoxModel.java 1.17 03/01/23 Copyright 2003 Sun Microsystems,
 * Inc. All rights reserved. SUN PROPRIETARY/CONFIDENTIAL. Use is subject to
 * license terms.
 */
package de.sab.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

/**
 * The default model for combo boxes.
 * 
 * @version 1.17 01/23/03
 * @author Arnaud Weber
 * @author Tom Santos
 */

@SuppressWarnings("rawtypes")
public class ListComboBoxModel extends AbstractListModel implements MutableComboBoxModel,
		Serializable
{
	private static final long	serialVersionUID	= 1L;
	List<Object>                objects;
	Object						selectedObject;

	/**
	 * Constructs an empty DefaultComboBoxModel object.
	 */
	public ListComboBoxModel()
	{
		objects = new LinkedList<Object>();
	}

	/**
	 * Constructs a DefaultComboBoxModel object initialized with an array of
	 * objects.
	 * 
	 * @param items an array of Object objects
	 */
	public ListComboBoxModel(final Object items[])
	{
		objects = new LinkedList<Object>();

		int i, c;
		for (Object item:items)
		{
			objects.add(item);
		}

		if (getSize() > 0)
		{
			selectedObject = getElementAt(0);
		}
	}

	/**
	 * Constructs a DefaultComboBoxModel object initialized with a vector.
	 * 
	 * @param v a Vector object ...
	 */
	public ListComboBoxModel(List<? extends Object> v)
	{
		objects=new ArrayList<Object>(v);

		if (getSize() > 0)
		{
			selectedObject = getElementAt(0);
		}
	}

	// implements javax.swing.ComboBoxModel
	/**
	 * Set the value of the selected item. The selected item may be null.
	 * <p>
	 * 
	 * @param anObject The combo box value or null for no selection.
	 */
	public void setSelectedItem(Object anObject)
	{
		if ((selectedObject != null && !selectedObject.equals(anObject)) || selectedObject == null
				&& anObject != null)
		{
			selectedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	// implements javax.swing.ComboBoxModel
	public Object getSelectedItem()
	{
		return selectedObject;
	}

	// implements javax.swing.ListModel
	public int getSize()
	{
		return objects.size();
	}

	// implements javax.swing.ListModel
	public Object getElementAt(int index)
	{
		if (index >= 0 && index < objects.size()) return objects.get(index);
		else return null;
	}

	/**
	 * Returns the index-position of the specified object in the list.
	 * 
	 * @param anObject
	 * @return an int representing the index position, where 0 is the first
	 *         position
	 */
	public int getIndexOf(Object anObject)
	{
		return objects.indexOf(anObject);
	}

	// implements javax.swing.MutableComboBoxModel
	public void addElement(Object anObject)
	{
		objects.add(anObject);
		fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
		if (objects.size() == 1 && selectedObject == null && anObject != null)
		{
			setSelectedItem(anObject);
		}
	}

	// implements javax.swing.MutableComboBoxModel
	public void insertElementAt(Object anObject, int index)
	{
		objects.add(index, anObject);
		fireIntervalAdded(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	public void removeElementAt(int index)
	{
		if (getElementAt(index) == selectedObject)
		{
			if (index == 0)
			{
				setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
			}
			else
			{
				setSelectedItem(getElementAt(index - 1));
			}
		}

		objects.remove(index);

		fireIntervalRemoved(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	public void removeElement(Object anObject)
	{
		int index = objects.indexOf(anObject);
		if (index != -1)
		{
			removeElementAt(index);
		}
	}

	/**
	 * Empties the list.
	 */
	public void removeAllElements()
	{
		if (objects.size() > 0)
		{
			int firstIndex = 0;
			int lastIndex = objects.size() - 1;
			objects.clear();
			selectedObject = null;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		}
		else
		{
			selectedObject = null;
		}
	}

	public void setValues(List<? extends Object> list)
	{
		if (list == null) return;
		objects.clear();
		objects.addAll(list);
		if (list.size() > 0)
		{
			selectedObject = objects.get(0);
			fireIntervalAdded(this, 0, objects.size());
		}
	}

	public void setValues(Object[] list)
	{
		removeAllElements();
		if (list == null) return;
		for (int i = 0; i < list.length; i++)
		{
			objects.add(list[i]);
		}
		if (list.length > 0)
		{
			selectedObject = objects.get(0);
			fireIntervalAdded(this, 0, objects.size());
		}
	}

	public List<?> getList()
	{
		return objects;
	}

	public void addAll(List<? extends Object> list)
	{
		int start = objects.size();
		objects.addAll(list);
		int ende = objects.size();
		fireIntervalAdded(this, start, ende);
	}
}
