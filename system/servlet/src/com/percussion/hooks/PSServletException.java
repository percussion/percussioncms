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
package com.percussion.hooks;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.error.PSStandaloneException;

import org.w3c.dom.Element;


/**
 * Exception class used to report general exceptions for servlets application,
 * or may be subclassed if necessary.  Handles formatting of messages stored in
 * the PSHookResources.properties resource bundle using error codes and
 * arguments. Localization is also supported.
 */
public class PSServletException extends PSStandaloneException
{
   /**
    * @see {@link com.percussion.error.PSStandaloneException(int, Object)
    */
   public PSServletException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(int, Object[])
    */
   public PSServletException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(int)
    */
   public PSServletException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(PSException)
    */
   public PSServletException(PSException ex)
   {
      super(ex);
   }

   /**
    * @see
    * {@link com.percussion.error.PSStandaloneException(PSStandaloneException)
    */
   public PSServletException(PSStandaloneException ex)
   {
      super(ex);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(Element)
    */
   public PSServletException(Element source) throws PSUnknownNodeTypeException
   {
      super(source);
   }

   /**
    * @see {@link com.percussion.error.getResourceBundleBaseName()
    */
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.hooks.PSServletErrorBundle";
   }

   /**
    * @see {@link com.percussion.error.getXmlNodeName()
    */
   protected String getXmlNodeName()
   {
      return "PSXServletException";
   }

}



