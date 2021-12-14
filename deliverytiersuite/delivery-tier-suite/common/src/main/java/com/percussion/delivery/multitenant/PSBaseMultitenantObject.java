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

package com.percussion.delivery.multitenant;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class must be extended by any pojo's that are intending to be multi tenant capable.
 * @author erikserating
 *
 */
public abstract class PSBaseMultitenantObject {

	
    protected String tenantId;
    
    /**
     * @return may be <code>null</code> if the pojo is being used in 
     * non multi tenant mode.
     */
    @XmlElement(name="tenantid")
	public String getTenantId() 
	{
		return tenantId;
	}

	/**
	 * @param tenantId may be <code>null</code> if the pojo is 
	 * being used in non multi tenant mode.
	 */
	public void setTenantId(String tenantId)
	{
		this.tenantId = tenantId;
	}

}
