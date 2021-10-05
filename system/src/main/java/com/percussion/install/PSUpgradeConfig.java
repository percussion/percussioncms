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
package com.percussion.install;

import com.percussion.util.PSProperties;
import com.percussion.xml.PSXPathEvaluator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/*
 * PSUpgradeConfig class is used to parse the config xml file and filters the
 * modules. That means when we pass the configuration file to its constructor,
 *
 */
public class PSUpgradeConfig implements IPSUpgradeConfig
{

   private static final Logger log = LogManager.getLogger(PSUpgradeConfig.class);

   /*
    * Constuctor of this class.
    * @param configFile Name of the configuration file.
    *    May not be <code>null<code>.
    */
   public PSUpgradeConfig(Document configDoc)
      throws IOException, SAXException
   {
      loadConfig(configDoc);
   }
   
   /*
    * Constuctor of this class.
    * @param configFile Name of the configuration file.
    *    May not be <code>null<code>.
    */
   public PSUpgradeConfig(String configFile)
      throws FileNotFoundException, IOException, SAXException
   {
      if(configFile==null || configFile.equals(""))
      {
         throw (new IllegalArgumentException("configFile may not be null or empty."));
      }

      RxUpgradeLog.logIt("About to load upgrade plugins from config file: " + configFile);

      //Load the configFile into Document configDoc.
      Document configDoc = PSXmlDocumentBuilder.createXmlDocument(
         new InputSource(
            new FileReader(RxUpgrade.getUpgradeRoot() + configFile)), false);

      loadConfig(configDoc);
  }
  
