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
package com.percussion.delivery.utils.properties;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Specifies a logical grouping of properties. 
 * 
 * @author natechadwick
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "propertygroup")
public class PSPropertyGroupDefinition {
	
    @XmlAttribute(required = true)
	private String name;
    @XmlAttribute(name = "display_name")
    private String displayName;
    @XmlAttribute
	private boolean expanded;
	@XmlAttribute(name = "help_text")
	private String helpText;
	
	@XmlElement
	private List<PSPropertyDefinition> properties;
	
	/**
	 * When true, the last known state for this property group is expanded.
	 * @return the expanded
	 */
	public boolean isExpanded() {
		return expanded;
	}
	/**
	 * When true, indicates that this property group should be displayed expaned.  When false
	 * collapsed. 
	 * 
	 * @param expanded the expanded to set
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the helpText
	 */
	public String getHelpText() {
		return helpText;
	}
	/**
	 * @param helpText the helpText to set
	 */
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}
	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return the properties
	 */
	public List<PSPropertyDefinition> getProperties() {
		if(properties==null)
			properties = new ArrayList<>();
		return properties;
	}
	/**
	 * @param properties the properties to set
	 */
	public void setProperties(List<PSPropertyDefinition> properties) {
		this.properties = properties;
	}
	

}
