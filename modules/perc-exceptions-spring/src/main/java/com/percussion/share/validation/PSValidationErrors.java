/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }    
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
}
