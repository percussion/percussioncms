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
package com.percussion.cms;

import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSFolderPermissions;
import com.percussion.cms.objectstore.PSObjectPermissions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class PSActionVisibilityCheckerTest extends TestCase
{
   public PSActionVisibilityCheckerTest(String name)
   {
      super(name);
   }

   @SuppressWarnings("unchecked")
   public void testBasics()
   {
      PSActionVisibilityChecker checker = new PSActionVisibilityChecker(-1,
            true, null);
      assertTrue(checker.isVisible(null, (Collection) null));
      
      assertTrue(checker.isVisible(
            new GenericGlobalState(new HashMap<String, Object>()), 
            new GenericObjectState(new HashMap<String, Object>())));
   }
   
   /**
    * Tests all contexts that are exposed in
    * {@link PSActionVisibilityGlobalState}.
    */
   public void testGlobalStates()
   {
      genericCheckGlobalVisibility(
            PSActionVisibilityContext.VIS_CONTEXT_COMMUNITY);
      genericCheckGlobalVisibility(
            PSActionVisibilityContext.VIS_CONTEXT_CLIENT_CONTEXT);
      genericCheckGlobalVisibility(
            PSActionVisibilityContext.VIS_CONTEXT_LOCALES_TYPE);
      checkGlobalVisibilityRoles();
   }
   
   /**
    * Tests all contexts that are exposed in
    * {@link PSActionVisibilityObjectState}.
    */
   public void testObjectStates()
   {
      //integer types
      genericCheckObjectVisibility(
            PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE);
      genericCheckObjectVisibility(
            PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE);
      genericCheckObjectVisibility(
            PSActionVisibilityContext.VIS_CONTEXT_OBJECT_TYPE);
      genericCheckObjectVisibility(
            PSActionVisibilityContext.VIS_CONTEXT_WORKFLOWS_TYPE);

      //string types
      genericCheckObjectVisibility(
            PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE);
      
      //special cases
      checkVisibilityCheckoutStatus();
      checkVisibilityFolderSecurity();
   }
   
   private void checkVisibilityFolderSecurity()
   {
      String contextId = PSActionVisibilityContext.VIS_CONTEXT_FOLDER_SECURITY;
      PSActionVisibilityContexts vcs = new PSActionVisibilityContexts();
      PSActionVisibilityContext ctx = new PSActionVisibilityContext(contextId,
            new String[] { "Read" }, null);
      vcs.add(ctx);
      PSActionVisibilityChecker checker = new PSActionVisibilityChecker(300,
            true, vcs);
      Map<String, Object> stateMap = new HashMap<String, Object>();
      stateMap.put(contextId, new PSFolderPermissions(0));
      GenericObjectState os = new GenericObjectState(stateMap);
      assertFalse(checker.isVisible(null, os));

      stateMap.put(contextId, new PSFolderPermissions(
            PSObjectPermissions.ACCESS_READ));
      os.reset(stateMap);
      assertFalse(checker.isVisible(null, os));

      stateMap.put(contextId,
            new PSFolderPermissions(PSObjectPermissions.ACCESS_READ
                  | PSObjectPermissions.ACCESS_WRITE));
      os.reset(stateMap);
      assertTrue(checker.isVisible(null, os));
   }

   @SuppressWarnings("unchecked")
   private void checkGlobalVisibilityRoles()
   {
      PSActionVisibilityContexts vcs = new PSActionVisibilityContexts();
      PSActionVisibilityContext ctx = new PSActionVisibilityContext(
            PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE, new String[] {
                  "r1", "r2", "r3", "r4", "r5" }, null);
      vcs.add(ctx);
      PSActionVisibilityChecker checker = new PSActionVisibilityChecker(300,
            true, vcs);
      Map<String, Object> stateMap = new HashMap<String, Object>();
      stateMap.put(PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE, 
            Arrays.asList(new String[] { "r3", "r5" }));
      GenericGlobalState gs = new GenericGlobalState(stateMap);
      assertFalse(checker.isVisible(gs, (Collection) null));

      stateMap.put(PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE, 
            Arrays.asList(new String[] { "r3", "r6" }));
      gs.reset(stateMap);
      assertTrue(checker.isVisible(gs, (Collection) null));

      stateMap.put(PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE, 
            Arrays.asList(new String[] { "r7", "r8" }));
      gs.reset(stateMap);
      assertTrue(checker.isVisible(gs, (Collection) null));
   }
   
   @SuppressWarnings("unchecked")
   private void genericCheckGlobalVisibility(String contextId)
   {
      PSActionVisibilityContexts vcs = new PSActionVisibilityContexts();
      PSActionVisibilityContext ctx = new PSActionVisibilityContext(contextId,
            new String[] { "100", "200" }, null);
      vcs.add(ctx);
      PSActionVisibilityChecker checker = new PSActionVisibilityChecker(300,
            true, vcs);
      Map<String, Object> stateMap = new HashMap<String, Object>();
      stateMap.put(contextId, new Integer(200));
      GenericGlobalState gs = new GenericGlobalState(stateMap);
      assertFalse(checker.isVisible(gs, (Collection) null));

      stateMap.put(contextId, new Integer(300));
      gs.reset(stateMap);
      assertTrue(checker.isVisible(gs, (Collection) null));
   }
   
   @SuppressWarnings("unchecked")
   private void genericCheckObjectVisibility(String contextId)
   {
      PSActionVisibilityContexts vcs = new PSActionVisibilityContexts();
      PSActionVisibilityContext ctx = new PSActionVisibilityContext(contextId,
            new String[] { "100", "200" }, null);
      vcs.add(ctx);
      PSActionVisibilityChecker checker = new PSActionVisibilityChecker(300,
            true, vcs);
      Map<String, Object> stateMap = new HashMap<String, Object>();
      stateMap.put(contextId, new Integer(200));
      GenericObjectState os = new GenericObjectState(stateMap);
      assertFalse(checker.isVisible(null, os));

      stateMap.put(contextId, new Integer(300));
      os.reset(stateMap);
      assertTrue(checker.isVisible(null, os));
   }
   
   @SuppressWarnings("unchecked")
   private void checkVisibilityCheckoutStatus()
   {
      String contextId = PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS;
      PSActionVisibilityContexts vcs = new PSActionVisibilityContexts();
      PSActionVisibilityContext ctx = new PSActionVisibilityContext(contextId,
            new String[] { "myself", "checked Out" }, null);
      vcs.add(ctx);
      PSActionVisibilityChecker checker = new PSActionVisibilityChecker(300,
            true, vcs);
      Map<String, Object> stateMap = new HashMap<String, Object>();
      stateMap.put(contextId, "checked Out By Me");
      GenericObjectState os = new GenericObjectState(stateMap);
      assertFalse(checker.isVisible(null, os));

      // not sure if this is the exact string
      stateMap.put(contextId, "Checked In");
      os.reset(stateMap);
      assertTrue(checker.isVisible(null, os));
   }
   
   private class GenericObjectState extends PSActionVisibilityObjectState
   {
      /**
       * @param state Assumed not <code>null</code>. The keys are the context
       * numbers, the values are what you want returned. Use
       * <code>Integer</code> if the method returns an <code>int</code>. If no
       * value is found, the default from the base class is used.
       */
      public GenericObjectState(Map<String, Object> state)
      {
         m_state = state;
      }

      /**
       * Replace the existing state with the supplied state.
       * 
       * @param state Same as ctor.
       */
      public void reset(Map<String, Object> state)
      {
         m_state = state;
      }

      @Override
      public int getAssignmentType()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE);
         return o == null ? super.getAssignmentType() : ((Integer) o).intValue();
      }

      @Override
      public String getCheckoutStatus()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS);
         return o == null ? super.getCheckoutStatus() : o.toString();
      }

      @Override
      public int getContentTypeUuid()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE);
         return o == null ? -1 : ((Integer) o).intValue();
      }

      @Override
      public PSObjectPermissions getFolderPermissions()
      {
         Object o = m_state
               .get(PSActionVisibilityContext.VIS_CONTEXT_FOLDER_SECURITY);
         return o == null ? super.getFolderPermissions()
               : (PSObjectPermissions) o;
      }

      @Override
      public int getObjectType()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_OBJECT_TYPE);
         return o == null ? super.getObjectType() : ((Integer) o).intValue();
      }

      @Override
      public String getPublishableType()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE);
         return o == null ? super.getPublishableType() : o.toString();
      }

      @Override
      public int getWorkflowAppUuid()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_WORKFLOWS_TYPE);
         return o == null ? super.getWorkflowAppUuid() : ((Integer) o).intValue();
      }
      
      /**
       * Set in ctor, then never modified.
       */
      private Map<String, Object> m_state;
   }
   
   private class GenericGlobalState extends PSActionVisibilityGlobalState
   {
      /**
       * @param state Assumed not <code>null</code>. The keys are the context
       * numbers, the values are what you want returned. Use
       * <code>Integer</code> if the method returns an <code>int</code>. If no
       * value is found, the default from the base class is used.
       */
      public GenericGlobalState(Map<String, Object> state)
      {
         m_state = state;
      }

      /**
       * Replace the existing state with the supplied state.
       * 
       * @param state Same as ctor.
       */
      public void reset(Map<String, Object> state)
      {
         m_state = state;
      }
      
      @Override
      public String getClientContext()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_CLIENT_CONTEXT);
         return o == null ? super.getClientContext() : o.toString();
      }

      @Override
      public int getCommunityUuid()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_COMMUNITY);
         return o == null ? super.getCommunityUuid() : ((Integer) o).intValue();
      }

      @Override
      public String getLocale()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_LOCALES_TYPE);
         return o == null ? super.getLocale() : o.toString();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Collection<String> getRoles()
      {
         Object o = m_state.get(
               PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE);
         return o == null ? super.getRoles() : (Collection<String>) o;
      }
      
      /**
       * Set in ctor, then never modified.
       */
      private Map<String, Object> m_state;
   }
   
}
