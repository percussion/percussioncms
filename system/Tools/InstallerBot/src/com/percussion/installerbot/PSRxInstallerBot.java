/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.installerbot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Console applications which performs automatic installation
 * of Rhythmyx server.
 *
 * @author Andriy Palamarchuk
 * @todo use generics, StringBuffer when backwards compatibility
 * with 1.4 is not required anymore. (Andriy) 
 */
public class PSRxInstallerBot
{
   /**
    * Standalone invocation entry point.
    */
   public static void main(String[] args)
   {
      final int result = INSTANCE.run(args);
      INSTANCE.exit(result);
   }
   
   /**
    * Exits the program and returns the specified result to the OS.
    */
   public void exit(int result)
   {
      System.exit(result);
   }

   /**
    * Instance entry point. To be directly called from {@link #main(String[])}.
    * Minimizes logic in {@link #main(String[])}.
    * @param args command-line arguments as received by {@link #main(String[])}.
    * @return value to be returned by the program to OS on exit.
    */
   public int run(String[] args)
   {
      if (args.length != EXPECTED_COMMAND_LINE_ARGS_COUNT)
      {
         m_out.println("Unexpected number of arguments. "
               + "Expected " + EXPECTED_COMMAND_LINE_ARGS_COUNT
               + " but got " + args.length);
         printUsage();
         return 1;
      }
      final String configurationFileName = args[0];
      final Properties propreties;
      try
      {
          propreties = loadProperties(configurationFileName);
      }
      catch (IOException e)
      {
         printExceptionError(e);
         return 1;
      }
      try
      {
         return configureAndRunInstaller(propreties);
      }
      catch (PSConsoleAppDriverException e)
      {
         printExceptionError(e);
         return 1;
      }
   }

   /**
    * Runs installer using parameters provided in specified properties.
    * @param properties installation and installer parameters.
    * Can't be <code>null</code>.
    * @return code to be returned by the program to OS on exit.
    * @throws PSConsoleAppDriverException on installation session failure.
    */
   int configureAndRunInstaller(final Properties properties)
         throws PSConsoleAppDriverException
   {
      final String message;
      message = parseConfiguration(properties);
      if (message != null) {
         m_out.println(message);
         return 1;
      }
      runInstaller();
      return 0;
   }

   /**
    * Prints exception description to program output. 
    */
   private void printExceptionError(final Exception e)
   {
      m_out.println("Installation error");
      e.printStackTrace(m_out);
   }

   /**
    * Loads properties from the specified file.
    * @param propertiesFileName properties file to load.
    * Should be valid file name.
    * @return properties from the properties file.
    * @throws FileNotFoundException if file not found.
    * @throws IOException on file reading failure.
    */
   Properties loadProperties(final String propertiesFileName) throws IOException
   {
      final Properties properties = new Properties();
      try(FileInputStream fs = new FileInputStream(propertiesFileName)){
         properties.load(fs);
      }
      return properties;
   }

   /**
    * Prints usage of the automated installation program to program output.
    */
   private void printUsage()
   {
      m_out.println(
            "Rhythmyx Installer runner should be called with one parameter -");
      m_out.println(
            "file name pointing to the property file storing configuration.");
      m_out.println(
            "The property file should have following properties.");
      m_out.println("You can copy and paste text below to create configuration "
            + "property file.\n\n");

      try(BufferedReader reader =
                  new BufferedReader(new InputStreamReader(openDefaultConfiguration())))
      {
         String s;
         while ((s = reader.readLine()) != null)
         {
            m_out.println(s);
         }
      }
      catch (IOException e)
      {
         assert false : e;
      }
   }

   /**
    * Returns stream to the default configuration resource.
    */
   InputStream openDefaultConfiguration()
   {
      return getClass().getResourceAsStream(
            "defaultRxInstallerBotConfig.properties");
   }
   
   /**
    * Launches and runs installation process. The bot should be configured.
    * @throws PSConsoleAppDriverException on installation failure.
    */
   void runInstaller() throws PSConsoleAppDriverException
   {
      m_driver.launchApplication(
            getLaunchInstallerCommand(), getInstallerTimeoutInSeconds());
      try {
         passWelcomePage();
         reviewAgreementPage();
         acceptAgreement();
         specifyInstallationType();
         specifyInstallationDirectory();
         specifyProductsToInstall();
         specifyPublisherFeatures();
         
         if (getInstallFastForward())
         {
            specifyFastForwardFeatures();
         }
         specifyServerType();
         enterLicense();
         if (isNewInstallation())
         {
            specifyDatabase();
            nameRhythmyxService();
            
            m_driver.expect(
                  "The service name you have specified already exists",
                  SHORT_WAIT);

            if (m_driver.isLastExpectTimeOut())
            {
               expectInOutput(RHYTHMYX_SERVER_SETTINGS_PATTERN);
               expectInOutput(BIND_PORT_PATTERN);
            }
            else
            {
               confirmDefaultChoice();
               gotoNextPage();
               expect(RHYTHMYX_SERVER_SETTINGS_PATTERN);
               expect(BIND_PORT_PATTERN);
               sleepOverExpectJBug();
            }

            configureRhythmyxServer();
         }
         
         if (isNewInstallation())
         {
            configureSearchEngine();
         }
         if (isNewInstallation())
         {
            configureApplicationServer();
         }
         
         confirmInstallationConfiguration();
         watchInstallation();
         readReadme();
         confirmSuccessfulCompletion();
      }
      finally
      {
         m_driver.stop();
      }
   }

