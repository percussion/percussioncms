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

package com.percussion.share.validation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A data object that represents validation errors.
 * <p>
 * The object some what mirrors the Spring validation framework.
 * <p>
 * The object is safe serialize with JAXB.
 * 
 * @author adamgent
 *
 */
@XmlRootElement(name="ValidationErrors")
public class PSValidationErrors extends PSErrors {

    private List<PSFieldError> fieldErrors = new ArrayList<>();
    private List<PSObjectError> globalErrors = new ArrayList<>();
    private String methodName;
    
    
    public PSValidationErrors() {
        super();
    }

    public boolean hasErrors() {
        return ( ! ( globalErrors.isEmpty() && fieldErrors.isEmpty() ) );
    }

    public String getMethodName() {
        return methodName;
    }


    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public List<PSFieldError> getFieldErrors() {
        return fieldErrors;
    }


    
    public void setFieldErrors(List<PSFieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }


    

    @Override
    public PSObjectError getGlobalError() {
        if (getGlobalErrors() == null || getGlobalErrors().isEmpty())
            return null;
        return getGlobalErrors().get(0);
    }


    @Override
    public void setGlobalError(PSObjectError globalError) {
        getGlobalErrors().add(0, globalError);
    }


    public List<PSObjectError> getGlobalErrors() {
        return globalErrors;
    }


    
    public void setGlobalErrors(List<PSObjectError> objectErrors) {
        this.globalErrors = objectErrors;
    }
    
    
    public static class PSFieldError extends PSObjectError {
        private String field;
        private Object rejectedValue;
        private boolean bindingFailure;
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
        
        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
        
        public boolean isBindingFailure() {
            return bindingFailure;
        }
        
        public void setBindingFailure(boolean bindingFailure) {
            this.bindingFailure = bindingFailure;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("PSFieldError{");
            sb.append("field='").append(field).append('\'');
            sb.append(", rejectedValue=").append(rejectedValue);
            sb.append(", bindingFailure=").append(bindingFailure);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSValidationErrors{");
        sb.append("fieldErrors=").append(fieldErrors);
        sb.append(", globalErrors=").append(globalErrors);
        sb.append(", methodName='").append(methodName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
