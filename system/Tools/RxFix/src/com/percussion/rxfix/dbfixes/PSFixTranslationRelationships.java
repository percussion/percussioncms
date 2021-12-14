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
package com.percussion.rxfix.dbfixes;

import com.percussion.cms.handlers.PSCloneHandler;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This fixer scans for all translation relationships and duplicates them so
 * that every revision larger than the lowest revision that contains a
 * translation relationship also has a copy of it (per language.)
 * <p>
 * This is used as part of the workaround for bug Rx-14773.
 * <p>
 * In the future, when the correct fix is implemented, this code should be
 * modified to support both types of fixup that will be required at that time.
 * 
 * @author paulhoward
 */
public class PSFixTranslationRelationships extends PSFixBase
{

   @Override
   public String getOperation()
   {
      return "Fix Translation Relationships";
   }

   @SuppressWarnings("unchecked")
   @Override
   public void fix(boolean preview)
         throws Exception
   {
      super.fix(preview);
      
      if (null == PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST))
      {
         logInfo(null, "This rxFix plugin cannot run outside of a server.");
         return;
      }
      
      IPSSystemWs sys = PSSystemWsLocator.getSystemWebservice();
      List<PSRelationshipConfig> defs = sys.loadRelationshipTypes(null, 
            PSRelationshipConfig.CATEGORY_TRANSLATION);
      
      //should this fixup be run against the config on this system?
      boolean valid = false;
      for (PSRelationshipConfig cfg : defs)
      {
         valid |= isValidConfigurationForFixup(cfg);
      }
      if (!valid)
      {
         logInfo(null, 
            "This plugin does not need to run with the given relationship configuration.");
         return;
      }
      
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCategory(PSRelationshipConfig.CATEGORY_TRANSLATION);
      List<PSRelationship> tranRels = sys.loadRelationships(filter);
      
      Map<Integer, PSComponentSummary> idToSum = 
         new HashMap<Integer, PSComponentSummary>();
      PSRelationshipProcessor rproc = 
         PSWebserviceUtils.getRelationshipProcessor();
      PSComponentSummaries ownerSums = rproc.getSummaries(filter, true);
      Iterator<PSComponentSummary> iter = ownerSums.iterator();
      while (iter.hasNext())
      {
         PSComponentSummary sum = iter.next();
         idToSum.put(sum.getContentId(), sum);
      }

      //group rels by owner id, then process groups
      Map<Integer, List<PSRelationship>> ownerIdToRels = 
         new HashMap<Integer, List<PSRelationship>>();
      for (PSRelationship rel : tranRels)
      {
         List<PSRelationship> ownerRels = 
            ownerIdToRels.get(rel.getOwner().getId());
         if (ownerRels == null)
         {
            ownerRels = new ArrayList<PSRelationship>();
            ownerIdToRels.put(rel.getOwner().getId(), ownerRels);
         }
         ownerRels.add(rel);
      }
      
      Map<Integer, List<PSRelationship>> contentIdToNewRels =
         new HashMap<Integer, List<PSRelationship>>();
      for (List<PSRelationship> rels : ownerIdToRels.values())
      {
            PSComponentSummary sum = idToSum.get(rels.get(0).getOwner().getId());
            addRels(contentIdToNewRels, sum, rels);
      }
      
