/******************************************************************************
 *
 * [ PSRxInstallerBotTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installerbot;

import static com.percussion.installerbot.PSRxInstallerBot.DB_DATABASE_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.DB_DRIVER_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.DB_PASSWORD_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.DB_SCHEMA_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.DB_SERVER_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.DB_USER_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.DEFAULT_RHYTHMYX_SERVICE_NAME;
import static com.percussion.installerbot.PSRxInstallerBot.DEFAULT_RHYTHMYX_SERVICE_DESCRIPTION;
import static com.percussion.installerbot.PSRxInstallerBot.DELETE_EXISTING_INSTALLATION_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.EXPECTED_COMMAND_LINE_ARGS_COUNT;
import static com.percussion.installerbot.PSRxInstallerBot.GENERATE_VALUE_AUTOMATICALLY;
import static com.percussion.installerbot.PSRxInstallerBot.INSTALLATION_DIR_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.INSTALLATION_TYPE;
import static com.percussion.installerbot.PSRxInstallerBot.INSTALLATION_TYPE_NEW;
import static com.percussion.installerbot.PSRxInstallerBot.INSTALLATION_TYPE_UPDATE;
import static com.percussion.installerbot.PSRxInstallerBot.INSTALLER_TIMEOUT_IN_SEC_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.INSTALL_DB_PUBLISHER_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.INSTALL_FASTFORWARD_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.INSTANCE;
import static com.percussion.installerbot.PSRxInstallerBot.LAUNCH_INSTALLER_COMMAND_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.LICENSE_NUMBER_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.LOG_FILE_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.RHYTHMYX_PORT_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.RHYTHMYX_SERVICE_DESCRIPTION_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.RHYTHMYX_SERVICE_NAME_PROP;
import static com.percussion.installerbot.PSRxInstallerBot.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;

import com.percussion.installerbot.PSRxInstallerBot;

/**
 * @author Andriy Palamarchuk
 */
public class PSRxInstallerBotTest extends MockObjectTestCase
{
   /**
    * Cleans up after unit test.
    */
   @Override
   protected void tearDown() throws Exception
   {
      INSTANCE = CURRENT_INSTANCE;
   }

   /**
    * Tests {@link PSRxInstallerBot#main(String[])}.
    */
   public void testMain()
   {
      assertNotNull(INSTANCE);
      final String[] args = new String[] {};
      final Mock mockBot = new Mock(PSRxInstallerBot.class);
      INSTANCE = (PSRxInstallerBot) mockBot.proxy();
      mockBot.expects(once()).method("run").with(same(args))
            .will(returnValue(12345));
      mockBot.expects(once()).method("exit").with(eq(12345));
      main(args);
      mockBot.verify();
   }
   
   /**
    * Tests {@link PSRxInstallerBot#run(String[])} error handling on an error
    * in {@link PSRxInstallerBot#runInstaller()} it calls.
    */
   public void testRun_runInstallerFailure()
   {
      final PSRxInstallerBot bot = new PSRxInstallerBot()
      {
         
         @Override
         @SuppressWarnings("unused")
         Properties loadProperties(String propertiesFileName)
         {
            return null;
         }

         @Override
         @SuppressWarnings("unused")
         String parseConfiguration(Properties properties)
         {
            return null;
         }

         @Override
         void runInstaller() throws PSConsoleAppDriverException
         {
            throw new PSConsoleAppDriverException(MESSAGE);
         }
      };
      runAndCheckExpectedFailure(bot);
   }

   /**
    * Makes sure the expected failure in {@link PSRxInstallerBot#run(String[])}
    * happened.
    */
   private void runAndCheckExpectedFailure(final PSRxInstallerBot bot)
   {
      final StringWriter out = new StringWriter();
      bot.m_out = new PrintWriter(out);
      assertEquals(1, bot.run(createArgs(EXPECTED_COMMAND_LINE_ARGS_COUNT)));
      assertTrue(out.toString().contains(MESSAGE));
   }

   /**
    * Tests {@link PSRxInstallerBot#run(String[])} error handling on an error
    * in {@link PSRxInstallerBot#loadProperties(String)} it calls.
    */
   public void testRun_loadPropertiesFailure()
   {
      final PSRxInstallerBot bot = new PSRxInstallerBot()
      {
         
         @Override
         @SuppressWarnings("unused")
         Properties loadProperties(String propertiesFileName) throws IOException
         {
            throw new IOException(MESSAGE);
         }

         @Override
         @SuppressWarnings("unused")
         String parseConfiguration(Properties properties)
         {
            throw new AssertionError();
         }

         @Override
         @SuppressWarnings("unused")
         void runInstaller() throws PSConsoleAppDriverException
         {
            throw new AssertionError();
         }
      };
      runAndCheckExpectedFailure(bot);
   }

   
   /**
    * Tests {@link PSRxInstallerBot#run(String[])} error handling on an error
    * in {@link PSRxInstallerBot#parseConfiguration(Properties)} it calls.
    */
   public void testRun_parseConfigurationError()
   {
      final PSRxInstallerBot bot = new PSRxInstallerBot()
      {
         
         @Override
         @SuppressWarnings("unused")
         Properties loadProperties(String propertiesFileName) throws IOException
         {
            return null;
         }

         @Override
         @SuppressWarnings("unused")
         String parseConfiguration(Properties properties)
         {
            return MESSAGE;
         }

         @Override
         @SuppressWarnings("unused")
         void runInstaller() throws PSConsoleAppDriverException
         {
            throw new AssertionError();
         }
      };
      runAndCheckExpectedFailure(bot);
   }

