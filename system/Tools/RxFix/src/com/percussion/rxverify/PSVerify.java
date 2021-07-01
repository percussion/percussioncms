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

import com.percussion.rxverify.data.PSInstallation;
import com.percussion.rxverify.modules.IPSVerify;
import com.percussion.rxverify.modules.PSJdbcTableCheck;
import com.percussion.rxverify.modules.PSVerifyDatabaseTables;
import com.percussion.rxverify.modules.PSVerifyExtensions;
import com.percussion.rxverify.modules.PSVerifyInstalledFiles;
import com.percussion.rxverify.modules.PSVerifyInstallerLogs;
import com.percussion.rxverify.modules.PSVerifyXSLVersion;
import com.percussion.utils.tools.PSParseArguments;
import com.percussion.utils.xml.PSEntityResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author dougrand
 * 
 * RxVerify performs two tasks. The first is the ability to generate a bill of
 * materials for a Rhythmyx installation, using appropriate guessing to label
 * each file with the source component. The second is to check such a bill of
 * materials against a candidate installation and generate for each component a
 * reported state of installed, partially installed or uninstalled.
 */
public class PSVerify
{
   private PSParseArguments m_arguments;

   private static final String YES_ANSWERS[] =
   {"y", "yes", "1", "true"};

   private static final String NO_ANSWERS[] =
   {"n", "no", "0", "false"};

   static private final IPSVerify ms_checkers[] =
   {new PSVerifyInstallerLogs(), new PSVerifyDatabaseTables(),
    new PSVerifyExtensions(), new PSVerifyXSLVersion(),
    new PSVerifyInstalledFiles()};
      
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
      PSVerify it = new PSVerify(args);
      
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
    * @throws Exception if there are problems performing a generation, a
    *            verification or performing io on the bill of materials file
    */
   private void run() throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      
      // Verify Rhythmyx directory
      List args = m_arguments.getRest();
      File rxdir = null;

      if (args.size() == 0 && m_arguments.getArgument("list") == null)
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

      boolean foundarg = false;
      boolean debug = false;
      
      if (m_arguments.getArgument("debug") != null)
         debug = true;

      if (m_arguments.getArgument("generate") != null)
      {
         foundarg = true;
         doGenerate(rxdir);
      }

      if (m_arguments.getArgument("list") != null)
      {
         foundarg = true;
         doList();
      }

      if (m_arguments.getArgument("verify") != null)
      {
         foundarg = true;
      }
      
      if (m_arguments.getArgument("all") != null)
      {
         foundarg = true;
         doVerify(rxdir,debug);
      }
      
      if (m_arguments.getArgument("logs") != null)
      {
         foundarg = true;
         doVerify(rxdir, new PSVerifyInstallerLogs());
      }
      
      if (m_arguments.getArgument("files") != null)
      {
         foundarg = true;
         doVerify(rxdir, new PSVerifyInstalledFiles());
      }
      
      if (m_arguments.getArgument("tables") != null)
      {
         foundarg = true;
         doVerify(rxdir, new PSVerifyDatabaseTables());
      }
      
      if (m_arguments.getArgument("tableindexes") != null)
      {
         foundarg = true;
         doVerifyIndexes(rxdir,debug);
      }
      
      if (m_arguments.getArgument("xslversion") != null)
      {
         foundarg = true;
         doVerify(rxdir, new PSVerifyXSLVersion());
      }
      
      if (m_arguments.getArgument("extensions") != null)
      {
         foundarg = true;
         doVerify(rxdir, new PSVerifyExtensions());
      }
      
      if (m_arguments.getArgument("test") != null)
      {
         foundarg = true;
         doTest(rxdir);
      }

