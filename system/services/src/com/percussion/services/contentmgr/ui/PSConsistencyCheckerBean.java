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
package com.percussion.services.contentmgr.ui;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.ui.PSConsistencyBase.Problem;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.guid.IPSGuid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.jcr.RepositoryException;
import javax.naming.NamingException;

/**
 * JSF bean for the consistency checker interface. This bean has a session
 * lifetime.
 * 
 * @author dougrand
 */
public class PSConsistencyCheckerBean
{
   /**
    * Represents a single content type in the UI
    */
   public static class CTypeEntry
   {
      /**
       * The content type's guid, never <code>null</code> after ctor.
       */
      IPSGuid mi_guid;

      /**
       * The content type's label, never <code>null</code> or empty after ctor.
       */
      String mi_label;

      /**
       * The content type's name, never <code>null</code> or empty after ctor.
       */
      String mi_name;

      /**
       * This value is <code>true</code> if this content type should be
       * checked.
       */
      Boolean mi_selected;

      /**
       * Ctor.
       * 
       * @param guid the guid, assumed not <code>null</code>.
       * @param label the label, assumed not <code>null</code> or empty.
       * @param name the name, assumed not <code>null</code> or empty.
       */
      public CTypeEntry(IPSGuid guid, String label, String name) {
         mi_guid = guid;
         mi_label = label;
         mi_name = name;
         mi_selected = true;
      }

      /**
       * @return the selected
       */
      public Boolean getSelected()
      {
         return mi_selected;
      }

      /**
       * @param selected the selected to set
       */
      public void setSelected(Boolean selected)
      {
         mi_selected = selected;
      }

      /**
       * @return the guid
       */
      public IPSGuid getGuid()
      {
         return mi_guid;
      }

      /**
       * @return the label
       */
      public String getLabel()
      {
         return mi_label;
      }

      /**
       * @return the name
       */
      public String getName()
      {
         return mi_name;
      }
   }

   /**
    * Represents a single content item and its state
    */
   public static class CItemEntry
   {
      /**
       * This item's content id
       */
      int mi_contentid;

      /**
       * A list of problems associated with the content item
       */
      Collection<Problem> mi_problems;

      /**
       * State of item, whether the user has "fixed" the item.
       */
      boolean mi_fixed = false;

      /**
       * Ctor
       * 
       * @param id the content id of the item being checked
       * @param problems a collection of found problems, may be
       *           <code>empty</code> but not <code>null</code>
       */
      public CItemEntry(int id, Collection<Problem> problems) {
         if (problems == null)
         {
            throw new IllegalArgumentException("problems may not be null");
         }
         mi_contentid = id;
         mi_problems = problems;
      }

      /**
       * @return the contentid
       */
      public int getContentid()
      {
         return mi_contentid;
      }

      /**
       * @return the problems
       */
      public Collection<Problem> getProblems()
      {
         return mi_problems;
      }

      /**
       * Get the number of problems registered
       * 
       * @return the count, might be <code>0</code>
       */
      public int getProblemCount()
      {
         return mi_problems.size();
      }

      /**
       * Is this content item fixable
       * 
       * @return <code>true</code> if the problems can all be fixed.
       */
      public Boolean getFixable()
      {
         boolean fixable = true;
         for (Problem p : mi_problems)
         {
            if (p.getMissingRevisions().contains(1))
            {
               fixable = false;
               break;
            }
         }
         return fixable;
      }

      /**
       * Is this content item fixed
       * 
       * @return <code>true</code> if fixed
       */
      public Boolean getFixed()
      {
         return mi_fixed;
      }

      /**
       * Can this problem be fixed?
       * 
       * @return <code>true</code> if this problem can be fixed
       */
      public Boolean getCanfix()
      {
         return !getFixed() && getFixable();
      }

      /**
       * Get the title of the content item
       * 
       * @return the title of the content item
       */
      public String getTitle()
      {
         PSComponentSummary s = ms_cms.loadComponentSummary(mi_contentid);
         return s.getName();
      }