   /**
    * Tests {@link PSRxInstallerBot#run(String[])}.
    */
   public void testRun()
   {
      final PSRxInstallerBot bot = new PSRxInstallerBot()
      {
         @Override
         void runInstaller()
         {
            runInstallerWasCalled = true;
         }

         @Override
         @SuppressWarnings("unused")
         String parseConfiguration(Properties properties)
         {
            parseConfigurationWasCalled = true;
            return null;
         }
      };
      
      // normal number of arguments
      {
         final StringWriter out = new StringWriter();
         bot.m_out = new PrintWriter(out);
         runInstallerWasCalled = false;
         final String[] args = createArgs(EXPECTED_COMMAND_LINE_ARGS_COUNT);
         args[0] = SUCCESSFUL_RHYTHMYX_SESSION1;
         assertEquals(0, bot.run(args));
         assertNull(bot.getLogFile());
         assertTrue(runInstallerWasCalled);
         assertTrue(parseConfigurationWasCalled);
         assertTrue(StringUtils.isEmpty(out.toString()));
      }
      
      // too many arguments
      {
         runInstallerWasCalled = false;
         parseConfigurationWasCalled = false;
         final StringWriter out = new StringWriter();
         bot.m_out = new PrintWriter(out);
         assertEquals(1, bot.run(
               createArgs(EXPECTED_COMMAND_LINE_ARGS_COUNT + 1)));
         assertNull(bot.getLogFile());
         assertFalse(runInstallerWasCalled);
         assertFalse(parseConfigurationWasCalled);
         assertFalse(StringUtils.isEmpty(out.toString()));
      }
      
      // no arguments
      {
         runInstallerWasCalled = false;
         final StringWriter out = new StringWriter();
         bot.m_out = new PrintWriter(out);
         assertEquals(1, bot.run(createArgs(0)));
         assertFalse(runInstallerWasCalled);
         assertFalse(parseConfigurationWasCalled);
         assertFalse(StringUtils.isEmpty(out.toString()));
      }

      // too few arguments
      {
         runInstallerWasCalled = false;
         final StringWriter out = new StringWriter();
         bot.m_out = new PrintWriter(out);
         assertEquals(1, bot.run(
               createArgs(EXPECTED_COMMAND_LINE_ARGS_COUNT - 1)));
         assertFalse(runInstallerWasCalled);
         assertFalse(parseConfigurationWasCalled);
         assertFalse(StringUtils.isEmpty(out.toString()));
      }
   }

   /**
    * Creates a sample array of command-line arguments.
    * @param argsSize number of arguments in the array.
    */
   private String[] createArgs(final int argsSize)
   {
      final String[] args = new String[argsSize];
      Arrays.fill(args, UNDEFINED);
      return args;
   }
   
   /**
    * Makes sure default installer validation is valid.
    */
   public void testValidateDefaultInstallerConfiguration() throws IOException
   {
      final PSRxInstallerBot bot = new PSRxInstallerBot()
      {
         @Override
         @SuppressWarnings("unused")
         void parseValues(Properties properties, StringBuilder message) {}
         
      };
      try(final InputStream in = bot.openDefaultConfiguration()){

         final Properties properties = new Properties();
         properties.load(in);
         assertNull(bot.parseConfiguration(properties), bot.parseConfiguration(properties));
      }

   }

   /**
    * Tests validation of configuration parameter names in
    * {@link PSRxInstallerBot#parseConfiguration(Properties)}.
    */
   public void testParseConfiguration_NamesValidation() {
      final Properties properties = createValidProperties();
      final PSRxInstallerBot bot = new PSRxInstallerBot();
      checkPropertyNameValidation(bot, properties, LOG_FILE_PROP);
      checkPropertyNameValidation(bot, properties, LAUNCH_INSTALLER_COMMAND_PROP);
      checkPropertyNameValidation(bot, properties, INSTALLATION_TYPE);
      checkPropertyNameValidation(bot, properties, INSTALLATION_DIR_PROP);
      checkPropertyNameValidation(bot, properties, INSTALLER_TIMEOUT_IN_SEC_PROP);
      checkPropertyNameValidation(bot, properties, LICENSE_NUMBER_PROP);
      checkPropertyNameValidation(bot, properties, DB_DRIVER_PROP);
      checkPropertyNameValidation(bot, properties, DB_SCHEMA_PROP);
      checkPropertyNameValidation(bot, properties, DB_DATABASE_PROP);
      checkPropertyNameValidation(bot, properties, DB_SERVER_PROP);
      checkPropertyNameValidation(bot, properties, DB_USER_PROP);
      checkPropertyNameValidation(bot, properties, DB_PASSWORD_PROP);
      checkPropertyNameValidation(bot, properties, DELETE_EXISTING_INSTALLATION_PROP);
      checkPropertyNameValidation(bot, properties, RHYTHMYX_SERVICE_NAME_PROP);
      checkPropertyNameValidation(bot, properties, RHYTHMYX_SERVICE_DESCRIPTION_PROP);
      checkPropertyNameValidation(bot, properties, RHYTHMYX_PORT_PROP);
      checkPropertyNameValidation(bot, properties, INSTALL_FASTFORWARD_PROP);
      checkPropertyNameValidation(bot, properties, INSTALL_DB_PUBLISHER_PROP);
      
      // unknown property
      {
         assertNull(bot.parseConfiguration(properties));
         final String unknownPropertyName = "Unknown.Property.Name";
         properties.setProperty(unknownPropertyName, "");
         assertTrue(StringUtils.contains(bot.parseConfiguration(properties), unknownPropertyName));
         assertEquals("", properties.remove(unknownPropertyName));
         assertNull(bot.parseConfiguration(properties));
      }
   }

