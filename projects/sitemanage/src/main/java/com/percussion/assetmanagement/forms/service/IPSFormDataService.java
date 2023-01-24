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
package com.percussion.assetmanagement.forms.service;

import com.percussion.assetmanagement.forms.data.PSFormSummary;
import com.percussion.share.service.exception.PSDataServiceException;

import java.util.List;

/**
 * @author peterfrontiero
 *
 */
public interface IPSFormDataService
{
    /**
     * Retrieves the data for the specified form captured by all form processors.
     * 
     * @param name of the form, may not be blank.
     * 
     * @return form summary containing submission data for the given form, it may be <code>null</code>
     * if the form does not exist on the delivery server(s).
     * 
     * @throws PSFormDataServiceException if a communication error occurs with a delivery tier form processor.
     */
    public PSFormSummary getFormData(String name) throws PSFormDataServiceException;

    /**
     * Retrieves all form data captured by all form processors.
     * 
     * @return a list of summary of forms, sorted by name (ascendant order), never <code>null</code>, may be empty.
     * 
     * @throws PSFormDataServiceException if a communication error occurs with a delivery tier form processor.
     */
    public List<PSFormSummary> getAllFormData(String site) throws PSFormDataServiceException;
    
    /**
     * Exports the data for the specified form captured by all form processors.
     * 
     * @param name of the form, may not be blank.
     * 
     * @return submission data merged from all form processors.  May be blank.
     * 
     * @throws PSFormDataServiceException if a communication error occurs with a delivery tier form processor.
     */
    public String exportFormData(String site,String name) throws PSFormDataServiceException;
    
    /**
     * Clears the data for the specified form captured by all form processors.
     * 
     * @param name of the form, may not be blank.
     * 
     * @throws PSFormDataServiceException if a communication error occurs with a delivery tier form processor.
     */
    public void clearFormData(String name,String siteName) throws PSFormDataServiceException;
    
    /**
     * (Runtime) Exception is thrown when an unexpected error occurs in this
     * service.
     */
    public static class PSFormDataServiceException extends PSDataServiceException
    {
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public PSFormDataServiceException()
        {
            super();
        }

        /**
         * Constructs an exception with the specified detail message and the
         * cause.
         * 
         * @param message the specified detail message.
         * @param cause the cause of the exception.
         */
        public PSFormDataServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public PSFormDataServiceException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public PSFormDataServiceException(Throwable cause)
        {
            super(cause);
        }
    }
}
