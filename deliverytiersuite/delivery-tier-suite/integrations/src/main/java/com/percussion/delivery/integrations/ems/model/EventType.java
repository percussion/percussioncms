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

package com.percussion.delivery.integrations.ems.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  <Data>
    <Description>Meeting</Description>
    <ID>1</ID>
    <DisplayOnWeb>true</DisplayOnWeb>
  </Data>
 * @author natechadwick
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EventType {
	
	private int id;
	private String description;
	private boolean displayOnWeb;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isDisplayOnWeb() {
		return displayOnWeb;
	}
	public void setDisplayOnWeb(boolean displayOnWeb) {
		this.displayOnWeb = displayOnWeb;
	}
}