   /**
    * Creates sample valid set of installation configuration parameters.
    */
   private Properties createValidProperties()
   {
      final Properties properties = new Properties();
      properties.put(LOG_FILE_PROP, "c:\\1.log");
      properties.put(LAUNCH_INSTALLER_COMMAND_PROP, "installer_command");
      properties.put(INSTALLATION_TYPE, INSTALLATION_TYPE_NEW);
      properties.put(INSTALLATION_DIR_PROP, ".");
      properties.put(INSTALLER_TIMEOUT_IN_SEC_PROP, "10");
      properties.put(LICENSE_NUMBER_PROP, "value3");
      properties.put(DB_DRIVER_PROP, "value3.2");
      properties.put(DB_SCHEMA_PROP, "value3.5");
      properties.put(DB_DATABASE_PROP, "value3.8");
      properties.put(DB_SERVER_PROP, "value4");
      properties.put(DB_USER_PROP, "value5");
      properties.put(DB_PASSWORD_PROP, "value6");
      properties.put(RHYTHMYX_SERVICE_NAME_PROP, "value7");
      properties.put(RHYTHMYX_SERVICE_DESCRIPTION_PROP, "value8");
      properties.put(RHYTHMYX_PORT_PROP, "4321");
      properties.put(INSTALL_FASTFORWARD_PROP, "false");
      properties.put(INSTALL_DB_PUBLISHER_PROP, "true");
      properties.put(DELETE_EXISTING_INSTALLATION_PROP, "false");
      return properties;
   }
   
