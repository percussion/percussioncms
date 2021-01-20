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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.percussion.gwt.pkgmgtui.client.controls.PSStatusDialog;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.fields.DataSourceImageField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuButton;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemIfFunction;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tab.Tab;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates the packages tab.
 * 
 * @author bjoginipally
 * 
 */
public class PSPackagesTab
{

   /**
    * Creates the packages tab. Gets the packages info through rest and creates
    * a table to hold that data and returns the tab.
    * 
    * @return Tab never <code>null</code>.
    */
   public Tab getTab()
   {
      Tab pkgsTab = new Tab(PkgMgtUI.getMessages().packagesLabel());
      Canvas pkgsCanvas = new Canvas();

      // Adds buttons panel
      HLayout buttonsPanel = createButtonsPanel();
      HLayout gridPanel = createGridPanel();
      VLayout mainLayout = new VLayout();
      mainLayout.setTop(2);
      mainLayout.setWidth100();
      mainLayout.setHeight("99%");
      mainLayout.setMembersMargin(6);
      mainLayout.addMember(buttonsPanel);
      mainLayout.addMember(gridPanel);
      pkgsCanvas.addChild(mainLayout);
      pkgsTab.setPane(pkgsCanvas);

      return pkgsTab;
   }

   /**
    * Creates the grid panel and adds the data source to it, sets the grid
    * properties.
    */
   private HLayout createGridPanel()
   {
      // Create the table
      m_packagesGrid.setWidth100();
      m_packagesGrid.setHeight100();
      m_packagesGrid.setHeaderHeight(30);
      m_packagesGrid.setAlternateRecordStyles(true);
      m_packagesGrid.setFields(createFields());
      m_packagesGrid.setSortField(2);
      m_packagesGrid.setDataPageSize(50);
      m_packagesGrid.setCanGroupBy(false);
      m_packagesGrid.setLeaveScrollbarGap(false);
      m_packagesGrid.setSelectionType(SelectionStyle.SIMPLE);
      m_packagesGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
      m_packagesGrid.setHoverWidth(100);
      HLayout gridLayout = new HLayout();
      gridLayout.setWidth100();
      gridLayout.setHeight100();
      gridLayout.setID("PSPackagesTab_gridPanel");
      gridLayout.addMember(m_packagesGrid);
      setGridData();
      return gridLayout;
   }

   /**
    * Helper method to set the packages grid data. Calls
    * {@link #setGridData(int[])} passing <code>null</code> for selected
    * indexes parameter.
    * 
    */
   private void setGridData()
   {
      setGridData(null);
   }

