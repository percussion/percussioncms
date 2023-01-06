/******************************************************************************
 *
 * [ PSACLNewUserDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.border.PSFocusBorder;
import com.percussion.cms.objectstore.PSSecurityProviderInstanceSummary;
import com.percussion.guitools.PSAccessibleActionListener;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.guitools.UTStandardCommandPanel;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


 /**
  * New user ACL dialog is used to enter ACL's and user name
  * for creation of a new user.
  */
 public class PSACLNewUserDialog extends PSDialog implements ItemListener
 {

     /**
      * Constructs a new <code>PSACLDialog</code> using the
      * passed in list of providers
      * @param dialog the dialog that is the owner of this dialog.
      * May be <code>null</code>.
      * @param providers <code>Iterator</code> of security providers
      * May not be <code>null</code>, but may be empty.
      */
     public PSACLNewUserDialog(Dialog dialog, Iterator providers, PSContentExplorerApplet applet)
     {
        super(dialog, applet.getResourceString(
              PSACLNewUserDialog.class, "New User ACL Entry"));
        
        if(applet==null)
           throw new IllegalArgumentException("applet may not be null");
        m_applet = applet;
        

        initDialog(providers);
     }

     /**
      * Constructs a new <code>PSACLDialog</code> using the
      * passed in list of providers
      * @param frame the frame that is the owner of this dialog.
      * May be <code>null</code>.
      * @param providers <code>Iterator</code> of security providers
      * May not be <code>null</code>, but may be empty.
      */
     public PSACLNewUserDialog(Frame frame, Iterator providers, PSContentExplorerApplet applet)
     {
        super(frame, applet.getResourceString(
           PSACLNewUserDialog.class, "New User ACL Entry"));
        
        if(applet==null)
           throw new IllegalArgumentException("applet may not be null");
        m_applet = applet;

        initDialog(providers);
     }

     /**
      * Returns the selected provider
      * @return returns the selected provider. May be <code>null</code>.
      */
     public PSSecurityProviderInstanceSummary getSelectedProvider()
     {
        return (PSSecurityProviderInstanceSummary)
           m_instanceComboBox.getSelectedItem();
     }

     /**
      * Returns the user name.
      * @return the user name. May be <code>null</code>.
      */
     public String getUserName()
     {
        return m_nameTextField.getText();
     }

     /**
      * Initializes the dialog and loads data in combo boxes.
      * @param providers the iterator of security provider
      * instances.
      */
     private void initDialog(Iterator providers)
     {
        JPanel mainPanel = new JPanel(new BorderLayout());

        PSPropertyPanel propPanel = new PSPropertyPanel();
        propPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,10));

        m_typeComboBox = new JComboBox();
        m_typeComboBox.addActionListener(new PSAccessibleActionListener());
        m_typeComboBox.addItemListener(this);
        m_instanceComboBox = new JComboBox();
        m_instanceComboBox.addActionListener(new PSAccessibleActionListener());
        m_instanceComboBox.addItemListener(this);
        m_nameTextField = new JTextField();

         propPanel.addPropertyRow(
               m_applet.getResourceString(
            getClass(),
            "Security provider type:"),
         m_typeComboBox,
            PSContentExplorerApplet.getResourceMnemonic(
            getClass(),
            "Security provider type:", 'p'));

         propPanel.addPropertyRow(
               m_applet.getResourceString(
            getClass(),
            "Provider instance:"),
         m_instanceComboBox,
            PSContentExplorerApplet.getResourceMnemonic(
            getClass(),
            "Provider instance:", 'i'));

        propPanel.addPropertyRow(
              m_applet.getResourceString(getClass(),"Name:"),
            m_nameTextField,
            PSContentExplorerApplet.getResourceMnemonic(
            getClass(),
            "Name:", 'N'));

        UTStandardCommandPanel defCommandPanel = 
            new UTStandardCommandPanel( this, 
                                        SwingConstants.HORIZONTAL, true);

        defCommandPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        mainPanel.add(propPanel, BorderLayout.CENTER);
        JPanel cmdPanel = new JPanel(new BorderLayout());
        cmdPanel.add(defCommandPanel, BorderLayout.EAST);
        mainPanel.add(cmdPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        loadComboBoxes(providers);

        pack();
        center();
        setResizable(true);
        
        // Add focus highlights
        PSDisplayOptions dispOptions =
           (PSDisplayOptions)UIManager.getDefaults().get(
              PSContentExplorerConstants.DISPLAY_OPTIONS);
        PSFocusBorder focusBorder = new PSFocusBorder(1, dispOptions);
        focusBorder.addToAllNavigable(mainPanel);        
     }

     /**
      * Loads instances into instance comboBox
      * @param provider the <code>Provider</code> that contains
      * the instances. May be <code>null</code>.
      */
     private void loadInstances(ProviderType provider)
     {
        if(null == provider)
           return;
        Iterator it = provider.getInstances();
        m_instanceComboBox.removeAllItems();
        while(it.hasNext())
           m_instanceComboBox.addItem(it.next());

     }

     /**
      * Initializes loading of combo boxes
      * @param providers. May be <code>null</code>.
      */
     private void loadComboBoxes(Iterator providers)
     {
        if(null == providers)
           return;

        PSSecurityProviderInstanceSummary provider = null;
        List temp = new ArrayList();
        ProviderType type = null;
        while(providers.hasNext())
        {
           provider = (PSSecurityProviderInstanceSummary)providers.next();
           type = new ProviderType(provider.getTypeId(), provider.getTypeName());
           if(temp.contains(type))
           {
              type = (ProviderType)temp.get(temp.indexOf(type));
              type.addInstance(provider);
           }
           else
           {
              type.addInstance(provider);
              temp.add(type);
           }
        }
        Iterator it = temp.iterator();
        while(it.hasNext())
           m_typeComboBox.addItem(it.next());

        loadInstances((ProviderType)m_typeComboBox.getSelectedItem());


     }


     // see ItemListener interface for details
     public void itemStateChanged(ItemEvent event)
     {
        Object source = event.getSource();

        if(m_typeComboBox == source)
        {
           loadInstances((ProviderType)m_typeComboBox.getSelectedItem());
        }

     }

     /**
      * Convenience inner class to represent a provider type group
      */
     class ProviderType
     {

        /**
         * Construct new ProviderType object
         * @param typeId the provider type id
         * @param typeName the provider type name
         */
        ProviderType(int typeId, String typeName)
        {
           m_typeId = typeId;
           m_typeName = typeName;
        }

        /**
         * Adds an instance to this provider type
         * @param instance the instance to add. May be <code>null</code>.
         */
        void addInstance(PSSecurityProviderInstanceSummary instance)
        {
           if(null == instance)
              return;
           m_instances.add(instance);
        }

        /**
         * Returns iterator of instances
         * @return iterator of instances. Never <code>null</code>.
         */
        public Iterator getInstances()
        {
           return m_instances.iterator();
        }

        /**
         * Returns an instance by name
         * @param name May not be <code>null</code>.
         * @return the instance if it exists, else <code>null</code>.
         */
        PSSecurityProviderInstanceSummary getInstance(String name)
        {
           if(null == name)
             throw new IllegalArgumentException("Instance name cannot be null.");
           Iterator it = m_instances.iterator();
           PSSecurityProviderInstanceSummary inst = null;
           while(it.hasNext())
           {
              inst = (PSSecurityProviderInstanceSummary)it.next();
              if(inst.getInstanceName().equals(name))
                 return inst;
           }
           return null;
        }

         public boolean equals(Object object) {
             if (this == object) return true;

             if (!(object instanceof ProviderType)) return false;

             ProviderType that = (ProviderType) object;

             return new org.apache.commons.lang.builder.EqualsBuilder()
                     .appendSuper(super.equals(object))
                     .append(m_typeId, that.m_typeId)
                     .append(m_typeName, that.m_typeName)
                     .append(m_instances, that.m_instances)
                     .isEquals();
         }

         public int hashCode() {
             return new org.apache.commons.lang.builder.HashCodeBuilder(17, 37)
                     .appendSuper(super.hashCode())
                     .append(m_typeId)
                     .append(m_typeName)
                     .append(m_instances)
                     .toHashCode();
         }

         /**
         * Returns the typeName as the string representation
         * for this object.
         * @return the typeName for this <code>ProviderType</code>.
         */
        public String toString()
        {
           return m_typeName;
        }

        /**
         * The provider type id. Set in the ctor.
         */
        protected int m_typeId;

        /**
         *  The provider type name. Initialized in the ctor.
         *  May be <code>null</code>.
         */
        protected String m_typeName;

        /**
         * The list of provider instances. Never <code>null</code>, may be
         * empty.
         */
        protected List m_instances = new ArrayList();


     }

     /**
      * Security provider type combo box. Initialized in 
      * {@link #initDialog(Iterator)}, never <code>null</code> after that.
      */
     private JComboBox m_typeComboBox;

     /**
      * Provider instance combo box. Initialized in 
      * {@link #initDialog(Iterator)}, never <code>null</code> after that.
      */
     private JComboBox m_instanceComboBox;

     /**
      * User name text field. Initialized in {@link #initDialog(Iterator)},
      * never <code>null</code> after that.
      */
     private JTextField m_nameTextField;
     
     /**
      * A reference back to the applet that initiated the action manager.
      */
     private PSContentExplorerApplet m_applet;
 }
