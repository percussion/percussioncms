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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.util.ArrayList;
import java.util.List;

public class PSItemBrowser extends PSContentBrowser
{
   private PSSchemeJexlTestPanel m_testPanel = null;
   private static final String ITEM_BROWSER = "pub-design-item-browser";
   
   PSItemBrowser(PSSchemeJexlTestPanel testPanel)
   {
      m_testPanel = testPanel;
   }

   @Override
   protected String childPerform(ChildItem item)
   {
      if (item.isFolder())
         return super.childPerform(item);
      
       m_testPanel.setItemPath(getPath() + "/" + item.getName());
       return m_testPanel.perform();
   }

   @Override
   protected String perform()
   {
      return ITEM_BROWSER;
   }
   
   @Override
   protected List<ChildItem> getChildItems() throws Exception
   {
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid id = mgr.makeGuid(new PSLocator(getFolderId()));
      List<PSItemSummary> summaries = cws.findFolderChildren(id, false);
      List<ChildItem> items = new ArrayList<>();
      for (PSItemSummary item : summaries)
      {
         boolean isFolder = item.getContentTypeId() == 
            PSFolder.FOLDER_CONTENT_TYPE_ID;
         items.add(new ChildItem(item.getGUID(), item.getName(), isFolder));
      }
      return items;

   }
   
   /**
    * Get the actual help file name for the Location Scheme Editor page.
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getHelpFile()
   {
      return PSHelpTopicMapping.getFileName("ItemBrowser");   
   }
   
}