   /**
    * Sets the grid data and selects the records with the supplied indexes.
    * 
    * @param selectedIndexes int array of record indexes that needs to be
    * selected. If <code>null</code> no records are selected.
    */
   private void setGridData(final int[] selectedIndexes)
   {
      // Set the RestDataSource
      RestDataSource restDS = new RestDataSource();
      restDS.setFetchDataURL(m_packagesUrl);
      restDS.setXmlRecordXPath("/Packages/*");
      restDS.setRecordXPath("/Packages/*");
      restDS.setFields(createDsFields());
      restDS.fetchData(null, new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            Record[] recs = response.getData();
            ListGridRecord[] lgrecs = new ListGridRecord[recs.length];
            logger.log(Level.INFO,"found "+recs.length+" records");
            for (int i=0; i<recs.length; i++)
            {
               JavaScriptObject jsObj = recs[i].getJsObj();
               logger.log(Level.INFO,"jsObj="+jsObj);


               ListGridRecord lgr = new ListGridRecord(recs[i].getJsObj());
               if (lgr.getAttribute(PACKAGE_STATUS).equals(
                     STATUS_UNINSTALLED))
               {
                   lgr.setEnabled(false);
               }
               lgrecs[i]=lgr;
            }
            m_packagesGrid.setData(lgrecs);
            if (selectedIndexes != null)
            {
               for (int i : selectedIndexes)
               {
                  if (i >= recs.length)
                     continue;
                  m_packagesGrid.selectRecord(i);
               }
            }
         }
      });
   }

   /**
    * Reloads the grids data.
    */
   public void refreshTab()
   {
      m_packagesGrid.invalidateCache();
      setGridData();
      m_packagesGrid.redraw();
   }

   /**
    * Creates the buttons panel for the packages ui and adds the mouse handlers.
    * 
    * @return the HStack object consisting ofthe buttons.
    */
   private HLayout createButtonsPanel()
   {
      m_actionsMenu.setShowShadow(true);
      m_actionsMenu.setShadowDepth(10);
      m_actionsMenu.setItems(m_uninstallMenuItem, m_verifyMenuItem,
            m_convertMenuItem);
      addEnableConditionsToMenus();
      MenuButton actionsBtn = new MenuButton("Actions", m_actionsMenu);
      
      m_reapplyMenu.setShowShadow(true);
      m_reapplyMenu.setShadowDepth(10);
      m_reapplyMenu.setItems(m_csMenuItem, m_vsMenuItem);
      MenuButton reapplyBtn = new MenuButton("Reapply", m_reapplyMenu);

      addOnClickHandlersToMenus();
      
      HLayout btnLayout = new HLayout();
      btnLayout.setID("PSPackagesTab_btnsPanel");
      btnLayout.setHeight(20);
      btnLayout.addMember(actionsBtn);
      btnLayout
            .addMember(new Img("spacer.png", 10, 10));
      btnLayout.addMember(reapplyBtn);

      return btnLayout;
   }
   
   /**
    * Helper method to add the onclick handlers to the menu items
    */
   private void addOnClickHandlersToMenus()
   {
      m_uninstallMenuItem
            .addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler()
            {
               public void onClick(MenuItemClickEvent event)
               {
                  handlePackageUninstall();
               }
            });
      m_verifyMenuItem
            .addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler()
            {
               public void onClick(MenuItemClickEvent event)
               {
                  handlePackageValidation();
               }
            });
      m_convertMenuItem
            .addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler()
            {
               public void onClick(MenuItemClickEvent event)
               {
                  handleConvertToSource();
               }
            });
      m_csMenuItem
            .addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler()
            {
               public void onClick(MenuItemClickEvent event)
               {
                  handleReapplyConfig();
               }
            });
      m_vsMenuItem
            .addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler()
            {
               public void onClick(MenuItemClickEvent event)
               {
                  handleReapplyPackageVisibility();
               }
            });

   }
   
   /**
    * Helper method to add the enable conditions to menu items. Uninstall,
    * Verify and Convert to Source menu items are enabled only if it is single
    * selection. Uninstall is disabled for System packages and Convert to Source
    * menu item is disabled for locked packages.
    * 
    */
   private void addEnableConditionsToMenus()
   {
      m_uninstallMenuItem.setEnableIfCondition(new MenuItemIfFunction()
      {
         public boolean execute(Canvas target, Menu menu, MenuItem item)
         {
            boolean result = false;
            if(m_packagesGrid.getSelection().length == 1)
            {
               ListGridRecord record = m_packagesGrid.getSelection()[0];
               String category = record.getAttribute("category");
               result = !category.equals("System");
            }
            return result;
         }
      }
      );
      m_verifyMenuItem.setEnableIfCondition(new MenuItemIfFunction()
      {
         public boolean execute(Canvas target, Menu menu, MenuItem item)
         {
            return m_packagesGrid.getSelection().length == 1;
         }
      }
      );
      m_convertMenuItem.setEnableIfCondition(new MenuItemIfFunction()
      {
         public boolean execute(Canvas target, Menu menu, MenuItem item)
         {
            boolean result = false;
            if(m_packagesGrid.getSelection().length == 1)
            {
               ListGridRecord record = m_packagesGrid.getSelection()[0];
               String lockStatus = record.getAttribute("lockStatus");
               result = !lockStatus.equals("Locked");
            }
            return result;
         }
      }
      );
   }
   
   /**
    * Creates the convert to source dialog if it is <code>null</code> and
    * calls the open method on that dialog by passing the selected record. The
    * convert to source dialog makes the rest request to the server and converts
    * the package to source.
    * 
    */
   private void handleConvertToSource()
   {
      if (m_convertToSourceDlg == null)
         m_convertToSourceDlg = new PSConvertToSourceDialog();

      ListGridRecord[] records = m_packagesGrid.getSelection();
      // The convert to source menu is disabled for multiple record selections.
      // This is to just safeguard.
      if (records.length != 1)
      {
         SC.say("Please select a single record for converting " +
               "the package to source.");
         return;
      }
      DSCallback dsc = new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            refreshTab();
         }
      };
      m_convertToSourceDlg.open(records[0], dsc);
   }

   /**
    * Creates the uninstall dialog if it is <code>null</code> and calls the
    * open method on that dialog by passing the packages grid. The uninstall
    * dialog makes the rest request to the server and uninstalls the package.
    * 
    */
   private void handlePackageUninstall()
   {
      if (m_uninstallDlg == null)
         m_uninstallDlg = new PSUninstallPackageDialog();

      ListGridRecord[] records = m_packagesGrid.getSelection();
      // The uninstall button is disabled for multiple record selections. This
      // is to just safe guard.
      if (records.length != 1)
      {
         SC.say("Please select a single record for uninstall.");
         return;
      }
      DSCallback dsc = new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            refreshTab();
         }
      };
      m_uninstallDlg.open(records[0], dsc);
   }

   /**
    * Handles the package validation and shows the results in status dialog.
    * 
    */
   private void handlePackageValidation()
   {
      ListGridRecord rec = m_packagesGrid.getSelectedRecord();
      String validationUrl = m_validationUrl + "?packageName="
            + rec.getAttribute("name");
      RestDataSource validateDs = new RestDataSource();
      DataSourceField[] fields = new DataSourceField[2];
      DataSourceTextField type = new DataSourceTextField("type");
      fields[0] = type;
      DataSourceTextField message = new DataSourceTextField("message");
      fields[1] = message;
      validateDs.setFields(fields);
      validateDs.setXmlRecordXPath("/Response");
      validateDs.setRecordXPath("/Response");
      validateDs.setFetchDataURL(validationUrl);
      DSCallback callBack = new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            Record[] records = response.getData();
            String respMsg = records[0].getAttribute("message");
            if (respMsg.trim().length() < 1)
               respMsg = "Unexpected error occurred while validating the "
                     + "package.";
            m_statusDlg.open("Verification Results", respMsg);
         }

      };
      validateDs.fetchData(null, callBack, null);
   }

   /**
    * Handles the reapplying of package configuration menu action. Makes a rest
    * request to the server and displays the results in a status window.
    */
   private void handleReapplyConfig()
   {
      final ListGridRecord[] selectedRecords = m_packagesGrid.getSelection();
      if (selectedRecords.length < 1)
      {
         SC.say("Please select at least one package for reapplying "
               + "configuration.");
         return;
      }
      final int[] recordIndexes = new int[selectedRecords.length];
      for (int i = 0; i < selectedRecords.length; i++)
      {
         recordIndexes[i] = m_packagesGrid.getRecordIndex(selectedRecords[i]);
      }
      List<List<String>> selectedPackages = getPkgsForConfigReapply();
      final List<String> configPakcages = selectedPackages.get(0);
      final List<String> noConfigPakcages = selectedPackages.get(1);
      if (!noConfigPakcages.isEmpty() && configPakcages.isEmpty())
      {
         String msg = "None of the selected packages is associated " +
               "with a configuration file.";
         for (String pkgName : noConfigPakcages)
         {
            msg += "<br/>" + pkgName;
         }
         SC.say(msg);
         return;
      }
      String packageNames = "";
      for (String pkgName : configPakcages)
      {
         packageNames += pkgName + PkgMgtUI.NAME_SEPARATOR;
      }
      if (packageNames.endsWith(PkgMgtUI.NAME_SEPARATOR))
         packageNames = packageNames.substring(0, packageNames.length() - 1);
      String reapplyConfigUrl = m_reapplyConfigUrl + "?packageNames="
            + packageNames;

      RestDataSource reapplyConfigDs = new RestDataSource();
      DataSourceField[] fields = new DataSourceField[2];
      DataSourceTextField type = new DataSourceTextField("type");
      fields[0] = type;
      DataSourceTextField message = new DataSourceTextField("message");
      fields[1] = message;
      reapplyConfigDs.setFields(fields);
      reapplyConfigDs.setXmlRecordXPath("/Response");
      reapplyConfigDs.setRecordXPath("/Response");
      reapplyConfigDs.setFetchDataURL(reapplyConfigUrl);
      DSCallback callBack = new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            Record[] records = response.getData();
            String respMsg = records[0].getAttribute("message");
            if (respMsg.trim().length() < 1)
            {
               respMsg = "Unexpected error occurred while reapplying the "
                     + " configuration for the following packages.";
               for (String pkg : configPakcages)
               {
                  respMsg += "<br/>" + pkg;
               }
            }
            if(!noConfigPakcages.isEmpty())
            {
               respMsg += "<br/><br/>No configuration files are associated " +
                     "with the following packages, so no configurations were " +
                     "applied.";
               for (String pkg : noConfigPakcages)
               {
                  respMsg += "<br/>" + pkg;
               }
            }
            m_statusDlg.open("Reapply Configuration Results", respMsg);
            setGridData(recordIndexes);
         }

      };
      reapplyConfigDs.fetchData(null, callBack, null);
   }

   /**
    * Gets the List of packages that has two entries the first one is the list
    * packages that have configuration files may be empty never
    * <code>null</code> and the second one is the list of packages that do not
    * have configuration files.
    * 
    * @return valid package names never <code>null</code>, may be empty.
    */
   private List<List<String>> getPkgsForConfigReapply()
   {
      List<List<String>> result = new ArrayList<List<String>>();
      final ListGridRecord[] selectedRecords = m_packagesGrid.getSelection();
      List<String> noConfigPakcages = new ArrayList<String>();
      List<String> configPakcages = new ArrayList<String>();
      for (int i = 0; i < selectedRecords.length; i++)
      {
         ListGridRecord selectedRecord = selectedRecords[i];
         String status = selectedRecord.getAttribute(CONFIG_STATUS);
         if(status.equals("None"))
         {
            noConfigPakcages.add(selectedRecord.getAttribute("name"));
         }
         else
         {
            configPakcages.add(selectedRecord.getAttribute("name"));
         }
      }
      result.add(configPakcages);
      result.add(noConfigPakcages);
      return result;
   }

   /**
    * Handles the reapplying of package visibility menu action. Makes a rest
    * request to the server and displays the results in a status window.
    */
   private void handleReapplyPackageVisibility()
   {
      String packageNames = getSelectedPackageNames();
      if (packageNames.length() < 1)
      {
         SC.say("Please select at least one installed package "
               + "for reapplying visibility settings.");
         return;
      }
      String reapplyVisibilityUrl = m_reapplyVisUrl + "?packageNames="
            + packageNames;
      RestDataSource reapplyVisibilityDs = new RestDataSource();
      DataSourceField[] fields = new DataSourceField[2];
      DataSourceTextField type = new DataSourceTextField("type");
      fields[0] = type;
      DataSourceTextField message = new DataSourceTextField("message");
      fields[1] = message;
      reapplyVisibilityDs.setFields(fields);
      reapplyVisibilityDs.setXmlRecordXPath("/Response");
      reapplyVisibilityDs.setRecordXPath("/Response");
      reapplyVisibilityDs.setFetchDataURL(reapplyVisibilityUrl);
      DSCallback callBack = new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            Record[] records = response.getData();
            String respMsg = records[0].getAttribute("message");
            if (respMsg.trim().length() < 1)
               respMsg = "Unexpected error occurred while reapplying the "
                     + "visibility settings.";
            m_statusDlg.open("Reapply Visibility Settings", respMsg);
         }

      };
      reapplyVisibilityDs.fetchData(null, callBack, null);
   }

   /**
    * Helper method to get the installed package names from the selected rows.
    * 
    * @return names of the packages, never <code>null</code>, may be empty.
    */
   private String getSelectedPackageNames()
   {
      ListGridRecord[] selection = m_packagesGrid.getSelection();
      String packageNames = "";
      for (ListGridRecord record : selection)
      {
         if (!record.getAttribute(PACKAGE_STATUS).equals(STATUS_UNINSTALLED))
         {
            packageNames += record.getAttribute("name")
                  + PkgMgtUI.NAME_SEPARATOR;
         }
      }
      if (packageNames.endsWith(PkgMgtUI.NAME_SEPARATOR))
         packageNames = packageNames.substring(0, packageNames.length() - 1);

      return packageNames;
   }

   /**
    * Helper method to create the list grid fields.
    * 
    * @return list grid field array, never <code>null</code>.
    */
   private ListGridField[] createFields()
   {
      ListGridField[] fields = new ListGridField[8];
      fields[0] = new ListGridField(PACKAGE_STATUS, PkgMgtUI.getMessages()
            .installedLabel(), 80);
      fields[0].setAlign(Alignment.CENTER);
      fields[0].setType(ListGridFieldType.IMAGE);
      fields[0].setImageURLPrefix("icons/16/");
      fields[0].setImageURLSuffix(".png");
      fields[0].setAttribute("wrap", true);
      fields[0].setShowHover(true);
      fields[0].setHoverCustomizer(new PackageStatusHover());

      fields[1] = new ListGridField(CONFIG_STATUS, PkgMgtUI
            .getMessages().configuredLabel(), 80);
      fields[1].setAlign(Alignment.CENTER);
      fields[1].setType(ListGridFieldType.IMAGE);
      fields[1].setImageURLPrefix("icons/16/");
      fields[1].setImageURLSuffix(".png");
      fields[1].setAttribute("wrap", true);
      fields[1].setShowHover(true);
      fields[1].setHoverCustomizer(new ConfigStatusHover());
      fields[2] = new ListGridField("name", PkgMgtUI.getMessages()
            .nameLabel());
      fields[3] = new ListGridField("publisher", PkgMgtUI.getMessages()
            .publisherLabel());
      fields[4] = new ListGridField("version", PkgMgtUI.getMessages()
            .versionLabel());
      fields[5] = new ListGridField("desc", PkgMgtUI.getMessages()
            .descriptionLabel());
      fields[5].setShowHover(true);
      fields[5].setHoverCustomizer(new HoverCustomizer()
      {
         public String hoverHTML(Object value, ListGridRecord record,
               int rowNum, int colNum)
         {
            return "<div style='width:300;'>" + record.getAttribute("desc")
                  + "</div>";
         }
      });
      fields[6] = new ListGridField("installer", PkgMgtUI.getMessages()
            .byWhoLabel());
      
      CellFormatter dateFormatter = new CellFormatter()
      {
         public String format(
            Object value, ListGridRecord record, int rowNum, int colNum)
         {
       
            DateTimeFormat inputFormat = 
               DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
            DateTimeFormat outputFormat = 
               DateTimeFormat.getFormat("MMM dd, yyyy h:mm:ss a");
            String val = value.toString();
            val = val.substring(0, val.lastIndexOf("-"));
            Date inputDate = inputFormat.parse(val);
            String output = outputFormat.format(inputDate);
       
            return output;
         }
      };

      fields[7] = new ListGridField("installdate",PkgMgtUI
            .getMessages().installDateLabel());
      fields[7].setCellFormatter(dateFormatter);
      
      return fields;

   }

   /**
    * A class to set the hover html on config status images. Implements
    * <code>HoverCustomizer</code> interface and returns the appropriate
    * message.
    */
   class ConfigStatusHover extends HoverCustomizer
   {
      public String hoverHTML(Object value, ListGridRecord record, int rowNum,
            int colNum)
      {
         String msg = "";
         String status = record.getAttribute(CONFIG_STATUS);
         if (status == "Success")
         {
            msg = "Configured Successfully";
         }
         else if (status == "Error")
         {
            msg = "Error during the configuration";
         }
         else if (status == "Warn")
         {
            msg = "Warnings during the configuration";
         }
         return msg;
      }
   }

   /**
    * A class to set the hover html on package status images. Implements
    * <code>HoverCustomizer</code> interface and returns the appropriate
    * message.
    */
   class PackageStatusHover extends HoverCustomizer
   {
      public String hoverHTML(Object value, ListGridRecord record, int rowNum,
            int colNum)
      {
         String msg = "";
         String status = record.getAttribute(PACKAGE_STATUS);
         if (status.equalsIgnoreCase( "Success"))
         {
            msg = "Installed Successfully";
         }
         else if (status.equalsIgnoreCase("Error"))
         {
            msg = "Error during the installation";
         }
         else if (status.equalsIgnoreCase("Uninstalled"))
         {
            msg = "Uninstalled";
         }
         return msg;
      }
   }

   /**
    * Helper method to create the DataSource fields.
    * 
    * @return datasource field array, never <code>null</code>.
    */
   private DataSourceField[] createDsFields()
   {
      DataSourceField[] fields = new DataSourceField[8];
      fields[0] = new DataSourceImageField(PACKAGE_STATUS, PkgMgtUI.getMessages()
            .installedLabel());
      fields[1] = new DataSourceImageField(CONFIG_STATUS, PkgMgtUI
            .getMessages().configuredLabel());
      fields[2] = new DataSourceTextField("name", PkgMgtUI.getMessages()
            .nameLabel());
      fields[2].setPrimaryKey(true);
      fields[2].setCanEdit(false);
      fields[3] = new DataSourceTextField("publisher", PkgMgtUI.getMessages()
            .publisherLabel());
      fields[4] = new DataSourceTextField("version", PkgMgtUI.getMessages()
            .versionLabel());
      fields[5] = new DataSourceTextField("desc", PkgMgtUI.getMessages()
            .descriptionLabel());
      fields[6] = new DataSourceTextField("installer", PkgMgtUI.getMessages()
            .byWhoLabel());
      fields[7] = new DataSourceTextField("installdate", PkgMgtUI
            .getMessages().installDateLabel());

      return fields;

   }
   
   /**
    * The packages grid to display the package information.
    */
   private ListGrid m_packagesGrid = new ListGrid();

   private Menu m_reapplyMenu = new Menu();
   MenuItem m_csMenuItem = new MenuItem("Configuration Settings");
   MenuItem m_vsMenuItem = new MenuItem("Visibility Settings");

   private Menu m_actionsMenu = new Menu();

   MenuItem m_uninstallMenuItem = new MenuItem("Uninstall");
   MenuItem m_verifyMenuItem = new MenuItem("Verify");
   MenuItem m_convertMenuItem = new MenuItem("Convert to Source");
   
   private PSUninstallPackageDialog m_uninstallDlg = null;
   private PSConvertToSourceDialog m_convertToSourceDlg = null;

   private static final String STATUS_UNINSTALLED = "Uninstall";

   /**
    * The rest url for getting the validation results.
    */
   private static final String m_validationUrl = PkgMgtUI.ms_serviceRoot
         + "validationResults.xml";

   /**
    * The rest url for getting the reapplying the configuration settings on the
    * selected packages.
    */
   private static final String m_reapplyConfigUrl = PkgMgtUI.ms_serviceRoot
         + "reapplyConfigs.xml";

   /**
    * The rest url for getting the reapplying the visibility settings on the
    * selected packages.
    */
   private static final String m_reapplyVisUrl = PkgMgtUI.ms_serviceRoot
         + "reapplyVisibility.xml";

   /**
    * The url for getting the packages
    */
   private static final String m_packagesUrl = PkgMgtUI.ms_serviceRoot
         + "packages.xml";

   /**
    * Creates the status dialog, use open method to show the status.
    */
   private PSStatusDialog m_statusDlg = new PSStatusDialog();

   private static final String CONFIG_STATUS = "configStatus";

   private static final String PACKAGE_STATUS = "packageStatus";

   private Logger logger = Logger.getLogger(PSPackagesTab.class.getName());

   
}
