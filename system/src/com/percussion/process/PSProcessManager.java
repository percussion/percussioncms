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
package com.percussion.process;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The process manager loads the process defintions from the specified Xml
 * file input stream. This Xml file must conform to the "sys_processes.dtd".
 */
public class PSProcessManager
{
   private static final Logger log = LogManager.getLogger(PSProcessManager.class);
   /**
    * Loads the process defintions from the specified Xml file input stream.
    * This Xml file must conform to the "sys_processes.dtd".
    *
    * @param stream the input stream from which to load the process defs,
    * may not be <code>null</code>. This class takes ownership of the stream
    * and will close it when finished.
    *
    * @throws IOException if any IO error occurs reading from the stream
    *
    * @throws SAXException if an error parsing the process defintions
    *
    * @throws PSProcessException if the process defintions XML is not valid.
    */
   public PSProcessManager(InputStream stream)
      throws IOException, SAXException, PSProcessException
   {
      if (stream == null)
         throw new IllegalArgumentException("stream may not be null");

      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(stream, false);
         Element root = doc.getDocumentElement();
         if ((root == null) || (!root.getTagName().equals(NODE_NAME)))
            throw new PSProcessException(
               "Invalid root element of process definitions.");

         NodeList nl = root.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node node = nl.item(i);
            if (node instanceof Element)
            {
               Element el = (Element)node;
               if (!el.getTagName().equals(IPSProcess.NODE_NAME))
               {
                  throw new PSProcessException(
                     "Invalid child element (" + el.getTagName() +
                     ") of root element (" + NODE_NAME + ")");
               }

               // process type
               String processType = el.getAttribute(IPSProcess.ATTR_TYPE);
               if ((processType == null) || (processType.trim().length() < 1))
               {
                  throw new PSProcessException(
                     "Process type specifed as attribute (" +
                     IPSProcess.ATTR_TYPE + ") + of element (" +
                     IPSProcess.NODE_NAME + ") may not be null or empty");
               }
               processType = processType.trim();

               IPSProcess process =
                  (IPSProcess)Class.forName(processType).newInstance();
               process.fromXml(el);

               m_processMap.put(process.getName(), process);
            }
         }
      }
      catch (ClassNotFoundException cls)
      {
         throw new PSProcessException(
            "Class not found: " + cls.getLocalizedMessage());
      }
      catch (IllegalAccessException iae)
      {
         throw new PSProcessException(
            "Illegal Access Exception: " + iae.getLocalizedMessage());
      }
      catch (InstantiationException ins)
      {
         throw new PSProcessException(
            "Instantiation Exception: " + ins.getLocalizedMessage());
      }
      finally
      {
         if (stream != null)
         {
            try
            {
               stream.close();
            }
            catch (IOException ex)
            {
               // no-op
            }
         }
      }
   }

   /**
    * Method for getting the process object representing the process with the
    * specified name.
    *
    * @param processName the name identifying the process, may not be
    * <code>null</code> or empty. Names are case-sensitive.
    *
    * @return the process object which can be used to control the specified
    * process, may be <code>null</code> if the specified process definition
    * does not exist.
    */
   public IPSProcess getProcess(String processName)
   {
      if ((processName == null) || (processName.trim().length() < 1))
      {
         throw new IllegalArgumentException(
            "process name may not be null or empty");
      }

      return (IPSProcess)m_processMap.get(processName);
   }

   /**
    * Utility method for logging messages.
    *
    * @param message the message to log, may not be <code>null</code> or empty
    */
   public static void log(String message)
   {
      if (null == message || message.trim().length() == 0)
      {
         throw new IllegalArgumentException("message cannot be null or empty");
      }
      log.info(message);
   }

   /**
    * Sets the internal static variable storing the current operating system.
    */
   private static void setOS()
   {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.indexOf("sunos") != -1)
      {
         // On Solaris, OS name contains String : "SunOS"
         ms_os = OS_SOLARIS;
      }
      else if (osName.indexOf("linux") != -1)
      {
         // On Linux, OS name contains String : "Linux"
         ms_os = OS_LINUX;
      }
      else if (osName.indexOf("win") != -1)
      {
         // On Windows, OS name contains String : "win"
         ms_os = OS_WIN;
      }
      else
      {
         throw new RuntimeException(
            "Unable to determine the operating system.");
      }
   }

   /**
    * Returns the current operating system.
    *
    * @return the <code>OS_XXX</code> value representing the current OS.
    */
   public static int getOS()
   {
      return ms_os;
   }

   /**
    * Returns the string form of the OS name specified by <code>os</code>.
    *
    * @param os one of the <code>OS_XXX</code> values
    *
    * @return one of the members of the <code>OS_TYPES</code> array, never
    * <code>null</code>
    */
   public static String getOSType(int os) throws PSProcessException
   {
      if ((os < 0) || (os >= OS_TYPES.length))
         throw new IllegalArgumentException("Invalid OS specified");
      return OS_TYPES[os];
   }

   /**
    * Returns the integer constant for the OS specified by <code>os</code>.
    *
    * @param os one of the members of the <code>OS_TYPES</code> array,
    * may not be <code>null</code> or empty. Name is case-insensitive.
    *
    * @return one of the <code>OS_XXX</code> value corresponding to the
    * specified OS.
    *
    * @throws PSProcessException if the specified OS is unsupported or invalid
    */
   public static int getOSType(String os) throws PSProcessException
   {
      if ((os == null) || (os.trim().length() < 1))
         throw new IllegalArgumentException("Invalid OS specified");

      for (int i = 0; i < OS_TYPES.length; i++)
      {
         if (OS_TYPES[i].equalsIgnoreCase(os))
            return i;
      }
      throw new PSProcessException("Invalid OS specified: " + os);
   }

   /**
    * Tag name of the root element of the <code>PROCESS_DEF_FILE</code> file.
    */
   public static final String NODE_NAME = "Processes";

   /**
    * Integer constant for the Windows OS.
    */
   public static final int OS_WIN = 0;

   /**
    * Integer constant for the Solaris OS.
    */
   public static final int OS_SOLARIS = 1;

   /**
    * Integer constant for the Linux OS.
    */
   public static final int OS_LINUX = 2;

   /**
    * String constant for the supported OS - windows, solaris and linux. The
    * OS_xxx types are indexes into this array.
    */
   public static final String[] OS_TYPES = {"win", "solaris", "linux"};

   /**
    * Stores the process definitions, initialized in the ctor,
    * never <code>null</code> or modified after that.
    */
   private Map m_processMap = new HashMap();

   /**
    * Stores the current Operating System, initialized in the static
    * initializer, never modified after initialization.
    */
   private static int ms_os = 0;

   static
   {
      setOS();
   }
}