   /**
    * Parse and load plugins configuration from the supplied doc.
    * @param configDoc, never <code>null</code>.
    * @throws IOException
    * @throws SAXException
    */
   private void loadConfig(Document configDoc)
      throws IOException, SAXException
   {
      if (configDoc == null)
         throw new IllegalArgumentException("configDoc may not be null");
      
      Properties props = getVersionProperties();

      NodeList nl = null;
      String temp = null;
      int serverBuildInt = 0;
      int serverMajorInt = 0;
      int serverMinorInt = 0;
      int serverMicroInt = 0;

      // retrieve the installed major version
      try
      {
         temp = props.getProperty ("majorVersion");

         serverMajorInt = Integer.parseInt(temp);
      }
      catch(Throwable t)
      {
         RxUpgradeLog.logIt("major version is missing from previous version "
            + "properties file, upgrade process aborted.");
         return;
      }

      // retrieve the installed minor version
      try
      {
         temp = props.getProperty ("minorVersion");

         serverMinorInt = Integer.parseInt(temp);
      }
      catch(Throwable t)
      {
         RxUpgradeLog.logIt("minor version is missing from previous version "
            + "properties file, upgrade process aborted.");
         return;
      }

      // retrieve the installed minor version
      try
      {
         temp = props.getProperty ("microVersion");

         if (temp==null || temp.trim().length()==0)
            temp = "0";

         serverMicroInt = Integer.parseInt(temp);
      }
      catch(Throwable t)
      {
         RxUpgradeLog.logIt("micro version is missing from previous version "
            + "properties file, upgrade assumed microVersion=0.");

      }

      // retrieve the installed build number
      try
      {
         temp = props.getProperty ("buildNumber");
         serverBuildInt = Integer.parseInt(temp);
      }
      catch(Throwable t)
      {
         RxUpgradeLog.logIt("build number is missing from previous version "
            + "properties file, upgrade process aborted.");
         return;
      }

      nl = configDoc.getElementsByTagName("module");
      if(nl == null || nl.getLength() < 1)
      {
         RxUpgradeLog.logIt("No module elements exist in config file, " +
            "upgrade process aborted.");
         return;
      }

      RxUpgradeLog.logIt("Server Version Props: " +
         " serverMajor: " + serverMajorInt +
         ", serverMinor: " + serverMinorInt +
         ", serverMicro: " + serverMicroInt +
         ", serverBuild: " + serverBuildInt);


      Element elemmodule = null;
      Element elemfrom = null;
      Element elemto = null;
      int pluginBuildFrom = 0;
      int pluginBuildTo = 0;
      int pluginMajorFrom = 0;
      int pluginMajorTo = 0;
      int pluginMinorFrom = 0;
      int pluginMinorTo = 0;
      int pluginMicroFrom = 0;
      int pluginMicroTo = 0;

      //Loop through all the module elements
      for(int i=0; nl != null && i < nl.getLength(); i++)
      {
         try
         {
            Element e = (Element)nl.item(i);

            elemfrom = InstallUtil.getElement(e, "from");
            elemto = InstallUtil.getElement(e, "to");

            RxUpgradeLog.logIt("");
            RxUpgradeLog.logIt("Evaluating conditions of upgrade module id: " + i +
               " , logfile: " + e.getAttribute("logfile"));

            //Get the from build number into varBuildFrom.
            try
            {
               temp = elemfrom.getAttribute("build");
               pluginBuildFrom = Integer.parseInt(temp);
               temp = elemfrom.getAttribute("major");
               pluginMajorFrom = Integer.parseInt(temp);
               temp = elemfrom.getAttribute("minor");
               pluginMinorFrom = Integer.parseInt(temp);

               temp = elemfrom.getAttribute("micro");

               if (temp==null || temp.trim().length()==0)
                  temp = "-1";

               pluginMicroFrom = Integer.parseInt(temp);

               temp = elemto.getAttribute("build");
               pluginBuildTo = Integer.parseInt(temp);
               temp = elemto.getAttribute("major");
               pluginMajorTo = Integer.parseInt(temp);
               temp = elemto.getAttribute("minor");
               pluginMinorTo = Integer.parseInt(temp);

               temp = elemto.getAttribute("micro");
               if (temp==null || temp.trim().length()==0)
                  temp = "-1";

               pluginMicroTo = Integer.parseInt(temp);

            }
            catch(Throwable t)
            {
               // This should never happen.
               // If happens varBuildFrom is still equals to 0
               RxUpgradeLog.logIt(t.getMessage());
               RxUpgradeLog.logIt(t);
               
               RxUpgradeLog.logIt("skipping upgrade module id: " + i + " due to exception");
               continue;
            }

            RxUpgradeLog.logIt("Evaluating version condition: " + 
               pluginMajorFrom + "." + pluginMinorFrom + "." + pluginMicroFrom +
                " <= " +
               serverMajorInt  + "." + serverMinorInt + "." + serverMicroInt +
                " <= " +
               pluginMajorTo + "." + pluginMinorTo + "." + pluginMicroTo);               
               
            //compare MajorFrom <= serverMajor <= MajorTo
            if(!isVersionInRange(pluginMajorFrom, pluginMajorTo, serverMajorInt))
            {
               RxUpgradeLog.logIt("--skipping module id: " + i +
                  " - Not(pluginMajorFrom <= serverMajor <= pluginMajorTo)");
               continue;
            }

            //if major is in range and pluginMajorFrom == serverMajorInt
            //then check if serverMinor < MinorFrom
            if (pluginMajorFrom == serverMajorInt)
            {
               // check the minor version in this case
               if (serverMinorInt < pluginMinorFrom)
               {
                  RxUpgradeLog.logIt("--skipping module id: " + i +
                        " - (pluginMajorFrom == serverMajorInt) and " +
                        "(serverMinorInt < pluginMinorFrom)");
                  continue;
               }
            }            
            
            //if major is in range and pluginMajorTo == serverMajorInt
            //then check if serverMinor > MinorTo
            if (pluginMajorTo == serverMajorInt)
            {
               // check the minor version in this case
               if (pluginMinorTo != -1 && serverMinorInt > pluginMinorTo)
               {
                  RxUpgradeLog.logIt("--skipping module id: " + i +
                        " - (pluginMajorTo == serverMajorInt) and " +
                        "(serverMinorInt > pluginMinorTo)");
                  continue;
               }   
            }
            
            //if major and minor from are in range and both are the same
            //then check if serverMicro < MicroFrom
            if(pluginMajorFrom == serverMajorInt &&
               pluginMinorFrom == serverMinorInt)
            {
               // check the micro version in this case
               if (serverMicroInt < pluginMicroFrom)
               {
                  RxUpgradeLog.logIt("--skipping module id: " + i +
                     " - (pluginMajorFrom == serverMajorInt) and " + 
                     "(pluginMinorFrom == serverMinorInt) and " +
                     "(serverMicroInt < pluginMicroFrom)");
                  continue;
               }
            }
            
            //if major and minor to are in range and both are the same
            //then check if serverMicro > MicroTo
            if(pluginMajorTo == serverMajorInt &&
               pluginMinorTo == serverMinorInt)
            {
               // check the micro version in this case
               if (pluginMicroTo != -1 && serverMicroInt > pluginMicroTo)
               {
                  RxUpgradeLog.logIt("--skipping module id: " + i +
                     " - (pluginMajorTo == serverMajorInt) and " + 
                     "(pluginMinorTo == serverMinorInt) and " +
                     "(serverMicroInt > pluginMicroTo)");
                  continue;
               }
            }

            //IF WE GOT HERE IT MEANS THAT VERSION IS IN THE RANGE
                        
            String buildFrom = (pluginBuildFrom == -1 ? "0" : "" + pluginBuildFrom);
            String buildTo = (pluginBuildTo == -1 ? "99999999" : "" + pluginBuildTo);

            int buildFromInt = 0;
            int buildToInt = 0;

            try
            {
               buildFromInt = Integer.parseInt(buildFrom);
               buildToInt = Integer.parseInt(buildTo);
            }
            catch(Throwable t)
            {
               // This should never happen.
               // If happens varBuildFrom is still equals to 0
               RxUpgradeLog.logIt(t.getMessage());
               RxUpgradeLog.logIt(t);
               RxUpgradeLog.logIt("--skipping module id: " + i + " due to build number error");
               continue;
            }
            
            RxUpgradeLog.logIt("Evaluating Build No condition: " + pluginBuildFrom + " <= "
               + serverBuildInt + " <= " + pluginBuildTo);

            //see if the current build number is in the range
            if (!(buildFromInt <= serverBuildInt && serverBuildInt <= buildToInt))
            {
               RxUpgradeLog.logIt("--skipping module id: " + i + " - build number not in range");
               continue;
            }

            //see if we have propertyMatch element(s)
            NodeList nlPropMatch =
               ((Element)e).getElementsByTagName("propertyMatch");

            if (nlPropMatch!=null && nlPropMatch.getLength() > 0)
            {
               RxUpgradeLog.logIt("Evaluating propertyMatch condition.");
               
               if (!isPropertiesMatch(nlPropMatch))
               {
                  RxUpgradeLog.logIt("--skipping module id: " + i + 
                     " - propertyMatch condition(s) returned false.");

                  continue; //skip this one
               }
            }

            //see if we have XPathMatch nodes
            NodeList nlXPathMatch =
                ((Element)e).getElementsByTagName("XPathMatch");

            if (nlXPathMatch!=null && nlXPathMatch.getLength() > 0)
            {
               RxUpgradeLog.logIt("Evaluating XPathMatch condition.");
               
               if (!isXPathsMatch(nlXPathMatch))
               {
                  RxUpgradeLog.logIt("--skipping module id: " + i + 
                     " - XPathMatch condition(s) returned false.");

                  continue; //skip this one
               }

            }

            // all conditions are true - add module for execution
            RxUpgradeLog.logIt("++adding module id: " + i + " for further execution.");
            
            m_modules.add(new PSUpgradeModule((Element)nl.item(i)));
         }
         catch(Throwable e)
         {
            RxUpgradeLog.logIt(e.getMessage());
         }
      }

      RxUpgradeLog.logIt("================================================");
      RxUpgradeLog.logIt("========FINISHED LOADING UPGRADE PLUGINS========");
      RxUpgradeLog.logIt("================================================");
      RxUpgradeLog.logIt("");      
   }

