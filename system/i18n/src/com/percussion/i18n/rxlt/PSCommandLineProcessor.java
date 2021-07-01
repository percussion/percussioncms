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
package com.percussion.i18n.rxlt;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This class serves the following two purposes:
 * <ol>
 * <li>Collect the action specifc data to modify the action configuration via
 * commandline user interface</li>
 * <li>Process the action(s) that have the "process" attributes set to "yes"</li>
 * <li>May also be run inline from another Java process without the command line user interface</li>
 * </ol>
 *
 */
public class PSCommandLineProcessor
{
   /**
    * Constructor. This takes the configuration XML document and the Rhythmyx
    * root directory.
    * @param rxroot Rhythmyx root directory (absolute path) may be
    * <code>null</code> in which case, root specified in the configuration
    * document is used.
    * @param standalone <code>true</code> to enable the commandline user interface, 
    * <code>false</code> to run with no command line ui from within another java process.
    * @see PSRxltMain#CONFIG_FILE
    * @throws SAXException if any error occurs parsing rxltconfig.xml file
    * @throws IOException in case of error building XML document from input
    * stream for the configuration file
    * obtained to
    */
   public PSCommandLineProcessor(String rxroot, boolean standalone)
      throws SAXException, IOException
   {
      try(InputStream is = getClass()
              .getResourceAsStream(CONFIG_FILE)){
      Document cfgDoc = PSXmlDocumentBuilder.createXmlDocument(is , false);
      if(rxroot != null)
      {
         cfgDoc.getDocumentElement().setAttribute(
            PSRxltConfigUtils.ATTR_RXROOT, rxroot);
      }
      
      ms_standalone = standalone;
      
      if (ms_standalone)
      {
         setupInputStream();
      }
      
      init(cfgDoc);
      }
   }

   private void setupInputStream()
   {
      if (ms_consoleLineReader == null)
      {
         try
         {
            ms_consoleInputStreamReader = new InputStreamReader(System.in);
            ms_consoleLineReader = new BufferedReader(ms_consoleInputStreamReader);
         }
         catch(Exception e) //This should never happen
         {
            e.printStackTrace();
         }
      }
   }

   /**
    * Method to set the configuration document and initializes the log file.
    * {@link process} cannot be run without the configuration document set.
    * @param cfgDoc must not be </code>null</code>
    * @throws IllegalArgumentException if cfgDoc is <code>null</code>
    * @throws IOException if there is any error setting the output log file
    */
   public void init(Document cfgDoc)
      throws IOException
   {
      if(cfgDoc == null)
         throw new IllegalArgumentException("cfgDoc must not be null");
      m_CfgDocument = cfgDoc;
      m_rxroot = cfgDoc.getDocumentElement().getAttribute(
         PSRxltConfigUtils.ATTR_RXROOT);
      
      
      //log version string
      logMessage("--------------------------------------------");
      logMessage(PSRxltMain.getVersionString());
      logMessage("--------------------------------------------");

   }

   /**
    * Method to allow any cleanup. must be last call.
    */
   public void terminate()
   {
      logMessage("End of the session");
      if (ms_consoleLineReader != null)
         IOUtils.closeQuietly(ms_consoleLineReader);
      if(ms_consoleInputStreamReader != null)
         IOUtils.closeQuietly(ms_consoleInputStreamReader);
   }
   /**
    * This method gets the action to run from UI. Gathers all the inoformation
    * specific to that action and midifies the config document in memory.
    * @return the actionid chosen by the user to run. One of the actions from
    * the list defined in the interface {@link IPSActionHandler}
    * @throws IOException if there is a problem reading console. Very rare
    * possibility.
    * @throws PSFatalException if there is a problem connecting to server
    * database to get the supported languages.
    */
   protected int getActionToRun()
      throws IOException, PSFatalException
   {
      if (!ms_standalone)
         throw new IllegalStateException("Not running standalone, user input disable");
      
      int action = -1;
  
      action = -1;
      while(action==-1)
      {
         //Display the list of languages already in the Rhythmyx Content Manager
         displayExistingLanguages();
         System.out.println();
         System.out.println("Available Actions:");
         System.out.println("------------------");
         NodeList nl = m_CfgDocument.getElementsByTagName("action");
         Element elem;
         String temp = "";
         for(int i=0; null!=nl && i<nl.getLength(); i++)
         {
            elem = (Element)nl.item(i);
            /*
             * Make sure to initialize process=no for all actions.
             * Only one action is run in one pass in UI mode.
             */
            elem.setAttribute("process", "no");
            //
            System.out.println(elem.getAttribute("actionid") + ". " +
               elem.getAttribute(PSRxltConfigUtils.ATTR_NAME));
         }
         //ask for user input until he enters a valid action
         do
         {
            System.out.println("(All Locale modifications must be performed through the workbench)");
            System.out.print("Choose an Action:");
            temp = ms_consoleLineReader.readLine();
            try
            {
               action = Integer.parseInt(temp);
            }
            catch(Exception e)//potentially NumberFormatException
            {
               action = -1;
            }
            if(action < IPSActionHandler.ACTIONID_FIRST ||
               action > IPSActionHandler.ACTIONID_LAST)
            {
               action = -1;
            }
         } while(action == -1);

      }
      gatherActionInfo(action);

      return action;
   }

