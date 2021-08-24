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
package com.percussion.rx.ui.jsf.beans;

import com.percussion.server.PSRequest;
import com.percussion.services.utils.jspel.PSRoleUtilities;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;

import javax.faces.model.DataModel;
import java.util.ArrayList;
import java.util.List;

/**
 * This tree model contains and tracks the top level navigation tabs for
 * Rhythmyx.
 * 
 * @author dougrand
 * 
 */
public class PSTopNavigation extends DataModel
{
   /**
    * Describes individual tabs. A tab is determined to be enabled or disabled
    * on creation based on roles. No tabs will be created if the user doesn't
    * contain at least one of the necessary roles to see at least one tab.
    * 
    * @author dougrand
    * 
    */
   public class Tab
   {
      /**
       * The url that this tab should navigate to, will be <code>null</code>
       * if this tab implements an action.
       */
      private String mi_url;

      /**
       * The table for this tab, never <code>null</code> or empty.
       */
      private String mi_label;

      /**
       * If this tab is enabled, this value will be <code>true</code>.
       */
      private boolean mi_enabled;

      /**
       * A calculated id for this component, never <code>null</code> or empty
       * after ctor.
       */
      private String mi_id;

      /**
       * If the user is viewing a page on this path then this tab is active.
       */
      private String mi_matchPath;

      /**
       * Ctor.
       * 
       * @param label the label for this tab, never <code>null</code> or
       *            empty.
       * @param url the url, never <code>null</code> or empty.
       * @param matchPath the path to match, <code>null</code> means that this
       *            is the default tab to show if nothing else matches.
       */
      public Tab(String label, String url, String matchPath) {
         if (StringUtils.isBlank(label))
         {
            throw new IllegalArgumentException(
                  "label may not be null or empty");
         }
         if (StringUtils.isBlank(url))
         {
            throw new IllegalArgumentException("url may not be null or empty");
         }
         mi_label = label;
         mi_url = url;
         mi_matchPath = matchPath;
         mi_enabled = true;
      }

      /**
       * Ctor.
       * 
       * @param label the label for this tab, never <code>null</code> or
       *            empty.
       * @param url the url, never <code>null</code> or empty.
       * @param matchPath the path to match, <code>null</code> means that this
       *            is the default tab to show if nothing else matches.
       * @param enabled <code>true</code> if this tab is enabled.
       */
      public Tab(String label, String url, String matchPath, boolean enabled) {
         this(label, url, matchPath);
         mi_enabled = enabled;
      }

      /**
       * Ctor.
       * 
       * @param label the label for this tab, never <code>null</code> or
       *            empty.
       * @param url the url, never <code>null</code> or empty.
       * @param matchPath the path to match, <code>null</code> means that this
       *            is the default tab to show if nothing else matches.
       * @param roles a comma separated list of roles, could be
       *            <code>null</code>.
       */
      public Tab(String label, String url, String matchPath, String roles)
      {
         this(label, url, matchPath);
         mi_id = "rx_tab_" + label.replace(" ", "_");
         if (StringUtils.isBlank(roles))
            mi_enabled = true;
         else
         {
            mi_enabled = hasCompBannerRoles(roles);
         }
      }

      /**
       * @return the url
       */
      public String getUrl()
      {
         if (mi_enabled)
            return mi_url;
         else
            return "#";
      }

      /**
       * @return the label
       */
      public String getLabel()
      {
         return mi_label;
      }

      /**
       * @return the enabled
       */
      public boolean getEnabled()
      {
         return mi_enabled;
      }

      /**
       * The style class to use when rendering this tab, calculated from the
       * enabled state of the tab and the containing classes selection state.
       * 
       * @return the class to use when rendering, never <code>null</code> or
       *         empty.
       */
      public String getStyle()
      {
         if (mi_enabled)
         {
            if (isSelected())
            {
               return "rx-tab-link-on";
            }
            else
            {
               return "rx-tab-link-off";
            }
         }
         else
         {
            return "rx-tab-link-disabled";
         }
      }

      /**
       * @return <code>true</code> if this is the selected tab.
       */
      public boolean isSelected()
      {
         return m_tabs.indexOf(this) == m_selected;
      }

      /**
       * Get the id to be used with this tab.
       * 
       * @return the id, never <code>null</code> or empty.
       */
      public String getId()
      {
         return mi_id;
      }

      /**
       * @return the matchPath
       */
      public String getMatchPath()
      {
         return mi_matchPath;
      }
   }

   /**
    * Tabs for this model, never <code>null</code> after ctor.
    */
   List<Tab> m_tabs;

   /**
    * Path, defaults to none. Set by the jsps.
    */
   private String m_path = null;

   /**
    * The selected tab
    */
   int m_selected = 0;

   /**
    * Current index row, <code>-1</code> indicates no row selected.
    */
   private int m_index = -1;

   /**
    * Ctor.
    */
   public PSTopNavigation() {
      m_tabs = new ArrayList<>();

      m_tabs.add(new Tab("Content", "/Rhythmyx/sys_cx/mainpage.html", null));
      m_tabs.add(new Tab("Publishing Design",
            "/ui/publishing",
            "/ui/publishing", PUB_DESIGN_ROLE));
      m_tabs.add(new Tab("Publishing Runtime",
            "/ui/pubruntime", "/ui/pubruntime",
            PUB_RUNTIME_ROLE));
      m_tabs.add(new Tab(
            "Workflow",
            "/Rhythmyx/sys_wfEditor/welcome.html?"
            + "sys_componentname=wf_all&amp;sys_pagename=wf_all",
            "wf", WORKFLOW_ROLE));
      m_tabs.add(new Tab("Admin", "/ui/admin", "/ui/admin",
            ADMIN_ROLE));
   }