   /**
    * This is a workaround for ExpectJ bug.
    * If there is a match in {@link IPSConsoleAppDriver#expect(String, long)}
    * it bombs out later.
    * Ideally needs to be further researched.
    */
   protected void sleepOverExpectJBug()
   {
      try
      {
         Thread.sleep(SHORT_WAIT * MILISECONDS_IN_SEC);
      }
      catch (InterruptedException ignore)
      {
         ignore.printStackTrace();
      }
   }

   /**
    * Handles installer completion report. 
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void confirmSuccessfulCompletion() throws PSConsoleAppDriverException
   {
      expect("InstallShield Wizard has successfully installed Suite");
      confirmDefaultChoice();
   }

   /**
    * Handles installer Readmy output.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void readReadme() throws PSConsoleAppDriverException
   {
      expect("Please read the information below.");
      expect("README for Percussion Rhythmyx");
      expect("[Press Enter]");
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Monitors installation progress as reported by installer.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void watchInstallation() throws PSConsoleAppDriverException
   {
      expect("Querying Rhythmyx Service status...");
      expect("Querying Rhythmyx Application Server Service status...");
      expect("Connecting to");
      expect(" extension:");
      if (isUpdateInstallation())
      {
         // sometimes installer asks whether it should overwrite JVM.
         // this is a confirmation of default choice (No), so the bot
         // won't have to recognize this conditional interaction
         confirmDefaultChoice();
      }
   }

   /**
    * Monitors installer report on installation target. 
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void confirmInstallationConfiguration()
      throws PSConsoleAppDriverException
   {
      expect("Suite will be installed in the following location");
      gotoNextPage();
   }

   /**
    * Provides to the installer parameters of application server configuration.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void configureApplicationServer() throws PSConsoleAppDriverException
   {
      expect("Application Server Settings");
      
      expect("Naming Service Port.");
      confirmDefaultChoice();
      expect("Naming Service RMI Port.");
      confirmDefaultChoice();
      expect("JRMP Invoker Service RMI Port.");
      confirmDefaultChoice();
      expect("Pooled Invoker Service Port.");
      confirmDefaultChoice();
      expect("JMS UIL2 Service Port.");
      confirmDefaultChoice();
      expect("AJP 1.3 Service Port.");
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Specifies search engine configuration to installer.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void configureSearchEngine() throws PSConsoleAppDriverException
   {
      expect("Rhythmyx Full Text Search Settings.");
      expect("host name");
      confirmDefaultChoice();
      expect("Install Location");
      confirmDefaultChoice();
      expect("Search and index port number");
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Specifies Rhythmyx server configuration to installer.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void configureRhythmyxServer() throws PSConsoleAppDriverException
   {
      m_driver.send(getRhythmyxPort() + "\n");
      gotoNextPage();
      skipWarning();
   }

   private void skipWarning() throws PSConsoleAppDriverException
   {
      expect("1. Don't repeat this warning");
      expect("Each port number in this installation must be unique.");
      confirmDefaultChoice();
   }
   

   /**
    * Specifies Rhythmyx service configuration to the installer.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void nameRhythmyxService() throws PSConsoleAppDriverException
   {
      expect("Rhythmyx Service Properties.");
      expect("Rhythmyx Service Name");
      final Date date = new Date();
      m_driver.send(generateRhythmyxServiceName(date) + "\n");
      expect("Rhythmyx Service Description");
      m_driver.send(generateRhythmyxServiceDescription(date) + "\n");
      gotoNextPage();
   }

   /**
    * Configures database as requested by installer.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void specifyDatabase() throws PSConsoleAppDriverException
   {
      expect(REPOSITORY_DATABASE_SELECTION_PATTERN);
      expect("Jdbc driver");
      m_driver.send(
            findItemNumberInNumberedList(getDbDriver(),
                  REPOSITORY_DATABASE_SELECTION_PATTERN) + "\n");
      gotoNextPage();
      expect(REPOSITORY_DATABASE_SELECTION_PATTERN);
      expect("Database Server:");
      m_driver.send(getDbServer() + "\n");
      expect("Login ID");
      m_driver.send(getDbUser() + "\n");
      expect("Password");
      m_driver.send(getDbPassword() + "\n");
      gotoNextPage();
      expect(SCHEMA_OWNER_PATTERN);
      expect(FIRST_ITEM_PATTERN);
      m_driver.send(findItemNumberInNumberedList(getDbSchema(),
            SCHEMA_OWNER_PATTERN) + "\n");
      if (!StringUtils.isEmpty(getDbDatabase()))
      {
         expect(FIRST_ITEM_PATTERN);
         m_driver.send(
            findItemNumberInNumberedList(getDbDatabase(), "  Database") + "\n");
      }
      expect("Please provide a name for this datasource configuration");
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * In the spawned process output finds list of items delimited in the
    * beginning by <code>driversListMarker</code> parameter, finds choice
    * specified by <code>choiceName</code> and returns its number in the list.
    * @throws PSConsoleAppDriverException if data format expectations
    * are not met,
    * match is not found.
    */
   String findItemNumberInNumberedList(String choiceName, String list)
         throws PSConsoleAppDriverException
   {
      final String outStr = m_driver.getCurrentStandardOutContents();
      final String choices =
         StringUtils.substringAfterLast(outStr, list);
      if (StringUtils.isEmpty(choices))
      {
         throw new PSConsoleAppDriverException(
               "Was not able to find list marker \""
               + list + "\" in current output:\n" + outStr);
      }
      final String[] lines = choices.split("\n");
      for (int i = 0; i < lines.length; i++)
      {
         final String s = lines[i].trim();
         final Matcher matcher = m_numberedChoicePattern.matcher(s);
         if (s.endsWith(" " + choiceName) && matcher.find())
         {
            assert matcher.groupCount() == 1 : s;
            assert StringUtils.isNotBlank(matcher.group());
            return matcher.group(1);
         }
      }
      
      throw new PSConsoleAppDriverException(
            "Requested choice \"" + choiceName + "\" "
            + "was not found in list:\n\"" + choices + "\"");
   }

