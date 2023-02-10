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

import com.percussion.pagemanagement.data.PSRegionWidgetAssociations;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;
import org.springframework.validation.ObjectError;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Validates Region Widget Assocations.
 * Makes sure there are not duplicate regionIds
 * @author adamgent
 * @param <BEAN> Page or Template
 *
 */
public abstract class PSRegionWidgetAssociationsValidator<BEAN> extends PSAbstractBeanValidator<BEAN>
{

    private IPSWidgetService widgetService;
    
    public PSRegionWidgetAssociationsValidator(IPSWidgetService widgetService)
    {
        super();
        this.widgetService = widgetService;
    }

    @Override
    protected void doValidation(BEAN bean, PSBeanValidationException e)
    {
        PSRegionWidgetAssociations wa = getWidgetAssociations(bean, e);
        if (wa != null)
            doWidgetAssociations(wa, e);
        
    }
    
    public abstract String getField();
    
    public abstract PSRegionWidgetAssociations getWidgetAssociations(BEAN wa, PSBeanValidationException e);
    
    
    protected void doWidgetAssociations(PSRegionWidgetAssociations a, PSBeanValidationException e) {
        Set<String> ids = new HashSet<>();
        for (PSRegionWidgets ws : a.getRegionWidgetAssociations()) {
            if (ids.contains(ws.getRegionId())) {
                e.reject("regionWidgetAssocations.dupIds", "Duplicate ids for region");
            }
            else {
                ids.add(ws.getRegionId());
            }
            
            List<PSWidgetItem> items = ws.getWidgetItems();
            if (items != null) {
                for(PSWidgetItem item : items) {
                    validateWidgetItem(item, e);
                }
            }
        }
    }
    
    protected void validateWidgetItem(PSWidgetItem widgetItem, PSBeanValidationException e) {
       try {
           PSSpringValidationException we = widgetService.validateWidgetItem(widgetItem);
           List<ObjectError> errors = we.getAllErrors();
           StringBuilder messageBuilder = new StringBuilder();
           if (errors != null && !errors.isEmpty()) {
               Iterator<ObjectError> iter = errors.iterator();
               while (iter.hasNext()) {
                   ObjectError error = iter.next();
                   messageBuilder.append(error.getDefaultMessage());
                   if (iter.hasNext()) {
                       messageBuilder.append(",");
                   }
               }
               e.reject("regionWidgetAssocations.widgetItem", messageBuilder.toString());
           }
       } catch (PSPropertiesValidationException psPropertiesValidationException) {
           e.addSuppressed(psPropertiesValidationException);
       }
    }
}