   /**
    * Collects the required information specific to the action from the user
    * interface and modifes the configuration document (in memory). Look at the
    * DTD for the configuration document for the configuration parameters.
    * @param actionid one of the
    * @throws IllegalArgumentException the supplied actionid is not match with
    * one of those listed in the configuration document.
    * possibility.
    * @throws IOException if there is a problem reading console. Very rare
    * possibility.
    * @throws PSFatalException if there is a problem connecting to server
    * database to get supported languages.
    * @see PSRxltMain#CONFIG_FILE
    */
   private void gatherActionInfo(int actionid)
      throws IOException, PSFatalException
   {
      Element elem = getActionElement(actionid);
      if(elem == null)
      {
         throw new IllegalArgumentException(
            "Action element for actionid='" + actionid + "' is missing");
      }
      String temp = "";
      switch(actionid)
      {
         case IPSActionHandler.ACTIONID_GENERATE_TMX_RESOURCES:
            List sections = null;
            boolean backtomain = false;
            boolean allSections = false;
            while((!backtomain && !allSections)
               && (sections == null || sections.size() < 1))
            {
               System.out.println();
               System.out.println("Available Sections:");
               System.out.println("------------------");
               NodeList nl = elem.getElementsByTagName("section");
               Element sect;
               for(int i=0; null!=nl && i<nl.getLength(); i++)
               {
                  sect = (Element)nl.item(i);
                  //make sure to initialize process=no for all actions
                  sect.setAttribute("process", "no");
                  //
                  System.out.println(sect.getAttribute("sectionid") + ". "
                     + sect.getAttribute(PSRxltConfigUtils.ATTR_NAME));
               }
               System.out.println("A. All");
               System.out.println("B. Back to action menu");
               System.out.print(
                  "Choose Sections (Separate section identifiers with commas):");
               temp = ms_consoleLineReader.readLine();
               //user interrupted ressing CTRL C
               if(temp == null)
                  return;

               temp = temp.toUpperCase();
               if(temp.startsWith("A"))
                  allSections = true;
               else if(temp.startsWith("B"))
                  backtomain = true;
               sections = parseSectionList(temp);
               String sectionid = null;
               for(int i=0; null!=nl && i<nl.getLength(); i++)
               {
                  sect = (Element)nl.item(i);
                  sectionid = sect.getAttribute("sectionid");
                  if(allSections || sections.contains(sectionid))
                     sect.setAttribute("process", "yes");
               }
            }
            if(backtomain)
               break;

            //output file path
            String outputfile = elem.getAttribute("outputfile");
            temp = outputfile;
            do
            {
               System.out.println("Specify output file path name:");
               if(temp.trim().length() > 0)
                  System.out.println("Default is <" + outputfile + ">");
               temp = ms_consoleLineReader.readLine();
               //user interrupted ressing CTRL C
               if(temp == null)
                  return;
               if(temp.trim().length() < 1)
                  temp = outputfile;
            } while(temp.trim().length() < 1);

            if(!temp.toLowerCase().endsWith(".tmx"))
               temp += ".tmx";

            elem.setAttribute("outputfile", temp.trim());
            String keepmissingkeysonly = elem.getAttribute("keepmissingkeysonly");
            temp = keepmissingkeysonly;
            do
            {
               System.out.println("Generate only the new resource keys missing in " +
                  "server TMX file (yes/no):");
               if(temp.trim().length() > 0)
                  System.out.println("Default is <" + keepmissingkeysonly + ">");
               temp = ms_consoleLineReader.readLine();
               //user interrupted ressing CTRL C
               if(temp == null)
                  return;
               temp = temp.trim();
               if(temp.trim().length() < 1)
               {
                  temp = keepmissingkeysonly;
               }
            } while((!temp.equalsIgnoreCase("yes")
                && !temp.equalsIgnoreCase("no")));

            elem.setAttribute("keepmissingkeysonly", temp);
            elem.setAttribute("process", "yes");
            break;
         case IPSActionHandler.ACTIONID_MERGE_MASTER:
            //file path
            String filepath = elem.getAttribute(PSRxltConfigUtils.ATTR_FILE_PATH);
            temp = filepath;
            do
            {
               System.out.println(
                  "Specify the path to the TMX file you want to merge: ");
               if(temp.trim().length() > 0)
                  System.out.println("Default is <" + filepath + ">");
               temp = ms_consoleLineReader.readLine();
               //user interrupted ressing CTRL C
               if(temp == null)
                  return;
               if(temp.trim().length() < 1)
                  temp = filepath;
            } while(temp.trim().length() < 1);

            elem.setAttribute(PSRxltConfigUtils.ATTR_FILE_PATH, temp.trim());
            elem.setAttribute("process", "yes");
            break;
         case IPSActionHandler.ACTIONID_EXIT:
            elem.setAttribute("process", "yes");
            break;
      }
   }

