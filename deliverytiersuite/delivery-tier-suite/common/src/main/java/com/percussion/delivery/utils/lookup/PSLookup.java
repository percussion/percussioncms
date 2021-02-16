/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.utils.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/***
 * Provides a generic sys_Lookup style
 * list that can be used by Rhythmyx controls.
 * 
 * @author natechadwick
 * @param <T>
 *
 */
@XmlRootElement(name = "sys_Lookup")
@XmlAccessorType(XmlAccessType.FIELD)
public class PSLookup implements List<PSXEntry>{

	@XmlElement(name="PSXEntry")
	private List<PSXEntry> list = new ArrayList<>();
	
	public PSLookup(){}
	
	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<PSXEntry> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray(Object[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(@SuppressWarnings("rawtypes") Collection c) {
		return list.containsAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(@SuppressWarnings("rawtypes") Collection c) {
		return list.addAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(int index, @SuppressWarnings("rawtypes") Collection c) {
		return list.addAll(index,c);
	}

	@Override
	public boolean removeAll(@SuppressWarnings("rawtypes") Collection c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(@SuppressWarnings("rawtypes") Collection c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	
	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<PSXEntry> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<PSXEntry> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<PSXEntry> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public boolean add(PSXEntry e) {
		return list.add(e);
	}

	@Override
	public PSXEntry get(int index) {
		return list.get(index);
	}

	@Override
	public PSXEntry set(int index, PSXEntry element) {
		return list.set(index, element);
	}

	@Override
	public void add(int index, PSXEntry element) {
		list.add(index, element);
		
	}

	@Override
	public PSXEntry remove(int index) {
		return list.remove(index);
	}
}