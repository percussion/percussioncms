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
