/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
