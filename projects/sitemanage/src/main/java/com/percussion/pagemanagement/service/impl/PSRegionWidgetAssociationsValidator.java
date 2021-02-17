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
import java.util.List;
import java.util.Set;


import com.percussion.share.service.exception.PSPropertiesValidationException;
import org.springframework.validation.ObjectError;

import com.percussion.pagemanagement.data.PSRegionWidgetAssociations;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;
import java.util.Iterator;

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
        PSRegionWidgetAssociations wa = getWidgetAssocations(bean, e);
        if (wa != null)
            doWidgetAssociations(wa, e);
        
    }
    
    public abstract String getField();
    
    public abstract PSRegionWidgetAssociations getWidgetAssocations(BEAN wa, PSBeanValidationException e);
    
    
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
