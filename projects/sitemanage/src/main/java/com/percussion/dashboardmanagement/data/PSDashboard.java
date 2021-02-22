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
package com.percussion.dashboardmanagement.data;

import com.percussion.share.data.PSAbstractPersistantObject;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

@XmlRootElement(name = "Dashboard")
public class PSDashboard extends PSAbstractPersistantObject {

    @NotNull
    @NotBlank
    private List<PSGadget> gadgets;
    private PSDashboardConfiguration config;
    
    @NotNull
    @NotBlank
    private String id;
    
    public List<PSGadget> getGadgets() {
        return gadgets;
    }

    public void setGadgets(List<PSGadget> gadgets) {
        this.gadgets = gadgets;
    }

    public PSDashboardConfiguration getDashboardConfiguration() {
    	return this.config;
    }
    
    public void setDashboardConfiguration(PSDashboardConfiguration config) {
    	this.config = config;
    }
    
	@Override
    public String getId() {
		return id;
	}

	@Override
    public void setId(String id) {
		this.id = id;
	}
    
    private static final long serialVersionUID = -6627409151209959037L;


    
}
