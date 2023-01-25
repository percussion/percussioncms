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

import org.springframework.validation.BeanPropertyBindingResult;

/**
 * 
 * Used to validate Java Bean data objects (aka POJOs with out behavior).
 * 
 * @author adamgent
 *
 */
public class PSBeanValidationException extends PSSpringValidationException {
    

    private static final long serialVersionUID = 8097878230304938879L;

    public PSBeanValidationException(Throwable cause){
        super(cause);
    }

    public PSBeanValidationException(Object target, String methodName) {
        super(methodName);
        init(target, methodName);
    }
    
    public PSBeanValidationException(Object target, String methodName, String message, Throwable cause) {
        super(message, cause);
        init(target, methodName);
        
    }
    
    protected void init(Object target, String objectName) {
        setSpringValidationErrors(new BeanPropertyBindingResult(target,objectName));
    }
}
