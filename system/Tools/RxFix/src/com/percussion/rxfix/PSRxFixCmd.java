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
package com.percussion.rxfix;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.tools.PSParseArguments;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.spi.NamingManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

/**
 * Command for running rxfix from the command line (or from ant using the java
 * task)
 * 
 * @author dougrand
 */
public class PSRxFixCmd
{

   private static final Logger log = LogManager.getLogger(PSRxFixCmd.class);

   /**
    * Template to create install xml beans file
    */
   public static PSStringTemplate ms_template = new PSStringTemplate(
         "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n"
               + "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
               + "       xmlns:aop=\"http://www.springframework.org/schema/aop\"\n"
               + "       xmlns:tx=\"http://www.springframework.org/schema/tx\"\n"
               + "       xsi:schemaLocation=\"\n"
               + "   http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n"
               + "   http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.0.xsd\n"
               + "   http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n"
               + "  <bean id=\"sys_protoLegacyDataSource\"\n"
               + "     class=\"com.percussion.util.PSDataSourceFactory\"\n"
               + "     factory-method=\"createDataSource\" >\n"
               + "     <constructor-arg>\n"
               + "        <props>\n"
               + "           <prop key=\"driverClassName\">{driver}</prop>\n"
               + "           <prop key=\"url\">{url}</prop>\n"
               + "{props}"
               + "        </props>\n"
               + "     </constructor-arg>\n"
               + "  </bean>\n"
               + "  <bean id=\"sys_mockJndiContextHelper\" \n"
               + "     class=\"com.percussion.utils.jndi.PSNamingContextHelper\">\n"
               + "     <!-- Set root before bindings! -->\n"
               + "     <property name=\"root\">\n"
               + "        <value>java:comp/env</value>\n"
               + "     </property>\n"
               + "     <property name=\"bindings\">\n"
               + "        <map>\n"
               + "           <entry key=\"jdbc/rxdefault\">\n"
               + "              <ref local=\"sys_protoLegacyDataSource\"/>\n"
               + "           </entry>\n"
               + "        </map>\n"
               + "     </property>\n"
               + "  </bean>\n"
               + "   <bean id=\"sys_datasourceManager\"\n"
               + "      class=\"com.percussion.services.datasource.impl.PSDatasourceManager\">\n"
               + "      <property name=\"datasourceResolver\">\n"
               + "         <ref bean=\"sys_datasourceResolver\" />\n"
               + "      </property>\n"
               + "   </bean>\n"
               + "   <bean id=\"sys_connectionHelper\"\n"
               + "      class=\"com.percussion.utils.jdbc.PSConnectionHelper\"\n"
               + "      factory-method=\"createInstance\">\n"
               + "      <constructor-arg>\n"
               + "         <ref local=\"sys_datasourceManager\" />\n"
               + "      </constructor-arg>\n"
               + "   </bean>"
               + "  <bean id=\"rhythmyxinfo\"\n"
               + "     class=\"com.percussion.services.general.impl.PSRhythmyxInfo\">\n"
               + "     <property name=\"bindings\">\n"
               + "        <map>\n"
               + "           <entry key=\"UNIT_TESTING\">\n"
               + "              <value>true</value>\n"
               + "           </entry>\n"
               + "        </map>\n"
               + "     </property>\n"
               + "  </bean>\n"
               + "  <bean id=\"sys_datasourceResolver\"\n"
               + "     class=\"com.percussion.services.datasource.PSDatasourceResolver\">\n"
               + "     <property name=\"repositoryDatasource\" value=\"rxdefault\"/>\n"
               + "     <property name=\"datasourceConfigurations\">\n"
               + "        <list>\n"
               + "             <bean id=\"rxdefault\" class=\"com.percussion.services.datasource.PSDatasourceConfig\">\n"
               + "              <property name=\"name\" value=\"rxdefault\"/>\n"
               + "              <property name=\"dataSource\" value=\"jdbc/rxdefault\"/>\n"
               + "              <property name=\"database\" value=\"{name}\"/>\n"
               + "              <property name=\"origin\" value=\"{schema}\"/>\n"
               + "             </bean>\n"
               + "        </list>\n"
               + "     </property>\n"
               + "  </bean>\n"
               + "  <bean id=\"sys_hibernateDialects\"\n"
               + "     class=\"com.percussion.services.datasource.PSHibernateDialectConfig\">\n"
               + "     <property name=\"dialects\">\n"
               + "        <map>\n"
               + "                <entry key=\"jtds:sqlserver\">\n"
               + "                    <value>org.hibernate.dialect.SQLServerDialect</value>\n"
               + "                </entry>\n"
               + "                <entry key=\"inetdae7\">\n"
               + "                    <value>org.hibernate.dialect.SQLServerDialect</value>\n"
               + "                </entry>\n"
               + "                <entry key=\"oracle:thin\">\n"
               + "                    <value>org.hibernate.dialect.Oracle9iDialect</value>\n"
               + "                </entry>\n"
               + "                <entry key=\"db2\">\n"
               + "                    <value>org.hibernate.dialect.DB2Dialect</value>\n"
               + "                </entry>\n"
               + "                <entry key=\"mysql\">\n"
               + "                    <value>org.hibernate.dialect.MySQLDialect</value>\n"
               + "                </entry>\n"
               + "        </map>\n"
               + "     </property>    \n"
               + "  </bean>  \n"
               + "   <bean id=\"sys_roleMgr\"\n"
               + "      class=\"com.percussion.services.security.impl.PSRoleMgr\">\n"
               + "      <property name=\"subjectCatalogers\">\n"
               + "         <list>\n"
               + "            <bean id=\"mockSubjectCataloger\" class=\"com.percussion.services.security.test.PSMockSubjectCataloger\">\n"
               + "               <property name=\"name\" value=\"Test Subject Cat\"/>\n"
               + "               <property name=\"description\" value=\"A test subject cataloger\"/>\n"
               + "               <property name=\"supportsGroups\" value=\"false\"/>\n"
               + "            </bean>\n"
               + "            <bean id=\"mockSubjectCatalogerGroup\" class=\"com.percussion.services.security.test.PSMockSubjectCataloger\">\n"
               + "               <property name=\"name\" value=\"Test Subject Cat Group\"/>\n"
               + "               <property name=\"description\" value=\"A test subject cataloger with group support\"/>\n"
               + "               <property name=\"supportsGroups\" value=\"true\"/>\n"
               + "            </bean>\n"
               + "         </list>\n"
               + "      </property>    \n"
               + "      <property name=\"roleCatalogers\">\n"
               + "         <list>\n"
               + "            <bean id=\"sys_mockRoleCataloger\" class=\"com.percussion.services.security.test.PSMockRoleCataloger\">\n"
               + "               <property name=\"name\" value=\"Test Role Cat\"/>\n"
               + "               <property name=\"description\" value=\"A test role cataloger\"/>\n"
               + "            </bean>\n" + "         </list>\n"
               + "      </property>          \n" + "   </bean>\n"
               + "</beans>\n");

