/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.data.macro;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.*;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContentItemStatus;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.server.*;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSParseUrlQueryString;
import com.percussion.util.PSSqlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * A container for common used macro utility functions.
 */
public class PSMacroUtils
{
   /**
    * Compute and return the item's revision as follows:
    * <ul>
    * <li>If the content valid flag for the item's current state is one of the
    * supplied characters, extract the last public revision of the item. If the
    * last public revision is not available for any reason return -1</li>
    * <li>Otherwise, return the default revision supplied.</li>
    * </ul>
    * 
    * @param request the request used to make the lookup, not <code>null</code>.
    * @param contentid the content of the item to make the lookup for, must not
    *           be <code>null</code> or empty.
    * @param contentValidFlags array of characters indicating the state valid
    *           flags for which the revision needs to be corrected.
    * @param defaultRevision the value to return if the item's current state
    *           valid flag is not one of the characters in the supplied array,
    *           may be <code>null</code> in which case the return value could
    *           be <code>null</code>.
    * @return the corrected or default revision computed as described in the
    *         method description. above.
    * @throws PSInternalRequestCallException for any errors during the lookup.
    * @throws PSNotFoundException if the lookup resource was not found.
    */
   public static String correctRevisionForFlags(IPSRequestContext request,
         String contentid, char[] contentValidFlags, String defaultRevision)
         throws PSInternalRequestCallException, PSNotFoundException
   {
      //If the item is not in a state whose flag is not one of the flags in 
      //supplied array of flags, return the default.
      if(!isItemStateFlag(request, contentid, contentValidFlags))
      {
         return defaultRevision;
      }
      return getLastPublicRevision(contentid);
   }

   /**
    * Correct the revision inside the linkurl and return it:
    * <ul>
    * <li>If {@link IPSHtmlParameters#SYS_CONTENTID}or {@link
    * IPSHtmlParameters#SYS_REVISION} missing in linkUrl an unchanged url will
    * be returned</li>
    * <li>If the content valid flag for the item's current state is one of the
    * supplied characters, the revision in supplied url will be replaced with
    * the last public revision. If the last public revision is not available for
    * any reason return value will be <code>null</code>.</li>
    * </ul>
    * 
    * @param request the request used to make the lookup, not <code>null</code>.
    * @param linkUrl the url of the item in which the revision needs to be fixed, 
    *          must not be <code>null</code> or empty.
    * @param contentValidFlags array of characters indicating the state valid
    *           flags for which the revision needs to be corrected.
    * @return the corrected url or <code>null</code>, see details above. 
    * @throws PSInternalRequestCallException for any errors during the lookup.
    * @throws PSNotFoundException if the lookup resource was not found.
    * @throws PSRequestParsingException
    */
   public static String fixLinkUrlRevisionForFlags(IPSRequestContext request,
         String linkUrl, char[] contentValidFlags)
         throws PSInternalRequestCallException, PSNotFoundException,
         PSRequestParsingException
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      if (linkUrl == null || linkUrl.length() < 1)
      {
         throw new IllegalArgumentException("linkUrl must not be null or empty");
      }
      Map params = PSParseUrlQueryString.parseParameters(linkUrl);
      String contentid = (String) params.get(IPSHtmlParameters.SYS_CONTENTID);
      if (contentid == null || contentid.length() < 1)
      {
         String msg = "Parameter " + IPSHtmlParameters.SYS_CONTENTID
               + " in the related link URL is does not exist or empty. "
               + "Revision not corrected";
         request.printTraceMessage(msg);
         return linkUrl;
      }
      String revision = (String) params.get(IPSHtmlParameters.SYS_REVISION);
      if (revision == null)
      {
         String msg = "Parameter " + IPSHtmlParameters.SYS_REVISION
               + " in the related link URL is does not exist. "
               + "Url not modified";
         request.printTraceMessage(msg);
         return linkUrl;
      }

      String lastPublicRevision = correctRevisionForFlags(request, contentid,
            contentValidFlags, revision);
      if(lastPublicRevision.equals("-1"))
         return null;
      int start = linkUrl.indexOf(IPSHtmlParameters.SYS_REVISION);
      String fixedUrl = linkUrl.substring(0, start);
      fixedUrl += IPSHtmlParameters.SYS_REVISION + "=" + lastPublicRevision;

