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
package com.percussion.gwt.pkgmgtui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.data.*;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.*;
import com.smartgwt.client.widgets.tab.TabSet;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Percussion CM Package Management User Interface. Entry point classes
 * define <code>onModuleLoad()</code>.
 */
public class PkgMgtUI implements EntryPoint
{

   /**
    * This is the entry point method.
    */
   public void onModuleLoad()
   {
      // Load the messages interface
      Window.setTitle(getMessages().packageMgtTitle());

      SectionStack sectionStack = new SectionStack();
      sectionStack.setWidth100();
      sectionStack.setHeight100();
      sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
      sectionStack.setAnimateSections(true);
      sectionStack.setOverflow(Overflow.HIDDEN);

      HLayout bnrLayout = new HLayout();
      bnrLayout.setHeight(66);

      VStack vStack = new VStack();

      HStack tStack = new HStack();
      tStack.setHeight(45);

      HStack bStack = createButtonStack();
      vStack.addMember(tStack);
      vStack.addMember(bStack);

      HStack lStack = new HStack();
      lStack.addMember(new Img("headerLogo.png", 44, 33));

      bnrLayout.addMember(lStack);
      bnrLayout.addMember(vStack);
      SectionStackSection bannerSection = new SectionStackSection();
      bannerSection.addItem(bnrLayout);
      bannerSection.setExpanded(true);
      bannerSection.setShowHeader(false);

      SectionStackSection bodySection = new SectionStackSection();
      bodySection.setExpanded(true);
      bodySection.setShowHeader(false);
      TabSet mainTabSet = new TabSet();
      m_packagesTab = new PSPackagesTab();
      mainTabSet.addTab(m_packagesTab.getTab());

      m_visibilityTab = new PSVisibilityTab();
      mainTabSet.addTab(m_visibilityTab.getTab());

      HLayout bodyLayout = new HLayout();
      bodyLayout.setMembers(mainTabSet);
      bodySection.setItems(bodyLayout);
      sectionStack.setPadding(4);
      sectionStack.setMembersMargin(4);
      sectionStack.setSections(bannerSection, bodySection);
      sectionStack.draw();
      initServerTimeout();
   }

   /**
    * Creates the logout, refresh and help buttons and adds the on click
    * handlers to it.
    * 
    * @return Horizontal stack consisting of buttons, never <code>null</code>.
    */
   private HStack createButtonStack()
   {
      HStack bStack = new HStack();
      bStack.setHeight(16);

      Img logout = new Img("logout.gif", 62, 17);
      logout.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            String redUrl = Window.Location.getHref();
            if(redUrl.indexOf("?")!=-1) 
               redUrl = redUrl.substring(0,redUrl.indexOf("?")) + "?";
            else
               redUrl += "?";
            Map<String, List<String>> pmap = Window.Location.getParameterMap();
            for (Entry<String, List<String>> entry : pmap.entrySet())
            {
               if (!entry.getKey().equals("tsid"))
                  redUrl += entry.getKey() + "=" + entry.getValue() + "&";
            }
            redUrl += "tsid=" + (new Date()).getTime();
            redUrl = URL.encode(redUrl);
            Window.Location.replace(LOGOUT_URL + redUrl);
         }
      });
      logout.setTooltip("Logout");
      Img refresh = new Img("icons/16/refresh16.png", 16, 16);
      refresh.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            refreshTabs();
         }
      });
      refresh.setTooltip("Refresh");
      Img help = new Img("icons/16/help16.png", 16, 16);
      help.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            Window.open(HELP_URL, "PkgMgmtUI", "");
         }
      });
      help.setTooltip("Help");
      Img spacer = new Img("spacer.png", 16, 16);
      bStack.addMember(logout);
      bStack.addMember(refresh);
      bStack.addMember(help);
      bStack.addMember(spacer);
      bStack.setMembersMargin(5);
      bStack.setAlign(Alignment.RIGHT);
      return bStack;
   }

   /**
    * Calls the refresh tab method on all tabs.
    */
   private void refreshTabs()
   {
      m_packagesTab.refreshTab();
      m_visibilityTab.refreshTab();
   }

   /**
    * A helper method to keep the session alive. Uses a timer with a scheduled
    * repeating of session time out time minus 100 milli seconds.
    */
   private void keepAlive()
   {
      Timer someTimer = new Timer()
      {
         @Override
         public void run()
         {
            SimpleDataSource restDs = new SimpleDataSource();
            restDs.setDataURL(ms_serviceRoot + "serverTimeout.xml");
            restDs.setAttribute("showPrompt", false, true);
            restDs.fetchData();
         }
      };
      someTimer.scheduleRepeating(m_sessionTimeout * 1000 - 100);
   }

   /**
    * Helper method to make a request to the server and get the session timeout
    * 
    */
   private void initServerTimeout()
   {
      RestDataSource restDs = new RestDataSource();
      restDs.setFetchDataURL(ms_serviceRoot + "serverTimeout.xml");
      DataSourceField[] fields = new DataSourceField[2];
      DataSourceTextField type = new DataSourceTextField("type");
      fields[0] = type;
      DataSourceTextField message = new DataSourceTextField("message");
      fields[1] = message;
      restDs.setFields(fields);
      restDs.setXmlRecordXPath("/Response");
      restDs.setRecordXPath("/Response");
      restDs.fetchData(null, new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            Record[] records = response.getData();
            String respType = records[0].getAttribute("type");
            if (!respType.equals("FAILURE"))
            {
               String respMsg = records[0].getAttribute("message");
               try
               {
                  m_sessionTimeout = Integer.parseInt(respMsg);
               }
               catch (Exception e)
               {
                  // Ignore
               }
            }
            keepAlive();
         }
      });
   }

   /**
    * A simple datasource class that overrides the <code>DataSource</code> to
    * override the setAttribute method.
    */
   class SimpleDataSource extends DataSource
   {
      @Override
      protected void setAttribute(String attribute, boolean value,
            boolean allowPostCreate)
      {
         super.setAttribute(attribute, value, allowPostCreate);
      }
   }

   /**
    * Creates the messages class and returns if it is <code>null</code>.
    * 
    * @return The messages never <code>null</code>.
    */
   public static IPSMessages getMessages()
   {
      if (m_messages == null)
         m_messages = GWT.create(IPSMessages.class);
      return m_messages;
   }

   /**
    * The messages interface. Initialized in {@link #onModuleLoad()}. Never
    * <code>null</code> after that.
    */
   private static volatile IPSMessages m_messages = null;

   /**
    * Packages tab initialized in the {@link #onModuleLoad()} never
    * <code>null</code>, after that.
    */
   private PSPackagesTab m_packagesTab;

   /**
    * Visibility tab initialized in the {@link #onModuleLoad()} never
    * <code>null</code>, after that.
    */
   private PSVisibilityTab m_visibilityTab;
   
   /**
    * Constant for the rest service call root.
    */
   public static final String ms_serviceRoot = "/services/pkgmgt/";

   /**
    * Constant for name separator
    */
   public static final String NAME_SEPARATOR = ",";

   /**
    * The logout url, the redirect url is filled with the current window
    * location during the onclick of logout button.
    */
   private static final String LOGOUT_URL = "/logout?sys_redirecturl=";

   /**
    * Help url for the package management UI.
    */
   private static final String HELP_URL = "/Docs/Percussion_Package_Manager_Help/index.htm";

   /**
    * Server session timeout in seconds
    */
   private int m_sessionTimeout = 7200;
}
