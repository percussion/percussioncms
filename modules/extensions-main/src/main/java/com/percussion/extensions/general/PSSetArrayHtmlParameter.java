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
package com.percussion.extensions.general;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.xml.PSXmlTreeWalker;

/**
 * Sets an HTML Parameter to the request with an
 * {@link java.util.List array list} of values. Takes the following parameters.
 * <ol>
 * <li>paramName Name of the HTML parameter, must not be <code>null</code> or
 * empty.</li>
 * <li>resourceName Makes an Internal request to this resource for the values.
 * Must not be <code>null</code> or empty. Throws an exception if resource
 * does not exist.</li>
 * <li>elementName - Loops through all the elements with this name and prepares
 * the list with the text values of this element.If the list is empty HTML
 * parameter will not be added.</li>
 * <li>maxNumber - The list size will be limited to this number or to the size
 * of elements, which ever is smaller.If provided, the value must be an Integer.
 * This will be overridden by the HTML parameter in the request (if exists). If
 * no value is provided for this and no HTML parameter is specified then all
 * element values will be added. One can control first or last n number of items
 * by building the rhythmyx request with a definite sort order</li>
 * </ol>
 */
public class PSSetArrayHtmlParameter implements IPSRequestPreProcessor
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSRequestPreProcessor#preProcessRequest(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      m_log.debug("Entering sys_SetArrayHtmlParameter Exit.");
      // validate and number of params
      if ((params == null) || (params.length < EXPECTED_NUMBER_OF_PARAMS))
      {
         m_log.debug("Supplied parameters do not match with the expected "
               + "number of parameters " + EXPECTED_NUMBER_OF_PARAMS);
         // must at least provide 1 ("base parameter name") parameter
         throw new PSParameterMismatchException(EXPECTED_NUMBER_OF_PARAMS,
               params == null ? 0 : params.length);
      }
      else if (request == null)
      {
         m_log.debug("Request context is null");
         throw new PSRequestValidationException(
               IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION,
               "Request context may not be null");
      }

      String paramName = "";
      if (params[0] != null)
         paramName = params[0].toString().trim();
      if (paramName.length() < 1)
      {
         m_log.debug("paramName is empty");
         throw new PSParameterMismatchException("paramName must not be empty");
      }
      m_log.debug("paramName parameter is " + paramName);

      String resourceName = "";
      if (params[1] != null)
         resourceName = params[1].toString().trim();
      if (resourceName.length() < 1)
      {
         m_log.debug("resourceName parameter is empty");
         throw new PSParameterMismatchException(
               "resourceName must not be empty");
      }
      m_log.debug("resourceName is " + resourceName);

      String elemName = "";
      if (params[2] != null)
         elemName = params[2].toString().trim();
      if (elemName.length() < 1)
      {
         m_log.debug("elementName parameter is empty");
         throw new PSParameterMismatchException("elementName must not be empty");
      }
      m_log.debug("elementName is " + elemName);
      //Get the maxNumber parameter from the request.
      String maxNumber = request.getParameter("maxNumber", "").trim();
      //If does not exist or empty then get it from exit parameter
      if (maxNumber.length() < 1 && params[3] != null)
         maxNumber = params[3].toString().trim();
      int maxitems = 0;
      if (maxNumber.length() > 0)
      {
         try
         {
            maxitems = Integer.parseInt(maxNumber);
         }
         catch (NumberFormatException e2)
         {
            m_log.debug("maxNumber parameter '" + maxNumber
                  + "' is not an integer");
            throw new PSParameterMismatchException(
                  "If provided maxNumber parameter must be an integer");
         }
      }
      m_log.debug("maxNumber is " + maxNumber);

      List valueList = new ArrayList();

      IPSInternalRequest resReq = null;
      try
      {
         resReq = request.getInternalRequest(resourceName);
         if (resReq == null)
         {
            String msg = ms_className
                  + ": Unable to locate handler for request: " + resourceName;
            m_log.debug(msg);
            request.printTraceMessage(msg);
            throw new PSExtensionProcessingException(0, msg);
         }

         Document doc = resReq.getResultDoc();
         NodeList nl = doc.getElementsByTagName(elemName);
         if (nl == null || nl.getLength() < 1)
         {
            m_log.debug("Element list is empty."
                  + "Leaving without adding the paramter:" + paramName);
            return;
         }
         int loopEnd = maxitems;
         int elemsReturned = nl.getLength();
         //If maxitmes is less than or zero or number of elements is less than
         // maxitems then add all element values
         if (maxNumber.length() < 1)
         {
            loopEnd = elemsReturned;
            m_log.debug("maxNumber parameter is not provided."
                  + " Adding all the elements");
         }
         else if (elemsReturned <= maxitems)
         {
            loopEnd = elemsReturned;
            m_log
                  .debug("The number of elements or less than or equal to maxNumber parameter."
                        + " Adding all elements.");
         }
         for (int i = 0; i < loopEnd; i++)
         {
            Element elem = (Element) nl.item(i);
            String elemValue = PSXmlTreeWalker.getElementData(elem).trim();
            if (elemValue.length() < 1)
               continue;
            valueList.add(elemValue);
         }
         if (!valueList.isEmpty())
         {
            request.setParameter(paramName, valueList);
         }
      }
      catch (PSInternalRequestCallException e1)
      {
         throw new PSExtensionProcessingException(0, e1.getMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {

   }

   public static void main(String[] args)
   {
   }

   /**
    * Logger for this exit.
    */
   private Logger m_log = LogManager.getLogger(PSSetArrayHtmlParameter.class);

   /**
    * Expected number of parameters of this exit
    */
   private static final int EXPECTED_NUMBER_OF_PARAMS = 4;

   /**
    * The exit name used for error handling
    */
   private static final String ms_className = "PSSetArrayHtmlParameter";

}
