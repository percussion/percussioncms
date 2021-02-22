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

package com.percussion.design.objectstore;

import com.percussion.error.PSException;


/**
 * PSSystemValidationException is thrown when a validation error occurs.
 * This usually occurs when an application is being saved to the object
 * store and an invalid value is detected for a particular setting.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSystemValidationException extends PSException {
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode       the error string to load
    *
    * @param singleArg      the argument to use as the sole argument in
    *                      the error message
    */
   public PSSystemValidationException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }
   
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode       the error string to load
    *
    * @param arrayArgs      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSSystemValidationException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSSystemValidationException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode       the error string to load
    *
    * @param   arrayArgs   the array of arguments to use as the arguments
    *                      in the error message
    *
    * @param   container   the container object holding the component
    *                        which is the source of the error
    *
    * @param   component   the component which is the source of the error
    *                        or <code>null</code> if the container is in error
    */
   public PSSystemValidationException(int msgCode, Object[] arrayArgs,
                                      IPSDocument container, IPSComponent component)
   {
      super(msgCode, arrayArgs);

      m_sourceDocument   = container;
      m_sourceComponent   = component;
   }

   /**
    * Get the container (document) holding the component which is the
    * source of the error.
    *
    * @return               the container object holding the component
    *                        which is the source of the error or
    *                        <code>null</code> if one was not specified
    */
   public IPSDocument getSourceContainer()
   {
      return m_sourceDocument;
   }

   /**
    * Get the component which is the source of the error.
    *
    * @return               the component which is the source of the error
    *                        or <code>null</code> if the container is in error
    */
   public IPSComponent getSourceComponent()
   {
      return m_sourceComponent;
   }


   protected IPSDocument   m_sourceDocument   = null;
   protected IPSComponent   m_sourceComponent   = null;
}

