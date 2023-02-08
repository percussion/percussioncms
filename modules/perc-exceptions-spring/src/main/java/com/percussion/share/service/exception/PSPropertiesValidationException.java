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
