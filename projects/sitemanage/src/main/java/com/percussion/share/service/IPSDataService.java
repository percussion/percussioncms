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
package com.percussion.share.service;

import java.io.Serializable;

import com.percussion.dashboardmanagement.service.IPSGadgetService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.IPSNotFoundException;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;

/**
 * A generic service wrapper around a DAO.
 * 
 * @param <FULL> Full loaded object
 * @param <SUMMARY> The summary version of the object
 * @param <PK> the primary key for that type
 */
public interface IPSDataService<FULL, SUMMARY, PK extends Serializable>
        extends
            IPSCatalogService<SUMMARY, PK>,
            IPSReadOnlyDataService<FULL, PK>
{

    /**
     * Generic method to save an object - handles both update and insert.
     * 
     * @param object the object to save
     * @return the persisted object
     * @throws PSBeanValidationException If the bean is not valid.
     */
    FULL save(FULL object) throws PSDataServiceException, IPSGadgetService.PSGadgetServiceException;

    /**
     * Generic method to delete an object based on class and id
     * 
     * @param id the identifier (primary key) of the object to remove
     * @throws DataServiceDeleteException
     */
    void delete(PK id) throws PSDataServiceException, IPSGadgetService.PSGadgetNotFoundException, IPSGadgetService.PSGadgetServiceException, PSNotFoundException;

    /**
     * Validate.
     * 
     * @param object crud object
     * @return validation exception.
     */
    PSValidationErrors validate(FULL object) throws PSValidationException, DataServiceSaveException;

    /**
     * DataServiceSaveException is thrown when a site cannot be saved
     * successfully.
     */
    public static class DataServiceSaveException extends PSDataServiceException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public DataServiceSaveException()
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
        public DataServiceSaveException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public DataServiceSaveException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public DataServiceSaveException(Throwable cause)
        {
            super(cause);
        }
    }

    /**
     * DataServiceDeleteException is thrown when a site cannot be deleted
     * successfully.
     */
    public static class DataServiceDeleteException extends PSDataServiceException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public DataServiceDeleteException()
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
        public DataServiceDeleteException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public DataServiceDeleteException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public DataServiceDeleteException(Throwable cause)
        {
            super(cause);
        }
    }

    /**
     * DataServiceLoadException is thrown when a site cannot be loaded
     * successfully.
     */
    public static class DataServiceLoadException extends PSDataServiceException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public DataServiceLoadException()
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
        public DataServiceLoadException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public DataServiceLoadException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public DataServiceLoadException(Throwable cause)
        {
            super(cause);
        }
    }

    /**
     * DataServiceLoadException is thrown when a site cannot be loaded
     * successfully.
     */
    public static class DataServiceNotFoundException extends PSDataServiceException implements IPSNotFoundException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public DataServiceNotFoundException()
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
        public DataServiceNotFoundException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public DataServiceNotFoundException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public DataServiceNotFoundException(Throwable cause)
        {
            super(cause);
        }
    }
    
    /**
     * Thrown when a theme is not found by the service.
     * 
     * @author Santiago M. Murchio
     * 
     */
    public static class PSThemeNotFoundException extends DataServiceNotFoundException
    {
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public PSThemeNotFoundException()
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
        public PSThemeNotFoundException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public PSThemeNotFoundException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public PSThemeNotFoundException(Throwable cause)
        {
            super(cause);
        }
    }

    
}