   /**
    * Determines if the current user of the request has one of the roles that
    * are defined by the given properties of the {@link #COMP_BANNER} component.
    * 
    * @param roles the property names defined in {@link #COMP_BANNER} component,
    * assumed not <code>null</code>, it may be delimited by comma, ','.
    * 
    * @return <code>true</code> if the current user has one of the roles 
    * defined by the component properties; otherwise return <code>false</code>.
    */
   private static boolean hasCompBannerRoles(String roles)
   {
      String[] rolearr = roles.split(",");
      for (String role : rolearr)
      {
         Boolean b = PSRoleUtilities.hasComponentRole(COMP_BANNER, role.trim());
         if (Boolean.TRUE.equals(b))
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Determines if the current user of the request has one of the roles that
    * are defined by the {@link #PUB_DESIGN_ROLE} properties of the 
    * {@link #COMP_BANNER} component.
    * 
    * @return <code>true</code> if the current user has one of the roles 
    * defined by the component properties; otherwise return <code>false</code>.
    */
   public static boolean hasPubDesignCompRoles()
   {
      return hasCompBannerRoles(PUB_DESIGN_ROLE);
   }
   
   /**
    * Determines if the current user of the request has one of the roles that
    * are defined by the {@link #PUB_RUNTIME_ROLE} properties of the 
    * {@link #COMP_BANNER} component.
    * 
    * @return <code>true</code> if the current user has one of the roles 
    * defined by the component properties; otherwise return <code>false</code>.
    */
   public static boolean hasPubRuntimeCompRoles()
   {
      return hasCompBannerRoles(PUB_RUNTIME_ROLE);
   }
   
   /**
    * Determines if the current user of the request has one of the roles that
    * are defined by the {@link #ADMIN_ROLE} properties of the 
    * {@link #COMP_BANNER} component.
    * 
    * @return <code>true</code> if the current user has one of the roles 
    * defined by the component properties; otherwise return <code>false</code>.
    */
   public static boolean hasAdminCompRoles()
   {
      return hasCompBannerRoles(ADMIN_ROLE);
   }
   
   /**
    * The name of the banner component.
    */
    private static final String COMP_BANNER = "cmp_banner";
   
   /**
    * The property names in {@link #COMP_BANNER} component for defining roles to 
    * control the visibility and accessibility of the "Publishing Design" tab.
    */
    private static final String PUB_DESIGN_ROLE = "pubrole,PubDesignRole";
   
   /**
    * The property names in {@link #COMP_BANNER} component for defining roles to 
    * control the visibility and accessibility of the "Publishing Runtime" tab.
    */
    private static final String PUB_RUNTIME_ROLE = "pubrole,PubRuntimeRole";
   
   /**
    * The property names in {@link #COMP_BANNER} component for defining roles to 
    * control the visibility and accessibility of the "Admin" tab.
    */
    private static final String ADMIN_ROLE = "sysrole";
   
   /**
    * The property names in {@link #COMP_BANNER} component for defining roles to 
    * control the visibility of the "Workflow" tab.
    */
    private static final String WORKFLOW_ROLE = "wfrole";
   
   /**
    * @return the current set path, may be <code>null</code>.
    */
   public Object getPath()
   {
      return m_path;
   }

   @Override
   public int getRowCount()
   {
      // If only one is enabled we return 0
      int count = 0;
      for(Tab t : m_tabs)
      {
         if (t.getEnabled()) count++;
      }
      if (count > 1)
         return m_tabs.size();
      else
         return 0;
   }

   @Override
   public Object getRowData()
   {
      if (isRowAvailable())
         return m_tabs.get(m_index);
      else
         return null;
   }

   @Override
   public int getRowIndex()
   {
      return m_index;
   }

   @Override
   public Object getWrappedData()
   {
      throw new UnsupportedOperationException("Not supported");
   }

   @Override
   public boolean isRowAvailable()
   {
      return m_index > -1 && m_index < m_tabs.size();
   }

   @Override
   public void setRowIndex(int arg0)
   {
      m_index = arg0;
   }

   @Override
   public void setWrappedData(Object arg0)
   {
      throw new UnsupportedOperationException("Not supported");
   }

   /**
    * Set the path.
    * 
    * @param opath the path, never <code>null</code> or empty.
    */
   public void setPath(Object opath)
   {
      if (!(opath instanceof String))
      {
         throw new IllegalArgumentException("path must be a string");
      }
      String path = (String) opath;
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path may not be null or empty");
      }
      if (path.equals("/ui/banner.jsp"))
      {
         // Invoked from xsl, use pagename instead
         PSRequest req = (PSRequest) PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         m_path = req.getServletRequest().getParameter("sys_pagename");
      }
      else
      {
         m_path =  path;
      }

      // Walk through the tabs. If one matches then set selected, otherwise
      // set selected to 0
      m_selected = 0;
      for (Tab t : m_tabs)
      {
         if (t.getMatchPath() != null && m_path.startsWith(t.getMatchPath()))
         {
            return;
         }
         m_selected++;
      }
      m_selected = 0;
   }

}
