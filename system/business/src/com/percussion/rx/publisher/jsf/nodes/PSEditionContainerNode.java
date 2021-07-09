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

import com.percussion.rx.jsf.PSEditableNodeContainer;
import com.percussion.rx.publisher.jsf.data.PSEditionContentListWrapper;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSEdition;
import com.percussion.services.publisher.data.PSEditionContentList;
import com.percussion.services.publisher.data.PSEditionContentListPK;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Edition container, contains actions for edition creation.
 * 
 * @author dougrand
 * 
 */
public class PSEditionContainerNode extends PSEditableNodeContainer
{
   /**
    * The outcome of views all editions of a site.
    */
   public static final String EDITION_VIEWS = "pub-design-edition-views";
   
   /**
    * Ctor.
    * 
    * @param title the node title
    * @param parent the parent site, used when creating an edition, never
    *            <code>null</code>
    */
   public PSEditionContainerNode(String title, IPSSite parent) {
      super(title, EDITION_VIEWS);
      if (parent == null)
      {
         throw new IllegalArgumentException("parent may not be null");
      }
      m_siteParent = parent;
   }

   /**
    * Create a new edition and navigate to it for editing.
    * 
    * @return the outcome
    */
   public String createEdition() throws PSNotFoundException {
      IPSEdition ed = getPublisherService().createEdition();
      return createEdition(ed, null, "Edition", false);
   }

   /**
    * Creates an edition node from the given edition object.
    *  
    * @param edition the edition object, assumed not <code>null</code>.
    * @param eclists the edition/contentList association. It may be 
    *    <code>null</code> or empty.
    * @param isCopyFrom <code>true</code> if the created edition will be cloned
    *    from an existing one.
    * @param baseName the base name for the created edition, assumed not 
    *    <code>null</code>.
    * 
    * @return the outcome.
    */
   private String createEdition(IPSEdition edition,
         List<PSEditionContentListWrapper> eclists, String baseName,
         boolean isCopyFrom) throws PSNotFoundException {
      edition.setSiteId(m_siteParent.getGUID());
      edition.setName(getUniqueName(baseName, isCopyFrom)); 
      
      final PSEditionNode node = new PSEditionNode(edition);
      return node.handleNewEdition(this, edition, node, eclists);
      
   }
   
   /**
    * Create a new edition and navigate to it for editing.
    * 
    * @return the outcome
    */
   public String copyEditionFromOtherSite() throws PSNotFoundException {
      String slctEditionName = getSelectedEditionName();
      if (slctEditionName == null)
         return null;
      
      IPSEdition copiedEdition = new PSEdition();
      IPSEdition slctEdition = getPublisherService().findEditionByName(
            slctEditionName);
      copiedEdition.copy(slctEdition);
      ((PSEdition ) copiedEdition).setGUID(PSGuidHelper.generateNext(
            PSTypeEnum.EDITION));
      
      return createEdition(copiedEdition, cloneEditionContentList(slctEdition,
            copiedEdition), slctEdition.getName(), true);
   }

   /**
    * Copy the edition/contentList association from the given edition.
    * @param srcEdition the edition object, assumed not <code>null</code>.
    * @return the copied association, never <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<PSEditionContentListWrapper> cloneEditionContentList(
         IPSEdition srcEdition, IPSEdition copiedEdition) throws PSNotFoundException {
      if (!m_isDeepClone)
         return null;
      
      // get the source association
      IPSPublisherService psvc = getPublisherService(); 
      List<IPSEditionContentList> associations = psvc
            .loadEditionContentLists(srcEdition.getGUID());
      
      // deep clone both association and ContentList
      List<PSEditionContentListWrapper> eclists = new ArrayList<>();
      PSEditionContentListWrapper ecWrapper;
      for (IPSEditionContentList association : associations)
      {
         ecWrapper = cloneEditionContentList(association, copiedEdition);
         eclists.add(ecWrapper);
      }
      Collections.sort(eclists);
      return eclists;
   }
   
   /**
    * Clone a association from a given instance.
    * @param src the source association, assumed not <code>null</code>.
    * @param tgtEdition the target Edition, assumed not <code>null</code>.
    * @return the cloned association wrapper, never <code>null</code>.
    */
   private PSEditionContentListWrapper cloneEditionContentList(
         IPSEditionContentList src, IPSEdition tgtEdition) throws PSNotFoundException {
      
      IPSContentList tgtClist = cloneContentList(src.getContentListId());
      IPSEditionContentList dest = getPublisherService()
            .createEditionContentList();
      PSEditionContentListPK eclPK = ((PSEditionContentList) dest).
      getEditionContentListPK();
      eclPK.setEditionid(tgtEdition.getGUID().longValue());
      eclPK.setContentlistid(tgtClist.getGUID().longValue());
      dest.copy(src);
      
      return new PSEditionContentListWrapper(dest, m_siteParent.getGUID());
   }

