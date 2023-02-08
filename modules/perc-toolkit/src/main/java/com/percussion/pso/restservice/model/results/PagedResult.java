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
package com.percussion.pso.restservice.model.results;

import com.percussion.pso.restservice.model.ItemRef;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="Results")
public class PagedResult {
	
	List<ItemRef> itemRefs;
	String next;
	Integer nextId;
	
	@XmlAttribute
	public Integer getNextId() {
		return nextId;
	}

	public void setNextId(Integer nextId) {
		this.nextId = nextId;
	}

	@XmlElement
	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	@XmlElement(name = "Item")
	@XmlElementWrapper(name="Items")
	public List<ItemRef> getItemRefs() {
		return itemRefs;
	}

	public void setItemRefs(List<ItemRef> itemRefs) {
		this.itemRefs = itemRefs;
	}
}
