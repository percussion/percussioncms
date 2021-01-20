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

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSFolderAcl;
import com.percussion.cms.objectstore.PSFolderPermissions;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSUserContextExtractor;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentType;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utility class for cms related methods.
 */
public class PSCms
{
   /**
    * Convenience method that calls {@link #isRelatedItemPublishable(Element, 
    * IPSRequestContext, String) linkurl, request, null}.
    */
   public static boolean isRelatedItemPublishable(Element linkurl,
      IPSRequestContext request)
      throws PSCmsException
   {
      return isRelatedItemPublishable(linkurl, request, null);
   }
   
   /**
    * Tests if the related item is publishable or not. Retrieves the contentid
    * from the <code>current</code> attribute of the first <code>Value</code> 
    * element found in the supplied <code>linkurl</code> element.
    *
    * @param linkurl the related linkurl element, must not be <code>null</code>.
    *    The expected DTD is:
    *    &lt;!ELEMENT linkurl (Value)&gt;
    *    &lt;!ELEMENT Value EMPTY&gt;
    *    &lt;!ATTLIST Value
    *       current CDATA #REQUIRED
    *    &gt;
    * @param request the request to operate on, must not be <code>null</code>.
    * @param publishableTokens a string with all tokens that are publishable, 
    *    defaults to <code>y,i</code> if <code>null</code> or empty. The 
    *    token delimiter is the comma.
    * @return <code>true</code> if the related item is publishable,
    *    <code>false</code> otherwise.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   public static boolean isRelatedItemPublishable(Element linkurl,
      IPSRequestContext request, String publishableTokens)
      throws PSCmsException
   {
      if (linkurl == null)
         throw new IllegalArgumentException("Param linkurl must not be null");

      if (request == null)
         throw new IllegalArgumentException("Param request must not be null");
      
      if (publishableTokens == null || publishableTokens.trim().length() == 0)
         publishableTokens = "y,i";

      boolean result = false;
      NodeList nodes = linkurl.getElementsByTagName("Value");
      if (nodes != null && nodes.getLength() > 0)
      {
         // Only first element will be considered.
         Element url = (Element) nodes.item(0);

         final String equalSign = "=";
         String contentid = null;
         StringTokenizer tokens = new StringTokenizer(
            url.getAttribute("current"), "?&");
         while (tokens.hasMoreTokens() && contentid == null)
         {
            String token = tokens.nextToken();
            if (token.startsWith(IPSHtmlParameters.SYS_CONTENTID))
               contentid = token.substring(
                  token.indexOf(equalSign) + equalSign.length());
         }

         Object[] params =
         {
            publishableTokens,
            contentid
         };
         result = isPublishable(params, request);
      }
      
      return result;
   }
   
   /**
    * Helper method that checks if the requested user has write access to the 
    * folder(s) specified by {@link IPSHtmlParameters#SYS_FOLDERID} in the 
    * request. This parameter value can be a single string value or an 
    * array of string values for one or more folders for which the check needs 
    * to be performed. 
    * @param request requets context object, must not be <code>null</code>. 
    * The method assumes the request context contains valid folderids otherwise 
    * throws {@link IllegalArgumentException} exception.
    * @return <code>true</code> if every folder in the request has write access 
    * for the current user. Returns <code>false</code> even one of the folders 
    * does not have write permission.
    * @throws PSCmsException if it fails to verify folder permissions for any
    * reason.
    */
   @SuppressWarnings("unchecked")
   static public boolean canWriteToFolders(IPSRequestContext request) 
      throws PSCmsException
   {
      if(request == null)
         throw new IllegalArgumentException("request context must not be null");
        
      //A valid sys_folderid parameter must be present
      Object obj = request.getParameterObject(IPSHtmlParameters.SYS_FOLDERID);

      if (obj == null || StringUtils.isBlank(obj.toString()))
      {
      throw new IllegalArgumentException(
         "request context must contain valid " 
         + IPSHtmlParameters.SYS_FOLDERID + " parameter");
      }

      List<Object> tgtFolders = null;
      if (obj instanceof List)
      {
        tgtFolders = (List<Object>)obj;
      }
      else
      {
        tgtFolders = new ArrayList<Object>();
        tgtFolders.add(obj.toString());
      }
      if(tgtFolders.size()<1)
      {
        throw new IllegalArgumentException(
           "request context must contain valid " 
           + IPSHtmlParameters.SYS_FOLDERID + " parameter");
      }
       
      int ids[] = new int[tgtFolders.size()];
      for (int i = 0; i < tgtFolders.size(); i++)
      {
         ids[i] = Integer.parseInt(tgtFolders.get(i).toString());
      }
      
      PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();
      PSFolderAcl[] acls = processor.getFolderAcls(ids);
      for (int i = 0; i < acls.length; i++)
      {
         PSFolderAcl acl = acls[i];
         PSFolderPermissions folderPerms;
         try
         {
            folderPerms = new PSFolderPermissions(acl);
            if(!folderPerms.hasWriteAccess())
            {
                return false;
            }
         }
         catch (PSAuthorizationException e)
         {
            return false;
         }
      }
      return true;
   }
   