   /**
    * Provides installer with licensing information.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void enterLicense() throws PSConsoleAppDriverException
   {
      expect("Enter your license number");
      m_driver.send(getLicenseNumber() + "\n");
      expect("Enter your installation code.");
      m_driver.send(getLicenseNumber() + "\n");
      gotoNextPage();
      expect("You are licensed");
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Provides server type to the installer.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void specifyServerType() throws PSConsoleAppDriverException
   {
      expect("Rhythmyx Server Type");
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Convenience function to approve installer's default choice.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void confirmDefaultChoice() throws PSConsoleAppDriverException
   {
      m_driver.send("\n");
   }

   /**
    * Chooses FastForward features to install.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void specifyFastForwardFeatures() throws PSConsoleAppDriverException
   {
      assert getInstallFastForward();
      expect("Select the features for \"Rhythmyx FastForward");
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Chooses Publisher features to install.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void specifyPublisherFeatures() throws PSConsoleAppDriverException
   {
      expect(SELECT_PUBLISHER_FEATURES);
      expect("1.  [ ] Database Publisher");
      if (getInstallDbPublisher())
      {
         m_driver.send(1 + "\n");
         expect(SELECT_PUBLISHER_FEATURES);
         expect("1.  [x] Database Publisher");
      }
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Makes installer to install required products.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void specifyProductsToInstall() throws PSConsoleAppDriverException
   {
      expect(SELECT_PRODUCTS_PATTERN);
      if (isNewInstallation())
      {
         if (!getInstallFastForward())
         {
            expect("7. [x] Rhythmyx FastForward (New Install Only)");
            m_driver.send("7\n");
            expect("1. Deselect 'Rhythmyx FastForward");
            m_driver.send(1 + "\n");
            expect(SELECT_PRODUCTS_PATTERN);
         }
      }
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Provides installer with the installation directory.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void specifyInstallationDirectory()
         throws PSConsoleAppDriverException
   {
      if (isUpdateInstallation())
      {
         expect(INSTALLATION_DIRECTORY_PATTERN);
         expect(FIRST_ITEM_PATTERN);
         m_driver.send(
               findItemNumberInNumberedList("Other...",
                     INSTALLATION_DIRECTORY_PATTERN) + "\n");
         gotoNextPage();
      }

      expect(INSTALLATION_DIRECTORY_PATTERN);
      m_driver.send(getInstallationDir() + "\n");
      gotoNextPage();

      if (isNewInstallation())
      {
         expect("The directory does not exist.  Do you want to create it?");
         confirmDefaultChoice();
      }
   }

   /**
    * Provides installer with installation type.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void specifyInstallationType() throws PSConsoleAppDriverException
   {
      expect(NEW_INSTALL_PATTERN);
      expect(FIRST_ITEM_PATTERN);
      m_driver.send(
            findItemNumberInNumberedList(getInstallationType(),
                  "Choose Installation Type") + "\n");
      gotoNextPage();
   }

   /**
    * Accepts agreement as presented by installer.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void acceptAgreement() throws PSConsoleAppDriverException
   {
      final String ACCEPT = "I accept";
      expect(ACCEPT);
      m_driver.send("1\n");
      expect(ACCEPT);
      confirmDefaultChoice();
      gotoNextPage();
   }

   /**
    * Reviews presented by installer agreement page.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void reviewAgreementPage() throws PSConsoleAppDriverException
   {
      expect("SOFTWARE LICENSE AND SUPPORT AGREEMENT");
      m_driver.send("q\n");
   }

   /**
    * Calls <code>expect</code> method of the driver while handling timeout.
    */
   private void expect(final String pattern) throws PSConsoleAppDriverException
   {
      m_driver.expect(pattern);
      if (m_driver.isLastExpectTimeOut())
      {
         reportTimeout(pattern);
      }
   }
   
   /**
    * Makes sure the provided patter already exists in installer output.
    * @param pattern the pattern to search for.
    * @throws PSConsoleAppDriverException if the pattern was not foudn.
    */
   private void expectInOutput(final String pattern)
         throws PSConsoleAppDriverException
   {
      if (!m_driver.getCurrentStandardOutContents().contains(pattern))
      {
         reportTimeout(pattern);
      }
   }

   /**
    * Throws an exception notifying user that provided pattern was not found.
    * @param pattern the pattern to report.
    * @throws PSConsoleAppDriverException the exception reporting the failure.
    */
   private void reportTimeout(final String pattern)
         throws PSConsoleAppDriverException
   {
      final PSConsoleAppDriverException e =
         new PSConsoleAppDriverException("Pattern \"" + pattern +
               "\" was not found in the installer output");
      e.setTimeOut(true);
      throw e;
   }
   
