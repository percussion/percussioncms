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

import com.google.gwt.user.client.Window;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.MouseUpEvent;
import com.smartgwt.client.widgets.events.MouseUpHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates visibility tab.
 * 
 * @author bjoginipally
 * 
 */
public class PSVisibilityTab
{
   /**
    * Creates the tab and places controls in it and returns the tab.
    * 
    * @return Tab, never <code>null</code>.
    */
   public Tab getTab()
   {
      Tab visibilityTab = new Tab(PkgMgtUI.getMessages().visibilityLabel());
      Canvas visCanvas = new Canvas();

      HLayout btnLayout = createButtonsPanel();

      HLayout pcLayout = createListsPanel();
      addLeftGridClickListener();
      VLayout mainLayout = new VLayout();
      mainLayout.setWidth100();
      mainLayout.setHeight("99%");
      mainLayout.setTop(2);
      mainLayout.setMembersMargin(6);
      mainLayout.addMember(btnLayout);
      mainLayout.addMember(pcLayout);

      visCanvas.addChild(mainLayout);
      visibilityTab.setPane(visCanvas);
      return visibilityTab;
   }

   /**
    * Creates the list grid panel
    * 
    * @return HLayout with the left and right grids placed in it.
    */
   private HLayout createListsPanel()
   {
      m_leftGrid.setDataSource(createShowByPkgDataSource());
      setGridProperties();
      HLayout pcLayout = new HLayout();
      pcLayout.setWidth100();
      pcLayout.setHeight100();
      pcLayout.addMember(m_leftGrid);
      pcLayout.setMembersMargin(10);
      pcLayout.addMember(m_rightGrid);
      return pcLayout;
   }

   /**
    * Refreshes the data in the grids.
    */
   public void refreshTab()
   {
     
      m_leftGrid.invalidateCache();
      m_leftGrid.fetchData();
      m_leftGrid.redraw();
      // Clear out right grid
      m_rightGrid.setData(new ListGridRecord[0]);
      m_rightGrid.refreshFields();
      
   }

   /**
    * Adds the data source to the left grid.
    */
   private RestDataSource createShowByPkgDataSource()
   {
      // Set the RestDataSource
      RestDataSource restDS = new RestDataSource();
      restDS.setFetchDataURL(GET_PACKAGE_COMMUNITIES_URL);
      restDS.setXmlRecordXPath("/Packages");
      restDS.setRecordXPath("/Packages");
      restDS.setFields(createShowByPkgDsFields());
      return restDS;
   }

   /**
    * Adds the data source to the left grid.
    */
   private RestDataSource createShowByComDataSource()
   {
      // Set the RestDataSource
      RestDataSource restDS = new RestDataSource();
      restDS.setFetchDataURL(GET_COMMUNITY_PACKAGES_URL);
      restDS.setXmlRecordXPath("/Communities");
      restDS.setRecordXPath("/Communities");
      restDS.setFields(createShowByCommDsFields());
      return restDS;
   }

   /**
    * Adds the left grid click listener.
    * 
    */
   private void addLeftGridClickListener()
   {
      m_leftGrid.addRecordClickHandler(new RecordClickHandler()
      {
         public void onRecordClick(RecordClickEvent event)
         {
            handleLeftGridClick(event.getRecord());
         }
      });
   }

   /**
    * Function to handles the left grid click event.
    * 
    * @param event assumed not <code>null</code>.
    */
   private void handleLeftGridClick(Record record)
   {
      String temp1 = ELEMENT_PACKAGES;
      String temp2 = RECORD_ATTR_PACKAGE;
      if (m_isShowByPkgs)
      {
         temp1 = ELEM_COMMUNITIES;
         temp2 = GRID_ATTR_COMMUNITY;
      }
      String commStr = record.getAttribute(temp1);
      if (commStr != null)
      {
         String[] comms = commStr.split(NAME_SEPARATOR);
         ListGridRecord[] dsArray = new ListGridRecord[comms.length];
         for (int i = 0; i < comms.length; i++)
         {
            ListGridRecord rec = new ListGridRecord();
            rec.setAttribute(temp2, comms[i]);
            dsArray[i] = rec;
         }
         m_rightGrid.setData(dsArray);
         m_rightGrid.setSortField(temp2);
         m_rightGrid.refreshFields();
      }
   }

