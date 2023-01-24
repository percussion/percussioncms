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

import com.percussion.error.IPSException;
import com.percussion.share.validation.PSErrorCause;
import com.percussion.share.validation.PSErrors;
import com.percussion.share.validation.PSErrors.PSObjectError;
import org.apache.commons.lang3.Validate;

/**
 * 
 * Utilities for create {@link PSErrors} objects.
 * 
 * @author adamgent
 *
 */
public class PSErrorUtils
{
    
    public static PSErrors createErrorsFromException(Throwable exception) {
        Validate.notNull(exception);
        PSErrors errors = new PSErrors();
        PSObjectError oe = new PSObjectError();

        if(exception instanceof IPSException){
            oe.setCode(
                    Integer.toString(
                            ((IPSException) exception).getErrorCode()));
        }else {
            oe.setCode(exception.getClass().getCanonicalName());
        }
        String cause = exception.getMessage();
        if(exception.getCause() != null ){
            if( exception.getCause().getLocalizedMessage() != null) {
                cause = exception.getCause().getLocalizedMessage();
            }else if (exception.getCause().getMessage() != null){
                cause = exception.getCause().getMessage();
            }
        }
        if(cause == null || cause.isEmpty()){
            cause = "Server error processing request, see log for details.";
        }
        oe.setDefaultMessage(cause);
        oe.setCause(new PSErrorCause(exception));
        errors.setGlobalError(oe);
        return errors;
    }
    
    public static RuntimeException createExceptionFromErrors(PSErrors errors) {
        Validate.notNull(errors);
        return new PSProxyException(errors);
    }
    
    
    public static class PSProxyException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;
        private String message;
        protected PSErrors errors;
        
        
        
        public PSProxyException(PSErrors errors)
        {
            super();
            this.errors = errors;
        }

        protected void convert(PSErrors errors) {
            this.errors = errors;
            Validate.notNull(errors);
            PSObjectError oe = errors.getGlobalError();
            setMessage(oe.getDefaultMessage());
        }

        @Override
        public String getMessage()
        {
            return message;
        }
        
        protected void setMessage(String message)
        {
            this.message = message;
        }
    }

}

