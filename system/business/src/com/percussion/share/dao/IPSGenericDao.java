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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
