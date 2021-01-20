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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStringOperation;

import java.text.MessageFormat;
import java.util.Iterator;

/**
 * This UDF formats a content item title for a new clone.
 * <p>
 * If the HTML parameter {@link IPSHtmlParameters#SYS_TITLE_OVERRIDE} is not
 * <code>null</code> or empty, then return the value of the parameter. The 
 * parameter will be removed afterwards. Otherwise, it will do the following. 
 * <p>
 * It queries CMS relationships in order to determine how many clones already
 * exist of an original item / owner, then, given java MessageFormat string
 * with or without insertion items and one or more dynamic parameters, formats
 * the final title for a new cloned item.
 * <pre>
 * <p>
 * Param1: (required) MessageFormat string, ie: [{0}]Copy $clone_count of {1}
 *    where:
 *    <p>
 *    {0} will be replaced by a replacement value supplied to this UDF
 *    in a Param2, which for example could be a sys_lang html param.
 *    <p> 
 *    $clone_count is a special keyword, if present then this UDF will
 *    replace it with a number that represents a number of clones,
 *    or with an empty string if there are no clones yet (creating a first clone).
 *    <p>
 *    {1} will be replaced by a replacement value supplied to this UDF
 *    in a Param3, which for example could be a sys_title html param.
 *    <p>
 *    Finally the result for the above example could look like this:
 *    [fr-fr]Copy of myContent
 * <p>
 * Param2: (required if {0} is used) any replacement value that will be
 *    inserted into insert item {0}.
 * <p>
 * Param3: (required if {1} is used) any replacement value that will be
 *    inserted into insert item {1}.
 * <p>
 * etc.
 *    
 * </pre>
 * 
 * Note: if the number of {inserts} in the MessageFormat string does not
 * match the number of paremeters (after format) given to this UDF, then
 * it will throw the PSConversionException.
 * 
 * @author Vitaly.
 */
public class PSSimpleJavaUdf_cloneTitle extends PSSimpleJavaUdfExtension
{

   /* (non-Javadoc)
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if (size < 1 || params[0]==null){  // one parameter is required
         int errCode = 0;
         String arg0 = "expect at least 1 parameter - format.";
         Object[] args = { arg0, "PSSimpleJavaUdf_cloneTitle/processUdf" };
         throw new PSConversionException(errCode, args);
      }
      
      String overrideName = request.getParameter(
            IPSHtmlParameters.SYS_TITLE_OVERRIDE);
      if (overrideName != null && overrideName.trim().length() > 0)
      {
         request.removeParameter(IPSHtmlParameters.SYS_TITLE_OVERRIDE);
         return overrideName; 
      }
      
      Object[] formatArgs = new Object[size-1];
      for (int i = 0; i < formatArgs.length; i++)
         formatArgs[i] = params[i+1];
         
      String format = params[0].toString();
      
      try
      {
         format = MessageFormat.format(format, formatArgs);
         
         String cloneCount = "";
         final String CLONE_COUNT = "$clone_count";
         
         if (format.indexOf(CLONE_COUNT) >= 0)
         {
            int count = calculateCloneCount(request);
              
            cloneCount = "" + (count+1);
         }
         
         format = PSStringOperation.replace(format, CLONE_COUNT, cloneCount);
         
         return format;
      }
      catch (Exception e)
      {
         int errCode = 0;
         Object[] args = { e.getLocalizedMessage(), "PSSimpleJavaUdf_concat/processUdf" };
         throw new PSConversionException(errCode, args);
      }
   }

   /**
    * Returns a number of clones of a supplied contentid that already exist in the system.
    *  
    * @param request request context, never <code>null</code>.
    * 
    * @return a positive number of the existing clones of a SYS_CONTENTID
    * that is set on the given request.
    *   
    * @throws PSCmsException if ProcessorProxy fails to fetch or parse the doc.
    */
   private int calculateCloneCount(IPSRequestContext request) throws PSCmsException
   {
      if (request== null)
         throw new IllegalArgumentException("request may not be null");
      
      String contentId = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      String revisionId = request.getParameter(IPSHtmlParameters.SYS_REVISION);
      String relType = request.getParameter(IPSHtmlParameters.SYS_RELATIONSHIPTYPE);
      String lang = request.getParameter(IPSHtmlParameters.SYS_LANG);
      
      if (contentId==null)
         throw new IllegalArgumentException("IPSHtmlParameters.SYS_CONTENTID is missing");

      if (revisionId==null)
         throw new IllegalArgumentException("IPSHtmlParameters.SYS_REVISION is missing");

      if (relType==null)
         throw new IllegalArgumentException("IPSHtmlParameters.SYS_RELATIONSHIPTYPE is missing");

      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      PSRelationshipConfig relConfig = relProxy.getConfig(relType);

      if (relConfig == null)
      {
         throw new IllegalArgumentException(
            "Relationship type '" + relType + "' is not defined in the CMS");
      }
      
      PSLocator owner = new PSLocator(contentId, revisionId);
            
      //load all existing relationships of the owner
      PSRelationshipFilter filter = new PSRelationshipFilter();
      if (relConfig.getCategory().equals(PSRelationshipConfig.CATEGORY_COPY))
      {
         // For relationship type, such as NewCopy, get all relationships 
         // regardless owner revision
         filter.setOwner(new PSLocator(owner.getId(), -1));
      }
      else
      {  
         filter.setOwner(owner);
      }
      filter.setName(relType);
                  
      PSRelationshipSet rels = relProxy.getRelationships(filter);
      
      if (rels.size() <= 1)
      {
         return rels.size();
      }
      else if (
         !relConfig.getCategory().equals(
            PSRelationshipConfig.CATEGORY_TRANSLATION))
      {
         return rels.size();
      }
      else
      {
         if (lang==null)
            throw new IllegalArgumentException("IPSHtmlParameters.SYS_LANG is missing");

         /* for translation, if there are more than one relationships with this owner,
          * in order to correctly determine clone count we need to look for dependents'
          * locales; the system should normally prevent multiple translations into the
          * same locale, which means that normally there shouldn't be more than one clone
          * with the same locale, we however check that to make sure that our count is correct.   
          */ 
         
         int countDepLocales = 0;
         
         Iterator sums = relProxy.getSummaries(filter, false).iterator();
         
         while (sums.hasNext())
         {
            PSComponentSummary summary = (PSComponentSummary) sums.next();
            
            String locale = summary.getLocale();
            
            if (locale.equals(lang))
            {
               //found one with the same locale, bump up the counter
               countDepLocales++; 
            }
            
         }
         return countDepLocales;
      }
   }
}
