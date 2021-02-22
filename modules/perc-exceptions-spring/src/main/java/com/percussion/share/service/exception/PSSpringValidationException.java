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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.share.service.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSErrors.PSObjectError;
import com.percussion.share.validation.PSValidationErrors.PSFieldError;

/**
 * 
 * An adapter to Spring Validation Framework.
 * 
 * @author adamgent
 *
 */
public abstract class PSSpringValidationException  extends PSValidationException implements Errors, IPSValidationException {
    

    private static final long serialVersionUID = 1L;
    private Errors springValidationErrors;


    public PSSpringValidationException(Throwable cause){
        super(cause);
    }


    public PSSpringValidationException(String message)
    {
        super(message);
    }


    public PSSpringValidationException(String message, Throwable cause) {
        super(message, cause);
        reject(cause, message);
    }

    @Override
    public PSSpringValidationException throwIfInvalid() throws PSSpringValidationException {
        if (hasErrors())
            throw this;
        return this;
    }

    @Override
    public PSValidationErrors getValidationErrors() {
        PSValidationErrors ve = new PSValidationErrors();
        convert(ve, getSpringValidationErrors());
        return ve;
    }
    
    protected Errors getSpringValidationErrors() {
        return springValidationErrors;
    }

    
    protected void setSpringValidationErrors(Errors validationErrors) {
        this.springValidationErrors = validationErrors;
    }
    
    public void reject(Throwable exception, String defaultMessage) {
        reject(exception.getClass().getCanonicalName(), defaultMessage);
    }
    
    
    @SuppressWarnings("unchecked")
    protected void convert(PSValidationErrors v, org.springframework.validation.Errors errors) {
        v.setMethodName(errors.getObjectName());
        List<org.springframework.validation.FieldError> fes = errors.getFieldErrors();
        List<org.springframework.validation.ObjectError> oes = errors.getGlobalErrors();
        for(org.springframework.validation.FieldError fe : fes) {
            v.getFieldErrors().add(convert(fe));
        }
        for(org.springframework.validation.ObjectError oe : oes) {
            v.getGlobalErrors().add(convert(oe));
        }
    }
    
    
    protected PSObjectError convert(org.springframework.validation.ObjectError oe) {
        PSObjectError oew = new PSObjectError();
        convert(oe, oew);
        return oew;
    }
    
    protected void convert(org.springframework.validation.ObjectError oe, PSObjectError oew) {
        oew.setCode(oe.getCode());
        oew.setDefaultMessage(oe.getDefaultMessage());
        oew.setArguments(args(oe.getArguments()));
    }
    
    private List<String> args(Object[] args) {
        List<String> rvalue = new ArrayList<>();
        if (args == null) return rvalue;
        for(Object o : args) {
            rvalue.add("" + o);
        }
        return rvalue;
    }
    
    protected PSFieldError convert(org.springframework.validation.FieldError oe) {
        PSFieldError oew = new PSFieldError();
        convert(oe, oew);
        oew.setField(oe.getField());
        return oew;
    }
    
    
// Delegate methods.
    
    @Override
    public String toString() {
        return super.toString() + springValidationErrors.toString();
    }
    
    public void addAllErrors(Errors arg0) {
        springValidationErrors.addAllErrors(arg0);
    }

    @SuppressWarnings("unchecked")
    public List<ObjectError> getAllErrors() {
        return springValidationErrors.getAllErrors();
    }

    public int getErrorCount() {
        return springValidationErrors.getErrorCount();
    }

    public FieldError getFieldError() {
        return springValidationErrors.getFieldError();
    }

    public FieldError getFieldError(String arg0) {
        return springValidationErrors.getFieldError(arg0);
    }

    public int getFieldErrorCount() {
        return springValidationErrors.getFieldErrorCount();
    }

    public int getFieldErrorCount(String arg0) {
        return springValidationErrors.getFieldErrorCount(arg0);
    }

    @SuppressWarnings("unchecked")
    public List<FieldError> getFieldErrors() {
        return springValidationErrors.getFieldErrors();
    }

    @SuppressWarnings("unchecked")
    public List<FieldError> getFieldErrors(String arg0) {
        return springValidationErrors.getFieldErrors(arg0);
    }

    @SuppressWarnings("unchecked")
    public Class getFieldType(String arg0) {
        return springValidationErrors.getFieldType(arg0);
    }

    public Object getFieldValue(String arg0) {
        return springValidationErrors.getFieldValue(arg0);
    }

    public ObjectError getGlobalError() {
        return springValidationErrors.getGlobalError();
    }

    public int getGlobalErrorCount() {
        return springValidationErrors.getGlobalErrorCount();
    }

    @SuppressWarnings("unchecked")
    public List<ObjectError> getGlobalErrors() {
        return springValidationErrors.getGlobalErrors();
    }

    public String getNestedPath() {
        return springValidationErrors.getNestedPath();
    }

    public String getObjectName() {
        return springValidationErrors.getObjectName();
    }

    public boolean hasErrors() {
        return springValidationErrors.hasErrors();
    }

    public boolean hasFieldErrors() {
        return springValidationErrors.hasFieldErrors();
    }

    public boolean hasFieldErrors(String arg0) {
        return springValidationErrors.hasFieldErrors(arg0);
    }

    public boolean hasGlobalErrors() {
        return springValidationErrors.hasGlobalErrors();
    }

    public void popNestedPath() throws IllegalStateException {
        springValidationErrors.popNestedPath();
    }

    public void pushNestedPath(String arg0) {
        springValidationErrors.pushNestedPath(arg0);
    }

    public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
        springValidationErrors.reject(errorCode, errorArgs, defaultMessage);
    }

    public void reject(String errorCode, String defaultMessage) {
        springValidationErrors.reject(errorCode, defaultMessage);
    }

    public void reject(String errorCode) {
        springValidationErrors.reject(errorCode);
    }

    public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
        springValidationErrors.rejectValue(field, errorCode, errorArgs, defaultMessage);
    }

    public void rejectValue(String field, String errorCode, String defaultMessage) {
        springValidationErrors.rejectValue(field, errorCode, defaultMessage);
    }

    public void rejectValue(String field, String errorCode) {
        springValidationErrors.rejectValue(field, errorCode);
    }

    public void setNestedPath(String arg0) {
        springValidationErrors.setNestedPath(arg0);
    }
}
