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
package com.percussion.test.ant;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Create the filter file for use in deploying the autotester client and server.
 * This task reads the server.xml from Tomcat and the server.properties file and
 * creates a property file for the autotester to use.
 * 
 * @author dougrand
 */
@SuppressFBWarnings("INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE")
public class PSMakeFilterFileTask extends Task
{

   /**
    * The rhythmyx root directory, a required property. Set via ANT
    */
   String m_rhythmyxRoot;

   /**
    * The output property file path, a required property. Set via ANT
    */
   String m_propertyFile;
   
   /**
    * The test port to use when getting host info, defaults to 32000
    */
   String m_testPort = "32000";

   /**
    * No arg ctor, do nothing
    */
   public PSMakeFilterFileTask() {
      super();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.tools.ant.Task#execute()
    */
   @Override
   public void execute() throws BuildException
   {
      if (StringUtils.isBlank(m_propertyFile))
      {
         throw new BuildException("propertyFile is a required attribute");
      }
      if (StringUtils.isBlank(m_rhythmyxRoot))
      {
         throw new BuildException("rhythmyxRoot is a required attribute");
      }

      Writer w = null;
      PrintWriter pw = null;

      try
      {
         w = new FileWriter(m_propertyFile);
         pw = new PrintWriter(w);

         File rxroot = new File(m_rhythmyxRoot);
         if (!rxroot.exists())
         {
            throw new BuildException("The given root directory doesn't exist: "
                  + rxroot);
         }
         File serverxml = new File(rxroot,
               "AppServer/server/rx/deploy/jbossweb-tomcat55.sar/server.xml");
         if (!serverxml.exists())
         {
            throw new BuildException("server.xml doesn't exist: " + serverxml);
         }
         DocumentBuilderFactory f = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(  new PSXmlSecurityOptions(
                 true,
                 true,
                 true,
                 false,
                 true,
                 false
         ));
         try
         {
            DocumentBuilder b = f.newDocumentBuilder();
            Document d = b.parse(serverxml);
            NodeList nl = d.getElementsByTagName("Connector");
            if (nl.getLength() == 0)
            {
               throw new BuildException(
                     "Couldn't find the connector element in server.xml");
            }
            Element el = (Element) nl.item(0);
            String port = el.getAttribute("port");
            if (port == null || port.trim().length() == 0)
            {
               handleErrorOutput("Missing port information from server.xml");
            }
            pw.println("wsport=" + port);
            pw.println("rxport=" + port);
         }
         catch (ParserConfigurationException e1)
         {
            throw new BuildException("Couldn't get an XML parser", e1);
         }
         catch (SAXException e)
         {
            throw new BuildException("Problem parsing server.xml", e);
         }
         catch (IOException e)
         {
            throw new BuildException(e);
         }
         // Grab host name and ip
         ServerSocket sock = null;
         try
         {
            short testPort = Short.parseShort(m_testPort);
            sock = new ServerSocket(testPort);
            InetAddress addr = sock.getInetAddress().getLocalHost();
            pw.println("host=" + addr.getHostName());
            pw.println("ip=" + addr.getHostAddress());
         }
         catch(NumberFormatException e)
         {
            handleErrorOutput("Badly formed test port number: " + m_testPort);
         }
         finally
         {
            if (sock != null) sock.close();
         }
      }
      catch (IOException e)
      {
         handleErrorOutput("Could not open property file: "
               + e.getLocalizedMessage());
      }
      catch (Exception e)
      {
         handleErrorOutput("Unexpected error: " + e.getLocalizedMessage());
         e.printStackTrace();
      }
      finally
      {
         if (pw != null)
            pw.flush();
         if (w != null)
            try
            {
               w.close();
            }
            catch (IOException e1)
            {
               handleErrorOutput("Unexpected problem: "
                     + e1.getLocalizedMessage());
            }
      }

   }

   /**
    * @return Returns the propertyFile.
    */
   public String getPropertyFile()
   {
      return m_propertyFile;
   }

   /**
    * @param propertyFile The propertyFile to set.
    */
   public void setPropertyFile(String propertyFile)
   {
      m_propertyFile = propertyFile;
   }

   /**
    * @return Returns the rhythmyxRoot.
    */
   public String getRhythmyxRoot()
   {
      return m_rhythmyxRoot;
   }

   /**
    * @param rhythmyxRoot The rhythmyxRoot to set.
    */
   public void setRhythmyxRoot(String rhythmyxRoot)
   {
      m_rhythmyxRoot = rhythmyxRoot;
   }
   
   
   /**
    * @return Returns the testPort.
    */
   public String getTestPort()
   {
      return m_testPort;
   }
   /**
    * @param testPort The testPort to set.
    */
   public void setTestPort(String testPort)
   {
      m_testPort = testPort;
   }
}