   private boolean isVersionInRange(int pluginVersionFrom, int pluginVersionTo,
      int serverVersion)
   {
      //compare pluginMajorFrom with serverMajor version
      if (pluginVersionFrom > serverVersion)
         return false;               
            
      //majorFrom is ok, compare pluginMajorTo with serverMajor version
      if (pluginVersionTo != -1 && pluginVersionTo < serverVersion)
         return false;
      
      return true;
   } 

   /**
    * Evaluates all given properties match conditions.
    * @param nlpm list of 'propertyMatch' elements, may be <code>null</code>
    * may be <code>empty</code>.
    * @return <code>true</code> if either all match or the list is null or empty.
    */
   private boolean isPropertiesMatch(NodeList nlpm)
   {
      if (nlpm==null)
         return true;

      for (int j = 0; nlpm != null && j < nlpm.getLength(); j++)
      {
         Node npm = nlpm.item(j);
         if (npm instanceof Element)
         {
            if (!isPropertyMatch((Element) npm))
            {
               //all must match
               return false;
            }
         }
      }

      return true;
   }

   /**
    * First tries to find and load previousVersion.properties, then 
    * Version.properties if none are found returns <code>null</code>.
    * @return loaded props or <code>null</code> is not found.
    * @throws FileNotFoundException if file is missing.
    * @throws IOException IO problems.
    */
   private Properties getVersionProperties()
      throws FileNotFoundException, IOException
   {
      Properties props = new Properties();
      
      //Construct the full path of the version properties file.
      String fileName = RxUpgrade.getRxRoot() +
         InstallUtil.PREVIOUS_VERSION_PROPS_FILE;
           
      File propFile = new File(fileName);
      if (propFile.isFile())
      {
         try(FileInputStream is = new FileInputStream(fileName)) {
            props.load(is);
         }
         
         return props;
      }
      
      fileName = RxUpgrade.getRxRoot() + InstallUtil.VERSION_PROPS_FILE;
      
      propFile = new File(fileName);
      if (!propFile.isFile())
      {
         RxUpgradeLog.logIt(fileName + " not found! returning, file: " +
            fileName);
      }
      else
      {
         try(FileInputStream is = new FileInputStream(fileName)) {
            props.load(is);
         }
         
         return props;
      }

      return null;
   }

