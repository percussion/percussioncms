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
package com.percussion.data;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSContentItemData;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSHtmlParameters;

/**
 * Extracts the content item data associated with the current execution
 * context.
 */
public class PSContentItemDataExtractor extends PSDataExtractor
{
   /**
    * Creates a new <code>IPSReplacementValue</code> extractor for the supplied
    * content item data.
    *
    * @param source the content item data to construct the
    *    IPSReplacementValue for, may be <code>null</code>.
    */
   public PSContentItemDataExtractor(PSContentItemData source)
   {
      super(source);
   }

   /**
    * Extract the content item data from the supplied execution data.
    *
    * @param data the execution data to extract the data from, may be
    *    <code>null</code>.
    * @return the extracted content item status as <code>String</code>, may
    *    be <code>null</code>.
    */
   public Object extract(PSExecutionData data) throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extract the content item data from the supplied execution data.
    *
    * @param data the execution data to extract the status from, may be
    *    <code>null</code> in which case the default value is returned.
    * @param defaultVal the default value to be returned if the source
    *    could not be extracted, may be <code>null</code>.
    * @return the extracted content item data as <code>String</code> or the
    *    supplied default value if the data cannot be found, may
    *    be <code>null</code>.
    */
   public Object extract(PSExecutionData data, Object defValue)
      throws PSDataExtractionException
   {
      if (data == null)
         return defValue;
      
      IPSReplacementValue source = getSingleSource();
      
      if (source == null)
         return defValue;
      
      String name = source.getValueText();
      
      if (name == null)
         return defValue;
         
      PSRequest req = data.getRequest();
      String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      String revisionid = req.getParameter(IPSHtmlParameters.SYS_REVISION);
      
      if (contentid == null)
         throw new PSDataExtractionException(0, "contentid may not be null");
         
      if (contentid.trim().length() < 1)
         throw new PSDataExtractionException(0, "contentid may not be empty");
         
      if (revisionid==null || revisionid.trim().length() < 1)
      {
         revisionid = "-1";
      }
      
      PSLocator locator = new PSLocator(Integer.parseInt(contentid),
         Integer.parseInt(revisionid));
            
      PSItemDefManager defMgr = PSItemDefManager.getInstance();
      
      PSItemDefinition itemDef = null;
      
      try
      {
         itemDef = defMgr.getItemDef(locator, req.getSecurityToken());
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new PSDataExtractionException(e.getErrorCode(),
            e.getMessage());
      }
      
      PSServerItem item = null;
      try
      {
         item = new PSServerItem(itemDef, locator, req.getSecurityToken());
      }
      catch (PSInvalidContentTypeException ex)
      {
         throw new PSDataExtractionException(ex.getErrorCode(),
            ex.getMessage());
      }
      catch (PSCmsException e1)
      {
         throw new PSDataExtractionException(e1.getErrorCode(),
            e1.getMessage());
      }
      
      PSItemField field = item.getFieldByName(name);
      
      if (field==null)
         return defValue;
      
      IPSFieldValue fv = field.getValue();
      
      if (fv==null)
         return defValue;
           
      try
      { 
         return fv.getValueAsString();
      }
      catch (PSCmsException e1)
      {
         throw new PSDataExtractionException(e1.getErrorCode(),
         e1.getMessage());
      }
   }
}