   /**
    * Tests configuration parameters values validation in
    * {@link PSRxInstallerBot#parseConfiguration(Properties)}.
    * @throws IOException
    */
   public void testParseConfiguration_ValuesValidation() throws IOException {
      final Properties properties = createValidProperties();
      final PSRxInstallerBot bot = new PSRxInstallerBot();
      assertNull(bot.parseConfiguration(properties), bot.parseConfiguration(properties));

      // LOG_FILE_PROP
      assertEquals(bot.getLogFile(), properties.getProperty(LOG_FILE_PROP));
      checkBadValueValidation(properties, bot, LOG_FILE_PROP, "");
      
      // LAUNCH_INSTALLER_COMMAND_PROP
      assertEquals(bot.getLaunchInstallerCommand(),
            properties.getProperty(LAUNCH_INSTALLER_COMMAND_PROP));
      checkBadValueValidation(properties, bot, LAUNCH_INSTALLER_COMMAND_PROP, "");
      
      // INSTALLATION_TYPE
      {
         assertEquals(bot.getInstallationType(),
               properties.getProperty(INSTALLATION_TYPE));
         checkBadValueValidation(properties, bot, INSTALLATION_TYPE, "");
         checkBadValueValidation(properties, bot, INSTALLATION_TYPE, "sss");
      }
      
      // INSTALLATION_DIR_PROP
      {
         assertEquals(bot.getInstallationDir(),
               properties.getProperty(INSTALLATION_DIR_PROP));
         checkBadValueValidation(properties, bot, INSTALLATION_DIR_PROP, "");
         final File file = File.createTempFile("file", "tmp");
         checkBadValueValidation(properties, bot, INSTALLATION_DIR_PROP, file.getAbsolutePath());
         file.delete();
      }
      
      // INSTALLER_TIMEOUT_IN_SEC_PROP
      {
         assertEquals(Integer.toString(bot.getInstallerTimeoutInSeconds()),
               properties.getProperty(INSTALLER_TIMEOUT_IN_SEC_PROP));
         checkBadValueValidation(properties, bot, INSTALLER_TIMEOUT_IN_SEC_PROP, "");
         checkBadValueValidation(properties, bot, INSTALLER_TIMEOUT_IN_SEC_PROP, "sss");
      }
      
      // LICENSE_NUMBER_PROP
      assertEquals(bot.getLicenseNumber(),
            properties.getProperty(LICENSE_NUMBER_PROP));
      checkBadValueValidation(properties, bot, LICENSE_NUMBER_PROP, "");
      
      // DB_DRIVER_PROP
      assertEquals(bot.getDbDriver(),
            properties.getProperty(DB_DRIVER_PROP));
      checkBadValueValidation(properties, bot, DB_DRIVER_PROP, "");
      checkIgnoredDuringUpdate(properties, bot, DB_DRIVER_PROP, "");

      // DB_SCHEMA_PROP
      assertEquals(bot.getDbSchema(),
            properties.getProperty(DB_SCHEMA_PROP));
      checkBadValueValidation(properties, bot, DB_SCHEMA_PROP, "");
      checkIgnoredDuringUpdate(properties, bot, DB_SCHEMA_PROP, "");
      
      // DB_DATABASE_PROP
      assertEquals(bot.getDbDatabase(),
            properties.getProperty(DB_DATABASE_PROP));
      properties.setProperty(DB_DATABASE_PROP, "");
      assertNull(bot.parseConfiguration(properties), bot.parseConfiguration(properties));
      
      // DB_SERVER_PROP
      assertEquals(bot.getDbServer(),
            properties.getProperty(DB_SERVER_PROP));
      checkBadValueValidation(properties, bot, DB_SERVER_PROP, "");
      checkIgnoredDuringUpdate(properties, bot, DB_SERVER_PROP, "");
      
      // DB_USER_PROP
      assertEquals(bot.getDbUser(),
            properties.getProperty(DB_USER_PROP));
      checkBadValueValidation(properties, bot, DB_USER_PROP, "");
      checkIgnoredDuringUpdate(properties, bot, DB_USER_PROP, "");
      
      // DB_PASSWORD_PROP
      assertEquals(bot.getDbPassword(),
            properties.getProperty(DB_PASSWORD_PROP));
      checkBadValueValidation(properties, bot, DB_PASSWORD_PROP, "");
      checkIgnoredDuringUpdate(properties, bot, DB_PASSWORD_PROP, "");
      
      // RHYTHMYX_SERVICE_NAME_PROP
      assertEquals(bot.getRhythmyxServiceName(),
            properties.getProperty(RHYTHMYX_SERVICE_NAME_PROP));
      checkBadValueValidation(properties, bot, RHYTHMYX_SERVICE_NAME_PROP, "");
      checkIgnoredDuringUpdate(properties, bot, RHYTHMYX_SERVICE_NAME_PROP, "");
      
      // RHYTHMYX_SERVICE_DESCRIPTION_PROP
      assertEquals(bot.getRhythmyxServiceDescription(),
            properties.getProperty(RHYTHMYX_SERVICE_DESCRIPTION_PROP));
      checkBadValueValidation(properties, bot, RHYTHMYX_SERVICE_DESCRIPTION_PROP, "");
      checkIgnoredDuringUpdate(properties, bot, RHYTHMYX_SERVICE_DESCRIPTION_PROP, "");
      
      // RHYTHMYX_PORT_PROP
      assertEquals(bot.getRhythmyxPort(),
            properties.getProperty(RHYTHMYX_PORT_PROP));
      checkBadValueValidation(properties, bot, RHYTHMYX_PORT_PROP, "");
      checkBadValueValidation(properties, bot, RHYTHMYX_PORT_PROP, "sss");
      checkIgnoredDuringUpdate(properties, bot, RHYTHMYX_PORT_PROP, "sss");
      
      // INSTALL_FASTFORWARD_PROP
      {
         assertEquals(bot.getInstallFastForward(),
               Boolean.valueOf(properties.getProperty(INSTALL_FASTFORWARD_PROP)).booleanValue());
         checkBadValueValidation(properties, bot, INSTALL_FASTFORWARD_PROP, "");
         checkBadValueValidation(properties, bot, INSTALL_FASTFORWARD_PROP, "Yes");

         //can't be true during upgrade
         final String oldInstallerType =
            (String) properties.setProperty(INSTALLATION_TYPE, INSTALLATION_TYPE_UPDATE);
         checkBadValueValidation(properties, bot, INSTALL_FASTFORWARD_PROP, "true");
         properties.setProperty(INSTALLATION_TYPE, oldInstallerType);
      }
      
      // INSTALL_DB_PUBLISHER_PROP
      {
         assertEquals(bot.getInstallDbPublisher(),
               Boolean.valueOf(properties.getProperty(INSTALL_DB_PUBLISHER_PROP)).booleanValue());
         checkBadValueValidation(properties, bot, INSTALL_DB_PUBLISHER_PROP, "");
         checkBadValueValidation(properties, bot, INSTALL_DB_PUBLISHER_PROP, "Yes");
      }

      // DELETE_EXISTING_INSTALLATION
      {
         checkBadValueValidation(
               properties, bot, DELETE_EXISTING_INSTALLATION_PROP, "");
         checkBadValueValidation(
               properties, bot, DELETE_EXISTING_INSTALLATION_PROP, "Yes");

         //can't be true during upgrade
         final String oldInstallerType = (String) properties.setProperty(
               INSTALLATION_TYPE, INSTALLATION_TYPE_UPDATE);
         checkBadValueValidation(
               properties, bot, DELETE_EXISTING_INSTALLATION_PROP, "true");
         properties.setProperty(INSTALLATION_TYPE, oldInstallerType);
      }
   }