   /**
    * Handles Welcome installer page.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void passWelcomePage() throws PSConsoleAppDriverException
   {
      expect("Welcome to the InstallShield wizard for Rhythmyx.");
      confirmDefaultChoice();
   }

   /**
    * Makes installer to go to next installation page.
    * @throws PSConsoleAppDriverException on installation failure
    */
   private void gotoNextPage() throws PSConsoleAppDriverException
   {
      expect(NEXT_PAGE_CONFIRMATION);
      confirmDefaultChoice();
   }

   /**
    * Validates and sets installer and installation parameters.
    * @return <code>null</code> if configuration is parsed without problems.
    * Othewise returns error message.
    */
   String parseConfiguration(final Properties properties)
   {
      final StringBuffer message = new StringBuffer();
      checkForUnrecognizedKeys(properties, message);
      checkForMissingKeys(properties, message);
      if (message.length() == 0)
      {
         parseValues(properties, message);
      }
      return message.length() == 0 ? null : message.toString();
   }

   /**
    * Validates and sets installation parameter values.
    * @param properties the installation configuration.
    * @param message buffer to add validation error messages to.
    */
   void parseValues(final Properties properties, final StringBuffer message)
   {
      setLogFile(properties.getProperty(LOG_FILE_PROP));
      setLaunchInstallerCommand(
            properties.getProperty(LAUNCH_INSTALLER_COMMAND_PROP));
      readInstallationTypeProperty(properties, message);

      for (Iterator<String> i = getMandatoryProperties().iterator();
            i.hasNext();)
      {
         final String property = i.next();
         propertyMustNotBeBlank(properties, message, property);
      }

      if (message.length() != 0)
      {
         return;
      }
      
      readInstallationDirectoryProperty(properties, message);
      readTimeoutProperty(properties, message);
      setLicenseNumber(properties.getProperty(LICENSE_NUMBER_PROP));
      setDbDriver(properties.getProperty(DB_DRIVER_PROP));
      setDbSchema(properties.getProperty(DB_SCHEMA_PROP));
      setDbDatabase(properties.getProperty(DB_DATABASE_PROP));
      setDbServer(properties.getProperty(DB_SERVER_PROP));
      setDbUser(properties.getProperty(DB_USER_PROP));
      setDbPassword(properties.getProperty(DB_PASSWORD_PROP));
      setRhythmyxServiceName(
            properties.getProperty(RHYTHMYX_SERVICE_NAME_PROP));
      setRhythmyxServiceDescription(
            properties.getProperty(RHYTHMYX_SERVICE_DESCRIPTION_PROP));

      readRhythmyxPort(properties, message);
      
      readFastForward(properties, message);

      validateBooleanProperty(properties, INSTALL_DB_PUBLISHER_PROP, message);
      setInstallDbPublisher(Boolean.valueOf(
            properties.getProperty(INSTALL_DB_PUBLISHER_PROP)).booleanValue());
      
      readDeleteExistingInstallation(properties, message);
   }

   /**
    * Reads value of property {@link #RHYTHMYX_PORT_PROP}.
    */
   private void readRhythmyxPort(Properties properties, StringBuffer message)
   {
      if (isNewInstallation())
      {
         validatePortNumber(properties, RHYTHMYX_PORT_PROP, message);
         setRhythmyxPort(properties.getProperty(RHYTHMYX_PORT_PROP));
      }
   }

   /**
    * Names of properties which must have non-blank values.
    */
   private Set<String> getMandatoryProperties()
   {
      final Set<String> mandatoryProperties =
            new HashSet<String>(CONFIGURATION_PROPERTY_NAMES);
      mandatoryProperties.remove(DB_DATABASE_PROP);
      if (isUpdateInstallation())
      {
         mandatoryProperties.remove(DB_DRIVER_PROP);
         mandatoryProperties.remove(DB_SCHEMA_PROP);
         mandatoryProperties.remove(DB_SERVER_PROP);
         mandatoryProperties.remove(DB_USER_PROP);
         mandatoryProperties.remove(DB_PASSWORD_PROP);
         mandatoryProperties.remove(RHYTHMYX_SERVICE_NAME_PROP);
         mandatoryProperties.remove(RHYTHMYX_SERVICE_DESCRIPTION_PROP);
         mandatoryProperties.remove(RHYTHMYX_PORT_PROP);
      }
      return mandatoryProperties;
   }

   /**
    * Makes sure boolean property has valid value.
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param propertyName configuration parameter name to validate.
    * @param message buffer to add validation error messages to.
    */
   private void validateBooleanProperty(final Properties properties,
         final String propertyName, StringBuffer message)
   {
      final String value = properties.getProperty(propertyName);
      if (!value.equalsIgnoreCase(Boolean.TRUE.toString()) &&
            !value.equalsIgnoreCase(Boolean.FALSE.toString()))
      {
         message.append("Property " + propertyName + " can have only values \""
               + Boolean.TRUE + "\" or \"" + Boolean.FALSE
               + "\" but found value \"" + value);
         message.append("\n");
      }
   }