   /**
    * The user name for the database connection
    */
   private String m_user;

   /**
    * The password for the database connection
    */
   private String m_password;

   /**
    * The jdbc connection url for the database connection
    */
   private String m_url;

   /**
    * The host for the database connection
    */
   private String m_host;

   /**
    * The schema for the database connection
    */
   private String m_schema;

   /**
    * The database name for the database connection
    */
   private String m_name;

   /**
    * The driver for the database connection
    */
   private String m_driver;
   
   /**
    * The external driver location
    */
   private String m_driverLocation;

   /**
    * The list of fix modules
    */
   private List<String> m_fixes = new ArrayList<String>();

   /**
    * The list of results are string representations of <code>PSFixResult</code>
    * objects.
    */
   private List<String> m_results = new ArrayList<String>();

   /**
    * Gets the list of results, never <code>null</code>, may be empty.
    * 
    * @return the list of results, never <code>null</code>, may be empty. See
    *         {@link PSFixResult#toString()} for more information.
    */
   public List<String> getResults()
   {
      return m_results;
   }

   /**
    * @param driver The driver to set.
    */
   public void setDriver(String driver)
   {
      m_driver = driver;
   }

   /**
    * @param host The host to set.
    */
   public void setHost(String host)
   {
      m_host = host;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      m_name = name;
   }

   /**
    * @param password The password to set.
    */
   public void setPassword(String password)
   {
      m_password = password;
   }

   /**
    * @param schema The schema to set.
    */
   public void setSchema(String schema)
   {
      m_schema = schema;
   }