   /**
    * Clone and persist the Content list
    * @param clistGuid the id of the source Content List.
    * @return the cloned (and persisted) Content List.
    */
   private IPSContentList cloneContentList(IPSGuid clistGuid) throws PSNotFoundException {
      IPSContentList clist = getPublisherService().loadContentList(clistGuid);
      IPSContentList tgtClist = clist.clone();
      tgtClist.setName(getUniqueContentListName(clist.getName()));
      getPublisherService().saveContentList(tgtClist);
      
      return tgtClist;
   }
   
   /**
    * Get the unique Content List name from a base name. 
    * @param baseName the base name, assumed not <code>null</code>.
    * @return the unique name, never <code>null</code> or empty.
    */
   private String getUniqueContentListName(String baseName) throws PSNotFoundException {
      PSContentListViewNode tmpNode = new PSContentListViewNode("dummy",
            PSContentListViewNode.Type.UNUSED, getSiteParent(), "dummyKey");
      
      return tmpNode.getUniqueName(baseName, true);
   }
   
   /**
    * @return the name of the selected Edition, may be <code>null</code> if
    *    there is no selected Edition.
    */
   public String getSelectedEditionName()
   {
      if (m_slctEditions == null)
         return null;
      
      for (CandidateEdition edition : m_slctEditions)
      {
         if (edition.isSelected())
            return edition.getName();
      }
      return null;
   }
   
   /**
    * Create a new edition and navigate to it for editing.
    * 
    * @return the outcome
    */
   public String selectEditionFromOtherSite()
   {
      m_isDeepClone = false;
      return "pub-design-editions-from-other-sites";
   }

   /**
    * This class is used to display the Editions (from different sites) in a
    * table.
    *
    */
   public static class CandidateEdition
   {
      /**
       * The edition instance, never <code>null</code> after initialized.
       */
      private IPSEdition mi_edition;
      
      /**
       * The site instance of the edition, never <code>null</code> after 
       * initialized.
       */
      private IPSSite mi_site;
      
      /**
       * See {@link #isSelected()}
       */
      private boolean mi_isSelected = false;
      
      /**
       * Create an instance from the given edition.
       * @param ed the source edition, assumed not <code>null</code>.
       */
      private CandidateEdition(IPSEdition ed, IPSSite site)
      {
         mi_edition = ed;
         mi_site = site;
      }

      /**
       * @return the name of the edition, never <code>null</code> or empty.
       */
      public String getName()
      {
         return mi_edition.getName();
      }
      
      /**
       * @return the string with name and id, see
       * {@link PSDesignNode#getNameWithId(String, long)}.
       */
      public String getNameWithId()
      {
         return PSDesignNode.getNameWithId(mi_edition.getName(), 
               ((PSEdition) mi_edition).getId());
      }
      
      /**
       * @return the site name, never <code>null</code> or empty.
       */
      public String getSiteName()
      {
         return mi_site.getName();
      }
      
      /**
       * @return the name and id of the site. Never <code>null</code> or empty.
       * @see PSDesignNode#getNameWithId(String, long)
       */
      public String getSiteNameWithID()
      {
         return PSDesignNode.getNameWithId(mi_site.getName(), mi_site.getSiteId());
      }
      
      /**
       * @return the comment of the edition, may be <code>null</code> or empty.
       */
      public String getComment()
      {
         return mi_edition.getComment();
      }
      
      /**
       * @return the edition type.
       */
      public String getEditionType()
      {
         return mi_edition.getEditionType().getDisplayTitle();
      }
      
      /**
       * Determines if this edition is selected.
       * @return <code>true</code> if the edition is selected; otherwise
       *    <code>false</code>.
       */
      public boolean isSelected()
      {
         return mi_isSelected;
      }
      
      /**
       * Set the selected state from the given flag.
       * 
       * @param selected <code>true</code> if the edition is selected.
       */
      public void setSelected(boolean selected)
      {
         mi_isSelected = selected;
      }
   }
   