   /**
    * Makes sure the string presents valid port number.
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param propertyName configuration parameter name to validate.
    * @param message buffer to add validation error messages to.
    */
   private void validatePortNumber(final Properties properties,
         final String propertyName, StringBuffer message)
   {
      final String value = properties.getProperty(propertyName);
      if (!StringUtils.isNumeric(value))
      {
         message.append("Numeric value is expected for port number "
               + "specified by property" + propertyName
               + " but found \"" + value + "\".\n");
      }
   }

   /**
    * Validates and sets install fast forward property.
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param message buffer to add validation error messages to.
    */
   private void readFastForward(final Properties properties,
         final StringBuffer message)
   {
      validateBooleanProperty(properties, INSTALL_FASTFORWARD_PROP, message);
      setInstallFastForward(Boolean.valueOf(
            properties.getProperty(INSTALL_FASTFORWARD_PROP)).booleanValue());
      if (getInstallFastForward() && isUpdateInstallation())
      {
         message.append(INSTALL_FASTFORWARD_PROP
               + " property can't be \"true\" during upgrade.\n");
      }
   }

   /**
    * Validates a property indicating whether to remove existing installation.
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param message buffer to add validation error messages to.
    */
   private void readDeleteExistingInstallation(final Properties properties,
         final StringBuffer message)
   {
      validateBooleanProperty(
            properties, DELETE_EXISTING_INSTALLATION_PROP, message);
      final boolean delete =  Boolean.valueOf(properties.getProperty(
            DELETE_EXISTING_INSTALLATION_PROP)).booleanValue();
      if (delete && isUpdateInstallation())
      {
         message.append(DELETE_EXISTING_INSTALLATION_PROP + " property "
               + "can't be \"true\" during upgrade.\n");
      }
   }

   /**
    * Validates and sets timeout value.
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param message buffer to add validation error messages to.
    */
   private void readTimeoutProperty(Properties properties, StringBuffer message)
   {
      final String value =
            properties.getProperty(INSTALLER_TIMEOUT_IN_SEC_PROP);
      if (StringUtils.isNumeric(value))
      {
         setInstallerTimeoutInSeconds(Integer.parseInt(value));
      }
      else
      {
         message.append("Was not able to interpret value \""
               + value +
               "\" for configuration property "
               + INSTALLER_TIMEOUT_IN_SEC_PROP
               + ". Please specify valid numeric value.\n");
      }
   }
   
   /**
    * Validates and sets installation type value.
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param message buffer to add validation error messages to.
    */
   private void readInstallationTypeProperty(final Properties properties,
         final StringBuffer message)
   {
      final String str = properties.getProperty(INSTALLATION_TYPE); 
      if (!str.equals(INSTALLATION_TYPE_NEW)
            && !str.equals(INSTALLATION_TYPE_UPDATE))
      {
         message.append("Installation type \"" + str
               + "\" specified by configuration property "
               + INSTALLATION_TYPE + " was not recognized. "
               + "Please specify valid installation type.\n");
      }
      setInstallationType(properties.getProperty(INSTALLATION_TYPE));
   }

   /**
    * Validates and sets installation directory value.
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param message buffer to add validation error messages to.
    */
   private void readInstallationDirectoryProperty(final Properties properties,
         final StringBuffer message)
   {
      final String dirStr = properties.getProperty(INSTALLATION_DIR_PROP); 
      final File dir = new File(dirStr);
      if (dir.exists() && !dir.isDirectory())
      {
         message.append("Installation directory \"" + dirStr
               + "\" specified by configuration property "
               + INSTALLATION_DIR_PROP + " was not found. "
               + "Please specify valid directory.\n");
      }
      setInstallationDir(dirStr);
   }

   /**
    * Makes sure the property has non-blank value.
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param propertyName configuration parameter name to validate.
    * @param message buffer to add validation error messages to.
    */
   private void propertyMustNotBeBlank(final Properties properties,
         final StringBuffer message, final String propertyName)
   {
      if (StringUtils.isBlank(properties.getProperty(propertyName)))
      {
         message.append(
               "Please specify valid value for property " + propertyName);
         message.append("\n");
      }
   }

   /**
    * Makes sure all the mandatory configuration parameters are present. 
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param message buffer to add validation error messages to.
    */
   private void checkForMissingKeys(Properties properties, StringBuffer message)
   {
      final Set<String> missingKeys =
            new HashSet<String>(CONFIGURATION_PROPERTY_NAMES);
      missingKeys.removeAll(properties.keySet());
      if (!missingKeys.isEmpty())
      {
         message.append("Missing configuration properties: ");
         message.append(missingKeys);
         message.append("\n");
      }
   }

   /**
    * Checks for non-recognized configuration parameters. 
    * In case of validation error
    * adds error messages to the <code>message</code> parameter.
    * @param properties the configuration parameters
    * @param message buffer to add validation error messages to.
    */
   @SuppressWarnings("unchecked")
   private void checkForUnrecognizedKeys(final Properties properties,
         StringBuffer message)
   {
      final Set<String> unrecognizedKeys = new HashSet(properties.keySet());
      unrecognizedKeys.removeAll(CONFIGURATION_PROPERTY_NAMES);
      if (!unrecognizedKeys.isEmpty())
      {
         message.append("Unrecognized configuration properties: ");
         message.append(unrecognizedKeys);
         message.append("\n");
      }
   }
   