      /**
       * Fix this item
       * 
       * @return the outcome, always fix
       * @throws SQLException
       * @throws NamingException
       */
      public String fix() throws NamingException, SQLException
      {
         PSConsistencyFixer fixer = new PSConsistencyFixer();

         for (Problem p : mi_problems)
         {
            fixer.fix(p);
         }

         mi_fixed = true;

         return "fix";
      }
   }

   /**
    * The content manager, used here to enumerate the node defs available
    */
   private static IPSContentMgr ms_cmgr = PSContentMgrLocator.getContentMgr();

   /**
    * The object manager
    */
   private static IPSCmsObjectMgr ms_cms = PSCmsObjectMgrLocator
         .getObjectManager();

   /**
    * Holds the content type entries, initialized in the ctor
    */
   private List<CTypeEntry> m_typeentries;

   /**
    * Holds the problems found after the user hits the check action, but never
    * <code>null</code> after construction
    */
   private List<CItemEntry> m_problemsFound;

   /**
    * If there is an error, store it here. Cleared on actions.
    */
   private String m_errorMessage = "";

   /**
    * Ctor
    * 
    * @throws RepositoryException if the content manager has a problem
    *            retrieving node definitions from the database
    */
   public PSConsistencyCheckerBean() throws RepositoryException {
      List<IPSNodeDefinition> nodedefs = ms_cmgr.findAllItemNodeDefinitions();
      List<CTypeEntry> defs = new ArrayList<CTypeEntry>();

      for (IPSNodeDefinition def : nodedefs)
      {
         CTypeEntry entry = new CTypeEntry(def.getGUID(), def.getLabel(), def
               .getName());
         defs.add(entry);
      }

      m_typeentries = defs;
      m_problemsFound = new ArrayList<CItemEntry>();
   }

   /**
    * @return the errorMessage
    */
   public String getErrorMessage()
   {
      return m_errorMessage;
   }

   /**
    * @return the problemsFound
    */
   public List<CItemEntry> getProblemsFound()
   {
      return m_problemsFound;
   }
   
   /**
    * Are there problems?
    * @return <code>true</code> if there are problems
    */
   public Boolean getHasProblems()
   {
      return m_problemsFound.size() > 0;
   }

   /**
    * @return the typeentries
    */
   public List<CTypeEntry> getTypeentries()
   {
      return m_typeentries;
   }

   /**
    * Do the check.
    * 
    * @see PSConsistencyProblemFinder for details on how the check is performed
    * @return the outcome of this action
    */
   public String check()
   {
      m_errorMessage = "";
      m_problemsFound = new ArrayList<CItemEntry>();
      Map<Integer, Collection<Problem>> pmap = new HashMap<Integer, Collection<Problem>>();
      try
      {
         PSConsistencyProblemFinder finder = new PSConsistencyProblemFinder();
         for (CTypeEntry e : m_typeentries)
         {
            if (!e.getSelected())
               continue;

            Collection<Problem> problems = finder.check(e.getName());

            for (Problem p : problems)
            {
               Collection<Problem> cidproblems = pmap.get(p.getContentid());
               if (cidproblems == null)
               {
                  cidproblems = new ArrayList<Problem>();
                  pmap.put(p.getContentid(), cidproblems);
               }
               cidproblems.add(p);
            }
         }
         // Build the UI data structure
         Set<Integer> sortedids = new TreeSet<Integer>(pmap.keySet());
         for (Integer cid : sortedids)
         {
            Collection<Problem> problems = pmap.get(cid);
            m_problemsFound.add(new CItemEntry(cid, problems));
         }
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance()
               .addMessage(
                     "Problem",
                     new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Problem while performing check:", e
                                 .getLocalizedMessage()));
         return "error";
      }
      return "report";
   }

   /**
    * Action to move to the report page
    * 
    * @return the outcome
    */
   public String finish()
   {
      return "done";
   }
   
   /**
    * Get the help file name for the ConsistencyChecker page.
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getHelpFile()
   {
      return PSHelpTopicMapping.getFileName("ConsistencyChecker");      
   }

}
