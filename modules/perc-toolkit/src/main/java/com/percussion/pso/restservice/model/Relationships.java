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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 */
public class Relationships {
	/**
	 * Field slots.
	 */
	List<Slot> slots = null;
	/**
	 * Field copies.
	 */
	List<Copy> copies = null;
	
	/**
	 * Field translations.
	 */
	private List<Translation> translations;

	/**
	 * Method getTranslations.
	 * @return List<Translation>
	 */
	@XmlElementWrapper(name="Translations")
	@XmlElement(name="Translation")
	public List<Translation> getTranslations() {
		return translations;
	}

	/**
	 * Method setTranslations.
	 * @param translations List<Translation>
	 */
	public void setTranslations(List<Translation> translations) {
		this.translations = translations;
	}
	
	/**
	 * Method getSlots.
	 * @return List<Slot>
	 */
	@XmlElementWrapper(name="Slots")
	@XmlElement(name="Slot")
	public List<Slot> getSlots() {
		return slots;
	}
	

	/**
	 * Method setSlots.
	 * @param slots List<Slot>
	 */
	public void setSlots(List<Slot> slots) {
		this.slots = slots;
	}
	
	/**
	 * Method getCopies.
	 * @return List<Copy>
	 */
	@XmlElementWrapper(name="Copies")
	@XmlElement(name="Copy")
	public List<Copy> getCopies() {
		return copies;
	}
	

	/**
	 * Method setCopies.
	 * @param copies List<Copy>
	 */
	public void setCopies(List<Copy> copies) {
		this.copies = copies;
	}
	
}
