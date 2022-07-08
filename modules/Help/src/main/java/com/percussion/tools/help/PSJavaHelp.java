/******************************************************************************
 *
 * [ PSJavaHelp.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.tools.help;

import javax.help.BadIDException;
import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * This is a helper class to launch regular and context sensitive help using
 * JavaHelp viewer. This is a singleton object. To prevent the class from being
 * unloaded, a single reference should be kept by a class that is always loaded.
 * This will prevent the help mapping file and the helpset file for JavaHelp
 * from being loaded more than once.
 * <p/>
 * The mapping file used is helptopicmapping.properties which has the mappings
 * for help id(The id identified by the application) to the help topic id(target
 * which is mapped to the html file) in helpset map file. Entries of the
 * following form are expected:
 * <p/>
 * <DEFAULT_HELP_KEY>=<default help topic to display if no help id is supplied>
 * <p/>
 * <helpID>=<The topic id in the help set file to display for a specific id><p/>
 * Typically, there will be a <helpID> for every dialog and tab and the help ids
 * used for the dialogs are class name of that dialog.
 *
 * @todo The 'helptopicmapping.properties' is required because it is not allowed
 * to change the help topic id in the helpset map file with the current
 * help authoring tool. Once we find the way to change the topic id, then the
 * help id identified by the application should be used as topic id in helpset
 * map file and the code to load and read helptopicmapping.properties file
 * should be removed.
 */
public class PSJavaHelp
{
   /**
    * Gets the singleton instance of this class.
    *
    * @return The one and only instance for this object. If it doesn't exist, it
    * will be created.
    **/
   public static synchronized PSJavaHelp getInstance()
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSJavaHelp();