   /**
    * See rxconfig/Server/server.properties.
    * The default is false for backwards compatibility and upgrades.
    * For new installations this will generally be true.
    * @return <code>true</code> if folder security overrides workflow default false.
    */
   public static boolean isFolderSecurityOverridesWorkflowSecurity()
   {
      if (PSServer.getServerProps() == null) return false;
      String prop = PSServer.getServerProps().getProperty("folderSecurityOverridesWorkflowSecurity", "false");
      return equalsIgnoreCase(prop, "yes") || equalsIgnoreCase(prop, "true");
   }
   
   /**
    * Checks to see if the current user has folder read access to the specified
    * item.
    *  
    * @param request The current request, may not be <code>null</code>.
    * @param contentid The id of the item to check.
    * 
    * @return <code>true</code> if the user has read access to at least one of
    * the folder's the item is in, or if the item is not in any folders,
    * <code>false</code> otherwise.
    * 
    * @throws SQLException If there is an error locating the item's folders.
    */
   public static boolean canReadInFolders( 
      int contentid) throws SQLException {
      return checkFolderPermission(contentid, PSFolderPermissions.ACCESS_READ);
   }

   /**
    * Checks to see if the current user has folder write access to the specified
    * item.
    *  
    * @param request The current request, may not be <code>null</code>.
    * @param contentid The id of the item to check.
    * 
    * @return <code>true</code> if the user has write access to at least one of
    * the folder's the item is in, or if the item is not in any folders,
    * <code>false</code> otherwise.
    * 
    * @throws SQLException If there is an error locating the item's folders.
    */
   public static boolean canWriteInFolders( 
      int contentid) throws SQLException {
      return checkFolderPermission(contentid, PSFolderPermissions.ACCESS_WRITE);
   }
   