      if (!foundarg)
      {
         l.error("Bad arguments");
         showHelp();
      }

   }

   /**
    * Generate the bill of materials file.
    * @param rxdir the rhythmyx directory, assumed not <code>null</code>.
    * @throws Exception
    */
   void doGenerate(File rxdir) throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      String bomfile = m_arguments.getArgument("generate");
      if (bomfile == null || bomfile.trim().length() == 0)
      {
         l.error("Bad bill of materials argument to generate");
         return;
      }
      File bom = new File(bomfile);
      if (bom.exists() && m_arguments.isFlag("force") == false)
      {
         if (!verifyOverwrite("OK to overwrite " + bomfile + "?"))
         {
            return;
         }
      }
      PSInstallation installation = new PSInstallation();
      l.info("Generate bom from " + rxdir);
      for (int i = 0; i < ms_checkers.length; i++)
      {
         IPSVerify verifier = ms_checkers[i];
         verifier.generate(rxdir, installation);
      }
      ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(bom));
      os.writeObject(installation);
      os.close();
      l.info("Finished");
   }
   
   /**
    * Test a single module, not used by QA, runs a generate followed by a verify
    * on the given installation with just one module. The installation is 
    * never serialized to the filesystem
    * @throws Exception
    */
   private void doTest(File rxdir) throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      String module = m_arguments.getArgument("test");
      l.info("Testing "  + module);
      String moduleClassName = "com.percussion.rxverify.modules." + module;
      Class moduleClass = Class.forName(moduleClassName);
      IPSVerify verifier = (IPSVerify) moduleClass.newInstance();
      PSInstallation installation = new PSInstallation();
      l.info("Generating info");
      verifier.generate(rxdir, installation);
      l.info("Verifying info");
      verifier.verify(rxdir, null, installation);
      l.info("Finished test");
   }

   /**
    * List the information contained in the given bill of materials file.
    * 
    * @throws Exception if an error occurs reading or processing the information
    *            in the bom file.
    */
   private void doList() throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      String bomfile = m_arguments.getArgument("list");
      if (bomfile == null || bomfile.trim().length() == 0)
      {
         l.error("Bad bill of materials argument to list");
         return;
      }
      File bom = new File(bomfile);
      ObjectInputStream is = new ObjectInputStream(new FileInputStream(bom));
      PSInstallation installation = (PSInstallation) is.readObject();
      is.close();

      installation.list(l);
   }

   /**
    * Verify the installation
    * 
    * @param rxdir the rhythmyx dir, assumed not <code>null</code>
    * @param debug debug flag, true for debugging
    * @throws Exception if there is a problem during verification
    */
   private void doVerify(File rxdir,boolean debug) throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      
      l.info("Started");
      for (int i = 0; i < ms_checkers.length; i++)
      {
         IPSVerify verifier = ms_checkers[i];
         doVerify(rxdir, verifier);
      }
      doVerifyIndexes(rxdir,debug);
      l.info("Finished");
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
    * Verify the installation
    * 
    * @param rxdir the rhythmyx dir, assumed not <code>null</code>
    * @param originalRxDir the original rhythmyx dir may be <code>null</code>
    * @param installation the rhythmyx installation snapshot, assumed not <code>null</code>
    * @param verifier the specific verification object
    * 
    * @throws Exception if there is a problem during verification
    */  
   private void doVerify(File rxdir, IPSVerify verifier) throws Exception
   {
      PSInstallation installation = null;
      File originalRxDir = null;
      Logger l = LogManager.getLogger(getClass());

      String bomfile = m_arguments.getArgument("verify");
      if (bomfile == null || bomfile.trim().length() == 0)
      {
         l.error("Bad bill of materials argument to verify");
         return;
      }
      File bom = new File(bomfile);
      if (bom.exists() == false)
      {
         l.error("Bill of materials file is missing " + bom);
         return;
      }
      ObjectInputStream is = new ObjectInputStream(new FileInputStream(bom));
      installation = (PSInstallation) is.readObject();
      is.close();
      String temp = m_arguments.getArgument("original");
      originalRxDir = null;
      if (temp != null)
      {
         originalRxDir = new File(temp);
      }
      
      verifier.verify(rxdir, originalRxDir, installation);
      
   }
   
   /**
    * Output a query message and return if the user has affirmed the question
    * 
    * @param message message to show, assumed never <code>null</code> or empty
    * @return <code>true</code> if the user typed y, yes, 1 or true, and
    *         <code>false</code> if n, no, 0 or false, and it will repeat the
    *         question otherwise.
    * @throws IOException
    */
   private boolean verifyOverwrite(String message) throws IOException
   {
      System.out.println(message);
      BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

      // Loop until we get a valid answer
      while (true)
      {
         String answer = r.readLine();
         if (answer == null)
            return false;
         if (oneOf(answer, YES_ANSWERS))
            return true;
         if (oneOf(answer, NO_ANSWERS))
            return false;
         System.err.println("Please answer yes or no");
      }
   }

   /**
    * Check to see if the passed answer is one of the answers in the passed
    * array
    * 
    * @param answer The user's answer, assumed not null or empty
    * @param answers An array of possible answers that cause this method to
    *           return <code>true</code>
    * @return <code>true</code> if the answer is contained in the passed array
    *         of answers. Answers are compared in a case-insensitive manner
    */
   private boolean oneOf(String answer, String[] answers)
   {
      answer = answer.toLowerCase();
      for (int i = 0; i < answers.length; i++)
      {
         String candidate = answers[i].toLowerCase();
         if (answer.equals(candidate))
            return true;
      }
      return false;
   }

   /**
    * Ctor
    * 
    * @param args command line args, assumed not <code>null</code>
    */
   private PSVerify(String[] args) {
      m_arguments = new PSParseArguments(args);

      if (m_arguments.isFlag("help"))
      {
         showNewHelp();
      }
   }

   /**
    * Print help information to console and exit
    */
   private void showHelp()
   {
      System.err.println("Usage: \njava -jar rxverifyqa.jar rhythmyxdir "
            + "-generate billofmaterials.bom [-force]\n"
            + "java -jar rxverifyqa.jar rhythmyxdir "
            + "-verify billofmaterials.bom [-original preupgraderxdir] \n"
            + "java -jar rxverifyqa.jar -list billofmaterials.bom\n" 
            + "add -debug to any of the above to add debug logging");
      System.exit(0);
   }
   
   /**
    * Print updated help information to console and exit
    */
   private void showNewHelp()
   {
      System.err.println("Help: \n\nStep 1) Run verification checks\njava -jar rxverifyqa.jar rhythmyxdir -tableindexes"
            + "\n\nThis will check database for table existence, primary key, index, foreign key, and column"
            + "\n\nStep 2) For more thorough checks, QA use, generate .bom file\njava -jar rxverifyqa.jar rhythmyxdir "
            + "-generate billofmaterials.bom [-force]\n"
            + "\n**Note: It is only necessary to generate once\n"
            + "\nRun verification checks\n" 
            + "java -jar rxverifyqa.jar rhythmyxdir "
            + "-verify billofmaterials.bom -options \n"
            + "\n-options must be one of the following:\n"
            + "-all verifies entire installation\n"
            + "-logs verifies installation logfiles\n"
            + "-files verifies installation files\n"
            + "-tables verifies database tables for column\n"
            + "-xslversion verifies xsl version\n"
            + "-extensions verifies extensions\n"
            + "-help displays help\n"
            + "\nYou can also list the contents of the .bom file with "
            + "the following command:\n"
            + "java -jar rxverifyqa.jar -list billofmaterials.bom");
      System.exit(0);
   }
}
