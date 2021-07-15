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

package com.percussion.webdav.objectstore;

import com.percussion.webdav.error.PSWebdavException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract public class PSWebdavComponent implements IPSWebdavComponent
{

   private static final Logger log = LogManager.getLogger(PSWebdavComponent.class);
   // implement IPSCmsComponent method
   public Object clone()
   {
      try
      {
         Constructor compCtor = this.getClass().getConstructor( new Class[]
            { Element.class });
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element element = toXml(doc);
         Object comp = compCtor.newInstance(
            new Object[] {element} );

         return comp;
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         
         throw new RuntimeException("Unexpected exception: " + e.toString());
      }
   }

   /**
    * Handles validation exception or the configuration def allowing us
    * to capture the validations instead of throwing them, for use
    * in the WebDav configuration validator.
    * @param e the exception that was thrown, Cannot be <code>null</code>
    * @throws PSWebdavException if the exception is not being captured.
    */
   protected void handleValidationExceptions(PSWebdavException e)
      throws PSWebdavException
   {
      if(e == null)
         throw new IllegalArgumentException(
            "Exception to be handled cannot be null.");
      if(m_captureValidationExceptions)
      {
         m_validationExceptions.add(e);
      }
      else
      {
         throw e;
      }
   }

   /**
    * Sets validation exception capture
    * @param captureOn capture flag, if <code>true</code> then
    * validation exceptions will be captured instead of thrown.
    */
   protected void setValidationExceptionCapture(boolean captureOn)
   {
      m_captureValidationExceptions = captureOn;
   }

   /**
    * Sets the exception capture list to be used to capture the
    * validation exceptions.
    * @param captureList the capture list, cannot be <code>null</code>.
    */
   protected void setValidationExceptionsList(List captureList)
   {
      if(captureList == null)
         throw new IllegalArgumentException("Capture list cannot be null.");
      m_validationExceptions = captureList;
   }

   /**
    * Return a list of captured <code>WebdavExceptions</code> thrown during
    * validation.
    * @return list of exceptions, never <code>null</code>, may be empty.
    */
   public List getValidationExceptionsList()
   {
      return m_validationExceptions;
   }

   /**
    * Capture flag, if <code>true</code> then
    * validation exceptions will be captured instead of thrown.
    */
   protected boolean m_captureValidationExceptions;

   /**
    * Validation exception capture list. Never <code>null</code>,
    * may be empty.
    */
   protected List m_validationExceptions = new ArrayList();


}