      int end = linkUrl.indexOf("&", start);
      if (end != -1)
         fixedUrl += linkUrl.substring(end);

      return fixedUrl;
   }
   /**
    * Get the last public revision for the supplied parameters. This will lookup
    * the last public revision from the items history.
    * 
    * @param contentid
    *            the content of the item to make the lookup for, not
    *            <code>null</code> or empty.
    * @return the last public revision for the supplied item or '-1' if the item
    *         was never public yet, never <code>null</code> or empty.
    */
   public static String getLastPublicRevision(String contentid)
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid cannot be null");

      contentid = contentid.trim();
      if (contentid.length() == 0)
         throw new IllegalArgumentException("contentid cannot be empty");

      int id;
      try
      {
         id = Integer.parseInt(contentid);
      }
      catch (NumberFormatException e)
      {
         return "-1";
      }
      
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = cms.loadComponentSummary(id);
      return String.valueOf(summary.getPublicRevision());
   }

   /**
    * Get the last public revision for the supplied parameters. This will lookup
    * the last public revision from the items history.
    * 
    * @param contentIds
    *            a list over one or more content ids (as <code>Integer</code>
    *            objects) for the item to make the lookup for, not 
    *            <code>null</code> or empty.
    * @return a map that maps content ids to its the last public revision. 
    *         Both map key and value are <code>Integer</code> objects. The
    *         map keys are the content ids; the map values are the related
    *         last public revision. The map value may be <code>null</code>
    *         if the item was never public yet, never <code>null</code> or 
    *         empty.
    * @throws PSInternalRequestCallException
    *             for any errors doing the lookup.
    * @throws PSNotFoundException
    *             if the lookup resource was not found.
    */
   public static Map<Integer, Integer> getLastPublicRevisions(
         List<Integer> contentIds)
         throws PSInternalRequestCallException, PSNotFoundException
   {
      if (contentIds == null || contentIds.isEmpty())
         throw new IllegalArgumentException(
               "contentIds cannot be null or empty");

      Map<Integer, Integer> result = new HashMap<>();
      
      if (contentIds.size() < PSSqlHelper.MAX_IN_CLAUSE_4_ORACLE)
         return getLastPublicRevisionsPerGroup(contentIds);
      
      List<Integer> group = new ArrayList<>();
      Iterator<Integer> ids = contentIds.iterator();
      int counter = 0;
      while (ids.hasNext())
      {
         if (counter < PSSqlHelper.MAX_IN_CLAUSE_4_ORACLE)
         {
            group.add(ids.next());
            counter++;
         }
         else // process one group at a time, then collect next group of ids
         {
            result.putAll(getLastPublicRevisionsPerGroup(group));
            counter = 0;
            group.clear();
         }
      }
      // process the remaining ids
      if (counter > 0)
         result.putAll(getLastPublicRevisionsPerGroup(group));
      
      return result;
   }

   /**
    * Utility method, used by
    * {@link #getLastPublicRevisions(List<Integer>)}to process a
    * group of content ids at a time..
    * 
    * @param contentIds
    *           a list over one or more content ids (as <code>Integer</code>
    *           objects) for the item to make the lookup for, assume not
    *           <code>null</code> or empty.
    * @return a map that maps content ids to its the last public revision. Both
    *         map key and value are <code>Integer</code> objects. The map keys
    *         are the content ids; the map values are the related last public
    *         revision. The map value may be <code>null</code> if the item was
    *         never public yet, never <code>null</code> or empty.
    */
   private static Map<Integer, Integer> getLastPublicRevisionsPerGroup(
         List<Integer> contentIds)
   {
      Map<Integer, Integer> result = new HashMap<>();

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> sums = cms.loadComponentSummaries(contentIds);
      for (PSComponentSummary sum : sums)
      {
         if (sum.getPublicRevision() != -1)
            result.put(new Integer(sum.getContentId()), new Integer(sum
                  .getPublicRevision()));
      }

      return result;
   }

   
   /**
    * Convenience method that calls {@link #isItemPublic(PSRequest, String)},
    * {@link #isItemPublic(PSRequest, String)} isItemPublic(new PSRequestContext(request), contentid)}.
    */
   public static boolean isItemPublic(PSRequest request, 
      String contentid) throws PSInternalRequestCallException, 
         PSNotFoundException
   {
      return isItemPublic(new PSRequestContext(request), contentid);
   }
   
   /**
    * Convenience method that calls
    * {@link #isItemStateFlag(IPSRequestContext, String, char[]) 
    * isItemStateFlag(new PSRequestContext(request), contentid, validFlags)} 
    * with validFlags = {'y'}.
    */
   public static boolean isItemPublic(IPSRequestContext request, 
      String contentid) throws PSInternalRequestCallException, 
         PSNotFoundException
   {
      char[] validFlags = {'y'};
      return isItemStateFlag(request, contentid, validFlags);
   }

   /**
    * Test if the current workflow state of the item with supplied contentid has
    * the valid flag matching one of the supplied characters.
    * 
    * @param request the request used to lookup the item details, not
    *           <code>null</code>.
    * @param contentid the content of the item to make the test for, not
    *           <code>null</code> or empty.
    * @param validFags array of valid characters, must not be <code>null</code>
    * @return <code>true</code> if the supplied item's current workflow state
    *         has a flag match one of the characters in the array.
    *         <code>false</code> otherwise.
    * @throws PSInternalRequestCallException for any errors doing the lookup.
    * @throws PSNotFoundException if the lookup resource was not found.
    */
   public static boolean isItemStateFlag(IPSRequestContext request,
         String contentid, char[] validFags)
         throws PSInternalRequestCallException, PSNotFoundException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      if (contentid == null)
         throw new IllegalArgumentException("contentid cannot be null");

      contentid = contentid.trim();
      if (contentid.length() == 0)
         throw new IllegalArgumentException("contentid cannot be empty");

      Map<String, String> params = new HashMap<>();
      params.put(IPSHtmlParameters.SYS_CONTENTID, contentid);

      PSInternalRequest ir = PSServer.getInternalRequest(DETAILS_RESOURCE,
            request, params, false, null);
      if (ir == null)
      {
         Object[] args =
         {DETAILS_RESOURCE, "No request handler found."};
         throw new PSNotFoundException(
               IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }

      Document doc = ir.getResultDoc();
      if (doc != null)
      {
         Element root = doc.getDocumentElement();
         if (root != null)
         {
            String contentValid = root.getAttribute("contentvalid")
                  .toLowerCase();
            if (contentValid.length() < 1)
               return false;
            char valid = contentValid.charAt(0);
            for (int i = 0; i < validFags.length; i++)
            {
               if (valid == Character.toLowerCase(validFags[i]))
                  return true;
            }
         }
      }
      return false;
   }

   /**
    * Extracts the content id from the supplied data. First this tries to
    * extract it from the execution data as database table
    * coolumn(CONTENTSTATUS.CONTENTID). If found, it will be returned. If not
    * found, this will try to get the content id from the request parameters.
    * 
    * @param data the execution data to extract the content id from, not
    * <code>null</code>.
    * @return the content id if found, <code>null</code> otherwise.
    * @throws PSDataExtractionException for errors extracting the content id
    * from the execution data.
    */
   public static String extractContentId(PSExecutionData data) 
      throws PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
      
      Object contentid = null;
      
      PSBackEndTable table =  new PSBackEndTable(
         IPSConstants.CONTENT_STATUS_TABLE);
      PSBackEndColumn column =  new PSBackEndColumn(table, 
         IPSConstants.ITEM_PKEY_CONTENTID);
      PSBackEndColumnExtractor cidExtractor = 
         new PSBackEndColumnExtractor(column);
      try
      {
         contentid = cidExtractor.extract(data);
      }
      catch (PSDataExtractionException e)
      {
         if(e.getErrorCode() == IPSDataErrors.BE_COL_EXTR_INVALID_COL)
         {
            //column does not exist pass through
         }
         else
         {
            //Column exists but failed to extract rethrow the exception
            throw e;
         }
      }
         
      if (contentid == null || contentid.toString().trim().length() == 0)
      {
         PSRequest request = data.getRequest();
         contentid = request.getParameter(
            IPSHtmlParameters.SYS_CONTENTID);
      }
      
      return contentid == null ? null : contentid.toString();
   }
   
   /**
    * Get the checkout user name from the content status for the supplied item.
    * 
    * @param contentid the content id for the item to extract the data for,
    *    not <code>null</code> or empty.
    * @param data the execution data used to make the lookup, not
    *    <code>null</code>.
    * @return the user name who has the supplied item checked out, may
    *    be <code>null</code> or empty.
    * @throws PSDataExtractionException for any errors extracting the data.
    */
   public static String extractCheckoutUser(String contentid, 
      PSExecutionData data) throws PSDataExtractionException
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid cannot be null");

      contentid = contentid.trim();
      if (contentid.length() == 0)
         throw new IllegalArgumentException("contentid cannot be empty");

      if (data == null)
         throw new IllegalArgumentException("data cannot be null");

      return extractContentItemStatus(contentid, data, 
         IPSConstants.ITEM_CONTENTCHECKOUTUSERNAME);
   }
   
   /**
    * Get the current revision from the content status for the supplied item.
    * 
    * @param contentid the content id for the item to extract the data for,
    *    not <code>null</code> or empty.
    * @param data the execution data used to make the lookup, not
    *    <code>null</code>.
    * @return the current revision for the supplied item, may be 
    *    <code>null</code> or empty.
    * @throws PSDataExtractionException for any errors extracting the data.
    */
   public static String extractCurrentRevision(String contentid, 
      PSExecutionData data) throws PSDataExtractionException
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid cannot be null");

      contentid = contentid.trim();
      if (contentid.length() == 0)
         throw new IllegalArgumentException("contentid cannot be empty");

      if (data == null)
         throw new IllegalArgumentException("data cannot be null");

      return extractContentItemStatus(contentid, data, 
         IPSConstants.ITEM_CURRENTREVISION);
   }
   
   /**
    * Get the tip revision from content status for the supplied item.
    * 
    * @param contentid the content id for the item to extract the data for,
    *    not <code>null</code> or empty.
    * @param data the execution data used to make the lookup, not
    *    <code>null</code>.
    * @return the tip revision for the supplied item, may be <code>null</code> 
    *    or empty.
    * @throws PSDataExtractionException for any errors extracting the data.
    */
   public static String extractTipRevision(String contentid, 
      PSExecutionData data) throws PSDataExtractionException
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid cannot be null");

      contentid = contentid.trim();
      if (contentid.length() == 0)
         throw new IllegalArgumentException("contentid cannot be empty");

      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
         
      return extractContentItemStatus(contentid, data, 
         IPSConstants.ITEM_TIPREVISION);
   }
   
   /**
    * Get the data for the supplied column from the contents status table.
    * 
    * @param contentid the content id for the item to extract the data for,
    *    assumed not <code>null</code> or empty.
    * @param data the execution data used to make the lookup, assumed not
    *    <code>null</code>.
    * @param column the name of the column from which to extract the data,
    *    assumed not <code>null</code>.
    * @return the extracted data for the supplied item, may be <code>null</code> 
    *    or empty.
    * @throws PSDataExtractionException for any errors extracting the data.
    */
   private static String extractContentItemStatus(String contentid,
         PSExecutionData data, String column) throws PSDataExtractionException
   {
      PSRequest request = data.getRequest();
      
      Map<String, Object> oldParams = request.getParameters();
      HashMap<String, Object> newParams = new HashMap<>();
      newParams.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
      request.setParameters(newParams);
      try
      {
         PSContentItemStatus source = new PSContentItemStatus(
               IPSConstants.CONTENT_STATUS_TABLE, column);

         PSContentItemStatusExtractor extractor = 
            new PSContentItemStatusExtractor(source);

         Object extractedData = extractor.extract(data);

         return extractedData == null ? null : extractedData.toString();
      }
      finally
      {
         request.setParameters(oldParams);
      }
   }
  
   /**
    * Enforce static usage.
    */
   private PSMacroUtils()
   {
   }
      
   /**
    * The resource used to lookup the content details.
    */
   private static final String DETAILS_RESOURCE = 
      "sys_ceSupport/contentdetails";
      
   /**
    * The resource through which the content item history will be looked up.
    */
   private static final String HISTORY_RESOURCE = "sys_ceSupport/history";

   /**
    * The resource through which the last public revision of content items 
    * will be looked up. The query resource requires a list of content ids
    * separated by comma.
    */
   private static final String GET_LAST_PUBLIC_REVISION = 
      "sys_ceSupport/getLastPublicRevisions";
}
