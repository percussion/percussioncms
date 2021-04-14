/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
/**
 */
@XmlRootElement(name="Slot")
public class Slot implements Comparable<Slot>{
	
	/**
	 * Field name.
	 */
	String name;
	/**
	 * Field type.
	 */
	String type;
	/**
	 * Field items.
	 */
	List<SlotItem> items;
	
	/**
	 * Method getItems.
	 * @return List<SlotItem>
	 */
	@XmlElement(name="Item")
	public List<SlotItem> getItems() {
		return items;
	}
	/**
	 * Method setItems.
	 * @param items List<SlotItem>
	 */
	public void setItems(List<SlotItem> items) {
		this.items = items;
	}
	/**
	 * Method getName.
	 * @return String
	 */
	@XmlAttribute
	public String getName() {
		return name;
	}
	/**
	 * Method setName.
	 * @param name String
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Method getType.
	 * @return String
	 */
	@XmlAttribute
	public String getType() {
		return type;
	}
	/**
	 * Method setType.
	 * @param type String
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Method compareTo.
	 * @param o Slot
	 * @return int
	 */
	public int compareTo(Slot o) {
		return this.name.compareTo(o.getName());
	}
	/**
	 * Method hashCode.
	 * @return int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	/**
	 * Method equals.
	 * @param obj Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Slot other = (Slot) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}