   /**
    * Evaluates all given XPathMatch match conditions.
    * @param nlxpe list of 'XPathMatch' elements, may be <code>null</code>
    * may be <code>empty</code>.
    * @return <code>true</code> if either all match or the list is null or empty.
    */
   private boolean isXPathsMatch(NodeList nlxpe)
   {
      if (nlxpe==null)
         return true;

      for (int j = 0; nlxpe != null && j < nlxpe.getLength(); j++)
      {
         Node nxpe = nlxpe.item(j);
         if (nxpe instanceof Element)
         {
            if (!isXPathMatch((Element) nxpe))
            {
               //all must match
               return false;
            }
         }
      }

      return true;
   }

   /**
    * Evaluates upgrade plugin property condition.
    * @param el 'propertyMatch' elem, may be <code>null</code>.
    * @return <code>true</code> if element doesn't exist or property matches,
    * otherwise returns false.
    *
    * <pre>
    * Example property conditions:
    * 1. Test if a given file exists (note: path is relative to the Rx root).
    * &lt;ppropertyMatch filePath="upgrade/my.properties"/&gt;
    *
    * 2. Test for 'null' property:
    * &lt;propertyMatch filePath="upgrade/my.properties"
    *  name="myprop"
    *  operator="null"
    * /&gt;
    *
    * 3. Test for 'not null' property:
    * &lt;propertyMatch filePath="upgrade/my.properties"
    *  name="myprop"
    *  operator="not null"
    * /&gt;
    *
    * 4. Test for property value (case insens. by default):
    * &lt;propertyMatch filePath="upgrade/my.properties"
    *  name="myprop"
    *  operator="=="
    *  compareTo="myvalue"
    * /&gt;
    *
    * 4. Test for property value (case insens. by default):
    * &lt;propertyMatch filePath="upgrade/my.properties"
    *  name="myprop"
    *  operator="!="
    *  compareTo="myvalue"
    * /&gt;
    *
    * 5. Test for property value (case sens.):
    * &lt;propertyMatch filePath="upgrade/my.properties"
    *  name="myprop"
    *  operator="=="
    *  compareTo="myvalue"
    *  caseSensitive="yes"
    * /&gt;
    *
    * 6. Test if file doesn't exist or property 'rwInstalledFlag' not equal 'yes'.
    * &lt;propertyMatch filePath="rxconfig/Installer/rxfts.properties"
    *  name="rwInstalledFlag"
    *  operator="!="
    *  compareTo="yes"
    * /&gt;
    * </pre>
    */
   private boolean isPropertyMatch(Element el)
   {
      if (el==null)
         return true;

      String filePath = el.getAttribute("filePath");
      if (filePath== null || filePath.trim().length()==0)
      {
         RxUpgradeLog.logIt("propertyMatch filePath attr. is missing "
          + " returning false, xml: " + PSXmlDocumentBuilder.toString(el));

         return false;
      }

      File f = new File(RxUpgrade.getRxRoot() + File.separator + filePath);

      boolean fileExists = f.exists();
      
      String operator = el.getAttribute("operator");

      if ( operator== null || operator.trim().length()==0)
      {
         RxUpgradeLog.logIt("WARN: propertyMatch operator attr. is missing "
          + " returning false, xml: " + PSXmlDocumentBuilder.toString(el));

         return false;
      }
      boolean isNegateOp = 
         operator.equalsIgnoreCase("null") || operator.equals("!=");

      String name = el.getAttribute("name");

      if ( name== null || name.trim().length()==0)
      {
         return (fileExists && !isNegateOp) ||(!fileExists && isNegateOp);
      }
      if (!fileExists)
      {
         return isNegateOp;
      }

      String compareTo = el.getAttribute("compareTo");

      boolean bCaseSensitive = false;
      String caseSensitive = el.getAttribute("caseSensitive");

      if (caseSensitive!= null && caseSensitive.compareToIgnoreCase("yes")==0)
         bCaseSensitive = true;

      PSProperties props = null;
      try
      {
         props = new PSProperties(f.getAbsolutePath());
      }
      catch (FileNotFoundException e)
      {
         fileExists = false;
      }
      catch (IOException e)
      {
         fileExists = false;
      }

      if (fileExists)
      {
         String prop = props.getProperty(name);

         RxUpgradeLog.logIt("evaluating property match: " +
            " prop: " + prop + " operator: " + operator +
            " compareTo: " + compareTo + " bCaseSensitive: " + bCaseSensitive);

         boolean res = isMatch(prop, operator, compareTo, bCaseSensitive);

         RxUpgradeLog.logIt("isMatch returned " + res);

         return res;
      }

      return false;
   }

