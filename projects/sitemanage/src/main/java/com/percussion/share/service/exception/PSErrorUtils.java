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
package com.percussion.share.service.exception;

import com.percussion.error.IPSException;
import com.percussion.share.validation.PSErrorCause;
import com.percussion.share.validation.PSErrors;
import com.percussion.share.validation.PSErrors.PSObjectError;
import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.Validate.notNull;

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
        notNull(exception);
        PSErrors errors = new PSErrors();
        PSObjectError oe = new PSObjectError();

        if(exception instanceof IPSException){
            oe.setCode(
                    Integer.toString(
                            ((IPSException) exception).getErrorCode()));
        }else {
            oe.setCode(exception.getClass().getCanonicalName());
        }
        oe.setDefaultMessage(StringUtils.defaultString(exception.getMessage(),"Server error processing request, see log for details"));
        oe.setCause(new PSErrorCause(exception));
        errors.setGlobalError(oe);
        return errors;
    }
    
    public static RuntimeException createExceptionFromErrors(PSErrors errors) {
        notNull(errors);
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
            notNull(errors);
            PSObjectError oe = errors.getGlobalError();
            setMessage(oe.getDefaultMessage());
            //setStackTrace(cause.getStackTrace());
            //initCause(cause.getCause());
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

