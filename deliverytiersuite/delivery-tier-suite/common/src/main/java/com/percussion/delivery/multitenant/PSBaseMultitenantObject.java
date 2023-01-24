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
