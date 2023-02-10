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

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The backing bean for the Site Root Path browser.
 */
public class PSSiteRootBrowser extends PSContentBrowser
{
   /**
    * The outcome of browsing the Site Root Path.
    */
   private static final String SITE_ROOT_BROWSER = "pub-design-site-root-browser";

   /**
    * The parent/site node, never <code>null</code> after the ctor.
    */
   private PSSiteNode m_site = null;


   /**
    * Constructs an instance of the Site Root Browser.
    * @param site the parent node. It may not be <code>null</code>.
    */
   public PSSiteRootBrowser(PSSiteNode site)
   {
      if (site == null)
         throw new IllegalArgumentException("site may not be null.");
      m_site = site;
   }

   /**
    * Finishes browsing folder root.
    * @return the outcome of the Site Editor, never <code>null</code> or empty.
    */
   public String done()
   {
      if (m_site != null)
      {
         m_site.setFolderRootPath(getPath());
         return m_site.perform();
      }
      else
      {
         return null;
      }
   }

   /**
    * Get the actual help file name for the Site Root Browser page.
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getHelpFile()
   {
      return PSHelpTopicMapping.getFileName("SiteRootBrowser");   
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected String perform()
   {
      return SITE_ROOT_BROWSER;
   }

   /*
    * //see base class method for details
    */
   @SuppressWarnings("unchecked")
   @Override
   protected List<ChildItem> getChildItems() throws Exception
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwnerId(getFolderId());
      filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      filter.setDependentObjectType(PSCmsObject.TYPE_FOLDER);

      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      PSComponentSummaries sums = getFolderSrv().getSummaries(filter, false);
      
      List<ChildItem> folders = new ArrayList<>();
      Iterator it = sums.getSummaries();
      ChildItem child;
      while (it.hasNext())
      {
         PSComponentSummary sum = (PSComponentSummary) it.next();
         child = new ChildItem(mgr.makeGuid(sum.getCurrentLocator()), sum
               .getName(), sum.isFolder());
         folders.add(child);
      }
      return folders;
   }
}