   /**
    * Checks how 
    *
    */
   public void testGenerateServiceNameDescription()
   {
      final Properties properties = createValidProperties();
      properties.setProperty(RHYTHMYX_SERVICE_NAME_PROP,
            GENERATE_VALUE_AUTOMATICALLY);
      properties.setProperty(RHYTHMYX_SERVICE_DESCRIPTION_PROP,
            GENERATE_VALUE_AUTOMATICALLY);
      final String dbDriverName = "DB Driver!";
      properties.setProperty(DB_DRIVER_PROP, dbDriverName);

      final PSRxInstallerBot bot = new PSRxInstallerBot();
      bot.parseConfiguration(properties);

      final Calendar calendar = Calendar.getInstance();
      final Date time = calendar.getTime();
      // automatically generated
      {
         final String name = bot.generateRhythmyxServiceName(time);
         final String description = bot.generateRhythmyxServiceDescription(time);
         
         final String year = Integer.toString(calendar.get(Calendar.YEAR));
         assertTrue(name.contains(year));
         assertTrue(name.contains(dbDriverName));
         assertTrue(name.contains(DEFAULT_RHYTHMYX_SERVICE_NAME));
         assertTrue(description.contains(year));
         assertTrue(description.contains(dbDriverName));
         assertTrue(description.contains(DEFAULT_RHYTHMYX_SERVICE_DESCRIPTION));
      }
      
      final String otherName = "Name !";
      final String otherDescription = "Description !";
      properties.setProperty(RHYTHMYX_SERVICE_NAME_PROP, otherName);
      properties.setProperty(RHYTHMYX_SERVICE_DESCRIPTION_PROP,
            otherDescription);
      bot.parseConfiguration(properties);

      // explicitely specified
      assertEquals(otherName, bot.generateRhythmyxServiceName(time));
      assertEquals(otherDescription,
            bot.generateRhythmyxServiceDescription(time));
   }

   /**
    * Makes sure that validation is ignored during upgrade.
    */
   private void checkIgnoredDuringUpdate(final Properties properties,
         final PSRxInstallerBot bot, final String propertyName, final String badValue)
   {
      final String oldValue = (String) properties.setProperty(propertyName, badValue);
      assertTrue(StringUtils.contains(bot.parseConfiguration(properties), propertyName));

      final String oldInstallerType =
         (String) properties.setProperty(INSTALLATION_TYPE, INSTALLATION_TYPE_UPDATE);
      assertFalse(StringUtils.contains(bot.parseConfiguration(properties), propertyName));

      properties.setProperty(INSTALLATION_TYPE, oldInstallerType);
      properties.setProperty(propertyName, oldValue);
}
   
   /**
    * Makes sure that validation catches bad configuration parameter value.
    */
   private void checkBadValueValidation(final Properties properties,
         final PSRxInstallerBot bot, final String propertyName, final String badValue)
   {
      final String oldValue = (String) properties.setProperty(propertyName, badValue);
      assertTrue(StringUtils.contains(bot.parseConfiguration(properties), propertyName));
      if (StringUtils.isNotBlank(badValue))
      {
         assertTrue(StringUtils.contains(bot.parseConfiguration(properties), badValue));
      }
      properties.setProperty(propertyName, oldValue);
      assertNull(bot.parseConfiguration(properties), bot.parseConfiguration(properties));
   }

   /**
    * Tests configuration parameter name validation.
    */
   private void checkPropertyNameValidation(final PSRxInstallerBot bot,
         final Properties properties, final String propertyName)
   {
      assertNull(bot.parseConfiguration(properties), bot.parseConfiguration(properties));
      final String propertyValue = (String) properties.remove(propertyName);
      assertNotNull(propertyValue);
      assertTrue(StringUtils.contains(bot.parseConfiguration(properties), propertyName));
      properties.put(propertyName, propertyValue);
   }
   
   /**
    * Tests {@link PSRxInstallerBot#findItemNumberInNumberedList(String, String)}
    * which makes selection from a choice presented by installer. 
    */
   public void testFindItemNumberInNumberedList() throws PSConsoleAppDriverException
   {
      final String listMarker = "list marker";
      final String item = "item 4 3 5";
      final String itemNumber = "123";
      final PSRxInstallerBot bot = new PSRxInstallerBot();
      final Mock mockDriver = new Mock(IPSConsoleAppDriver.class);
      bot.m_driver = (IPSConsoleAppDriver) mockDriver.proxy();
      
      // normal usage
      {
         final String items = listMarker + "\n 1. item 1\n " + itemNumber + ". " + item;
         mockDriver.expects(once()).method("getCurrentStandardOutContents")
               .will(returnValue(items));
         assertEquals(itemNumber, bot.findItemNumberInNumberedList(item, listMarker));
      }
      
      // no marker
      mockDriver.expects(once()).method("getCurrentStandardOutContents")
            .will(returnValue(" 1. item1\n 2. item2"));
      try
      {
         bot.findItemNumberInNumberedList(item, listMarker);
         fail();
      }
      catch (PSConsoleAppDriverException success) {}
      
      // no item found
      mockDriver.expects(once()).method("getCurrentStandardOutContents")
            .will(returnValue(listMarker + " 1. item1\n 2. item2"));
      try
      {
         bot.findItemNumberInNumberedList(item, listMarker);
         fail();
      }
      catch (PSConsoleAppDriverException success) {}
      
      mockDriver.verify();
   }

   /**
    * Makes sure bot can handle first installation scenario. 
    */
   public void testInstallationByScenario1() throws PSConsoleAppDriverException, IOException
   {
      doTestInstallationByScenario(SUCCESSFUL_RHYTHMYX_SESSION1, getScenario1Properties());
   }

   /**
    * Makes sure bot can handle second installation scenario. 
    */
   public void testInstallationByScenario2()
         throws PSConsoleAppDriverException, IOException
   {
      doTestInstallationByScenario(SUCCESSFUL_RHYTHMYX_SESSION2, getScenario2Properties());
   }

