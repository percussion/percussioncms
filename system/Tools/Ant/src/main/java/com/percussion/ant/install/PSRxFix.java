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

package com.percussion.ant.install;

import com.percussion.install.IPSUpgradeModule;
import com.percussion.install.PSLogger;
import com.percussion.rxfix.PSRxFixCmd;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * PSRxFix will run the RxFix tool with a set of modules for database
 * consistency.  The set of modules executed are defined by
 * {@link #m_fixModules}.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="rxFix"
 *              class="com.percussion.ant.install.PSRxFix"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to set the properties.
 *
 *  <code>
 *  &lt;rxFix fixModules="com.percussion.rxfix.PSFixContentStatusHistory"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSRxFix extends PSAction
{
   // see base class
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   @Override
   public void execute()
   {
      String strRootDir = getRootDir();
      PSProperties props = null;
      PSJdbcDbmsDef dbmsDef = null;

      if(strRootDir != null)
      {
         if (!(strRootDir.endsWith(File.separator)))
            strRootDir += File.separator;

         try
         {
            //Get the db info
            props = new PSProperties(strRootDir
                  + IPSUpgradeModule.REPOSITORY_PROPFILEPATH);
            props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
            dbmsDef = new PSJdbcDbmsDef(props);

            //Setup the RxFix tool
            PSRxFixCmd cmd = new PSRxFixCmd();
            cmd.setDriver(dbmsDef.getDriverClassName());
            cmd.setHost(dbmsDef.getServer());
            cmd.setName(dbmsDef.getDataBase());
            cmd.setPassword(dbmsDef.getPassword());
            cmd.setSchema(dbmsDef.getSchema());
            cmd.setUrl("jdbc:" + dbmsDef.getDriver());
            cmd.setUser(dbmsDef.getUserId());

            ArrayList<String> fixes = new ArrayList<String>();
            for (int i = 0; i < m_fixModules.length; i++)
               fixes.add(m_fixModules[i]);

            cmd.setFixes(fixes);

            //Run RxFix
            PSLogger.logInfo("#### Running RxFix ####");
            cmd.execute();

            //Log the results
            List results = cmd.getResults();

            if (results.size() == 0)
               PSLogger.logInfo("No modifications were required");
            else
            {
               for (int i=0; i < results.size(); i++)
               {
                  String result = (String) results.get(i);
                  PSLogger.logInfo(result);
               }
            }

            PSLogger.logInfo("#### Completed RxFix ####");
         }
         catch(Exception e)
         {
            PSLogger.logError("PSRxFix#execute : "
                  + e.getMessage());
            PSLogger.logError("PSRxFix#execute : "
                  + e);
         }
      }
   }

   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/

   /**
    * Accessor for the fix modules property
    */
   public String[] getFixModules()
   {
      return m_fixModules;
   }

   /**
    * Mutator for the fix modules property.
    */
   public void setFixModules(String fixModules)
   {
      m_fixModules = convertToArray(fixModules);
   }

   /***************************************************************************
    * Bean properties
    ***************************************************************************/

   /**
    * The list of RxFix modules to be executed.
    */
   private String m_fixModules[] = new String[0];

   /**************************************************************************
    * private function
    **************************************************************************/

   /**************************************************************************
    * properties
    **************************************************************************/


}
