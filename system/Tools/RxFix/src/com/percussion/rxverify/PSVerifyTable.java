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
package com.percussion.rxverify;

import com.percussion.rxverify.modules.PSJdbcTableCheck;
import com.percussion.utils.tools.PSParseArguments;
import com.percussion.utils.xml.PSEntityResolver;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author peterfrontiero
 * 
 * PSVerifyTable performs one task. It checks a given installation and
 * verifies whether or not the backend database utilized by the installation
 * conforms to the table definitions contained in sys_cmstableDef.xml.
 * These checks include table existence, primary key, index, and foreign key.
 * Any inconsistencies found are displayed to the user.
 */
public class PSVerifyTable
{
   private PSParseArguments m_arguments;
   
   /**
    * Main program, standard arguments
    * 
    * @param args the argument supplied by the JVM
    */
   public static void main(String[] args)
   {
      if (System.getProperty("log4j.configuration") == null)
      {
         System.setProperty("log4j.configuration",
               "com/percussion/rxverify/log4j.properties");
      }
      
      System.setProperty("javax.xml.parsers.SAXParserFactory",
         "com.percussion.xml.PSSaxParserFactoryImpl");
      
      Logger l = LogManager.getLogger("Main");
      PSVerifyTable it = new PSVerifyTable(args);
            
      try
      {
         it.run();
      }
      catch (Exception e)
      {
         l.error("Error occurred while running rxverify", e);
      }
   }

   /**
    * Run the verification, generating or consuming a descriptor file.
    * 
    * @throws Exception if there are problems performing a verification
    */
   private void run() throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      
      // Verify Rhythmyx directory
      List args = m_arguments.getRest();
      File rxdir = null;

      if (args.size() == 0)
      {
         l.error("Missing rhythmyx directory");
         showHelp();
         return;
      }

      if (args.size() > 0)
      {
         String r = (String) args.get(0);
         rxdir = new File(r);
         if (rxdir.exists() == false)
         {
            l.error("Given Rhythmyx Directory does not exist " + r);
            return;
         }
      }
      
      // Setup entity resolver
      PSEntityResolver resolver = PSEntityResolver.getInstance();
      resolver.setResolutionHome(rxdir);

      boolean debug = false;
      
      if (m_arguments.getArgument("debug") != null)
         debug = true;
      
      doVerifyIndexes(rxdir,debug);
      
   }   
   
   /**
    * Verify the installation database for table existence, primary key,
    * index, and foreign key.
    * 
    * @param rxdir the rhythmyx dir, assumed not <code>null</code>
    * @param debug debug flag, true for debugging
    * @throws Exception if there is a problem during verification
    */
   private void doVerifyIndexes( File rxdir, boolean debug ) throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      
      l.info("Started");
      PSJdbcTableCheck tc = new PSJdbcTableCheck();
      tc.checkTables( rxdir, debug );
      l.info("Finished");
   }
   
   /**
    * Ctor
    * 
    * @param args command line args, assumed not <code>null</code>
    */
   private PSVerifyTable(String[] args) {
      m_arguments = new PSParseArguments(args);

      if (m_arguments.isFlag("help"))
      {
         showHelp();
      }
   }

   /**
    * Print help information to console and exit
    */
   private void showHelp()
   {
      System.err.println("Usage: \njava -jar rxverifytable.jar rhythmyxdir [-help] [-debug]\n"
            + "use -help to display detailed help information\n"
            + "use -debug to turn debugging on");
            
      System.exit(0);
   }
      
}