   /**
    * Checks to see if the current user has the given folder permission
    * access to the specified item.
    *  
    * @param request The current request, may not be <code>null</code>.
    * @param contentid The id of the item to check.
    * @param folderPermAccess is of one of {@link PSFolderPermissions}.ACCESS_XXX
    * 
    * @return <code>true</code> if the user has write access to at least one of
    * the folder's the item is in, or if the item is not in any folders,
    * <code>false</code> otherwise.
    * 
    * @throws SQLException If there is an error locating the item's folders.
    */   
   public static boolean checkFolderPermission(
         int contentid, int folderPermAccess) throws SQLException      
   {
      
      // check to see if feature is disabled
      if ("true".equals(PSServer.getServerProps().getProperty(
         "disableFolderItemSecurity", "false").toLowerCase()))
      {
         return true;
      }
      
      try
      {
         String userName = ((PSLiteralSet)PSUserContextExtractor.getUserContextInformation("User/Name",
               PSThreadRequestUtils.getPSRequest(), new PSLiteralSet(String.class))).toString();
         
         if (userName.equals(IPSConstants.INTERNAL_USER_NAME))
            return true;
      }
      catch (PSDataExtractionException e1)
      {
         // assume not internal user
      }

      try
      {
         PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();
         PSLocator loc = new PSLocator(contentid);
         List<List<PSLocator>> pathList = processor.getFolderLocatorPaths(loc);
         
         // item in no folders is readable
         if (pathList.isEmpty())
            return true;
         
         // get list of folder ids
         int[] folderids = new int[pathList.size()];
         for (int i = 0; i < folderids.length; i++)
         {
            List<PSLocator> locs = pathList.get(i);
            if (locs.isEmpty())
               continue; // shouldn't happen
            
            // first locator is parent folder
            PSLocator folderLoc = (PSLocator) locs.get(0);
            folderids[i] = folderLoc.getId();
         }
         
         // get list of acls and check each for read access
         boolean hasAccess = false;
         PSFolderAcl[] acls = processor.getFolderAcls(folderids);

         // no acl means full access, so see if any folders have no acls
         if (acls.length < folderids.length)
            return true;
         
         for (int i = 0; i < acls.length; i++)
         {
            PSFolderAcl acl = acls[i];
            PSFolderPermissions folderPerms;
            try
            {
               folderPerms = new PSFolderPermissions(acl);
               
               if(folderPerms.hasAccessOrHigher(folderPermAccess))
               {
                   hasAccess = true;
                   break;
               }
            }
            catch (PSAuthorizationException e)
            {
               break;
            }
         }
         
         
         return hasAccess;         
      }
      catch (PSCmsException e)
      {
         SQLException sqe = new SQLException(e.getLocalizedMessage());
         sqe.initCause(e);
         throw sqe;
      }
   }

   /**
    * This is a utility method to test whether or not the addressed content is
    * publishable or not. This is determined with an internal request to the
    * <code>sys_casSupport/ContentValid</code> resource.
    * 
    * @param params A list of expected param values:
    * <ol>
    * <li>params[0] a comma separated list of tokens that represent
    *    publishable content, may be <code>null</code> or empty, in which case
    *    the defaults <code>y,i</code> are used.</li>
    * <li>params[1] the content id of the item to test, may be
    *    <code>null</code>, in which case the content id of the supplied
    *    request is used. If no valid content id is supplied, an exception is
    *    thrown.</li>
    * <li>params[2] the revision of the item to test, may be
    *    <code>null</code>, in which case the current revision is used.</li>
    * </ol>
    * @param request the request to operate on, not <code>null</code>.
    * 
    * @return a <code>boolean</code> with a value of <code>true</code> if the
    *    supplied content item is publishable, <code>false</code> otherwise.
    *    
    * @throws PSCmsException for any missing or invalid required
    *    parameter and any other error that can occur.
    */
   public static boolean isPublishable(Object[] params,
      IPSRequestContext request) throws PSCmsException
   {
      String validTokens = "y,i";
      String contentid = null;
      String revision = null;

      if (params != null)
      {
         if (params.length > 0 && params[0] != null &&
            params[0].toString().trim().length() > 0)
            validTokens = params[0].toString();

         if (params.length > 1 && params[1] != null)
            contentid = params[1].toString();

         if (params.length > 2 && params[2] != null)
            revision = params[2].toString();
      }

      // get the content id from the request if not supplied
      if (contentid == null)
         contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);

      if (contentid == null || contentid.trim().length() == 0)
         throw new PSCmsException(0, "Missing required parameter " +
            IPSHtmlParameters.SYS_CONTENTID);