   /**
    * Makes sure bot can handle third installation scenario. 
    */
   public void testInstallationByScenario3()
         throws PSConsoleAppDriverException, IOException
   {
      doTestInstallationByScenario(SUCCESSFUL_RHYTHMYX_SESSION3, getScenario3Properties());
   }

   /**
    * Makes sure the bot handles provided installation scenario.
    */
   private void doTestInstallationByScenario(final String fileName,
         final Properties properties)
         throws IOException, FileNotFoundException, PSConsoleAppDriverException
   {
      final StringReader sessionReader = loadInstallationScenario(fileName);

      boolean wasStopCalled = false;
      int timeoutStep = 0;
      while (!wasStopCalled)
      {
         try {
            wasStopCalled = installByScenarioWithTimeout(timeoutStep,
                  sessionReader, properties);
         }
         catch (PSConsoleAppDriverException e)
         {
            if (e.isTimeOut())
            {
               assertFalse(wasStopCalled);
            }
            else
            {
               throw e;
            }
         }
         timeoutStep++;
         sessionReader.reset();
      }
      assertTrue("First steps should timeout. Current step: " + timeoutStep,
            timeoutStep >= 10);
   }

   /**
    * Configuration for installation scenario 1.
    */
   private Properties getScenario1Properties()
   {
      final Properties properties = new Properties();
      properties.setProperty(LOG_FILE_PROP, "c:/1.log");
      properties.setProperty(LAUNCH_INSTALLER_COMMAND_PROP, "C:\\rx\\windows\\jar\\install.bat");
      properties.setProperty(INSTALLATION_TYPE, "New install");
      properties.setProperty(INSTALLATION_DIR_PROP, "c:\\rx\\rx3");
      properties.setProperty(INSTALLER_TIMEOUT_IN_SEC_PROP, "120");
      properties.setProperty(LICENSE_NUMBER_PROP, "B-BAIC-HWWWVC-VUES-01");
      properties.setProperty(DB_DRIVER_PROP, "oracle:thin");
      properties.setProperty(DB_SCHEMA_PROP, "RX");
      properties.setProperty(DB_DATABASE_PROP, "");
      properties.setProperty(DB_SERVER_PROP, "@apalamarchuk:1521:orcl");
      properties.setProperty(DB_USER_PROP, "rx");
      properties.setProperty(DB_PASSWORD_PROP, "demo");
      properties.setProperty(RHYTHMYX_SERVICE_NAME_PROP, "Rhythmyx Server rx3");
      properties.setProperty(RHYTHMYX_SERVICE_DESCRIPTION_PROP,
            "Percussion Rhythmyx Server rx3");
      properties.setProperty(RHYTHMYX_PORT_PROP, "1992");

      properties.setProperty(INSTALL_FASTFORWARD_PROP, "false");
      properties.setProperty(INSTALL_DB_PUBLISHER_PROP, "true");
      properties.setProperty(DELETE_EXISTING_INSTALLATION_PROP, "false");
      return properties;
   }

   /**
    * Configuration for installation scenario 2.
    */
   private Properties getScenario2Properties()
   {
      final Properties properties = getScenario1Properties();
      properties.setProperty(DB_DRIVER_PROP, "jtds:sqlserver");
      properties.setProperty(DB_SCHEMA_PROP, "dbo");
      properties.setProperty(DB_DATABASE_PROP, "rx6");
      properties.setProperty(DB_SERVER_PROP, "//localhost");
      properties.setProperty(DB_USER_PROP, "sa");
      properties.setProperty(DB_PASSWORD_PROP, "demo");
      properties.setProperty(RHYTHMYX_SERVICE_NAME_PROP, "Rhythmyx Server 4");
      properties.setProperty(RHYTHMYX_SERVICE_DESCRIPTION_PROP, "Percussion Rhythmyx Server 4");
      properties.setProperty(INSTALL_FASTFORWARD_PROP, "true");
      return properties;
   }

   /**
    * Configuration for installation scenario 3.
    */
   private Properties getScenario3Properties()
   {
      final Properties properties = getScenario1Properties();
      properties.setProperty(INSTALLATION_TYPE, "Upgrade existing install");
      
      properties.setProperty(DB_DRIVER_PROP, "jtds:sqlserver");
      properties.setProperty(DB_SCHEMA_PROP, "dbo");
      properties.setProperty(DB_DATABASE_PROP, "rxmaster");
      properties.setProperty(DB_SERVER_PROP, "//localhost");
      properties.setProperty(DB_USER_PROP, "sa");
      properties.setProperty(DB_PASSWORD_PROP, "demo");
      properties.setProperty(RHYTHMYX_SERVICE_NAME_PROP, "Rhythmyx 4");
      properties.setProperty(RHYTHMYX_SERVICE_DESCRIPTION_PROP, "Percussion Rhythmyx");
      properties.setProperty(INSTALL_FASTFORWARD_PROP, "false");
      properties.setProperty(INSTALL_DB_PUBLISHER_PROP, "false");
      return properties;
   }

   /**
    * Reads and returns installation scenario.
    */
   private StringReader loadInstallationScenario(final String fileName) throws IOException
   {
      final BufferedReader reader =
         new BufferedReader(new FileReader(fileName));
      try
      {
         String s;
         StringBuilder sb = new StringBuilder();
         while ((s = reader.readLine()) != null)
         {
            sb.append(s);
            sb.append("\n");
         }
         return new StringReader(sb.toString());
      }
      finally
      {
         reader.close();
      }
   }

