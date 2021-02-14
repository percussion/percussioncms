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
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.data.PSResourceInstance;

/**
 * 
 * This object contains state that is passed to script evaluators
 * for resolving links, locations, mime-type, and (eventually) output.
 * <strong>Since the script can mutate this object, 
 * this object may also contain the output data of the script.</strong>
 * <p>
 * Scripts can access this object through the binding/variable
 * <code>$perc</code>. One way a link and location generation script
 * can pass its data on is by getting the {@link #getResourceInstance() resource instance} 
 * and then setting the 
 * {@link PSResourceInstance#setLinkAndLocations(java.util.List) links and locations}.
 * <p>
 * The scripts can be found in the resource definition files.
 * 
 * @author adamgent
 *
 */
public class PSResourceScriptEvaluatorContext {
    
    private PSResourceInstance resourceInstance;


    /**
     * The resource for this context.
     * @return never <code>null</code>.
     */
    public PSResourceInstance getResourceInstance()
    {
        return resourceInstance;
    }

    public void setResourceInstance(PSResourceInstance resourceInstance)
    {
        this.resourceInstance = resourceInstance;
    }
}