   /**
    * Evaluates a single XPathMatchMatch condition.
    * @param el XPathMatchMatch element, may be <code>null</code>.
    * @return <code>true</code> if element doesn't exist or xpath evaluates to
    * something and/or matches a given compareTo value,
    * otherwise returns false.
    * <pre>
    * 1. Test if file doesn't exist or Xpath returned value doesn't evaluate
    *  to 'yes'.
    *
    * &lt;XPathMatch filePath="rxconfig/Installer/myfile.xml"
    *  operator="!=" compareTo="yes"&gt;
    *  &lt;XPathExpression&gt;
    *     //MyRoot/SubNode[@attrName='attrVal']/valueNode
    *  &lt;/XPathExpression&gt;
    * &lt;/XPathMatch&gt;
    * </pre>
    */
   private boolean isXPathMatch(Element el)
   {
      if (el==null)
         return true;

      String filePath = el.getAttribute("filePath");
      if (filePath== null || filePath.trim().length()==0)
      {
         RxUpgradeLog.logIt("XPathMatch filePath attr. is missing "
          + " returning false, xml: " + PSXmlDocumentBuilder.toString(el));

         return false;
      }

      File f = new File(RxUpgrade.getRxRoot() + File.separator + filePath);

      boolean fileExists = f.exists();

      String operator = el.getAttribute("operator");

      if ( operator== null || operator.trim().length()==0)
      {
         RxUpgradeLog.logIt("XPathMatch operator attr. is missing "
          + " returning false, xml: " + PSXmlDocumentBuilder.toString(el));

         return false;
      }

      String XpathExpression = InstallUtil.getElemValue(el, "XPathExpression");

      RxUpgradeLog.logIt("XpathExpression string: " + XpathExpression);

      if (XpathExpression==null || XpathExpression.trim().length()==0)
      {
         return fileExists && operator.equalsIgnoreCase("null") ||
             operator.equalsIgnoreCase("!=");
      }

      if (!fileExists)
      {
         return operator.equalsIgnoreCase("null") || operator.equalsIgnoreCase("!=");
      }

      Document doc = null;
      try
      {
         doc =
            PSXmlDocumentBuilder.createXmlDocument(
               new FileInputStream(f.getAbsolutePath()),
               false);
      }
      catch (FileNotFoundException e1)
      {
         log.error(e1.getMessage());
         log.debug(e1.getMessage(), e1);
         return false;
      }
      catch (IOException e1)
      {
         RxUpgradeLog.logIt(e1);
         return false;
      }
      catch (SAXException e1)
      {
         RxUpgradeLog.logIt(e1);
         return false;
      }

      PSXPathEvaluator xp = null;
      try
      {
         xp = new PSXPathEvaluator( doc,  ms_xpathTransformerOutputProps);
      }
      catch (TransformerException e2)
      {
         RxUpgradeLog.logIt(e2);
         return false;
      }

      String evaluatedXPath = null;
      try
      {
         evaluatedXPath = xp.evaluate(XpathExpression);
      }
      catch (Exception e)
      {

         RxUpgradeLog.logIt(e);
         return false;
      }

      String compareTo = el.getAttribute("compareTo");

      boolean bCaseSensitive = false;
      String caseSensitive = el.getAttribute("caseSensitive");

      if (caseSensitive!= null && caseSensitive.compareToIgnoreCase("yes")==0)
         bCaseSensitive = true;

      //get rid of xml formatting
      evaluatedXPath = evaluatedXPath.replace('\n', ' ');
      evaluatedXPath = evaluatedXPath.replace('\r', ' ');
      evaluatedXPath = evaluatedXPath.replace('\t', ' ');
      evaluatedXPath = evaluatedXPath.trim();

      RxUpgradeLog.logIt("evaluating property match: " +
         " evaluatedXPath: " + evaluatedXPath + " operator: " + operator +
         " compareTo: " + compareTo + " bCaseSensitive: " + bCaseSensitive);

      boolean res = isMatch(evaluatedXPath, operator, compareTo, bCaseSensitive);

      RxUpgradeLog.logIt("isMatch returned " + res);

      return res;
   }

