/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