   /**
    * Helper method to parse the comma separated list of sectionids.
    * @param line line input to be parsed which is read from the UI. Must not
    * be <code>null</code>
    * @return List of sectionids chosen by the user for processing. Never
    * <code>null</code>.
    */
   private List parseSectionList(String line)
   {
      ArrayList list = new ArrayList();
      StringTokenizer tokenizer = new StringTokenizer(line, ",");
      String temp = null;
      int val;
      while(tokenizer.hasMoreTokens())
      {
         temp = tokenizer.nextToken();
         try
         {
            val = Integer.parseInt(temp);
            if(val < IPSSectionHandler.SECTIONID_FIRST ||
               val > IPSSectionHandler.SECTIONID_LAST)
            {
               continue;
            }
            list.add(temp);
         }
         catch(NumberFormatException e)
         {
            //ignore
         }
      }
      return list;
   }

   /**
    * Helper method to locate the action element matching the given actionid
    * from the configuration document.
    * @param action actionid of the action element to find
    * @return Matching action element for the given actionid. Will be
    * <code>null</code> if not found.
    */
   private Element getActionElement(int action)
   {
      String actionString = String.valueOf(action);
      NodeList nl = m_CfgDocument.getElementsByTagName("action");
      Element elem;
      String temp;
      for(int i=0; null!=nl && i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         temp = elem.getAttribute("actionid");
         if(temp.equals(actionString))
            return elem;
      }
      return null;
   }

   /**
    * This is the method that processes all actions that have the "process"
    * attribute set to "yes". With UI mode user will run only one action in pass.
    * However, in non-UI mode (in which case the config document shall not be
    * modifed) the configuartion document should define the actions in a
    * meaningful way. For example, {@link IPSActionHandler#ACTIONID_EXIT} must
    * always be disabled by setting the attribute process=no.
    * @throws PSActionProcessingException in case of any failure to process any
    * of the actions.
    * @return returns <code>true</code> if the process has to be terminated
    * since user chose to exit. This used UI mode to keep the user in loop until
    * he wants to quit.
    */
   public boolean process()
      throws PSActionProcessingException
   {
      try
      {
         NodeList nl = m_CfgDocument.getElementsByTagName("action");
         Element elem;
         String temp;
         int actionid;
         for(int i=0; null!=nl && i<nl.getLength(); i++)
         {
            elem = (Element)nl.item(i);
            temp = elem.getAttribute("process");
            if(!temp.equalsIgnoreCase("yes"))
               continue;

            temp = elem.getAttribute("actionid");
            actionid = Integer.parseInt(temp);
            IPSActionHandler handler = null;
            switch(actionid)
            {
               case IPSActionHandler.ACTIONID_GENERATE_TMX_RESOURCES:
                  handler = new PSGenerateTMXResourcesActionHandler();
                  break;
               case IPSActionHandler.ACTIONID_MERGE_MASTER:
                  handler = new PSMergeWithMaster();
                  break;
               case IPSActionHandler.ACTIONID_EXIT:
                     return false;
            }
            if(handler != null)
            {
               logMessage("Processing started at " + new Date());
               handler.process(elem);
               logMessage("Processing ended at " + new Date());
            }
         }
         return true;
      }
      //catch the section processing exception log message and  rethrow as
      //action processing exception
      catch(PSActionProcessingException e)
      {
         logMessage("Processing failed. Error: " + e.getMessage());
         throw e;
      }

   }