      return ms_theInstance;
   }

   /**
    * Sets the helpset to use with this instance. Displays an error message to
    * the user if the supplied helpset url is not found or unable to parse the
    * url content.
    *
    * @param helpSetURL the url of the helpset file, may not be <code>null
    * </code> or empty.
    * @param helpMappingResourceFile the help topic mapping resource file name,
    * will be loaded through resource bundle, supply <code>null</code> if the
    * requested help ids to the {@link #launchHelp(String) #launchHelp(helpID)}
    * is going to be topic ids.
    *
    * @throws IllegalArgumentException if helpSetUrl is <code>null</code> or
    * empty or helpMappingResourceFile is empty.
    */
   public void setHelpSet(String helpSetURL, String helpMappingResourceFile)
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSJavaHelp();
      synchronized (ms_theInstance) {
         if (helpSetURL == null || helpSetURL.trim().length() == 0)
            throw new IllegalArgumentException(
                    "helpSetURL may not be null or empty");

         m_helpSetURL = helpSetURL;
         if (helpMappingResourceFile != null) {
            if (helpMappingResourceFile.trim().length() != 0)
               loadHelpTopicMappings(helpMappingResourceFile);
            else
               throw new IllegalArgumentException(
                       "helpMappingResourceFile may not be empty.");
         }
      }
   }

   /**
    * Convenience method to call {@link #setHelpSet(String, String)
    * setHelpSet(helpSetURL, null)}.
    */
   public void setHelpSet(String helpSetURL)
   {
      setHelpSet(helpSetURL, null);
   }

   /**
    * Gets helpset url for the specified helpset file prefixed with the proper
    * protocol by looking its location (located in jar file or outside). If
    * the url is required for an applet, uses applet's codebase as the path will
    * be relative to the applet location.
    *
    * @param hsFilePath the helpset file for which to get url, if <code>null
    * </code> or empty, <code>null</code> or empty will be returned.
    * @param isApplet if <code>true</code> assumes the caller is an applet and
    * prefixes supplied codebase to file url, otherwise not
    * @param codeBase the base url of the applet, may not be <code>null</code>
    * or empty, if <code>isApplet</code> is <code>true</code>
    *
    * @return the url string, may be <code>null</code> or empty if supplied
    * helpset file path is <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>isApplet</code> is <code>true
    * </code> and <code>codeBase</code> is <code>null</code> or empty.
    */
   public static String getHelpSetURL(String hsFilePath, boolean isApplet,
                                      String codeBase)
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSJavaHelp();
      synchronized (ms_theInstance) {
         if (isApplet && (codeBase == null || codeBase.trim().length() == 0))
            throw new IllegalArgumentException(
                    "applet codeBase may not be null or empty.");

         String helpSetURL = hsFilePath;
         if (hsFilePath != null && hsFilePath.trim().length() != 0) {
            String prefix = "file:";
/*         if(isApplet)
            prefix = codeBase;

         if(hsFilePath.indexOf(".jar!/") == -1)
            helpSetURL = prefix + hsFilePath;
         else
            helpSetURL = "jar:" + prefix + hsFilePath; */

            if (hsFilePath.indexOf(".jar!/") == -1) {
               if (isApplet) {
                  try {
                     helpSetURL = new URL(new URL(codeBase), hsFilePath).toString();
                  } catch (MalformedURLException e) {
                     throw new IllegalArgumentException(
                             "malformed url for helpset");
                  }
               } else
                  helpSetURL = prefix + hsFilePath;
            } else
               helpSetURL = "jar:" + prefix + hsFilePath;

         }

         return helpSetURL;
      }
   }

   /**
    * Convenience method for {@link #getHelpSetURL(String, boolean, String)
    * getHelpSetURL(hsFilePath, false, null)}. Please see the link for
    * description of the method and its parameter.
    */
   public static String getHelpSetURL(String hsFilePath)
   {
      return getHelpSetURL(hsFilePath, false, null);
   }

   /**
    * Convenience method for {@link #launchHelp(String, boolean, Window)}
    * launchHelp(helpID, false, null) }. Assumes the help frame is not parented
    * by any dialog.
    */
   public static void launchHelp( String helpID )
   {
      launchHelp(helpID, false, null);
   }

   /**
    * Launches the JavaHelp viewer to display the HTML help file associated with
    * the supplied id.
    * <br>
    * If the helptopicmapping.properties file is not available or <code>
    * isTopicID</code> is <code>true</code>, then it treats the supplied help id
    * as the help topic id, otherwise gets the topic id from the map file. If
    * there is no mapping for the supplied Id, the main help topic will be
    * displayed.
    * <br>
    * Logs the error messages if the helpset file is not available. Displays an
    * error message if help topic is not found for the specified help id.
    *
    * @param helpID A key that is used to retrieve the HTML help file. If empty
    * or null, or the key is not present in the mapping file, or the help topic
    * id is not found for the id, the main help is shown.
    * @param isTopicID if <code>true</code> supplied id is considered as topic
    * id, otherwise finds the topic id from map file.
    * @param window the window which is invoking the help dialog, may be
    * <code>null</code>. This should not be <code>null</code> if this is called
    * from a modal dialog because if the dialog is not set as parent to the help
    * viewer window, it won't be accessible.
    **/
   public static void launchHelp( String helpID, boolean isTopicID, Window window)
   {
      PSJavaHelp helpInst = getInstance();
      if ( null == ms_theInstance )
         ms_theInstance = new PSJavaHelp();
      synchronized (ms_theInstance) {
         HelpBroker broker = helpInst.getHelpBroker();
         if (null == broker) {
            showErrorDialog(getResourceString("noHelpSet"),
                    getResourceString("noHelpTitle"), JOptionPane.ERROR_MESSAGE);
            return;
         }

         if (null == helpID || 0 == helpID.trim().length())
            helpID = MAIN_HELP_ID;

         String helpTopicID = null;
         try {
            /* If the provided help id is topic id or there is no map file,
             * provided id is considered as topic id.
             */
            if (helpInst.m_helpIDToFileMap == null || isTopicID) {
               System.out.println("using supplied help id as topic id " + helpID);
               helpTopicID = helpID;
            } else {
               try {
                  helpTopicID = helpInst.m_helpIDToFileMap.getProperty(helpID);
               } catch (MissingResourceException e) {
                  //no key present with this help id, so we have to use main help.
               }
               if (null == helpTopicID) {
                  System.out.println("Couldn't find help mapping for '" + helpID +
                          "'. Trying default.");
                  helpTopicID = helpInst.m_helpIDToFileMap.getProperty(
                          MAIN_HELP_ID);
               }
            }

            System.out.println("The help topic id to launch is " + helpTopicID);
            broker.setCurrentID(helpTopicID);
            ((DefaultHelpBroker) broker).setActivationWindow(window);
            broker.setDisplayed(true);
         } catch (MissingResourceException e) {
            //Requested or main Help Id is not found in the map file
            showErrorDialog(getResourceString("noHelp"),
                    getResourceString("noHelpTitle"), JOptionPane.ERROR_MESSAGE);
         } catch (BadIDException e) {
            System.out.println("Invalid ID " + e.getID());
            //Requested Help Topic is not found in  helpset file
            showErrorDialog(getResourceString("noHelp"),
                    getResourceString("noHelpTitle"), JOptionPane.ERROR_MESSAGE);
         }
      }
   }

   /**
    * Finds whether the help viewer currently is visible or not.
    *
    * @return <code>true</code> if the help viewer is visible, otherwise <code>
    * false</code>
    */
   public boolean isHelpDisplayed()
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSJavaHelp();
      synchronized (ms_theInstance) {
         HelpBroker broker = getHelpBroker();
         if (broker != null)
            return broker.isDisplayed();
         else
            return false;
      }
   }

   /**
    * Sets the parent window to the help viewer window. Makes the help viewer
    * visible only if <code>show</code> is </code>true</code>, otherwise makes
    * it invisible. This method is useful to call while activating/deactivating
    * the modal dialogs to prevent blocking the helpviewer access and closing
    * the help viewer. Call <code>setParent(null, true)</code> to prevent the
    * help viewer getting closed when the modal dialog which is parent of this
    * viewer gets closed.
    *
    * @param modal the dialog to be set as parent to help viewer, may be
    * <code>null</code>. This
    * @param show if </code>true</code>, makes the help viewer visible otherwise
    * invisible.
    */
   public void setParent(JDialog modal, boolean show)
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSJavaHelp();
      synchronized (ms_theInstance) {
         HelpBroker broker = getHelpBroker();
         if (broker != null) {
            ((DefaultHelpBroker) broker).setActivationWindow(modal);
            broker.setDisplayed(show);
         }
      }
   }

   /**
    * Clear the help broker so that a new one gets instantiated
    * the next time we try to get the help broker.
    */
   public void clearBroker()
   {
      m_hbroker = null;
   }

   /**
    * Private constructor to implement Singleton pattern. Use getInstance()
    * to get the single instance. <p/>
    * Attempts to load the resource that contains the help id mappings. If it
    * fails, a message is displayed to the user via a dialog.
    **/
   private PSJavaHelp()
   {
   }

   /**
    * Loads the supplied help mapping file using the specified properties file.
    * Displays an error message if it is unable to load the file.
    * ".properties" extension is added to <code>helpMappingResourceFile</code>
    * for backwards compatibility.
    *
    * @param helpMappingResourceFile the help topic mapping properties file name,
    * assumed not to be <code>null</code> or empty.
    */
   private void loadHelpTopicMappings(String helpMappingResourceFile)
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSJavaHelp();
      synchronized (ms_theInstance) {
         InputStream stream = null;
         helpMappingResourceFile = helpMappingResourceFile.replace('.', '/');
         helpMappingResourceFile = "/" + helpMappingResourceFile + ".properties";
         try {
            stream = this.getClass().getResourceAsStream(helpMappingResourceFile);
            if (stream == null)
               throw new FileNotFoundException("helpMappingResourceFile");
            m_helpIDToFileMap = new Properties();
            m_helpIDToFileMap.load(stream);
         } catch (IOException ioex) {
            System.out.println("DEBUG - disabled help loading errors");
         } finally {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Exception ex) {

               } finally {

               }
            }
         }
      }
   }

   /**
    * Create the help broker on demand, but do nothing if there's no
    * help set url.
    * @return the help broker, should never be <code>null</code>
    */
   private HelpBroker getHelpBroker()
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSJavaHelp();
      synchronized (ms_theInstance) {
         // This may be called before initialization
         if (m_helpSetURL == null) return null;

         if (m_hbroker == null || m_hbroker.getHelpSet() == null) {
            createHelpSet();
         }
         return m_hbroker;
      }
   }


   /**
    * Creates the JavaHelp Object broker and caches to launch the help later.
    * Displays an error message to the user if the supplied helpset url to this
    * instance is not found or unable to load/parse the url content.
    */
   private void createHelpSet()
   {
      ClassLoader loader = this.getClass().getClassLoader();
      URL url;
      HelpSet hs;

      try {
         url = new URL(m_helpSetURL);
         hs = new HelpSet(loader, url);
         System.out.println("Helpset URL is " + hs.getHelpSetURL());
         java.util.Enumeration ids = hs.getLocalMap().getAllIDs();
         //for (; ids.hasMoreElements(); )
         // System.out.println("Map - " + ids.nextElement().toString());
         m_hbroker = hs.createHelpBroker();
      }
      catch(MalformedURLException me)
      {
         System.out.println ("Malformed/invalid HelpSetURL: " + m_helpSetURL);
         me.printStackTrace();

         Object[] params = { m_helpSetURL };
         String text = MessageFormat.format(getResourceString(
                 "malformedHelpSetURL" ) , params);
         showErrorDialog( text,
                 getResourceString( "invalidHelpSetTitle" ),
                 JOptionPane.ERROR_MESSAGE );
      }
      catch (HelpSetException ee) {
         System.out.println ("Unable to parse HelpSetURL: " + m_helpSetURL);
         ee.printStackTrace();

         Object[] params = { m_helpSetURL };
         String text = MessageFormat.format(
                 getResourceString( "invalidHelpSetFile" ),params);
         showErrorDialog( text,
                 getResourceString( "invalidHelpSetTitle" ),
                 JOptionPane.ERROR_MESSAGE );
      }
   }

   /**
    * Gets resource bundle for the error messages while loading the helpset or
    * displaying the help.
    *
    * @return the resource bundle, may be <code>null</code> if it can not find
    * the resource file.
    */
   private static ResourceBundle getResources()
   {
      try {
         if ( null == m_res )
         {
            m_res = ResourceBundle.getBundle(
                    "com.percussion.tools.help.PSJavaHelpErrorResources",
                    Locale.getDefault() );
         }
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
      return m_res;
   }

   /**
    * Gets the resource string identified by the specified key.  If the
    * resource cannot be found, the key itself is returned.
    *
    * @param key identifies the resource to be fetched; may not be <code>null
    * </code> or empty.
    *
    * @return String value of the resource identified by <code>key</code>, or
    * <code>key</code> itself.
    *
    * @throws IllegalArgumentException if key is <code>null</code> or empty.
    */
   private static String getResourceString(String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      String resourceValue = key;
      try
      {
         if (getResources() != null)
            resourceValue = getResources().getString( key );
      } catch (MissingResourceException e)
      {
         // not fatal; warn and continue
         System.err.println( e );
      }
      return resourceValue;
   }

   /**
    * Displays error message in a text area.
    *
    * @param errorBody error message to show, may be <code>null</code> or
    * empty.
    * @param errorTitle title for the dialog, may be <code>null</code> or
    * empty.
    * @param type The type of message, must be one of the <code>JOptionPane
    * </code> message types.
    */
   public static void showErrorDialog(
           String errorBody, String errorTitle, int type)
   {
      JTextArea textBox = new JTextArea(errorBody, 8, 20);
      textBox.setWrapStyleWord( true );
      textBox.setLineWrap( true );
      textBox.setEditable( false );
      JScrollPane pane = new JScrollPane( textBox );
      pane.setPreferredSize(new Dimension( 400, 125));
      JOptionPane.showMessageDialog(getPermanetFocusOwner(), pane, errorTitle, type );
   }

   /**
    * Component currently keeping focus.
    */
   private static Component getPermanetFocusOwner()
   {
      return KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
   }

   /**
    * The resource bundle for error messages. Gets initialized when <code>
    * getResources()</code> is called and may be <code>null</code> if it could
    * not find the bundle, but never modified after it is initialized.
    */
   private static ResourceBundle m_res = null;

   /**
    * The properties file with mappings of help id used by application to the
    * help topic id used in the helpset map file for the html documents.
    * May be <code>null </code> if the file can not be loaded while this
    * instance is created.
    **/
   private Properties m_helpIDToFileMap = null;

   /**
    * The help broker object which represents the helpset of this instance. Used
    * to display the help for the suppplied help id. Gets initialized in <code>
    * createHelpSet()</code>, may be <code>null</code> if there is an exception
    * accessing or parsing the helpset file.
    */
   private HelpBroker m_hbroker = null;

   /**
    * The URL of the helpset file, gets initialized in <code>setHelpSet</code>
    * and never <code>null</code> or modified after that.
    */
   private String m_helpSetURL = null;

   /**
    * The single instance of this class. Use getInstance() to obtain it.
    **/
   private static PSJavaHelp ms_theInstance = null;

   /**
    * The id that will bring up the primary HTML help file for the workbench.
    **/
   public static final String MAIN_HELP_ID = "default";

}
