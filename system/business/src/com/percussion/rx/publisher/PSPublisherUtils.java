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
package com.percussion.rx.publisher;

import static org.apache.commons.lang.Validate.notNull;
import static org.apache.commons.lang.Validate.notEmpty;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSRuntimeException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseHttpUtils;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for publishing activities.
 */
public class PSPublisherUtils
{
   /**
    * The URL path can be used to fetch the content list in XML format.
    */
   public final static String SERVLET_URL_PATH = "/Rhythmyx/contentlist";
   

   /**
    * The logger for this class.
    */
   private static Log ms_log = LogFactory.getLog(PSPublisherUtils.class);
   
   
   /**
    * Get the URL for a specified Content List, which can be used to get 
    * the XML representation of the Content List.
    * 
    * @param siteId the ID of the site to be published, never <code>null</code>.
    * @param ecList the edition content list, never <code>null</code>.
    * @param cList the content list of the URL, never <code>null</code>.
    * 
    * @return the created URL, never <code>null</code> or empty. It contains
    *    URL base path and parameters, but does not contain host, port and 
    *    protocol. 
    *    
    * @throws PSPublisherException if failed to load Content List. 
    */
   public static String getCListDocumentURL(IPSGuid siteId,
         IPSEditionContentList ecList, IPSContentList cList) 
   {
      if (siteId == null)
         throw new IllegalArgumentException("siteId may not be null.");
      if (ecList == null)
         throw new IllegalArgumentException("ecList may not be null.");
      if (cList == null)
         throw new IllegalArgumentException("cList may not be null.");
      
      int context = ecList.getDeliveryContextId().getUUID();
      // If this is a legacy list, we need the full list, not paged
      String url = cList.getUrl();

      Map<String, Object> additionalParams = new HashMap<>();
      additionalParams.put(IPSHtmlParameters.SYS_CONTEXT, context);
      
      // ignore "sys_assembly_context" parameter in the URL of the Content List
      // use the one defined in the Edition/ContentList association if exists.
      if (cList.getUrl().contains(IPSHtmlParameters.SYS_ASSEMBLY_CONTEXT))
      {
         url = PSBaseHttpUtils.removeQueryParam(cList.getUrl(),
               IPSHtmlParameters.SYS_ASSEMBLY_CONTEXT);
         ms_log
               .warn("URL parameter, sys_assembly_context, is ignored in Content List: id="
                     + cList.getGUID() + ", name=" + cList.getName());
      }
      
      if (ecList.getAssemblyContextId() != null)
      {
         additionalParams.put(IPSHtmlParameters.SYS_ASSEMBLY_CONTEXT, ecList
               .getAssemblyContextId().longValue());
      }
      if (cList.getFilter() == null && ecList.getAuthtype() != null)
      {
         // New content lists use item filters, so we only set this if it is not
         // a new content list
         additionalParams.put(IPSHtmlParameters.SYS_AUTHTYPE, ecList
               .getAuthtype().toString());
      }
      additionalParams.put(IPSHtmlParameters.SYS_SITEID, 
            siteId.longValue());
      additionalParams.put(IPSHtmlParameters.SYS_EDITIONID, 
            ecList.getEditionId().longValue());
      url = PSBaseHttpUtils.addQueryParams(url, additionalParams, false);
      
      return url;
   }

   /**
    * Gets the Content List from the given Edition and Content List association.
    * 
    * @param ecList the edition and content list association, never null.
    * 
    * @return the loaded Content List, may be <code>null</code> if cannot  
    *    find the Content List.
    */
   public static IPSContentList getContentList(IPSEditionContentList ecList)
   {
      if (ecList == null)
         throw new IllegalArgumentException("ecList may not be null.");

      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      return psvc.loadContentList(ecList.getContentListId());
   }
   
   /**
    * Resolves any relative paths to a valid path based on the root directory
    * of the containing web application, which is "../rxapp.ear" not 
    * "../rxapp.ear/rxapp.war".
    *   
    * @param path The path to resolve, may be <code>null</code> or empty.
    * 
    * @return The resolved path, or the supplied path if it was 
    * <code>null</code> or empty.
    */
   public static String resolveFilePath(String path)
   {
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path may not be null or empty.");
      
      String fileSeparator = System.getProperty("file.separator");
      if (path != null && path.trim().length() > 0)
      {
         //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
         if (fileSeparator.equals("/"))
            path = path.replace('\\', '/');
         else
            path = path.replace('/', '\\');
         
         if (!(path.startsWith("\\") || path.startsWith("/") || 
            path.contains(":")))
         {
            // get the "../rxapp.ear/rxapp.war" directory 
            File rootDir = PSServletUtils.getServletDirectory();
            // make it relative to the "../rxapp.ear" directory
            if(rootDir.getName().equals("rxapp.war"))
                path = ".." + fileSeparator + path;
            
            File tmpFile = new File(rootDir, path);
            path = tmpFile.getPath();
         }
      }
      
      return path;
   }
   
