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
package com.percussion.services.publisher.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.data.PSQueryResult;
import com.percussion.services.contentmgr.data.PSRow;
import com.percussion.services.contentmgr.data.PSRowComparator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.IPSContentListGenerator;
import com.percussion.utils.jsr170.PSLongValue;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The base generator class, used for more than one implementation.
 * 
 * @author dougrand
 */
public abstract class PSBaseGenerator implements IPSContentListGenerator
{
   /**
    * The logger
    */
   protected static Log ms_log = LogFactory.getLog(PSBaseGenerator.class);

   /**
    * The cms service
    */
   protected static IPSCmsObjectMgr ms_cms = PSCmsObjectMgrLocator
         .getObjectManager();

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
   {
      // TODO Auto-generated method stub

   }

   /**
    * Create an empty query result with the contentId and folderId columns
    * 
    * @return The empty query result to which rows can be added, never <code>null</code>.
    */
   protected PSQueryResult createQueryResult()
   {
      List<PSPair<String, Boolean>> fields = new ArrayList<>();
      fields.add(new PSPair<>(
            IPSContentPropertyConstants.RX_SYS_CONTENTID, true));
      PSRowComparator comparator = new PSRowComparator(fields);
      String[] columns =
      {IPSContentPropertyConstants.RX_SYS_CONTENTID,
            IPSContentPropertyConstants.RX_SYS_FOLDERID};
      PSQueryResult qr = new PSQueryResult(columns, comparator);
      return qr;
   }

   /**
    * Add a single content id to the result set
    * 
    * @param qr the results, assumed never <code>null</code>
    * @param contentid the id
    */
   protected void addToResults(PSQueryResult qr, int contentid)
   {
      addToResults(qr, contentid, -1);
   }
   
   protected void addToResults(PSQueryResult qr, int contentid, int folderid)
   {
      Map<String, Object> data = new HashMap<>();
      Value idval = new PSLongValue(contentid);
      data.put(IPSContentPropertyConstants.RX_SYS_CONTENTID, idval);
      if (folderid != -1)
      {
         Value folderval = new PSLongValue(folderid);
         data.put(IPSContentPropertyConstants.RX_SYS_FOLDERID, folderval);         
      }
      PSRow row = new PSRow(data);
      qr.addRow(row);
   }
}
