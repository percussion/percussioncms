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
package com.percussion.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class GetUserInfoDialog extends JDialog  implements ActionListener
{

    private static final Logger log = LogManager.getLogger(GetUserInfoDialog.class);

  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();

      /** Returns the port number */
  public String getPort() {   return(m_port); }

   /** Returns the server name. */
  public String getServerName() { return m_server; }

   /** Returns the users name. */
  public String getUserName()   { return m_userId; }

   /** Returns the password string. */
  public String getPasswordString() { return m_password; }

  /**
  * Event handler, checks for the button checked
  * and calls the appropiated routine.
  *
  *@param e the action event
  *
  */
    public void actionPerformed( ActionEvent e )
    {
         JButton button = (JButton)e.getSource( );
      if(button.getText().equals(m_OKLabel))
      {
         onOk();
      }
      if( button.getText().equals(m_CancelLabel) )
      {
         onCancel();
      }

   }

 /**
  * Extracts the labels and info from the property file.
  *
  */
  private void getLogingInfo()
  {
    try
    {
       // initialize the labels with the default
        m_serverLabel="Server";
        m_portLabel="Port";
        m_userIdLabel="UserID";
        m_passwordLabel="Password";
        m_OKLabel="OK";
        m_CancelLabel="Cancel";
        m_ErrorMsg="All fields must be completed";
        m_errorTitle="Error";

       // do not fail for any reason
        File file = new File(PROPERTIES_FILENAME);
        if(!file.exists())
             file.createNewFile();
        adminProps =  new PSProperties(PROPERTIES_FILENAME);


        // we use the same key names as the workbench
         String strServer = adminProps.getProperty( LAST_SERVER );
        if(strServer != null)
           m_server=strServer;

         String strPort = adminProps.getProperty( LAST_PORT );
        if(strPort != null)
             m_port=strPort;

        String str=adminProps.getProperty(SERVER_LABEL);
        if(str != null )
        {
          m_serverLabel=str;
        }

        str=adminProps.getProperty(PORT_LABEL);
        if(str != null )
        {
          m_portLabel=str;
        }

        str=adminProps.getProperty(USER_LABEL);
        if(str != null )
        {
          m_userIdLabel=str;
        }

        str=adminProps.getProperty(PASSWORD_LABEL);
        if(str != null )
        {
          m_passwordLabel=str;
        }
        str=adminProps.getProperty(OK_LABEL);
        if(str != null )
        {
          m_OKLabel=str;
        }
        str=adminProps.getProperty(CANCEL_LABEL);
        if(str != null )
        {
          m_CancelLabel=str;
        }
        str=adminProps.getProperty(ERROR_MSG);
        if(str != null )
        {
          m_ErrorMsg=str;
        }
        str=adminProps.getProperty(ERROR_TITLE);
        if(str != null )
        {
          m_ErrorMsg=str;
        }



     }
     catch(IOException e) //I don't want to show any dialog here
     {
        System.out.println( "Couldn't find properties file: " + PROPERTIES_FILENAME );
     }

     String strUID = System.getProperty("user.name");
     if ( strUID != null )
        m_userId=strUID;
  }
 
  /**
  *save the server name, port.
  * 
  */
  public void saveProperties()
  {
      if(adminProps != null)
      {
         try
         {
            adminProps.store(new FileOutputStream(PROPERTIES_FILENAME), null);
        }
         catch(IOException e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }
      }
   }

  /**
  *create the panel with the labels
  *
  *@return the panel with the labels attached
  */
  private JPanel initializeLabels()
   {
    getLogingInfo();

       JLabel serverLabel = new JLabel(m_serverLabel, SwingConstants.RIGHT);
      serverLabel.setPreferredSize(new Dimension(100, 15));
      JLabel userLabel = new JLabel(m_userIdLabel, SwingConstants.RIGHT);
      JLabel passwordLabel = new JLabel(m_passwordLabel, SwingConstants.RIGHT);
      JLabel portLabel = new JLabel(m_portLabel, SwingConstants.RIGHT);


     JPanel labelPanel = new JPanel(new GridLayout(4,1));
      ((GridLayout)labelPanel.getLayout()).setVgap(4);
      labelPanel.add(serverLabel);
      labelPanel.add(portLabel);
      labelPanel.add(userLabel);
      labelPanel.add(passwordLabel);
    return labelPanel;
   }
 /**
  *closes the dialog and verify that the user fills all the
  * fields.
  */
  public void onOk()
  {

     m_port=m_portField.getText();
     m_server= m_serverField.getText();
     m_userId=m_userField.getText();
     m_password=new String(m_passwordField.getPassword());
   if( m_port.length()     == 0  ||
        m_server.length()     == 0  ||
        m_userId.length()   == 0  ||
        m_password.length()  == 0 )
    {
        JOptionPane.showMessageDialog( this,
                                       m_ErrorMsg,
                                       m_errorTitle,
                                       JOptionPane.ERROR_MESSAGE );
       return;
    }



    adminProps.setProperty( LAST_SERVER,m_server );
    adminProps.setProperty( LAST_PORT,m_port );


    saveProperties();
    setVisible( false );



  }
   /**
  * Sets the cancel flag to true and closes the dialog.
  */
    public void onCancel()
   {
      m_bCancelled = true;
      setVisible( false );
   }

   /**
    * @return <code>true</code> if the dialog was closed by pressing the Cancel
    * button, <code>false</code> otherwise. Can be called after the caller gets
    * control back.
   **/
   public boolean isCancelled()
   {
      return m_bCancelled;
   }

   /** Constructs the buttons used in this dialog. */
   private JPanel initializeButtons()
   {
     JPanel commandPanel = new JPanel();
     m_ok= new JButton(m_OKLabel);
     m_cancelButton = new JButton(m_CancelLabel);
     m_ok.addActionListener(this);
     m_cancelButton.addActionListener(this);
     commandPanel.add(m_ok);
     commandPanel.add(m_cancelButton) ;
     getRootPane().setDefaultButton( m_ok );
       return commandPanel;
   }

  /**
  * create the panel to display the server, user id and password
  *
  *@return panel with the jtextFields
  */
    private JPanel initializeFields(String serverName, String userName)
   {

    if (m_server == null)
         m_serverField = new JTextField(30);
     else
         m_serverField = new JTextField(m_server, 30);


      if (m_userId == null)
         m_userField = new JTextField(30);
      else
         m_userField = new JTextField(m_userId, 30);

      m_userField.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_ok.doClick();
         }
      });

      if(m_port == null)
         m_portField = new JTextField(DEFAULT_PORT, 10);
      else
         m_portField = new JTextField(m_port, 10);

      m_portField.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_ok.doClick();
         }
      });


      m_passwordField = new JPasswordField(30);
      m_passwordField.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_ok.doClick();
         }
      });

      JPanel fieldPanel = new JPanel(new GridLayout(4, 1));
      ((GridLayout)fieldPanel.getLayout()).setVgap(4);
      fieldPanel.add(m_serverField);
      fieldPanel.add(m_portField);
      fieldPanel.add(m_userField);
      fieldPanel.add(m_passwordField);
      return fieldPanel;
   }

  public GetUserInfoDialog(Frame frame, String title, boolean modal)
  {
    super(frame, title, modal);
    try
    {
      jbInit(null,null);
      pack();
    }
    catch(Exception ex) {
        log.error(ex.getMessage());
        log.debug(ex.getMessage(), ex);
    }
  }

  /**
   * Initializes the dialog, constructing the different panels 
   * 
   *@param serverName the server name to be displayed
   *
   *@param userName   the name of the user displayed
   */
  void jbInit(String serverName, String userName) throws Exception
  {

    JPanel labels = initializeLabels();
      labels.setBorder( BorderFactory.createEmptyBorder(4,4,3,2));
    getContentPane().add( labels, BorderLayout.WEST );

    JPanel fields = initializeFields(serverName, userName);
    fields.setBorder( BorderFactory.createEmptyBorder(4,2,3,2));
    getContentPane().add( fields, BorderLayout.CENTER );



    JPanel buttons = initializeButtons();
    buttons.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ));
    getContentPane().add( buttons, BorderLayout.SOUTH );

    setSize(400, 160);

  }




   public static final String DEFAULT_PORT="9992";

   /**
    * The name of the file where various properties are stored, such as the
    * last server name, port #, etc.
   **/
   public static final String PROPERTIES_FILENAME = "remoteconsole.properties";

      /**
    * The name of a key in the designer properties file. If present, the value
    * of this key will be used as the default for the server field.
    */
   public final static String LAST_SERVER = "last_server_opened";

   /**
    * The name of a key in the designer properties file. If present, the value
    * of this key should contain a delimited list of servers that have previously
    * been connected to. The delimiter to use is UserConfig.DELIMITER. The
    * server combobox will be populated with these values.
    */
   public final static String ALL_SERVERS = "last_all_server_opened";

   /**
    * The name of a key in the designer properties file. If present, the value
    * of this key will be used as the default for the port field.
    */
   public final static String LAST_PORT = "last_port_opened";

   /** todo - ?? The installer requires this. What's it for? */
   public final static String ALL_PORTS = "last_all_port_opened";

   public final static String  SERVER_LABEL="SERVER_LABEL";
   public final static String  PORT_LABEL="PORT_LABEL";
   public final static String  USER_LABEL="USER_LABEL";
   public final static String  PASSWORD_LABEL="PASSWORD_LABEL";
   public final static String  OK_LABEL="OK";
   public final static String  CANCEL_LABEL="CANCEL";
   public final static String  ERROR_MSG="ERROR_MSG";
   public final static String  ERROR_TITLE="ERROR_TITLE";

   static String m_errorTitle=new String();
   static String m_OKLabel=new String();
   static String m_CancelLabel=new String();

   static String m_port=new String();
   static String m_userId=new String();
   static String m_server=new String();
   static String m_password=new String();
   static String m_ErrorMsg=new String();

   static  String m_portLabel=new String();
   static String m_userIdLabel=new String();
   static String m_serverLabel=new String();
   static String m_passwordLabel=new String();

   private JTextField m_userField=null;
   private JTextField m_portField=null;
   private JTextField m_serverField=null;
   private JPasswordField m_passwordField=null;

   private static boolean m_bCancelled = false;
   private JButton m_ok=null;
   private JButton m_cancelButton = null;
   PSProperties adminProps=null;
}