   /**
    * Validates the given edition, which must meet the following requirement:
    * <ul>
    *    <li>One or more content list must contain the supplied generator</li>
    *    <li>The last on-demand content list must be marked as the last</li>
    *    <li>If there are more than one on-demand content list, the non-last one cannot be marked as last one.</li>
    * </ul>
    * @param editionid the ID of the edition in question.
    * @param generator the generator used by the on-demand content list, not blank.
    */
   public static void validateEditionForOnDemandContentLists(int editionid, String generator)
   {
      notEmpty(generator);
      
      List<PSPair<String, Boolean>> isLastFlags = getIsLastFlags4OnDemandLists(
            editionid, generator);
      
      int len = isLastFlags.size();
      for (int i=0; i<len; i++)
      {
         boolean isLastFlag = isLastFlags.get(i).getSecond();
         
         // last generator must be true
         if ((i == len-1) && isLastFlag)
            return;
         
         if (isLastFlag)
         {
            String msg = "The parameter \""
                  + IS_LAST_DEMAND_GEN
                  + "\" of the generator '"
                  + generator
                  + "' in content list \""
                  + isLastFlags.get(i).getFirst()
                  + "\" cannot be marked to true as it is not the last on demand content list in the Edition (" + editionid + ").";
            throw new PSRuntimeException(msg);
         }
      }
      
      String msg = "The parameter of \""
         + IS_LAST_DEMAND_GEN
         + "\" of the generator '"
         + generator
         + "' in content list \""
         + isLastFlags.get(len-1).getFirst()
         + "\" must be marked to true as it is the last on demand content list in the Edition (" + editionid + ").";
      throw new PSRuntimeException(msg);
   }

   /**
    * Gets a list of content-list name and its "isLast" flag pairs for the given edition, 
    * where the content-list uses the given generator
    * @param editionid the edition ID.
    * @param generator the generator, assumed not empty.
    * @return the list, never empty.
    */
   private static List<PSPair<String, Boolean>> getIsLastFlags4OnDemandLists(
         int editionid, String generator)
   {
      IPSGuid editionGuid = new PSGuid(PSTypeEnum.EDITION, editionid);
      TreeSet<IPSEditionContentList> ecLists = getEditionContentList(editionGuid);
      List<PSPair<String, Boolean>> isLastFlags = new ArrayList<>();
      for (IPSEditionContentList ecList : ecLists)
      {
         IPSContentList clist = getContentList(ecList);
         if (generator.equals(clist.getGenerator()))
         {
            Map<String, String> params = clist.getGeneratorParams();
            boolean isLast = isLastOnDemandGenerator(params);
            PSPair<String, Boolean> pair = new PSPair<>(clist.getName(), isLast);
            isLastFlags.add(pair);
         }
      }
      
      if (isLastFlags.size() == 0)
      {
         String errorMsg = "Your system is not properly configured to support demand publishing with Edition ("
               + editionid
               + ") because it did not contain the generator \""
               + generator + "\".";
         throw new PSRuntimeException(errorMsg);
      }
      
      return isLastFlags;
   }
   
   /**
    * Gets a list of sorted edition-contentList(s) for the given edition. 
    * @param editionId the ID of the edition.
    * @return the sorted set, never <code>null</code>, but may be empty.
    */
   public static TreeSet<IPSEditionContentList> getEditionContentList(
         IPSGuid editionId)
   {
      final IPSPublisherService psvc = PSPublisherServiceLocator.getPublisherService();
      List<IPSEditionContentList> clists = psvc.loadEditionContentLists(editionId);
      TreeSet<IPSEditionContentList> sortedclists = new TreeSet<>(new EditionClistSorter());
      sortedclists.addAll(clists);
      return sortedclists;
   }

   /**
    * Comparator to sort edition content lists by sequence information.
    */
   static class EditionClistSorter implements Comparator<IPSEditionContentList>
   {
      public int compare(IPSEditionContentList o1, IPSEditionContentList o2)
      {
         int seq1 = o1.getSequence() == null ? 0 : o1.getSequence();
         int seq2 = o2.getSequence() == null ? 0 : o2.getSequence();
         return seq1 - seq2;
      }
   }

   /**
    * Gets the value of "isLastOnDemandGenerator" parameter from the given generator parameters.
    * 
    * @param parameters the generator parameters, not <code>null</code>.
    * 
    * @return <code>true</code> if the generator is marked as the last on demand content list.
    */
   public static boolean isLastOnDemandGenerator(Map<String, String> parameters)
   {
      notNull(parameters);
      
      boolean isLast = true;
      
      String isLastParam = parameters.get(IS_LAST_DEMAND_GEN);
      if (StringUtils.isNotBlank(isLastParam))
      {
         isLast = !("false".equalsIgnoreCase(isLastParam.trim()));
      }

      return isLast;
   }
   
   /**
    * The parameter used by on-demand content generator, {@link com.percussion.services.publisher.impl.PSSelectedItemsGenerator}
    * It determines if the content list of the generator is the last on-demand content list.  
    */
   public static String IS_LAST_DEMAND_GEN = "isLastOnDemandGenerator";
}