   /**
    * Generates Rhythmyx service name based on value returned by
    * {@link #getRhythmyxServiceName()}.
    * @param date date to use for auto-generated name.
    * Assumed to be not <code>null</code>.
    * @return {@link #getRhythmyxServiceName()}. If that method returns
    * {@link #GENERATE_VALUE_AUTOMATICALLY} returns value composed of
    * {@link #DEFAULT_RHYTHMYX_SERVICE_NAME}, date/time and db driver.
    * Never blank.
    */
   String generateRhythmyxServiceName(final Date date)
   {
      return getRhythmyxServiceName().equals(GENERATE_VALUE_AUTOMATICALLY)
            ? DEFAULT_RHYTHMYX_SERVICE_NAME + getInstallationSuffix(date)
            : getRhythmyxServiceName();
   }

   /**
    * Generates Rhythmyx service description based on value returned by
    * {@link #getRhythmyxServiceDescription()}.
    * @param date date to use for auto-generated description.
    * Assumed to be not <code>null</code>.
    * @return {@link #getRhythmyxServiceDescription()}. If that method returns
    * {@link #GENERATE_VALUE_AUTOMATICALLY} returns value composed of
    * {@link #DEFAULT_RHYTHMYX_SERVICE_DESCRIPTION}, date/time and db driver.
    * Never blank.
    */
   String generateRhythmyxServiceDescription(final Date date)
   {
      final String serviceDescription = getRhythmyxServiceDescription();
      return serviceDescription.equals(GENERATE_VALUE_AUTOMATICALLY)
            ? DEFAULT_RHYTHMYX_SERVICE_DESCRIPTION + getInstallationSuffix(date)
            : serviceDescription;
   }

   /**
    * Generates string unique to installation if <code>date</code>
    * is unique.
    * @param date the time installation runs on.
    * @return string composed of date/time, current db driver.
    */
   private String getInstallationSuffix(final Date date)
   {
      final FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
      return " " + format.format(date) + " - " + getDbDriver();
   }

   /**
    * Installation session log file. Passed to the driver.
    * Set from configuration property.
    * @see IPSConsoleAppDriver#setLogFile(String)
    */
   String getLogFile()
   {
      return m_driver.getLogFile();
   }

   /**
    * @see #getLogFile()
    */
   private void setLogFile(final String logFile)
   {
      m_driver.setLogFile(logFile);
   }

   /**
    * Command to run in order to start installer.
    * Set from configuration property.
    */
   String getLaunchInstallerCommand()
   {
      return m_launchInstallerCommand;
   }

   /**
    * @see #getLaunchInstallerCommand()
    */
   private void setLaunchInstallerCommand(String launchInstallerCommand)
   {
      m_launchInstallerCommand = launchInstallerCommand;
   }

   /**
    * Returns <code>true</code> when installation type returned by
    * {@link #getInstallationType()} is {@link #INSTALLATION_TYPE_NEW}.
    */
   private boolean isNewInstallation()
   {
      return getInstallationType().equals(INSTALLATION_TYPE_NEW);
   }

   /**
    * Returns <code>true</code> when installation type returned by
    * {@link #getInstallationType()} is {@link #INSTALLATION_TYPE_UPDATE}.
    */
   private boolean isUpdateInstallation()
   {
      return getInstallationType().equals(INSTALLATION_TYPE_UPDATE);
   }

   /**
    * Installation type (whether new or update).
    * Set from configuration property.
    */
   String getInstallationType()
   {
      return m_installationType;
   }

   /**
    * @see #getInstallationType()
    */
   private void setInstallationType(String installationType)
   {
      m_installationType = installationType;
   }

   /**
    * Directory to install Rhythmyx to.
    * Set from configuration property.
    */
   String getInstallationDir()
   {
      return m_installationDir;
   }

   /**
    * @see #getInstallationDir()
    */
   private void setInstallationDir(String installationDir)
   {
      m_installationDir = installationDir;
   }

   /**
    * Time in seconds after which installation times out.
    * Set from configuration property.
    */
   int getInstallerTimeoutInSeconds()
   {
      return m_installerTimeoutInSeconds;
   }

   /**
    * @see #getInstallerTimeoutInSeconds()
    */
   private void setInstallerTimeoutInSeconds(int installerTimeoutInSeconds)
   {
      m_installerTimeoutInSeconds = installerTimeoutInSeconds;
   }

   /**
    * Rhythmyx license number to pass to the installer.
    * Set from configuration property.
    */
   String getLicenseNumber()
   {
      return m_licenseNumber;
   }

   /**
    * @see #getLicenseNumber()
    */
   private void setLicenseNumber(String licenseNumber)
   {
      m_licenseNumber = licenseNumber;
   }

   /**
    * Database driver to choose from choices presented by installer. 
    * Set from configuration property.
    */
   String getDbDriver()
   {
      return m_dbDriver;
   }

   private void setDbDriver(String dbDriver)
   {
      this.m_dbDriver = dbDriver;
   }

   /**
    * Database schema/owner to provide to installer.
    * Set from configuration property.
    */
   String getDbSchema()
   {
      return m_dbSchema;
   }

   private void setDbSchema(String dbSchema)
   {
      this.m_dbSchema = dbSchema;
   }

   /**
    * Database name (for some DB servers only) to provide to installer.
    * Set from configuration property.
    */
   String getDbDatabase()
   {
      return m_dbDatabase;
   }

   private void setDbDatabase(String dbDatabase)
   {
      m_dbDatabase = dbDatabase;
   }

   /**
    * Database driver to choose from choices presented by installer. 
    * Set from configuration property.
    */
   String getDbServer()
   {
      return m_dbServer;
   }