   /**
    * @param url The url to set.
    */
   public void setUrl(String url)
   {
      m_url = url;
   }

   /**
    * @param user The user to set.
    */
   public void setUser(String user)
   {
      m_user = user;
   }

   /**
    * @param driverLocation The driver location to set.
    */
   public void setDriverLocation(String driverLocation)
   {
      m_driverLocation = driverLocation;
   }
   
   /**
    * @param fixnames
    */
   public void setFixes(List<String> fixnames)
   {
      m_fixes = fixnames;
   }

   /**
    * Setup the fix and execute
    * 
    * @param args
    */
   @SuppressWarnings("unchecked")
   public static void main(String[] args)
   {
      PSRxFixCmd cmd = new PSRxFixCmd();
      PSParseArguments pargs = new PSParseArguments(args);
      cmd.setDriver(pargs.getArgument("driver"));
      cmd.setHost(pargs.getArgument("host"));
      cmd.setName(pargs.getArgument("name"));
      cmd.setPassword(pargs.getArgument("password"));
      cmd.setSchema(pargs.getArgument("schema"));
      cmd.setUrl(pargs.getArgument("url"));
      cmd.setUser(pargs.getArgument("user"));
      cmd.setFixes(pargs.getRest());

      cmd.execute();
   }

   /**
    * Do the fixes. First setup the Spring configuration, then run the fix or
    * fixes
    */
   public void execute()
   {
      // Setup the spring config for the install
      boolean using_jtds = m_driver.contains("jtds");
      File installbeans = null;

      try
      {
         installbeans = File.createTempFile("install", ".xml");
         Writer w = new FileWriter(installbeans);
         Map<String, String> vars = new HashMap<String, String>();
         vars.put("driver", m_driver);
         if (m_name != null && !m_name.equals("null"))
         {
            vars.put("name", m_name);
         }
         vars.put("schema", m_schema);
         if (using_jtds)
         {
            vars.put("url", m_url + ":" + m_host + ";user=" + m_user
                  + ";password=" + m_password);
         }
         else
         {
            vars.put("url", m_url + ":" + m_host);
            
            String propsStr = "           <prop key=\"username\">" + m_user
                  + "</prop>\n" + "           <prop key=\"password\">"
                  + m_password + "</prop>\n";
            
            if (PSJdbcUtils.isExternalDriver(PSJdbcUtils.getDriverFromUrl(
                  vars.get("url"))))
            {
               propsStr += "<prop key=\"database\">" + m_name + "</prop>\n"
                  + "<prop key=\"driverLocation\">" + m_driverLocation
                  + "</prop>\n";
            }
            
            vars.put("props", propsStr);
         }
         w.write(ms_template.expand(vars));
         w.close();

         String ctxfiles[] = new String[1];
         // ctxfiles[1] = "ear/config/spring/server-beans.xml";
         ctxfiles[0] = installbeans.getAbsolutePath();

         // The naming context builder can only be set once
         if (!NamingManager.hasInitialContextFactoryBuilder())
            NamingManager
                  .setInitialContextFactoryBuilder(new SimpleNamingContextBuilder());

         PSBaseServiceLocator.initCtx(ctxfiles);

         // Do fixes
         for (String fix : m_fixes)
         {
            Class fc = getClass().getClassLoader().loadClass(fix);
            IPSFix finstance = (IPSFix) fc.newInstance();
            finstance.fix(false);
            logAndSaveInfo("Fix: " + fix);
            List<PSFixResult> results = finstance.getResults();
            for (PSFixResult result : results)
               logAndSaveInfo(result.toString());
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         logAndSaveError("Problem creating install beans: "
               + e.getLocalizedMessage());
      }

      if (installbeans != null)
      {
         installbeans.delete();
      }

   }
   
   /**
    * Logs output to System.out and adds to list of results.
    * 
    * @param str the text to be logged, assumed not <code>null</code>.
    */
   private void logAndSaveInfo(String str)
   {
      System.out.println(str);
      m_results.add(str);
   }
   
   /**
    * Logs output to System.err and adds to list of results.
    * 
    * @param str the text to be logged, assumed not <code>null</code>.
    */
   private void logAndSaveError(String str)
   {
      System.err.println(str);
      m_results.add(str);
   }
}
