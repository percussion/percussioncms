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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