   /**
    * Sets the grid properties.
    */
   private void setGridProperties()
   {
      m_leftGrid.setWidth("30%");
      m_leftGrid.setAlternateRecordStyles(true);
      m_leftGrid.setAutoFetchData(true);
      m_leftGrid.setFields(createGridField(ELEM_PACKAGE, PkgMgtUI
            .getMessages().packagesLabel()));
      m_leftGrid.setCanGroupBy(false);
      m_leftGrid.setSortField(ELEM_PACKAGE);
      m_leftGrid.setSelectionType(SelectionStyle.SINGLE);
      m_leftGrid.setLeaveScrollbarGap(false);
      m_leftGrid.draw();

      m_rightGrid.setWidth("68%");
      m_rightGrid.setAlternateRecordStyles(true);
      m_rightGrid.setAutoFetchData(true);
      m_rightGrid.setFields(createGridField(GRID_ATTR_COMMUNITY, PkgMgtUI
            .getMessages().associatedCommunities()));
      m_rightGrid.setCanGroupBy(false);
      m_rightGrid.setSortField(GRID_ATTR_COMMUNITY);
      m_rightGrid.setLeaveScrollbarGap(false);
      m_rightGrid.draw();
   }

   /**
    * Creates the buttons panel and adds the click listeners.
    * 
    * @return The horizontal stack with the buttons placed in it.
    */
   private HLayout createButtonsPanel()
   {
      m_showByBtn.setSelected(true);
      m_showByBtn.setAutoFit(true);
      m_showByBtn.setWidth(150);
      m_showByBtn.addMouseUpHandler(new MouseUpHandler()
      {
         public void onMouseUp(MouseUpEvent event)
         {
            m_isShowByPkgs = !m_isShowByPkgs;
            String btnTitle = m_isShowByPkgs ? PkgMgtUI.getMessages()
                  .showByCommunities() : PkgMgtUI.getMessages()
                  .showByPackages();
            m_showByBtn.setTitle(btnTitle);
            refreshGridPanel();
         }
      });
      m_editAssociationsBtn.setWidth(150);
      m_editAssociationsBtn.setAutoFit(true);
      m_editAssociationsBtn.addMouseUpHandler(new MouseUpHandler()
      {
         public void onMouseUp(MouseUpEvent event)
         {
            editAssociationsClickHandler();
         }
      });
      HLayout btnLayout = new HLayout();
      btnLayout.setMembersMargin(10);
      btnLayout.setHeight(20);
      btnLayout.addMember(m_showByBtn);
      btnLayout.addMember(m_editAssociationsBtn);
      return btnLayout;
   }

   /**
    * Function to handle the edit communities button click.
    */
   private void editAssociationsClickHandler()
   {
      String temp1 = ELEM_ALLPACKAGES;
      String temp2 = ELEMENT_PACKAGES;
      String temp3 = ELEM_COMMUNITY;
      if (m_isShowByPkgs)
      {
         temp1 = ELEM_ALLCOMMUNITIES;
         temp2 = ELEM_COMMUNITIES;
         temp3 = ELEM_PACKAGE;
      }
      ListGridRecord rec = m_leftGrid.getSelectedRecord();
      if (rec == null)
      {
         Window.alert(PkgMgtUI.getMessages().selectRecordMessage());
         return;
      }
      String[] av = rec.getAttribute(temp1).split(NAME_SEPARATOR);
      String[] sel = rec.getAttribute(temp2).split(NAME_SEPARATOR);
      av = removeAll(av, sel);
      String packageName = rec.getAttribute(temp3);
      openAssociationSelectionDlg(packageName, av, sel);
   }

   /**
    * Refreshes the grids.
    * 
    * @param leftGridField filed name of left grid, assumed not
    * <code>null</code>.
    * 
    * @param rightGridField field name of right grid, assumed not
    * <code>null</code>.
    */
   private void refreshGridPanel()
   {
      String leftGridSortField;
      String rightGridSortField;
      if (m_isShowByPkgs)
      {
         m_leftGrid.setDataSource(createShowByPkgDataSource());
         m_leftGrid.setFields(createGridField(ELEM_PACKAGE, PkgMgtUI
               .getMessages().packagesLabel()));
         leftGridSortField = ELEM_PACKAGE;
         m_rightGrid.setFields(createGridField(GRID_ATTR_COMMUNITY, PkgMgtUI
               .getMessages().associatedCommunities()));
         rightGridSortField = GRID_ATTR_COMMUNITY;
      }
      else
      {
         m_leftGrid.setDataSource(createShowByComDataSource());
         m_leftGrid.setFields(createGridField(ELEM_COMMUNITY, PkgMgtUI
               .getMessages().communitiesLabel()));
         leftGridSortField = ELEM_COMMUNITY;
         m_rightGrid.setFields(createGridField(RECORD_ATTR_PACKAGE, PkgMgtUI
               .getMessages().associatedPackages()));
         rightGridSortField = RECORD_ATTR_PACKAGE;
      }
      m_leftGrid.fetchData();
      m_leftGrid.setSortField(leftGridSortField);
      m_leftGrid.refreshFields();
      m_leftGrid.redraw();
      m_rightGrid.setSortField(rightGridSortField);
      m_rightGrid.refreshFields();
      m_rightGrid.redraw();
   }