   private List<CandidateEdition> m_slctEditions = null;
   
   /**
    * Gets all edition names from different sites
    * 
    * @return the list of names, never <code>null</code>, may be empty.
    */
   public CandidateEdition[] getEditionsFromOtherSites()
   {
      int siteId = m_siteParent.getSiteId().intValue();
      List<CandidateEdition> edList = new ArrayList<>();
      List<IPSEdition> edAll = getPublisherService().findAllEditions(
            m_editionListFilter);
      IPSSite site = null;
      for (IPSEdition ed : edAll)
      {
         if (ed.getSiteId() != null && ed.getSiteId().getUUID() != siteId)
         {
            site = getSite(ed);
            if (site != null)
               edList.add(new CandidateEdition(ed, site));
         }
      }
      
      m_slctEditions = edList;
      CandidateEdition[] rval = new CandidateEdition[edList.size()];
      edList.toArray(rval);
      if (rval.length > 0)
         rval[0].setSelected(true);
      
      return rval;
   }
   
   /**
    * Get the (destination) Site of the given Edition.
    * @param edition the Edition instance, assumed not <code>null</code>.
    * @return the site instance, may be <code>null</code>.
    */
   private IPSSite getSite(IPSEdition edition)
   {
      if (edition.getSiteId() == null)
         return null;
      
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      try
      {
         return smgr.loadUnmodifiableSite(edition.getSiteId());
      }
      catch (PSNotFoundException e)
      {
         return null;
      }
   }
   
   // see base
   @Override
   protected boolean findObjectByName(String name)
   {
      IPSPublisherService psvc =
            PSPublisherServiceLocator.getPublisherService();
      return psvc.findEditionByName(name) != null;
   }

   /**
    * @return the siteParent
    */
   public IPSSite getSiteParent()
   {
      return m_siteParent;
   }

   /**
    * Returns the names for the editions on all the sites.
    */
   @Override
   public Set<Object> getAllNames()
   {
      final IPSSiteManager siteManager =
            PSSiteManagerLocator.getSiteManager();
      final Set<Object> names = new HashSet<>();

      for (final IPSSite site : siteManager.findAllSites())
      {
         final List<IPSEdition> editions =
            getPublisherService().findAllEditionsBySite(site.getGUID());
         for (final IPSEdition edition : editions)
         {
            names.add(edition.getDisplayTitle());
         }
      }
      return names;
   }

   /**
    * Get the edition list filter.
    * 
    * @return the filter, never <code>null</code>, but may be empty.
    */
   public String getEditionListFilter()
   {
      return m_editionListFilter;
   }
   
   /**
    * Set the edition list filter.
    * @param filter the filter, it may be <code>null</code> or empty. The
    *    filter will be set to empty if it is <code>null</code>.
    */
   public void setEditionListFilter(String filter)
   {
      m_editionListFilter = filter == null ? "" : filter;
   }
   
   /**
    * Convenience method to access the publisher service.
    * @return the publisher service. Never <code>null</code>.
    */
   private IPSPublisherService getPublisherService()
   {
      return PSPublisherServiceLocator.getPublisherService();
   }

   @Override
   public String returnToListView()
   {
      return "return-to-editions";
   }

   /**
    * Set the deep clone flag. See {@link #isDeepClone()} for detail.
    * @param isDeepClone the flag.
    */
   public void setDeepClone(boolean isDeepClone)
   {
      m_isDeepClone = isDeepClone;
   }
   
   /**
    * Determines if deep clone the selected Edition, including its 
    * ContentLists (from different site), or not to copy the ContentList and
    * the Edition/ContentLists association.
    * 
    * @return <code>true</code> if deep copy; otherwise the copied Edition will 
    *    have empty ContentLists.
    */
   public boolean isDeepClone()
   {
      return m_isDeepClone;
   }
   
   @Override
   public String getHelpTopic()
   {
      return "EditionList";
   }

   /**
    * @see {@link #isDeepClone()}
    * Default to <code>false</code>.
    */
   private boolean m_isDeepClone = false;
   
   /**
    * The parent site, set in the ctor, never <code>null</code>.
    */
   private IPSSite m_siteParent = null;
   
   /**
    * If set, this string should filter the content lists we can select from in
    * the association page. Default to empty string, never <code>null</code>.
    */
   private String m_editionListFilter = "";
}
