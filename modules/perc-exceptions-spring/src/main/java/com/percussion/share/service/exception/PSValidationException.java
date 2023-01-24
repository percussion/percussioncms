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

import com.percussion.share.validation.PSValidationErrors;

/**
 * The base validation exception for <strong>expected</strong> failures.
 * <p>
 * The REST conversion of these exceptions will be HTTP Code <code>400</code>.
 * All other {@link RuntimeException RuntimeExceptions} will be HTTP Code <code>500</code>. 
 * <p>
 * The validation exceptions are loosly based on the Spring Frameworks Validation.
 * 
 * @see PSSpringValidationException
 * @see PSValidationErrors
 * @author adamgent
 *
 */
public abstract class PSValidationException extends PSDataServiceException implements IPSValidationException
{


    private PSValidationErrors validationErrors;
    
    public PSValidationException(PSValidationErrors validationErrors)
    {
        super(validationErrors.toString());
        setValidationErrors(validationErrors);
    }

    public PSValidationException(String message)
    {
        super(message);
    }

    public PSValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PSValidationException(Throwable cause)
    {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public PSValidationErrors getValidationErrors()
    {
        return validationErrors;
    }

    public void setValidationErrors(PSValidationErrors validationErrors)
    {
        this.validationErrors = validationErrors;
    }
    
    /**
     * This exception will be thrown if its invalid.
     * @return never <code>null</code>.
     */
    public PSValidationException throwIfInvalid() throws PSValidationException {
        if (validationErrors != null && validationErrors.hasErrors()) {
            throw this;
        }
        return this;
    }
    
    private static final long serialVersionUID = 1L;

}