   private void setDbServer(String dbServer)
   {
      m_dbServer = dbServer;
   }

   /**
    * Database user connecting to DB to provide to installer.
    * Set from configuration property.
    */
   String getDbUser()
   {
      return m_dbUser;
   }

   private void setDbUser(String dbUser)
   {
      m_dbUser = dbUser;
   }

   /**
    * DB password to provide to installer. 
    * Set from configuration property.
    */
   String getDbPassword()
   {
      return m_dbPassword;
   }

   private void setDbPassword(String dbPassword)
   {
      m_dbPassword = dbPassword;
   }

   /**
    * Rhythmyx service name to provide to installer.
    * Set from configuration property.
    */
   String getRhythmyxServiceName()
   {
      return m_rhythmyxServiceName;
   }

   private void setRhythmyxServiceName(String rhythmyxServiceName)
   {
      m_rhythmyxServiceName = rhythmyxServiceName;
   }

   /**
    * Rhythmyx service description to provide to installer.
    * Set from configuration property.
    */
   String getRhythmyxServiceDescription()
   {
      return m_rhythmyxServiceDescription;
   }

   private void setRhythmyxServiceDescription(String rhythmyxServiceDescription)
   {
      m_rhythmyxServiceDescription = rhythmyxServiceDescription;
   }

   /**
    * Port Rhythmyx server will run on to provide to installer.
    * Set from configuration property.
    */
   String getRhythmyxPort()
   {
      return m_rhythmyxPort;
   }

   private void setRhythmyxPort(String rhythmyxPort)
   {
      m_rhythmyxPort = rhythmyxPort;
   }

   /**
    * Whether to install FastForward. Passed to installer.
    * Set from configuration property.
    */
   boolean getInstallFastForward()
   {
      return m_installFastForward;
   }

   private void setInstallFastForward(boolean installFastForward)
   {
      m_installFastForward = installFastForward;
   }

   /**
    * Whether to install database publisher. Passed to installer.
    * Set from configuration property.
    */
   boolean getInstallDbPublisher()
   {
      return m_installDbPublisher;
   }

   private void setInstallDbPublisher(boolean installDbPublisher)
   {
      m_installDbPublisher = installDbPublisher;
   }


   /**
    * The Rhythmyx Server Settings section pattern to expect.
    */
   private static final String RHYTHMYX_SERVER_SETTINGS_PATTERN =
         "Rhythmyx Server Settings";
   
   /**
    * The bind port pattern to expect.
    */
   private static final String BIND_PORT_PATTERN = "bindPort";

   /**
    * Number of milliseconds in a second.
    */
   private static final int MILISECONDS_IN_SEC = 1000;

   /**
    * Repository database selection pattern to expect.
    */
   private static final String REPOSITORY_DATABASE_SELECTION_PATTERN =
         "Repository Database Selection";

   /**
    * The pattern for the first item in the presented list.
    */
   private static final String FIRST_ITEM_PATTERN = "[1]";

   /**
    * The pattern for the schema/ower field.
    */
   private static final String SCHEMA_OWNER_PATTERN = "Schema/Owner";

   /**
    * Title for the Select Products page.
    */
   private static final String SELECT_PRODUCTS_PATTERN =
         "Select the products you would like to install";

   /**
    * The string to expect for the installation directory prompt.
    */
   private static final String INSTALLATION_DIRECTORY_PATTERN =
         "Installation Directory";

   /**
    * The pattern for the "New install" option of the installation type page.
    */
   private static final String NEW_INSTALL_PATTERN = "New install";

   /**
    * Number of command-line arguments {@link #main(String[])} expects. 
    */
   static final int EXPECTED_COMMAND_LINE_ARGS_COUNT = 1;
   /**
    * Pattern for the next page confirmation.
    */
   private static final String NEXT_PAGE_CONFIRMATION = "Press 1 for Next";
   /**
    * Pattern for the publisher features page.
    */
   private static final String SELECT_PUBLISHER_FEATURES =
         "Select the features for \"Rhythmyx Publisher\" you would like to " +
         "install";

   // configuration property names
   /**
    * Property name for console session log file.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String LOG_FILE_PROP = "log.file";

   /**
    * Property name for command to run installer.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String LAUNCH_INSTALLER_COMMAND_PROP = "installer.command";
   
   /**
    * Property name for installation type (whether new installation or update).
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String INSTALLATION_TYPE = "installation.type";

   /**
    * Property name for directory where to install Rhythmyx.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String INSTALLATION_DIR_PROP = "installation.dir";

   /**
    * Property name for whole installtaion session timeout (in seconds).
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String INSTALLER_TIMEOUT_IN_SEC_PROP = "installation.timeout";

   /**
    * Property name for Rhythmyx license number.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String LICENSE_NUMBER_PROP = "license.number";

   /**
    * Property name for database driver.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String DB_DRIVER_PROP = "db.driver";

   /**
    * Property name for Rhythmyx database schema.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String DB_SCHEMA_PROP = "db.schema";

   /**
    * Property name for Rhythmyx database.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String DB_DATABASE_PROP = "db.database";

   /**
    * Property name for database server name.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String DB_SERVER_PROP = "db.server";

   /**
    * Property name for database user.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String DB_USER_PROP = "db.user";

   /**
    * Property name for database user password.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String DB_PASSWORD_PROP = "db.password";

   /**
    * Property name for Rhythmyx service name.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String RHYTHMYX_SERVICE_NAME_PROP = "rhythmyx.service.name";

   /**
    * Property name for Rhythmyx service description.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String RHYTHMYX_SERVICE_DESCRIPTION_PROP =
         "rhythmyx.service.desc";

   /**
    * Property name for Rhythmyx server port.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String RHYTHMYX_PORT_PROP = "rhythmyx.port";

   /**
    * Property name for indicator whether to install FastForward.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String INSTALL_FASTFORWARD_PROP = "install.fastforward";
   
   /**
    * Property name for the indicator whether to delete existing installation.
    * Installbot does nothing with this value except making sure it exists
    * and is valid.
    * Can't be <code>true</code> on update.
    */
   static final String DELETE_EXISTING_INSTALLATION_PROP =
         "delete.existing.installation";