      try
      {
         Map<String,Object> htmlParams = new HashMap<String,Object>();
         htmlParams.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
         if (revision != null && revision.trim().length() > 0)
            htmlParams.put(IPSHtmlParameters.SYS_REVISION, revision);

         String resource = "sys_casSupport/ContentValid";

         IPSInternalRequest ir =
            request.getInternalRequest(resource, htmlParams, false);
         if (ir == null)
            throw new PSCmsException(0,
               "Required resource not found: " + resource);

         Document testDoc = ir.getResultDoc();
         if (testDoc != null)
         {
            NodeList nodes = testDoc.getElementsByTagName(ELEM_CONTENTVALID);
            if (nodes != null && nodes.getLength() > 0)
            {
               // there is always only 1 element in the node list
               Element contentValid = (Element) nodes.item(0);
               String publishable = contentValid.getAttribute(ATTR_PUBLISHABLE);

               StringTokenizer tokens = new StringTokenizer(validTokens, ",");
               while (tokens.hasMoreTokens())
               {
                  String token = tokens.nextToken().trim();
                  if (publishable.equalsIgnoreCase(token))
                     return true;
               }
            }
         }

         return false;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e.getErrorCode(),
            e.getErrorArguments());
      }
   }

   /**
    * Get the new request command handler resource string for the supplied
    * request and object.
    *
    * @param request the request used to make the content type definition
    *    lookup, not <code>null</code>.
    * @param source the source object for which we want the new request
    *    command handler resource, not <code>null</code>.
    * @return the new request command handler resource or <code>null</code> if 
    *    not found.
    * @throws PSRequestValidationException for any request validation errors.
    * @throws PSAuthorizationException for any failed authorization.
    * @throws PSInternalRequestCallException for any errors making an internal
    *    request.
    * @throws PSValidationException for any failed validation.
    * @throws PSAuthenticationFailedException for any failed authentication.
    * @throws PSNotFoundException for any file not found.
    */
   public static String getNewRequestResource(IPSRequestContext request,
      PSLocator source)
      throws PSRequestValidationException, PSAuthorizationException,
         PSInternalRequestCallException, PSValidationException,
         PSAuthenticationFailedException, PSNotFoundException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      if (source == null)
         throw new IllegalArgumentException("source cnnot be null");

      PSContentType contentType = getContentType(request, source);
      return contentType == null ? null : contentType.getNewURL();
   }
   
   /**
    * Get the content type definition for the supplied request and source.
    * 
    * @param request the request used to make the content type definition
    *    lookup, not <code>null</code>.
    * @param source the source object for which we want the content type
    *    definition, not <code>null</code>.
    * @return the content type definition, or <code>null</code> if not found.
    * @throws PSRequestValidationException for any request validation errors.
    * @throws PSAuthorizationException for any failed authorization.
    * @throws PSInternalRequestCallException for any errors making an internal
    *    request.
    * @throws PSValidationException for any failed validation.
    * @throws PSAuthenticationFailedException for any failed authentication.
    * @throws PSNotFoundException for any file not found.
    */
   public static PSContentType getContentType(IPSRequestContext request,
      PSLocator source)
      throws PSRequestValidationException, PSAuthorizationException,
      PSInternalRequestCallException, PSValidationException,
      PSAuthenticationFailedException, PSNotFoundException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (source == null)
         throw new IllegalArgumentException("source cnnot be null");

      String resource = PSRelationshipUtils.SYS_PSXRELATIONSHIPSUPPORT + "/" +
         PSRelationshipUtils.GET_CONTENTTYPEINFO;

      Map<String,Object> params = new HashMap<String,Object>();
      params.put(IPSHtmlParameters.SYS_CONTENTID, 
         Integer.toString(source.getId()));
      params.put(IPSHtmlParameters.SYS_REVISION,
         Integer.toString(source.getRevision()));

      IPSInternalRequest ir = request.getInternalRequest(resource, params, 
         false);
      if (ir != null)
      {
         try
         {
            Document doc = ir.getResultDoc();
            return new PSContentType(
               (Element) doc.getDocumentElement().getFirstChild());
         }
         catch (PSUnknownNodeTypeException e)
         {
            // ignore, we will return null if this happens
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
    * Get publication Url for the item with specified contentid, variantid and
    * context. Delegates to {@link #getPublicationUrl(IPSRequestContext, Map)}
    * method.
    * 
    * @param request request context to make an iternal request to get the
    *           publication Url.
    * @param contentid ContentId of the item as string, must not be
    *           <code>null</code> or empty.
    * @param variantid VariantId for the Url, must not be <code>null</code> or
    *           empty.
    * @param context publishing context, may be <code>null</code> or emoty in
    *           which case, the value is assmued to be "0" which is the preview
    *           context.
    * @return the publication url for the item, may be <code>null</code> or
    *         empty.
    * @throws PSCmsException if it cannot build the publication Url.
    * @see #getPublicationUrl(IPSRequestContext, Map)
    * @deprecated replaced by {@link #getPublicationUrl(IPSRequestContext, Map)}.
    */
   public static String getPublicationUrl(
      IPSRequestContext request,
      String contentid,
      String variantid,
      String context)
      throws PSCmsException
   {
      if (contentid == null || contentid.length() < 1)
         throw new IllegalArgumentException("contentid must not be null or empty");

      if (variantid == null || variantid.length() < 1)
         throw new IllegalArgumentException("variantid must not be null or empty");

      if (context == null || context.length() < 1)
         context = "0";

      Map<String,Object> params = new HashMap<String,Object>();
      params.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
      params.put(IPSHtmlParameters.SYS_VARIANTID, variantid);
      params.put(IPSHtmlParameters.SYS_CONTEXT, context);
      String siteid = request.getParameter(IPSHtmlParameters.SYS_SITEID);
      if(siteid != null)
         params.put(IPSHtmlParameters.SYS_SITEID, siteid);

      return getPublicationUrl(request, params);
   }

   /**
    * Get publication Url for the item with specified set of assembly parameter 
    * map. The map must contain at least two parameters namely, 
    * {@link IPSHtmlParameters#SYS_CONTENTID contentid} and 
    * {@link IPSHtmlParameters#SYS_VARIANTID variantid}. Rest of the assembly 
    * parameters are optional. 
    * context. The Url will contain the current revision of the item.
    * @param request request context to make an iternal request to get the 
    * publication Url.
    * @param assemblyParamMap map of assembly parameters, must not be 
    * <code>null</code> or empty.
    * @return the publication url for the item, may be <code>null</code> or 
    * empty.
    * @throws PSCmsException if it cannot build the publication Url.
    */
   public static String getPublicationUrl(IPSRequestContext request,
            Map<String,Object> assemblyParamMap) throws PSCmsException
   {
       if(assemblyParamMap == null)
       {
           throw new IllegalArgumentException(
                   "assemblyParamMap must not be null");
       }
       Object obj = assemblyParamMap.get(IPSHtmlParameters.SYS_CONTENTID);
       
      if (obj == null || obj.toString().length() < 1)
            throw new IllegalArgumentException(
                    "contentid must not be null or empty");
      String contentid = obj.toString();

      obj = assemblyParamMap.get(IPSHtmlParameters.SYS_VARIANTID);
      
      if (obj == null || obj.toString().length() < 1)
            throw new IllegalArgumentException(
                    "variantid must not be null or empty");
      String variantid = obj.toString();

      Object context = "0";
      Object authtype = "0";

      Map<String,Object> params = new HashMap<String,Object>();
      params.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
      params.put(IPSHtmlParameters.SYS_VARIANTID, variantid);
      Iterator<String> iter = assemblyParamMap.keySet().iterator();
      while (iter.hasNext()) 
      {
         String element = iter.next().toString();
         //Already taken care
         if(element.equals(IPSHtmlParameters.SYS_CONTENTID) ||
            element.equals(IPSHtmlParameters.SYS_VARIANTID))
         {
            continue;
         }
         else if(element.equals(IPSHtmlParameters.SYS_CONTEXT))
         {
             context = assemblyParamMap.get(element);
         }
         else if(element.equals(IPSHtmlParameters.SYS_AUTHTYPE))
         {
             authtype = assemblyParamMap.get(element);
         }
         else
         {
            params.put(element, assemblyParamMap.get(element));
         }
        
      }
      params.put(IPSHtmlParameters.SYS_CONTEXT, context);
      params.put(IPSHtmlParameters.SYS_AUTHTYPE, authtype);
      
      String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if (command != null
         && command.equals(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY))
      {
         params.put(IPSHtmlParameters.SYS_COMMAND, command);
         params.put(IPSHtmlParameters.SYS_ACTIVEITEMID, "");
      }

      IPSInternalRequest iReq =
         request.getInternalRequest(
            "sys_casSupport/PublicationUrl",
            params,
            false);
      Document doc = null;
      try
      {
         doc = iReq.getResultDoc();
      }
      catch (PSInternalRequestCallException e)
      {
         throw new PSCmsException(e);
      }
      if (doc != null)
         return doc.getDocumentElement().getAttribute("current");

      return null;
   }
   
   /**
    * Return the default workflowid for the supplied content type and community.
    * A run time error will be thrown if the intersection returns more than 
    * one workflow.
    *
    * @param request the request needed to make an internal request
    *    to get workflow communities, not <code>null</code>.
    * @param ce the content editor for which to get the default workflow id,
    *    not <code>null</code>.
    * Initialized in ctor, then never changed. If present, this list is used in 
    * conjunction with the type field of the PSWorkflowInfo to determine how to 
    * interpret this list.
    * @param communityId the community id that is used to validate the returned
    *    workflowid. It may be <code>null</code>, the validation will not be
    *    performed in this case.
    * @throws PSInternalRequestCallException if the request to get
    *    the list of workflows for a community fails while processing.
    * @throws PSAuthorizationException if the user is not authorized to
    *    execute the query.
    * @throws PSAuthenticationFailedException if the user cannot be
    *    authenticated.
    * @return an Integer representing the default workflow id for this content 
    *    item and community or the default workflow for this content item 
    *    if no community exists.
    */
   public static int getDefaultWorkflowId(PSRequest request, 
      PSContentEditor ce, String communityId)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (ce == null)
         throw new IllegalArgumentException("ce cannot be null");
      
      PSWorkflowInfo info = ce.getWorkflowInfo();
      String defaultWorkflowId = Integer.toString(ce.getWorkflowId());
      
      String theWorkflowIdStr = request.getParameter(
         IPSHtmlParameters.SYS_WORKFLOWID, defaultWorkflowId);
      int theWorkflowId;
      
      try
      {
         theWorkflowId = Integer.parseInt(theWorkflowIdStr);
      }
      catch(Exception e)
      {
         theWorkflowId = IPSConstants.INVALID_WORKFLOW_ID;
      }

      // Are communities enabled?
      boolean communitiesEnabled = isCommunitiesEnabled();

      /*
       * If a community exists and communities are enabled, then get the 
       * workflow intersection else we just return the default workflow id 
       * for this content type.
       */
      if (null != communityId && communitiesEnabled)
      {
         List<String> workflowIds = findCommunityWorkflows(communityId);

         // Get the workflow intersection list
         Collection<Integer> resultWorkflows = new ArrayList<Integer>();

         Iterator<String> it = workflowIds.iterator();
         boolean haveWorkflowInfo = info != null 
            && info.getWorkflowIds() != null;
         boolean isInclusionary = haveWorkflowInfo ? 
            info.getType().equals(
               PSWorkflowInfo.TYPE_INCLUSIONARY) : true;

         boolean matchesDefault = false;
         while(it.hasNext())
         {
            String id = it.next();
            Integer workflowId = new Integer(id);
            
            if (defaultWorkflowId.equals(id))
               matchesDefault = true;

            if (! haveWorkflowInfo)
            {
               resultWorkflows.add(workflowId);
            }
            else if (defaultWorkflowId.equals(id))
            {
               resultWorkflows.add(workflowId);
            }
            else if(isInclusionary)
            {
               if (info.getWorkflowIds().contains(workflowId))
                 resultWorkflows.add(workflowId);
            }
            else
            {
               if (!info.getWorkflowIds().contains(workflowId))
                 resultWorkflows.add(workflowId);
            }
         }
         
         // if a valid workflow was specified or is the default, use that
         if (resultWorkflows.contains(theWorkflowId))
            return theWorkflowId;

         /*
          * Set the workflowid if there is only one, otherwise throw a
          * runtime error.
          */
         switch (resultWorkflows.size())
         {
            case 1:
               theWorkflowId = resultWorkflows.iterator()
                     .next().intValue();
               break;
               
            default:
               if (matchesDefault)
                  theWorkflowId = Integer.parseInt(defaultWorkflowId);
               else
               {
                  throw new RuntimeException(
                     "Couldn't find unique workflow for community id " + 
                     communityId + " and content type id " + 
                     ce.getContentType() + ".");
               }
         }
      }

      return theWorkflowId;
   }


   /**
    * Determine if the specified workflow is allowed for the content type and community.
    * @param request
    * @param ce
    * @param communityId
    */
   public static boolean isAllowedWorkflow( 
         PSContentEditor ce, String communityId, int workflowId)
   {
      
      if (isCommunitiesEnabled())
      {
         List<String> workflowIds = findCommunityWorkflows(communityId);
         //FB: GC_UNRELATED_TYPES NC 1-17-16 
         if (!workflowIds.contains(Integer.toString(workflowId)))
            return false;
         
         PSWorkflowInfo info = ce.getWorkflowInfo();
         if (info != null && !info.getWorkflowIds().contains(Integer.valueOf(workflowId)))
            return false;
      }
      
      return true;
   }
   
   private static boolean isCommunitiesEnabled()
   {
      boolean communitiesEnabled = PSServer.getServerProps().getProperty(
         "communities_enabled").trim().equalsIgnoreCase("yes");
      return communitiesEnabled;
   }

   /**
    * Find all workflows visible to the specified community
    * 
    * @param communityId The id of the community, assumed not <code>null</code> 
    * or empty and to represent a valid community id.
    * 
    * @return The list of visible workflow ids, never <code>null</code>, may be
    * empty.
    */
   private static List<String> findCommunityWorkflows(String communityId)
   {
      try
      {
         List<String> workflowIds = new ArrayList<String>();

         IPSBackEndRoleMgr beRoleMgr = PSRoleMgrLocator.getBackEndRoleManager();
         PSCommunity comm = beRoleMgr.loadCommunity(
            new PSGuid(PSTypeEnum.COMMUNITY_DEF, Long.valueOf(communityId)));
         
         IPSAclService svc = PSAclServiceLocator.getAclService();
         Collection<IPSGuid> guids = svc.findObjectsVisibleToCommunities(
            Collections.singletonList(comm.getName()), PSTypeEnum.WORKFLOW);
         
         for (IPSGuid guid : guids)
         {
            workflowIds.add(String.valueOf(guid.longValue()));
         }
         
         return workflowIds;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to determine workflows for " +
                "community " + communityId + ": " + e.getLocalizedMessage(), e);
      }
   }

   /**
    * Just like {@link #getDefaultWorkflowId(PSRequest, PSContentEditor, String)}
    * except the community id is supplied from the session of the supplied 
    * request.
    */
   public static int getDefaultWorkflowId(PSRequest request, 
            PSContentEditor ce)
            throws PSInternalRequestCallException, PSAuthorizationException,
               PSAuthenticationFailedException
   {
      // get the community from the request session
      PSUserSession session = request.getUserSession();
      Object obj = session.getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
      String communityId = null;
      if (obj != null)
      {
         communityId = obj.toString();
         if (null == communityId || communityId.trim().length() == 0)
            communityId = null;
      }

      return getDefaultWorkflowId(request, ce, communityId);
   }
   
   /**
    * Constatant for ContentValid XML Element
    */
   public static String ELEM_CONTENTVALID = "ContentValid";
   /**
    * Constatant for publishable XML Attribute
    */
   public static String ATTR_PUBLISHABLE = "publishable";
}
