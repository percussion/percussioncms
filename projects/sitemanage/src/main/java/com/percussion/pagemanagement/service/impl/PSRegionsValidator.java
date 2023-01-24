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
        Set<String> ids = new HashSet<>();
        
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
