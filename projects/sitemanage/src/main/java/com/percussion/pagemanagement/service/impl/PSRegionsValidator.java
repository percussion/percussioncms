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
package com.percussion.pagemanagement.service.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;

/**
 * Validates Regions for duplicate ids.
 * Makes sure there are not duplicate regionIds
 * @author adamgent
 * @param <BEAN> bean type to validate
 *
 */
public abstract class PSRegionsValidator<BEAN> extends PSAbstractBeanValidator<BEAN>
{

    @Override
    protected void doValidation(BEAN bean, PSBeanValidationException e)
    {
        Iterator<PSRegion> regions = getRegions(bean, e);
        if (regions != null)
            doRegions(regions, e);
        
    }
    
    public abstract String getField();
    
    public abstract Iterator<PSRegion> getRegions(BEAN wa, PSBeanValidationException e);
    
    
    protected void doRegions(Iterator<PSRegion>  it, PSBeanValidationException e) {
        Set<String> ids = new HashSet<String>();
        
        while(it.hasNext()) {
            PSRegion region = it.next();
            if (ids.contains(region.getRegionId())) {
                e.reject("region.dupIds", "Duplicate ids for region");
            }
            else {
                ids.add(region.getRegionId());
            }
        }
    }

}