   /**
    * Helper method to create the left list grid fields.
    * 
    * @return list grid field array, never <code>null</code>.
    */
   private ListGridField[] createGridField(String name, String dName)
   {
      ListGridField[] fields = new ListGridField[1];
      fields[0] = new ListGridField(name, dName);
      return fields;
   }

   /**
    * Helper method to create the DataSource fields.
    * 
    * @return data source field array, never <code>null</code>.
    */
   private DataSourceField[] createShowByPkgDsFields()
   {
      DataSourceField[] fields = new DataSourceField[3];
      fields[0] = new DataSourceTextField(ELEM_PACKAGES, PkgMgtUI
            .getMessages().nameLabel());
      fields[1] = new DataSourceTextField(ELEM_COMMUNITIES);
      fields[2] = new DataSourceTextField(ELEM_ALLCOMMUNITIES);
      fields[2].setValueXPath("//allcommunities");
      return fields;

   }

   /**
    * Helper method to create the DataSource fields.
    * 
    * @return datasource field array, never <code>null</code>.
    */
   private DataSourceField[] createShowByCommDsFields()
   {
      DataSourceField[] fields = new DataSourceField[3];
      fields[0] = new DataSourceTextField(ELEM_COMMUNITIES, PkgMgtUI
            .getMessages().nameLabel());
      fields[1] = new DataSourceTextField(ELEMENT_PACKAGES);
      fields[2] = new DataSourceTextField(ELEM_ALLPACKAGES);
      fields[2].setValueXPath("//allpackages");
      return fields;

   }

