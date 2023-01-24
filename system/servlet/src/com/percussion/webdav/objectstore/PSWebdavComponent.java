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

package com.percussion.webdav.objectstore;

import com.percussion.error.PSExceptionUtils;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         
         throw new RuntimeException("Unexpected exception. " , e);
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
