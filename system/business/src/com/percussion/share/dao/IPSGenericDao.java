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
package com.percussion.share.dao;

import java.io.Serializable;
import java.util.List;

import com.percussion.share.service.exception.PSDataServiceException;

/**
 * Generic DAO (Data Access Object) with common methods to CRUD POJOs.
 * 
 * <p>
 * Extend this interface if you want typesafe (no casting necessary) DAO's for
 * your domain objects.
 * 
 * @author <a href="mailto:bwnoll@gmail.com">Bryan Noll</a>
 * @param <T> a type variable
 * @param <PK> the primary key for that type
 */
public interface IPSGenericDao<T, PK extends Serializable>
{

    /**
     * Generic method used to get all objects of a particular type. This is the
     * same as lookup up all rows in a table.
     * 
     * @return List of populated objects
     * @throws PSDataServiceException
     */
    List<T> findAll() throws PSDataServiceException;

    /**
     * Generic method to get an object based on class and identifier. An
     * ObjectRetrievalFailureException Runtime Exception is thrown if nothing is
     * found.
     * 
     * @param id the identifier (primary key) of the object to get
     * @return a populated object. It may be <code>null</code> if the object does not exist.
     * @throws LoadException 
     * @see org.springframework.orm.ObjectRetrievalFailureException
     */
    T find(PK id) throws PSDataServiceException;

    /**
     * Generic method to save an object - handles both update and insert.
     * 
     * @param object the object to save
     * @return the persisted object
     * @throws SaveException 
     */
    T save(T object) throws PSDataServiceException;

    /**
     * Generic method to delete an object based on class and id
     * 
     * @param id the identifier (primary key) of the object to remove
     * @throws DeleteException 
     */
    void delete(PK id) throws PSDataServiceException;

    /**
     * DataServiceSaveException is thrown when a site cannot be saved
     * successfully.
     */
    public static class SaveException extends PSDataServiceException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public SaveException()
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
        public SaveException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public SaveException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public SaveException(Throwable cause)
        {
            super(cause);
        }
    }

    /**
     * DataServiceDeleteException is thrown when a site cannot be deleted
     * successfully.
     */
    public static class DeleteException extends PSDataServiceException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public DeleteException()
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
        public DeleteException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public DeleteException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public DeleteException(Throwable cause)
        {
            super(cause);
        }
    }

    /**
     * DataServiceLoadException is thrown when a site cannot be loaded
     * successfully.
     */
    public static class LoadException extends PSDataServiceException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public LoadException()
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
        public LoadException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public LoadException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public LoadException(Throwable cause)
        {
            super(cause);
        }
    }

}
