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
package com.percussion.util;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequestValidationException;

import java.io.IOException;
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class provides all functionality to run relationship effects.
 */
public class PSRelationshipUtils
{

   /**
    * Gets the first existing translation for the supplied request.
    *
    * @param request the request to operate on, not <code>null</code>. The
    *    request uses the <code>IPSHtmlParameters.SYS_CONTENTID</code> and
    *    <code>IPSHtmlParameters.SYS_LANG</code> HTML parameters.
    * @return <code>null</code> if no translation exists, the dependent
    *    locator if there is a translation.
    * @throws PSRequestValidationException for any failed request validation.
    * @throws PSValidationException for any failed validation.
    * @throws SQLException for any failed SQL operation.
    * @throws PSNotFoundException for any file not found.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSAuthenticationFailedException if the user failed to
    *    authenticate.
    * @throws IOException for any IO error occurred.
    * @throws PSUnknownNodeTypeException if the supplied document does not
    *    contain a valid relationship set.
    * @throws IllegalArgumentException if the sullpied request is
    *    <code>null</code>.
    */
   public static PSLocator getExistingTranslation(IPSRequestContext request)
      throws PSRequestValidationException, PSAuthorizationException,
         PSInternalRequestCallException, PSValidationException, SQLException,
         PSAuthenticationFailedException, PSNotFoundException, IOException,
         PSUnknownNodeTypeException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      String resource = SYS_PSXRELATIONSHIPSUPPORT + "/" + GET_TRANSLATIONS;

      IPSInternalRequest ir = request.getInternalRequest(resource);
      if (ir != null)
      {
         Document doc = ir.getResultDoc();
         Element elem = doc.getDocumentElement();
         if (elem != null)
         {
            PSRelationshipSet relationships =
               new PSRelationshipSet(elem, null, null);

            if (relationships.isEmpty())
               return null;

            // there can only be one clone
            PSRelationship relationship = (PSRelationship) relationships.get(0);
            return relationship.getDependent();
         }

         return null;
      }
      else
      {
         Object[] args =
         {
            resource,
            "No request handler found."
         };
         throw new PSNotFoundException(
            IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
   }

   /**
    * Does the supplied language already exist for the provided content id?
    *
    * @param request the request to check, not <code>null</code>. The request
    *    uses the <code>IPSHtmlParameters.SYS_CONTENTID</code> and
    *    <code>IPSHtmlParameters.SYS_LANG</code> HTML parameters.
    * @return <code>true</code> if the requested language already exists for
    *    the provided content id, <code>false</code> otherwise.
    * @throws PSRequestValidationException for any failed request validation.
    * @throws PSValidationException for any failed validation.
    * @throws SQLException for any failed SQL operation.
    * @throws PSNotFoundException for any file not found.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSAuthenticationFailedException if the user failed to
    *    authenticate.
    * @throws IOException for any IO error occurred.
    * @throws IllegalArgumentException if the sullpied request is
    *    <code>null</code>.
    */
   public static boolean doesTranslationExist(IPSRequestContext request)
      throws PSRequestValidationException, PSAuthorizationException,
         PSInternalRequestCallException, PSValidationException, SQLException,
         PSAuthenticationFailedException, PSNotFoundException, IOException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      String resource = SYS_PSXRELATIONSHIPSUPPORT + "/" + GET_TRANSLATIONS;

      IPSInternalRequest ir = request.getInternalRequest(resource);
      if (ir != null)
      {
         Document doc = ir.getResultDoc();
         if (doc.getDocumentElement() != null)
         {
            Element elem = doc.getDocumentElement();
            return elem.getFirstChild().getNodeName().equals(
               PSRelationship.XML_NODE_NAME);
         }

         return false;
      }
      else
      {
         Object[] args =
         {
            resource,
            "No request handler found."
         };
         throw new PSNotFoundException(
            IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
   }

   /**
    * Filters out all relationships that have the supplied category from the
    * provided relationship set and returns the filtered set. The passed in
    * relationship set is not changed.
    *
    * @param relationships the relationship set from which to filter out
    *    relationships that have the suppleid category, assumed not
    *    <code>null</code>, may be empty.
    * @param category the category to filter out, assumed not <code>null</code>.
    * @return a relationship set with all relationships filter out that have
    *    the supplied category.
    */
   public static PSRelationshipSet filterCategory(
      PSRelationshipSet relationships, String category)
   {
      PSRelationshipSet filtered = new PSRelationshipSet();

      for (int i=0; i<relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);
         String currentCategory = relationship.getConfig().getCategory();
         if (currentCategory == null || !currentCategory.equals(category))
            filtered.add(relationship);
      }

      return filtered;
   }

   /**
    * The name of the application used to query or update relationships in
    * the repository.
    */
   public static final String SYS_PSXRELATIONSHIPSUPPORT =
      "sys_psxRelationshipSupport";

   /**
    * The name of the translations query resource.
    */
   public static final String GET_TRANSLATIONS = "getTranslations";

   /**
    * The name of the query resource to get the contenttype information for the
    * requested contentid and revision.
    */
   public static final String GET_CONTENTTYPEINFO = "getContenttypeInfo";

   /**
    * The name of the query resource to get the workflow status of the current
    * item.
    */
   public static final String GET_WORKFLOWSTATUS = "getWorkflowStatus";

   /**
    * The name of the query resource to get all transitions leaving the
    * current state.
    */
   public static final String GET_TRANSITIONS = "getTransitions";

   /**
    * The name of the query resource to get the requested transition
    * information.
    */
   public static final String GET_REQUESTEDTRANSITION =
      "getRequestedTransition";

   /**
    * The name of the query resource to get the current workflow state of an
    * item.
    */
   public static final String GET_CURRENTSTATE = "getCurrentState";
}