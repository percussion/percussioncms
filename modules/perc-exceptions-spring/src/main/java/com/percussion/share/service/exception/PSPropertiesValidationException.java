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

package com.percussion.share.service.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.validation.MapBindingResult;


/**
 * 
 * Used to validate property objects like {@link Map}
 * and {@link Properties}.
 * 
 * @author adamgent
 *
 */
public class PSPropertiesValidationException extends PSSpringValidationException {


    private static final long serialVersionUID = 1L;
    private Map<String, Object> properties = new HashMap<>();
    
    public PSPropertiesValidationException(Object target, String methodName) {
        super(methodName);
        init(target, methodName);
        setProperties(getProperties());
    }
    
    public PSPropertiesValidationException(Object target, String methodName, String message, Throwable cause) {
        super(message, cause);
        init(target, methodName);
        setProperties(getProperties());
    }
    
    protected void init(Object target, String objectName) {
        init(getProperties(), objectName);
    }
    
    protected void init(Map<String, Object> properties, String objectName) {
        MapBindingResult mbr = new MapBindingResult(properties, objectName);
        setSpringValidationErrors(mbr);    
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }

    
    public void setProperties(Map<String, Object> parameters) {
        this.properties = parameters;
        init(parameters, getObjectName());
    }
    
}