   /**
    * Compare two properties.
    * @param leftHandSide prop. value, may be <code>null</code> or
    *  <code>empty</code>.
    * @param operator 'null' | 'not null' | == | != | < | > | <= | >=,
    * never <code>null</code> or <code>empty</code>.
    * @param compareTo right side value to compare prop. value to,
    * may only be <code>null</code> if operator is either 'null' or 'not null'
    * may be <code>empty</code>.
    * @param caseSensitive <code>true<code> or <code>false<code>.
    * @return <code>true<code> if match.
    */
   private boolean isMatch(String leftHandSide, String operator,
      String compareTo, boolean caseSensitive)
   {
      if (operator== null || operator.trim().length()==0)
         throw new IllegalArgumentException("operator may not be null or empty");

      if (operator.equalsIgnoreCase("not null"))
         return (leftHandSide!=null);
      else if (operator.equalsIgnoreCase("null"))
         return (leftHandSide==null);

      if (leftHandSide==null)
      {
         if(operator.equalsIgnoreCase("null"))
            return true;
         else if (operator.equalsIgnoreCase("!=") &&
                 (compareTo!=null && compareTo.length() > 0))
            return true;
         else
            return operator.equalsIgnoreCase("==") &&
                    (compareTo==null || compareTo.length() < 1);
      }

      if ( compareTo== null || compareTo.trim().length()==0)
      {
         RxUpgradeLog.logIt("isMatch compareTo param. is invalid "
          + " returning false.");
         return false;
      }

      if (operator.equals("=="))
         return caseSensitive? leftHandSide.compareTo(compareTo)==0 :
            leftHandSide.compareToIgnoreCase(compareTo)==0;
      if (operator.equals("!="))
         return caseSensitive? leftHandSide.compareTo(compareTo)!=0 :
            leftHandSide.compareToIgnoreCase(compareTo)!=0;
      if (operator.equals("<"))
         return caseSensitive? leftHandSide.compareTo(compareTo)<0 :
            leftHandSide.compareToIgnoreCase(compareTo)<0;
      if (operator.equals(">"))
         return caseSensitive? leftHandSide.compareTo(compareTo)>0 :
            leftHandSide.compareToIgnoreCase(compareTo)>0;
      if (operator.equals("<="))
         return caseSensitive? leftHandSide.compareTo(compareTo)<=0 :
            leftHandSide.compareToIgnoreCase(compareTo)<=0;
      if (operator.equals(">="))
         return caseSensitive? leftHandSide.compareTo(compareTo)>=0 :
            leftHandSide.compareToIgnoreCase(compareTo)>=0;

      return false;
   }