      if (contentIdToNewRels.size() == 0)
      {
         logInfo(null, 
               "There are no translation relationships that need fixing.");
      }
      for (Integer id : contentIdToNewRels.keySet())
      {
         List<PSRelationship> newRels = contentIdToNewRels.get(id);
         int count = newRels == null ? 0 : newRels.size();
         if (count == 0)
            continue;
         if (preview)
         {
            logPreview(id.toString(), 
               "Would add " + count + " translation relationships to this item.");
         }
         else
         {
            sys.saveRelationships(newRels);
            logSuccess(id.toString(), 
                  "Added " + count + " translation relationships to this item.");
         }
      }
   }

   /**
    * Checks if shallow cloning is enabled on the supplied config.
    * 
    * @param cfg Assumed not <code>null</code>.
    * @return <code>true</code> if shallow cloning is enabled,
    * <code>false</code> otherwise.
    */
   private boolean isValidConfigurationForFixup(PSRelationshipConfig cfg)
   {
      /**
       * There is some oddity here (the RS_CLONESHALLOW param.) This constant
       * should really be on the cfg object (in fact there is a similar
       * constant that is not used, SHALLOW_CLONING.)
       */
      return (cfg.isCloningAllowed() 
            && cfg.getProcessCheck(PSCloneHandler.RS_CLONESHALLOW) != null);
   }

   /**
    * Takes the revision of the supplied rel and compares it to the supplied
    * item summary. If they match, nothing is done. Otherwise, for every
    * revision beyond that specified in the rel, a new relationship is created.
    * This relationship will match the supplied one except for owner revision.
    * All of these are added as a collection to the supplied
    * <code>contentIdToNewRels</code> results map.
    * 
    * @param contentIdToNewRels The newly created relationships will be added to
    * this map. If no relationships are created for an item, no entry will be
    * added to the map. The ownerId is used as the key. Assumed not
    * <code>null</code>.
    * 
    * @param sum The owner of the supplied <code>rels</code>. Assumed not
    * <code>null</code>.
    * 
    * @param rels The original translation relationships that originate on the
    * item with the supplied summary. The new relationships will nearly be
    * clones, only differing by the owner revision. Assumed not
    * <code>null</code>.
    */
   private void addRels(Map<Integer, List<PSRelationship>> contentIdToNewRels, 
         PSComponentSummary sum, List<PSRelationship> rels)
   {
      //map rels by dependent item id
      Map<Integer, List<PSRelationship>> depContentIdToRels = 
         new HashMap<Integer, List<PSRelationship>>();
      for (PSRelationship rel : rels)
      {
         //skip rels whose cfg is not valid for fixing
         if (!isValidConfigurationForFixup(rel.getConfig()))
            continue;
         List<PSRelationship> depRels = 
            depContentIdToRels.get(rel.getDependent().getId());
         if (depRels == null)
         {
            depRels = new ArrayList<PSRelationship>();
            depContentIdToRels.put(rel.getDependent().getId(), depRels);
         }
         depRels.add(rel);
      }
      
      int tipRev = sum.getTipLocator().getRevision();
      List<PSRelationship> newRels = new ArrayList<PSRelationship>();
      for (List<PSRelationship> depItemRels : depContentIdToRels.values())
      {
         // find the lowest owner revision
         int startingRev = Integer.MAX_VALUE;
         Set<Integer> allRevs = new HashSet<Integer>();
         for (PSRelationship rel : depItemRels)
         {
            int ownerRev = rel.getOwner().getRevision();
            allRevs.add(ownerRev);
            if (ownerRev < startingRev)
               startingRev = ownerRev; 
         }

         //create missing rels
         if (startingRev >= tipRev)
            continue;
         PSRelationship sourceRel = depItemRels.get(0);
         for (int rev = startingRev+1; rev <= tipRev; rev++)
         {
            if (!allRevs.contains(rev))
               newRels.add(new Relationship(sourceRel, rev));
         }
      }
      if (!newRels.isEmpty())
         contentIdToNewRels.put(sum.getContentId(), newRels);
   }
   
   /**
    * Local override so we can access the protected ctor.
    *
    * @author paulhoward
    */
   private class Relationship extends PSRelationship
   {
      /**
       * Clones the supplied rel, changing the owner revision to that supplied.
       * 
       * @param rel The source to clone. Assumed not <code>null</code>.
       * @param rev The new revision of the owner.
       */
      public Relationship(PSRelationship rel, int rev)
      {
         super(rel);
         setId(-1);
         setPersisted(false);
         setOwner(new PSLocator(getOwner().getId(), rev));
      }
   }
}
