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