   /**
    * Loads and returns server version props.
    * First it looks for the PreviousVersion.properties file, if one exists
    * it loads it and sets PreviousVersionExists=yes, otherwise it loads
    * version props from the rxclient.jar file and sets PreviousVersionExists=no.
    *
    * @returns server version props, may be <code>null</code>.
    */
   private Properties getVersionProps() {
      if (ms_versionProps != null)
         return ms_versionProps;

      ms_isUpgrade = false;

      //Construct the full path of the previous properties file.
      String prevPropertiesFile = RxUpgrade.getRxRoot() +
              InstallUtil.PREVIOUS_VERSION_PROPS_FILE;

      File propFile = new File(prevPropertiesFile);
      if (propFile.isFile()) {
         ms_versionProps = new Properties();
         try (FileInputStream is = new FileInputStream(prevPropertiesFile)) {
            ms_versionProps.load(is);
            ms_isUpgrade = true;
            return ms_versionProps;
         } catch (IOException e1) {
            //this shouldn't happen is File sais that file exists?
            RxUpgradeLog.logIt(e1.getMessage());
            RxUpgradeLog.logIt(e1);
         }
      }

      String strRootDir = RxUpgrade.getRxRoot();

      //get jar file with version props
      File jarFile = new File(strRootDir + File.separator +
              RxFileManager.VERSION_JAR_FILE);

      if (jarFile.exists()) {
         try (JarFile jar = new JarFile(jarFile)) {
            JarEntry jarEntry = jar.getJarEntry(RxFileManager.VERSION_FILE);
            if (jarEntry != null) {
               try (InputStream in = jar.getInputStream(jarEntry)) {
                  Properties verProp = new Properties();
                  verProp.load(in);
                  ms_versionProps = verProp;
                  ms_isUpgrade = false;
               }
            }
         } catch (IOException e) {
            RxUpgradeLog.logIt(e.getMessage());
            RxUpgradeLog.logIt(e);
         }
      }
      return ms_versionProps;
   }

   /**
    * Converts the m_modules arraylist to Iterator and returns.
    * One can iterate through the returned iterator to get the modules that
    * are needed run.
    * @return module iterator
    */
   public Iterator getModuleList()
   {
      return m_modules.iterator();
   }

   /**
    * Arraylist intended to hold the valid modules. When initialized must be
    * empty but never be <code>null</code>.
    */
   private ArrayList m_modules = new ArrayList();
   private static boolean ms_isUpgrade;

   /**
    * Server version properties. Initialized by getVersionProps() method.
    */
   private static Properties ms_versionProps = null;

   /**
    * Constant for PreviousVersionExists property name.
    */
   private static final String Previous_Version_Exists_Prop =
      "PreviousVersionExists";

   /**
    * Plugin Attribute name.
    */
   private static final String runOnUpgradeOnly_Attr =
      "runOnUpgradeOnly";
     
   private static Properties ms_xpathTransformerOutputProps = new Properties();
   static
   {
      ms_xpathTransformerOutputProps.put(OutputKeys.ENCODING, "UTF-8");
      ms_xpathTransformerOutputProps.put(OutputKeys.INDENT, "no");
      ms_xpathTransformerOutputProps.put(OutputKeys.METHOD, "xml");
   }
}