   /**
    * Opens the community selection dialog and processes the selected items.
    * 
    * @param ownerName The name of the association owner, assumed not
    * <code>null</code>.
    * @param available String array of available options.
    * @param selected String array of selected options.
    */
   private void openAssociationSelectionDlg(final String ownerName,
         String[] available, String[] selected)
   {
      if (m_selectionDlg == null)
      {
         m_selectionDlg = new PSCommPkgSelectionDialog();
         m_selectionDlg.setHeight(350);
         m_selectionDlg.setWidth(480);
         m_selectionDlg.setID("SelectionDlg");
         m_selectionDlg.setIsModal(true);
      }
      DSCallback okCallBack = new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            saveAssociations(ownerName, m_selectionDlg.getSelectedData());
         }
      };
      if (m_isShowByPkgs)
      {
         m_selectionDlg.setTitle(PkgMgtUI.getMessages()
               .editCommunityAssociations());
         m_selectionDlg.open(available, selected, okCallBack);
      }
      else
      {
         m_selectionDlg.setTitle(PkgMgtUI.getMessages()
               .editPackageAssociations());
         m_selectionDlg.open(available, selected, okCallBack);
      }
   }

   /**
    * Makes a rest request to the server and refreshes the grid data if it
    * succeeds otherwise displays the error message to the user.
    * 
    * @param ownerName the name of the owner of the association, it is assumed
    * to be the name of the package from packages grid and name of the community
    * from communities grid, assumed not <code>null</code>.
    * @param assocData the {@link PkgMgtUI#NAME_SEPARATOR} separated list of
    * either package names or community names based on the owner.
    */
   private void saveAssociations(final String ownerName, final String assocData)
   {
      String actionUrl = "";
      if (m_isShowByPkgs)
      {
         actionUrl = UPDATE_PACKAGE_COMMUNITIES_URL
               + "packageName=" + ownerName + "&selectedComms=" + assocData;
      }
      else
      {
         actionUrl = UPDATE_COMMUNITY_PACKAGES_URL
               + "communityName=" + ownerName + "&selectedPkgs=" + assocData;
      }

      RestDataSource saveDs = new RestDataSource();
      DataSourceField[] fields = new DataSourceField[2];
      DataSourceTextField type = new DataSourceTextField("type");
      fields[0] = type;
      DataSourceTextField message = new DataSourceTextField("message");
      fields[1] = message;
      saveDs.setFields(fields);
      saveDs.setXmlRecordXPath("/Response");
      saveDs.setRecordXPath("/Response");
      saveDs.setUpdateDataURL(actionUrl);
      saveDs.updateData(new ListGridRecord(), new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            Record[] records = response.getData();
            String respType = records[0].getAttribute("type");
            if (respType.equals("FAILURE"))
            {
               String respMsg = records[0].getAttribute("message");
               if (respMsg.trim().length() < 1)
                  respMsg = "Unexpected error occurred while saving the "
                        + "associations.";
               SC.say(respMsg);
            }
            else
            {
               updateGrids(assocData);
            }
            m_selectionDlg.hide();
         }

      });
   }

   /**
    * Updates the grids with updated data. Gets the selected left grid row and
    * updates its attributes and then updates the right grid data with the
    * supplied associations data.
    * 
    * @param assocData The {@value #NAME_SEPARATOR} string of associations,
    * assumed not <code>null</code>..
    */
   private void updateGrids(String assocData)
   {
      ListGridRecord[] recs = m_leftGrid.getSelection();
      String atrName1 = m_isShowByPkgs ? ELEM_COMMUNITIES : ELEMENT_PACKAGES;
      recs[0].setAttribute(atrName1, assocData);

      String atrName2 = m_isShowByPkgs ? GRID_ATTR_COMMUNITY
            : RECORD_ATTR_PACKAGE;
      String[] comms = assocData.split(NAME_SEPARATOR);
      ListGridRecord[] dsArray = new ListGridRecord[comms.length];
      for (int i = 0; i < comms.length; i++)
      {
         ListGridRecord rec = new ListGridRecord();
         rec.setAttribute(atrName2, comms[i]);
         dsArray[i] = rec;
      }
      m_rightGrid.setData(dsArray);
      m_rightGrid.setSortField(atrName2);
      m_rightGrid.refreshFields();
      m_rightGrid.redraw();
   }

   /**
    * Helper function to remove the contents of remove array from the contents
    * of from array.
    * 
    * @param from assumed not <code>null</code>.
    * @param remove assumed not <code>null</code>.
    * @return The removed string array never <code>null</code>, may be empty.
    */
   private String[] removeAll(String[] from, String[] remove)
   {
      List<String> l1 = new ArrayList<String>();
      for (int i = 0; i < from.length; i++)
      {
         l1.add(from[i]);
      }
      List<String> l2 = new ArrayList<String>();
      for (int i = 0; i < from.length; i++)
      {
         l2.add(remove[i]);
      }
      l1.removeAll(l2);
      return l1.toArray(new String[l1.size()]);
   }

   // Controls used in this class.
   private Button m_showByBtn = new Button(PkgMgtUI.getMessages()
         .showByCommunities());

   private Button m_editAssociationsBtn = new Button(PkgMgtUI.getMessages()
         .editCommunities());

   private ListGrid m_leftGrid = new ListGrid();

   private ListGrid m_rightGrid = new ListGrid();

   private PSCommPkgSelectionDialog m_selectionDlg = null;

   private boolean m_isShowByPkgs = true;

   // Constants for element names in the xml.
   private static final String ELEM_PACKAGES = "Packages";

   private static final String NAME_SEPARATOR = ",";

   private static final String ELEM_ALLCOMMUNITIES = "allcommunities";

   private static final String ELEM_COMMUNITY = "community";

   private static final String ELEM_ALLPACKAGES = "allpackages";

   private static final String ELEM_PACKAGE = "package";

   private static final String GRID_ATTR_COMMUNITY = "Community";

   private static final String RECORD_ATTR_PACKAGE = "Package";

   private static final String ELEM_COMMUNITIES = "communities";

   private static final String ELEMENT_PACKAGES = "packages";
   
   //Constants for urls
   private static final String UPDATE_COMMUNITY_PACKAGES_URL = 
      PkgMgtUI.ms_serviceRoot + "updateCommunityPackages.xml?";

   private static final String UPDATE_PACKAGE_COMMUNITIES_URL = 
      PkgMgtUI.ms_serviceRoot + "updatePackageCommunities.xml?";

   private static final String GET_COMMUNITY_PACKAGES_URL = 
      PkgMgtUI.ms_serviceRoot + "communityPackages.xml";

   private static final String GET_PACKAGE_COMMUNITIES_URL = 
      PkgMgtUI.ms_serviceRoot + "packageCommunities.xml";


}
