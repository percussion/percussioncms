/******************************************************************************
 *
 * [ PSDeliveryTierDatabasePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.PSDeliveryTierDatabaseModel;
import com.percussion.installer.model.PSDeliveryTierProtocolModel;

import java.awt.Choice;
import java.awt.Label;


/**
 * AWT Implementation of {@link PSDeliveryTierDatabaseModel}.
 */
public class PSDeliveryTierDatabasePanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      PSDeliveryTierDatabaseModel dm = new PSDeliveryTierDatabaseModel(this);
      setModel(dm);
      getDM();
      
      rxAdd(m_lblSchema = new Label(RxInstallerProperties.getString("schema")));
      m_chSchema = new Choice();
      refreshSchemas();
            
      rxAdd(m_chSchema);
      
      rxAdd(3);
      rxAdd(m_lblDatabase = new Label(RxInstallerProperties.getString(
            "database")));
      m_chDatabase = new Choice();
      refreshDatabases();
      
      rxAdd(m_chDatabase);
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
       String databaseLabel = RxInstallerProperties.getResources().getString("database");
       
       if (m_dM.isMySql())
       {
           m_lblSchema.setText(databaseLabel);
       }
       else
       {
           m_lblSchema.setText(RxInstallerProperties.getString("schema"));
       }
       
       if (m_dM.isDB2() || m_dM.isOracle() || m_dM.isMySql())
       {
           // Don't show databases, since DB2, Oracle, and MySql only use schemas
           m_chDatabase.setVisible(false);
           m_lblDatabase.setVisible(false);
           m_lblDatabase.setText("");
       }
       else
       {
           m_chDatabase.setVisible(true);
           m_lblDatabase.setVisible(true);
           m_lblDatabase.setText(databaseLabel);

           // Refresh the databases
           refreshDatabases();

           String strDatabase = (String) m_dM.getPropertyValue(
                 PSDeliveryTierProtocolModel.DB_NAME);
           if (strDatabase != null)
               setSelection(m_chDatabase, strDatabase);
       }

       // Refresh the schemas
       refreshSchemas();

       //schema property
       String strSchema = (String) m_dM.getPropertyValue(
             PSDeliveryTierProtocolModel.SCHEMA_NAME);
       if (strSchema != null)
           setSelection(m_chSchema, strSchema);
   }
   
   @Override
   public void exiting()
   {
      String selSchema = m_chSchema.getSelectedItem();
      if (selSchema != null && selSchema.length() > 0)
         m_dM.setSchema(selSchema);
      
      String selDatabase = m_chDatabase.getSelectedItem();
      if (selDatabase != null && selDatabase.length() > 0)
         m_dM.setDatabase(selDatabase);
   }
   
   @Override
   public void propertyChanged(String propName)
   {
      super.propertyChanged(propName);
      
      if (propName.equalsIgnoreCase("RxSchema"))
         setSelection(m_chSchema, m_dM.getSchema());
      
      if (propName.equalsIgnoreCase("RxDatabase"))
         setSelection(m_chDatabase, m_dM.getDatabase());
   }
   
   /**
    * Refreshes the schema ui component values.
    */
   private void refreshSchemas()
   {
      m_chSchema.removeAll();
      for (String schema : getDM().getSchemas())
         m_chSchema.add(schema);
   }
   
   /**
    * Refreshes the database ui component values.
    */
   private void refreshDatabases()
   {
      m_chDatabase.removeAll();
      for (String database : getDM().getDatabases())
         m_chDatabase.add(database);
   }
   
   /**
    * Cache the model since it is used heavily.
    * 
    * @return the data model for this panel.
    */
   private PSDeliveryTierDatabaseModel getDM()
   {
      if (m_dM == null)
         m_dM = (PSDeliveryTierDatabaseModel) getModel();
      return m_dM;
   }
   
   /**
    * Dropdown ui component for schemas.
    */
   private Choice m_chSchema   = null;
   
   /**
    * Dropdown ui component for databases.
    */
   private Choice m_chDatabase = null;
   
   /**
    * Schema ui component label.
    */
   @SuppressWarnings("unused")
   private Label m_lblSchema = null;
   
   /**
    * Database ui component label.
    */
   private Label m_lblDatabase = null;
   
   /**
    * Maintains the database model for this panel, never <code>null</code>
    * after {@link #getDM()} is called.
    */
   private static PSDeliveryTierDatabaseModel m_dM = null;
}
