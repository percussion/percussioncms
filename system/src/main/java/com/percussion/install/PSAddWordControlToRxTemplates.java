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
package com.percussion.install;

import com.percussion.util.IOTools;
import com.percussion.util.PSStringOperation;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
/**
 * This plugin has been written to add the sys_FileWord control to 
 * rx_Templates.xsl file.
 */

public class PSAddWordControlToRxTemplates implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSAddWordControlToRxTemplates()
   {
   }

   /**
    * This plugin runs only once on an installation. 
    * We load the rx_Templates.xsl file and add the sys_FileWord 
    * control if does not exist already. We grab the control from
    * sys_Templates.xsl file. If it is 5.0 to 5.0 upgrade then we
    * change the inline slot id's from 103 to 101 and 104 to 102.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {

      config.getLogStream().println("Adding the word control to " +
         "rx_Templates.xsl...");

      File rxfile = null;
      FileInputStream rxfis = null;
      File upgfile = null;
      FileInputStream upgfis = null;
      ByteArrayOutputStream rxbos = null;
      ByteArrayOutputStream upgbos = null;
      OutputStreamWriter writer = null;
      try
      {
         rxfile = new File(RxUpgrade.getRxRoot() +
            "rx_resources/stylesheets/rx_Templates.xsl");
         rxfis = new FileInputStream(rxfile);
         rxbos = new ByteArrayOutputStream();
         IOTools.copyStream(rxfis, rxbos);
         String rxstr = rxbos.toString("UTF8");
         
         if(rxstr.indexOf("name=\"sys_FileWord\"") != -1)
         {
            config.getLogStream().println(
               "A template with name sys_FileWord already " +
               "exists in rx_Templates.xsl. Leaving the plugin...");
            return null;
         }

         upgfile = new File(RxUpgrade.getRxRoot() + 
            "sys_resources/stylesheets/sys_Templates.xsl");

         upgfis = new FileInputStream(upgfile);
         Document tempdoc = PSXmlDocumentBuilder.createXmlDocument(
                              upgfis,false);
         NodeList cnl = tempdoc.getElementsByTagName("psxctl:ControlMeta");
         Element wcelem = null;
         if(cnl == null || cnl.getLength() < 1)
         {
            config.getLogStream().println("ControlMeta elements are missing in \n" +
               upgfile.getAbsolutePath() +
               "Skipped adding WordControl. Leaving the plugin...");
            return null;
         }
         for(int i=0; i<cnl.getLength(); i++)
         {
            wcelem = (Element) cnl.item(i);
            if(wcelem.getAttribute("name").equalsIgnoreCase("sys_FileWord"))
               break;
         }
         if(wcelem == null)
         {
            config.getLogStream().println("WordControl element is missing in \n" +
               upgfile.getAbsolutePath() +
               "Skipped adding WordControl. Leaving the plugin...");
            return null;
         }
         StringWriter sw = new StringWriter();
         PSXmlTreeWalker treew = new PSXmlTreeWalker(wcelem);
         treew.write(sw,true,true,true);
         String upgstr = sw.toString();
         sw.close();
        
         if(config.getElement("from").getAttribute("major").equals("5"))
         {
            upgstr = PSStringOperation.replace(upgstr,"103","101");
            upgstr = PSStringOperation.replace(upgstr,"104","102");
         }

         int nLoc = rxstr.indexOf("</xsl:stylesheet>");
         if(nLoc == -1)
         {
            config.getLogStream().println(
               "rx_Templates.xsl file is missing closing" +
               "xsl:stylesheet element. Leaving the plugin...");
            return null;
         }
         String result = rxstr.substring(0, nLoc) + 
            upgstr + rxstr.substring(nLoc);
         writer = new OutputStreamWriter(
            new FileOutputStream(rxfile), "UTF8");
         writer.write(result);
         writer.flush();
      }
      catch(IOException e)
      {
         e.printStackTrace(config.getLogStream());
      } catch (SAXException e)
      {
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
         if(rxfis != null)
         {
            try
            {
               rxfis.close();
            }
            catch(IOException e)
            {
               e.printStackTrace(config.getLogStream());
            }
         }
         if(upgfis != null)
         {
            try
            {
               upgfis.close();
            }
            catch(IOException e)
            {
               e.printStackTrace(config.getLogStream());
            }
         }
         if(writer != null)
         {
            try
            {
               writer.close();
            }
            catch (IOException e)
            {
               e.printStackTrace(config.getLogStream());
            }
         }
         if(rxbos != null)
         {
            try
            {
               rxbos.close();
            }
            catch (IOException e)
            {
               e.printStackTrace(config.getLogStream());
            }
         }
         if(upgbos != null)
         {
            try
            {
               upgbos.close();
            }
            catch (IOException e)
            {
               e.printStackTrace(config.getLogStream());
            }
         }
      }
      config.getLogStream().println("leaving the process() of the plugin...");
      return null;
   }
}