   /**
    * Makes sure installation runs according to the scenario and handles timeout
    * on the specified step.
    * @param timeoutStep step on which driver will indicate timeout.
    * @param sessionReader the scenario reader reset to the beginning.
    * @param properties scenario installation configuration.
    * @return <code>true</code> if bot terminated installation with
    * {@link IPSConsoleAppDriver#stop()}.
    */
   private boolean installByScenarioWithTimeout(final int timeoutStep,
         final Reader sessionReader, final Properties properties)
         throws PSConsoleAppDriverException
   {
      final boolean wasStopCalled;
      final PSRxInstallerBot bot = new PSRxInstallerBot()
      {
         @Override
         protected void sleepOverExpectJBug()
         {
         }
      };
      final MockConsoleAppDriver mockConsoleAppDriver =
         new MockConsoleAppDriver(sessionReader, timeoutStep);
      assertFalse(mockConsoleAppDriver.mi_wasStopCalled);
      bot.m_driver = mockConsoleAppDriver;
      
      bot.configureAndRunInstaller(properties);
      
      assertTrue(mockConsoleAppDriver.mi_wasStopCalled);
      wasStopCalled = mockConsoleAppDriver.mi_wasStopCalled; 
      return wasStopCalled;
   }
   
   /**
    * <p>{@link IPSConsoleAppDriver} for testing. Reproduces actual communication
    * session with a console application using slightly changed
    * log of previous interaction with console application in following format</p>
    * <p>Any text is interpreted as if returned by console application except
    * user input - text starting with &gt;&gt;%gt; to the end of line including
    * new line character.</p>
    */
   private static class MockConsoleAppDriver implements IPSConsoleAppDriver
   {
      /**
       * Creates new driver.
       * @param sessionScript session script to reproduce.
       * @param timeoutStep step os the script upon which to indicate timeout.
       */
      public MockConsoleAppDriver(final Reader sessionScript, final int timeoutStep)
      {
         this.mi_sessionScript = new BufferedReader(sessionScript);
         this.mi_timeoutStep = timeoutStep;
      }
      
      /**
       * does nothing.
       */
      @SuppressWarnings("unused")
      public void launchApplication(final String command, final long defaultTimeoutInSec) {}

      /**
       * Validates expectation is met according to the scenario.
       * It is assumed that this method is called when match which always
       * occurs is expected.
       * @see IPSConsoleAppDriver#expect(String)
       */
      public void expect(final String pattern) throws PSConsoleAppDriverException
      {
         final String appOutput = expectStep(pattern);
         if (!isLastExpectTimeOut())
         {
            mi_currentStep++;
         }
         assertTrue(
               "Pattern \"" + pattern + "\" was not found in application " +
               "output:\"" + appOutput + "\"",
               appOutput.contains(pattern));
      }

      /**
       * It is assumed that this method is called when match may or may not
       * occur is expected. The mock makes sure that the next call after call
       * to this method must be {@link #isLastExpectTimeOut()}, not any of the
       * expect methods.
       * This method does not affect on number of steps passed.
       */
      public void expect(String pattern,
            @SuppressWarnings("unused") long timeoutInSec)
            throws PSConsoleAppDriverException
      {
         final String appOutput = expectStep(pattern);
         m_insureTimeoutCheck = true;
         m_isTimeout = !appOutput.contains(pattern);
      }

      /**
       * Performs next expect search through the scenario.
       */
      private String expectStep(final String pattern) throws PSConsoleAppDriverException
      {
         assertFalse(m_insureTimeoutCheck);
         assertFalse(isClosed());
         assertTrue(mi_currentStep <= mi_timeoutStep);
         final String appOutput;
         try
         {
            appOutput = readApplicationOutput(pattern);
         }
         catch (IOException e)
         {
            throw new PSConsoleAppDriverException(e);
         }
         return appOutput;
      }

      /**
       * Returns part of the scenario file corresponding to the installer output.
       * This is the portion ending or on user/bot input or on the specified pattern.
       * @param pattern pattern to expect
       */
      private String readApplicationOutput(final String pattern) throws IOException
      {
         mi_currentStr = "";
         final StringBuilder appOutput = new StringBuilder();
         while (!isClosed()
               && !mi_currentStr.contains(USER_INPUT_MARKER)
               && !mi_currentStr.contains(pattern))
         {
            appOutput.append(mi_currentStr);
            readNextLine();
         }
         if (mi_currentStr != null)
         {
            appOutput.append(mi_currentStr);
         }
         return appOutput.toString();
      }

      /**
       * Reads next line from the scenario.
       */
      private void readNextLine() throws IOException
      {
         mi_currentStr = mi_sessionScript.readLine();
         if (mi_currentStr != null)
         {
            mi_currentStr += "\n";
            mi_stdOut.append(mi_currentStr);
         }
      }

      /**
       * Validates input is provided to the installer according to the scenario.
       * @see IPSConsoleAppDriver#send(String)
       */
      public void send(String line) throws PSConsoleAppDriverException
      {
         assertTrue(line.endsWith("\n"));
         assertEquals(line, getLastUserInput());
      }

