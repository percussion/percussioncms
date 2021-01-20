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
package com.percussion.rxfix;

import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.rxfix.dbfixes.*;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.cache.PSCacheProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * A framework program that runs a series of fixup modules for a Rhythmyx
 * installation. The modules are run in order, when you add a module, please
 * consider its position in the overall list. Each module implements
 * {@link IPSFix}.
 */
public class PSRxFix
{
   /**
    * Represents and entry in the ui model
    */
   public class Entry
   {
      boolean mi_dofix;

      String mi_fixname;

      Class mi_fix;
      
      List<PSFixResult> mi_results;

      /**
       * Ctor
       * @param fixclass fix class must impleemnt {@link IPSFix}
       * @throws InstantiationException
       * @throws IllegalAccessException
       */
      public Entry(Class fixclass) throws InstantiationException,
            IllegalAccessException {
         // Instantiate to get the descriptive info
         mi_fix = fixclass;
         IPSFix fix = (IPSFix) mi_fix.newInstance();
         mi_fixname = fix.getOperation();
         mi_dofix = true;
      }

      /**
       * @return Returns the dofix.
       */
      public boolean isDofix()
      {
         return mi_dofix;
      }

      /**
       * @param dofix The dofix to set.
       */
      public void setDofix(boolean dofix)
      {
         mi_dofix = dofix;
      }

      /**
       * @return Returns the fix.
       */
      public Class getFix()
      {
         return mi_fix;
      }

      /**
       * @param fix The fix to set.
       */
      public void setFix(Class fix)
      {
         mi_fix = fix;
      }

      /**
       * @return Returns the fixname.
       */
      public String getFixname()
      {
         return mi_fixname;
      }

      /**
       * @param fixname The fixname to set.
       */
      public void setFixname(String fixname)
      {
         mi_fixname = fixname;
      }

      /**
       * @return Returns the results.
       */
      public List<PSFixResult> getResults()
      {
         return mi_results;
      }

      /**
       * @param results The results to set.
       */
      public void setResults(List<PSFixResult> results)
      {
         mi_results = results;
      }
   }

   /**
    * Set after the preview has been done, guards the page flow
    */
   private boolean m_previewDone = false;

   /**
    * Set after the fix run has been done, used in page flow
    */
   private boolean m_fixDone = false;

   /**
    * The array of fixes that exist. The order of these fixes is important.
    */
   private Class m_fixes[] = new Class[]
   {
      PSFixNextNumberTable.class, 
      PSFixContentStatusHistory.class,
      //PSFixContentStatusHistoryWFInfo.class, 
      PSFixOrphanedSlots.class, 
      PSFixBrokenRelationships.class,
      // PSFixOrphanedData.class, omitted since the data is missing 
      PSFixInvalidFolders.class,
      PSFixOrphanedFolders.class,
      PSFixInvalidFolderRelationships.class,
      PSFixDanglingAssociations.class,
      PSFixCommunityVisibilityForViews.class,
      PSFixTranslationRelationships.class,
      PSFixInvalidSysTitle.class,
      PSFixAllowedSitePropertiesWithBadSites.class,
      PSFixOrphanedContentChangeEvents.class,
      PSFixZerosInRelationshipProperties.class,
      PSFixOrphanedManagedLinks.class,
      PSFixStaleDataForContentTypes.class,
      PSFixPageCatalog.class,
           PSFixAcls.class
   };

   /**
    * These entries dictate what do to for each fix. The data is presented and
    * modified in the UI as the model, and is used directly in the doFix call.
    * Initialized on reset or construction, and never <code>null</code> after.
    */
   private List<Entry> m_entries = null;

   /**
    * Ctor
    * @throws Exception 
    */
   public PSRxFix() throws Exception {
      init();
   }

   /**
    * Initialize state
    */
   private void init() throws Exception
   {
      m_previewDone = false;
      m_fixDone = false;
      m_entries = new ArrayList<Entry>();
      for(int i = 0; i < m_fixes.length; i++)
      {
         Entry e = new Entry(m_fixes[i]);
         e.setResults(null);
         m_entries.add(e);
      }
   }

   /**
    * @return Returns the previewDone.
    */
   public boolean isPreviewDone()
   {
      return m_previewDone;
   }

   /**
    * @return Returns the fixDone.
    */
   public boolean isFixDone()
   {
      return m_fixDone;
   }

   /**
    * @param fixDone The fixDone to set.
    */
   public void setFixDone(boolean fixDone)
   {
      m_fixDone = fixDone;
   }

   /**
    * @param previewDone The previewDone to set.
    */
   public void setPreviewDone(boolean previewDone)
   {
      m_previewDone = previewDone;
   }
   
   /**
    * Get entries, which include result data
    * @return the entries, never <code>null</code>
    */
   public List<Entry> getEntries()
   {
      return m_entries;
   }
   
   /**
    * Get only those entries that were actually run
    * @return the entries, might be empty
    */
   public List<Entry> getRunentries()
   {
      List<Entry> rval = new ArrayList<Entry>();
      
      for(Entry e : m_entries)
      {
         if (e.isDofix())
         {
            rval.add(e);
         }
      }
      
      return rval;
   }

   /**
    * Preview action
    * 
    * @return the outcome
    */
   public String preview()
   {
      try
      {
         doFix(true);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return "admin-rxfix-preview";
   }

   /**
    * Fix action
    * 
    * @return the outcome
    */
   public String next()
   {
      if (m_fixDone)
      {
         return "admin-rxfix";
      }
      
      try
      {
         doFix(false);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return "admin-rxfix-preview";
   }
   
   /**
    * UI label for next action
    * @return the label for the "next" button on the results page
    */
   public String getFixnextlabel()
   {
      return m_fixDone ? "Done" : "Fix";
   }

   /**
    * Reset action
    * 
    * @return outcome
    * @throws Exception 
    */
   public String reset() throws Exception
   {
      init();
      return "reset";
   }

   /**
    * Startup and do one or more fixes
    * 
    * @param preview Run the fixups in preview mode
    * @throws Exception if there is a problem setting up to perform the fixes
    * 
    */
   public void doFix(boolean preview) throws Exception
   {
      for (Entry e : m_entries)
      {
         if (! e.isDofix())
            continue;

         // Instantiate
         IPSFix f = null;
         f = (IPSFix) e.getFix().newInstance();
         f.fix(preview);

         // Get results
         e.setResults(f.getResults());
      }
      
      if (PSCacheManager.isAvailable()) {
         PSCacheManager cacheManager = PSCacheManager.getInstance();
         cacheManager.flush();
         PSCacheProxy.flushFolderCache();
      }

      m_previewDone = preview;
      m_fixDone = !preview;
   }
   
   /**
    * Get the help file name for the RxFix page.
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getHelpFile()
   {
      return PSHelpTopicMapping.getFileName("RxFix");      
   }
   
}