   /**
    * Helper method to display the existing languages in the Rhythmyx Content
    * Manager.
    * @throws PSFatalException if could not connect to server database and get
    * the supported Locales.
    */
   private void displayExistingLanguages()
      throws PSFatalException
   {
      PSLocaleHandler localehandler = new PSLocaleHandler();
      try
      {
         Document doc = localehandler.getLocaleDocument(m_rxroot);
         List  langstring = new ArrayList();

         List  disname = new ArrayList();
         List  desc = new ArrayList();
         List status =new ArrayList();
         getLanguageList(doc, langstring, status, disname, desc);
         System.out.println();
         System.out.println("Supported Languages:");
         System.out.println("--------------------");
         String temp = "";
         for(int i=0; langstring != null && i<langstring.size(); i++)
         {
            temp = "* " + langstring.get(i).toString();
            temp += " (" + status.get(i).toString() + ") ";
            temp += " (" + disname.get(i).toString() + ") ";
            temp += " - " + desc.get(i).toString();
            System.out.println(temp);
         }
      }
      /*
       * Can throw SQLException, PSJdbcTableFactoryException, SAXException,
       * IOException, FileNotFoundException
       */
      catch(Exception e)
      {
         System.out.println("Error encountered getting list of supported " +
            "languages from Rhythmyx Content Manager ");
         throw new PSFatalException(e.getMessage());
      }
   }