      /**
       * Returns next user input from the scenario.
       */
      private String getLastUserInput() throws PSConsoleAppDriverException
      {
         assertFalse(isClosed());
         if (!mi_currentStr.contains(USER_INPUT_MARKER))
         {
            try
            {
               readApplicationOutput(USER_INPUT_MARKER);
            }
            catch (IOException e)
            {
               throw new PSConsoleAppDriverException(e);
            }
         }
         assertTrue(mi_currentStr.contains(USER_INPUT_MARKER));
         return mi_currentStr.substring(userInputIdx());
      }

      /**
       * Index of user input start in {@link #mi_currentStr}.
       */
      private int userInputIdx()
      {
         return mi_currentStr.indexOf(USER_INPUT_MARKER) + USER_INPUT_MARKER.length();
      }

      /**
       * {@link IPSConsoleAppDriver#stop()} implementation
       * validating that application is terminated according to the scenario.
       * Tip: comment out call to this method in
       * {@link PSRxInstallerBot#runInstaller()} to debug a failure.
       */
      public void stop()
      {
         try
         {
            readApplicationOutput(USER_INPUT_MARKER);
         }
         catch (IOException e)
         {
            throw new AssertionError(e);
         }
         // could be trigged by exception when stop() is called
         // during exception handling finally clause.
         // temporarily comment out next line to debug the original cause
         assertTrue("Current str: \"" + mi_currentStr + "\". Standard out:\n"
               + getCurrentStandardOutContents(), isClosed() || isLastExpectTimeOut());
         mi_wasStopCalled = true;
      }

      /**
       * {@link IPSConsoleAppDriver#isLastExpectTimeOut()} timeout implementation
       * which returns timeout indicator according to the specified timeout step.
       */
      public boolean isLastExpectTimeOut()
      {
         if (m_insureTimeoutCheck)
         {
            m_insureTimeoutCheck = false;
            return m_isTimeout;
         }
         assertTrue("Current step: " + mi_currentStep +
               " timeout step: " + mi_timeoutStep, mi_currentStep <= mi_timeoutStep);
         return mi_currentStep == mi_timeoutStep;
      }

      /**
       * @see IPSConsoleAppDriver#isClosed() 
       */
      public boolean isClosed()
      {
         return mi_currentStr == null;
      }

      /**
       * Should not be called.
       * @see IPSConsoleAppDriver#getLogFile()
       */
      public String getLogFile()
      {
         throw new AssertionError();
      }

      /**
       * Does nothing.
       */
      @SuppressWarnings("unused")
      public void setLogFile(String logFile) {}

      /**
       * Returns standard output up to the current point.
       */
      public String getCurrentStandardOutContents()
      {
         return mi_stdOut.toString();
      }
      /**
       * String marking beginning of user input. User input begins until and
       * including end of line.
       */
      private static final String USER_INPUT_MARKER = ">>>";
      
      /**
       * An installation script.
       */
      private final BufferedReader mi_sessionScript;
      
      /**
       * Current line is being read from the script. Is <code>null</code> when
       * no more script can be read.
       */
      private String mi_currentStr = "";
      
      /**
       * Becomes <code>true</code> only when {@link #stop()} was called.
       */
      private boolean mi_wasStopCalled;
      
      /**
       * Standard output.
       */
      private StringBuilder mi_stdOut = new StringBuilder();

      /**
       * Step on which return timeout.
       */
      private int mi_timeoutStep;
      
      /**
       * Current step.
       */
      private int mi_currentStep;
      
      /**
       * If <code>true</code> it is expected that next method call will be to
       * {@link #isLastExpectTimeOut()}, not to any of expect methods.
       */
      private boolean m_insureTimeoutCheck;
      
      /**
       * Value returned by {@link #isLastExpectTimeOut()} when
       * {@link #m_insureTimeoutCheck} is <code>true</code>.
       */
      private boolean m_isTimeout;
   }

   /**
    * Current instance of {@link PSRxInstallerBot#INSTANCE} singleton variable.
    * Is used to temporarily save this instance while replacing it with testing
    * value.
    */
   private final PSRxInstallerBot CURRENT_INSTANCE = INSTANCE;
   
   /**
    * Indicates whether {@link PSRxInstallerBot#runInstaller()} method was called.
    */
   private boolean runInstallerWasCalled;
   
   /**
    * Indicates whether {@link PSRxInstallerBot#parseConfiguration(Properties)}
    * method was called.
    */
   private boolean parseConfigurationWasCalled;
   
   /**
    * First Rhythmyx installation session file name.
    */
   private static final String SUCCESSFUL_RHYTHMYX_SESSION1 =
         "./UnitTestResources/com/percussion/installerbot/successfulRhythmyxInstallation1.txt";
   
   /**
    * Second Rhythmyx installation session file name.
    */
   private static final String SUCCESSFUL_RHYTHMYX_SESSION2 =
         "./UnitTestResources/com/percussion/installerbot/successfulRhythmyxInstallation2.txt";
   
   /**
    * Second Rhythmyx installation session file name.
    */
   private static final String SUCCESSFUL_RHYTHMYX_SESSION3 =
         "./UnitTestResources/com/percussion/installerbot/successfulRhythmyxInstallation3.txt";
   
   /**
    * Undefined value string.
    */
   private static final String UNDEFINED = "Undefined";
   
   /**
    * Sample message string.
    */
   private static final String MESSAGE = "Mesasage !";
}
