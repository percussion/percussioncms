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
package com.percussion.server.clone;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;

/**
 * This abstract server extension supplies cloning functionality which is of
 * interest for all cloning extensions.
 */
public abstract class PSCloneBase extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{
   /*
    * (non-Javadoc)
    * 
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Get the clone source id HTML parameter as <code>int</code>.
    * 
    * @param request the request from which to get the parameter, not
    *           <code>null</code>.
    * @return the clone source id as <code>int</code> or -1 if not found.
    * @throws PSParameterMismatchException if the clone source id parameter
    *            exists but cannot be parsed into an <code>int</code>.
    */
   protected int getCloneSourceId(IPSRequestContext request)
         throws PSParameterMismatchException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      String idString = request.getParameter(CLONESOURCEID);
      if (idString != null)
      {
         try
         {
            return Integer.parseInt(idString);
         }
         catch (NumberFormatException e)
         {
            Object[] args =
            {idString, e.getLocalizedMessage()};
            throw new PSParameterMismatchException(
                  IPSCloneErrors.INVALID_CLONESOURCEID, args);
         }
      }

      return -1;
   }

   /**
    * Utility method to clone the children of an object. Makes an internal
    * request to the queryresource and gets the result document, updates the
    * document's keyelementvalue and then makes an internal request to update
    * resource to create the child object. Each query resource should have a
    * corresponding update resource and same dtd should be used for these two
    * resources.
    * 
    * @param request IPSRequestContext object must not be <code>null</code>.
    * @param keyElementName If either of the keyElementName or keyElementValue
    *           <code>null</code> then unmodified result document of the query
    *           resource will be supplied to update resource.
    * @param keyElementValue If either of the keyElementName or keyElementValue
    *           <code>null</code> then unmodified result document of the query
    *           resource will be supplied to update resource.
    * @param queryResources String array of query resources AppName/ResName. The
    *           length of this array should match with the length of
    *           updateResources array.
    * @param updateResources String array of update resources AppName/ResName.
    *           The length of this array should match with the length of
    *           updateResources array.
    * @param queryParams Map of name and value pair of paramesters that will be
    *           used while making an internal request to query resources.
    * @param updateParams Map of name and value pair of paramesters that will be
    *           used while making an internal request to update resources.
    * @throws PSExtensionProcessingException when there is an error while making
    *            an internal request to query resources or update resources.
    */
   protected void cloneChildObjects(IPSRequestContext request,
         String keyElementName, String keyElementValue,
         String[] queryResources, String[] updateResources, Map queryParams,
         Map updateParams) throws PSExtensionProcessingException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      if (queryResources.length != updateResources.length)
         throw new IllegalArgumentException(
               "The length of query and update resources is different.");

      for (int i = 0; i < queryResources.length; i++)
      {
         Document inputdoc = null;
         IPSInternalRequest qrir = null;
         qrir = request.getInternalRequest(queryResources[i], queryParams,
               false);
         if (qrir == null)
            throw new PSExtensionProcessingException(
                  IPSCloneErrors.REQUIRED_RESOURCE_MISSING, queryResources[i]);
         try
         {
            inputdoc = qrir.getResultDoc();
         }
         catch (PSInternalRequestCallException e)
         {
            Object[] args =
            {updateResources[i], e.getLocalizedMessage()};
            throw new PSExtensionProcessingException(
                  IPSCloneErrors.INTERNAL_REQUEST_ERROR, args);
         }
         if (inputdoc != null)
         {
            if (keyElementName != null && keyElementValue != null)
            {
               NodeList nl = inputdoc.getElementsByTagName(keyElementName);
               if (nl != null && nl.getLength() > 0)
               {
                  for (int j = 0; j < nl.getLength(); j++)
                  {
                     Element elem = (Element) nl.item(j);
                     ((Text) elem.getFirstChild()).setData(keyElementValue);
                  }
               }
            }
            IPSInternalRequest upir = null;
            request.setInputDocument(inputdoc);
            upir = request.getInternalRequest(updateResources[i], updateParams,
                  false);
            if (upir == null)
               throw new PSExtensionProcessingException(
                     IPSCloneErrors.REQUIRED_RESOURCE_MISSING,
                     updateResources[i]);
            try
            {
               upir.performUpdate();
            }
            catch (PSAuthenticationFailedException e)
            {
               Object[] args =
               {e.getLocalizedMessage()};
               throw new PSExtensionProcessingException(
                     IPSCloneErrors.NOT_AUTHENTICACATED, args);
            }
            catch (PSAuthorizationException e)
            {
               Object[] args =
               {e.getLocalizedMessage()};
               throw new PSExtensionProcessingException(
                     IPSCloneErrors.NOT_AUTHORIZED, args);
            }
            catch (PSInternalRequestCallException e)
            {
               Object[] args =
               {updateResources[i], e.getLocalizedMessage()};
               throw new PSExtensionProcessingException(
                     IPSCloneErrors.INTERNAL_REQUEST_ERROR, args);
            }
         }

      }

   }

   /**
    * Utility method to make an internal request to supplied update resource
    * with the supplied parameters.
    * 
    * @param request IPSRequestContext object must not be <code>null</code>.
    * @param updateResourceName Name of the update resource must not be
    *           <code>null</code> or empty
    * @param updateParams Map of name and value pair of paramesters that will be
    *           used while making an internal request to update resources.
    * @throws PSExtensionProcessingException when there is an error while making
    *            an internal request to query resources or update resources.
    */
   protected void updateContent(IPSRequestContext request,
         String updateResourceName, Map updateParams)
         throws PSExtensionProcessingException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      if (updateResourceName == null || updateResourceName.trim().length() < 1)
         throw new IllegalArgumentException(
               "updateResourceName must not be be null or empty");

      IPSInternalRequest upir = null;
      upir = request
            .getInternalRequest(updateResourceName, updateParams, false);
      if (upir == null)
         throw new PSExtensionProcessingException(
               IPSCloneErrors.REQUIRED_RESOURCE_MISSING, updateResourceName);
      try
      {
         upir.performUpdate();
      }
      catch (PSAuthenticationFailedException e)
      {
         Object[] args =
         {e.getLocalizedMessage()};
         throw new PSExtensionProcessingException(
               IPSCloneErrors.NOT_AUTHENTICACATED, args);
      }
      catch (PSAuthorizationException e)
      {
         Object[] args =
         {e.getLocalizedMessage()};
         throw new PSExtensionProcessingException(
               IPSCloneErrors.NOT_AUTHORIZED, args);
      }
      catch (PSInternalRequestCallException e)
      {
         Object[] args =
         {updateResourceName, e.getLocalizedMessage()};
         throw new PSExtensionProcessingException(
               IPSCloneErrors.INTERNAL_REQUEST_ERROR, args);
      }

   }

   /**
    * The HTML parameter name which will ccontain the id of the object being
    * cloned.
    */
   public static String CLONESOURCEID = "clonesourceid";

}