   /**
    * Helper method to get the list of languages from the Locales XML document
    * produced by the {@link PSLocaleHandler#getLocaleDocument}.
    * @param doc must not be <code>null</code>.
    * @param language may be <code>null</code> in which case a new List is
    * created.
    * @param status may be <code>null</code> in which case a new List is
    * created.
    * @param disname may be <code>null</code> in which case a new List is
    * created.
    * @param desc may be <code>null</code> in which case a new List is
    * created.
    */
   private void getLanguageList(Document doc, List language, List status,
      List disname, List desc)
   {
      if(language == null)
         language = new ArrayList();
      if(status == null)
         status = new ArrayList();
      if(disname == null)
         disname = new ArrayList();
      if(desc == null)
         desc = new ArrayList();
      if(doc == null)
         return;

      NodeList nl = doc.getElementsByTagName("column");
      Element elem = null;
      String name = null;
      String value =null;
      Node node = null;
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         name = elem.getAttribute(PSRxltConfigUtils.ATTR_NAME);
         node = elem.getFirstChild();
         value = "";
         if(node instanceof Text)
            value = ((Text)node).getData();
         if(value != null)
            value = value.trim();

         if(name.equals("LANGUAGESTRING"))
         {
            language.add(value);
         }
         else if(name.equals("STATUS"))
         {
            value = value.equals("1")?"enabled":"disabled";
            status.add(value);
         }
         else if(name.equals("DISPLAYNAME"))
         {
            disname.add(value);
         }
         else if(name.equals("DESCRIPTION"))
         {
            desc.add(value);
         }
      }
   }


   /**
    * The Configuration XML document. Never <code>null</code> after the class
    * object constructed.
    */
   Document m_CfgDocument = null;

   /**
    * The console line reader initialized when this class is loaded.
    */
   static BufferedReader ms_consoleLineReader = null;

   static InputStreamReader ms_consoleInputStreamReader = null;

   /**
    * Root directory for Rhythmyx, initialized in {@link #init()} method, never
    * <code>null</code> after that.
    */
   private String m_rxroot = null;
   
   private boolean ms_standalone = false;
   
   private static final org.apache.logging.log4j.Logger ms_logger = LogManager.getLogger(PSCommandLineProcessor.class);
   
   /**
    * String constant representing the Logfile name. Log file is always written
    * to working  directory.
    */
   private static final String LOG_FILE = "rxlt" + File.separator + "rxlt.log";

   /**
    * Easy method to log the message to the file/console. Directly writes the
    * message to the log file/condole.
    * @param msg must not be <code>null<code> or <code>empty</code>.
    */
   static void logMessage(String msg)
   {
      if (ms_logEnabled)
      {
         ms_logger.info(msg);
      }
   }

   /**
    * Easy method to log the message to the file/console. Looks up for the text
    * message pattern for the given key in the resource bundle and then formats
    * the message with supplied single argument.
    * @param key must not be <code>null<code> or <code>empty</code>.
    * @param singleArg may not be <code>null<code> or <code>empty</code>.
    */
   static void logMessage(String key, String singleArg)
   {
      if (!ms_logEnabled)
         return;
      
      Object[] args = { singleArg };
      String msgText = getRes().getString(key);
      if(msgText != null)
      {
         ms_logger.info(msgText, args);
      }
      else
         ms_logger.info(key);
   }

   /**
    * Easy method to log the message to the file/console. Looks up for the text
    * message pattern for the given key in the resource bundle and then formats
    * the message with supplied arguments.
    * @param key must not be <code>null<code> or <code>empty</code>.
    * @param args list of arguments to foratting the message text, may not be
    * <code>null<code> or <code>empty</code>.
    */
   static void logMessage(String key, Object[]args)
   {
      if (!ms_logEnabled)
         return;
      
      String msgText = getRes().getString(key);
      if(msgText != null)
      {
         ms_logger.info(msgText, args);
      }
      else
         ms_logger.info(key);
   }

   /**
    * Access method for logger object.
    * @return logger object never <code>null</code>.
    */
   static Logger getLogger()
   {
      return ms_logger;
   }

   /**
    * Get the program resources.
    * @return Java ResourceBundle object, never <code>null</code> unless the
    * loading of resources fails.
    */
   public static ResourceBundle getRes()
   {
      /* load the resources first. this will throw an exception if we can't
      find them */
      if (m_res == null)
         m_res = ResourceBundle.getBundle("com.percussion.i18n.rxlt.PSRxltMain"
         + "Resources");

      return m_res;
   }
   
   /**
    * Set if the various <code>logMessage()</code> methods will actually 
    * produce log messages.  
    * 
    * @param enabled <code>true</code> to log messages, <code>false</code> to
    * ignore them.
    */
   public static void setIsLogEnabled(boolean enabled)
   {
      ms_logEnabled = enabled;
   }
   
   /**
    * Determine if the various <code>logMessage()</code> methods will actually 
    * produce log messages.  
    * 
    * @return <code>true</code> if messages are logged, <code>false</code> if 
    * they are ignored.
    */
   public static boolean isLogEnabled()
   {
      return ms_logEnabled;
   }
   
   public static boolean areDotsEnabled()
   {
      return ms_dotsEnabled;
   }
   
   public static void setDotsEnabled(boolean enabled)
   {
      ms_dotsEnabled = enabled;
   }
   
   /**
    * Name of the default config file used by the tool. This is part of the JAR.
    */
   static final String CONFIG_FILE = "rxltconfig.xml";

   /**
    * Path of the Repository Properties file relative to the Rhythmyx root
    * directory.
    */
   public static final String REPOSITORY_PROPFILEPATH =
      "rxconfig" + File.separator + "Installer" + File.separator +
      "rxrepository.properties";
   /**
    * The program resources. You must access this variable through the {@link
    * #getRes getRes} method.
    */
   private static ResourceBundle m_res = null;
   
   /**
    * Indicates if logging is enabled.  Intially <code>true</code>, modified
    * by {@link #setIsLogEnabled(boolean)}.
    */
   private static boolean ms_logEnabled = true;
   
   private static boolean ms_dotsEnabled = true;
}