   /**
    * Property name for indicator whether to install database publisher.
    * See defaultRxInstallerBotConfig.properties in this package for details.
    */
   static final String INSTALL_DB_PUBLISHER_PROP = "install.db.publisher";

   /**
    * All the configuration property names.
    */
   private static final Set<String> CONFIGURATION_PROPERTY_NAMES =
         new HashSet<String>(Arrays.asList(new String[]
         {LOG_FILE_PROP, LAUNCH_INSTALLER_COMMAND_PROP,
               INSTALLATION_TYPE,
               INSTALLATION_DIR_PROP, INSTALLER_TIMEOUT_IN_SEC_PROP,
               LICENSE_NUMBER_PROP,
               DB_DRIVER_PROP, DB_SCHEMA_PROP, DB_DATABASE_PROP, DB_SERVER_PROP,
               DB_USER_PROP, DB_PASSWORD_PROP,
               RHYTHMYX_SERVICE_NAME_PROP,
               RHYTHMYX_SERVICE_DESCRIPTION_PROP, RHYTHMYX_PORT_PROP,
               INSTALL_FASTFORWARD_PROP,
               INSTALL_DB_PUBLISHER_PROP,
               DELETE_EXISTING_INSTALLATION_PROP}));
   
   /**
    * This is a new installation - one of the values property
    * {@link #getInstallationType()} can accept.
    */
   static final String INSTALLATION_TYPE_NEW = NEW_INSTALL_PATTERN;

   /**
    * This is an upgrade - one of the values property
    * {@link #getInstallationType()} can accept.
    */
   static final String INSTALLATION_TYPE_UPDATE = "Upgrade existing install";
   
   /**
    * Property value indicating that data specified by this property should
    * be automatically generated.
    */
   static final String GENERATE_VALUE_AUTOMATICALLY = "auto";
   
   /**
    * Default Rhythmyx service name as suggested by installer.
    */
   static final String DEFAULT_RHYTHMYX_SERVICE_NAME = "Rhythmyx Server";
   
   /**
    * Short wait in seconds for the installer response.
    */
   private static final long SHORT_WAIT = 10; 

   /**
    * Default Rhythmyx service description as suggested by installer.
    */
   static final String DEFAULT_RHYTHMYX_SERVICE_DESCRIPTION =
         "Percussion Rhythmyx Server";
   
   /**
    * Singleton instance.
    */
   static PSRxInstallerBot INSTANCE = new PSRxInstallerBot();
   
   /**
    * Program output. Usually corresponds to {@link System#out}
    */
   PrintWriter m_out = new PrintWriter(System.out, true);
   
   /**
    * Object providing access to the installer.
    */
   IPSConsoleAppDriver m_driver = new PSExpectJConsoleAppDriver();

   /**
    * Regex pattern used to extract choice number from the list of choices
    * provided by installer (e.g. list of DB drivers).
    */
   private final Pattern m_numberedChoicePattern =
         Pattern.compile("(\\d+)\\.\\s");
   
   /**
    * @see #getLaunchInstallerCommand()
    */
   private String m_launchInstallerCommand;
   
   /**
    * @see #getInstallationType()
    */
   private String m_installationType;
   
   /**
    * @see #getInstallationDir()
    */
   private String m_installationDir;
   
   /**
    * @see #getInstallerTimeoutInSeconds()
    */
   private int m_installerTimeoutInSeconds;
   
   /**
    * @see #getLicenseNumber()
    */
   private String m_licenseNumber;
   
   /**
    * @see #getDbDriver()
    */
   private String m_dbDriver;
   
   /**
    * @see #getDbSchema()
    */
   private String m_dbSchema;
   
   /**
    * @see #getDbDatabase()
    */
   private String m_dbDatabase;
   
   /**
    * @see #getDbDriver()
    */
   private String m_dbServer;
   
   /**
    * @see #getDbUser()
    */
   private String m_dbUser;
   
   /**
    * @see #getDbPassword()
    */
   private String m_dbPassword;
   
   /**
    * @see #getRhythmyxServiceName()
    */
   private String m_rhythmyxServiceName;
   
   /**
    * @see #getRhythmyxServiceDescription()
    */
   private String m_rhythmyxServiceDescription;
   
   /**
    * @see #getRhythmyxPort()
    */
   private String m_rhythmyxPort;
   
   /**
    * @see #getInstallFastForward()
    */
   private boolean m_installFastForward;
   
   /**
    * @see #getInstallDbPublisher()
    */
   private boolean m_installDbPublisher;
}
