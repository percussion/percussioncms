/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.process;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A very simple implementation of a process. Can be used as a base class.
 */
public class PSSimpleProcess implements IPSProcess
{
   // see interface
   public void fromXml(Element el) throws PSProcessException
   {
      if (el == null)
         throw new IllegalArgumentException("element may not be null");

      String tagName = el.getTagName();
      if (!(tagName.equals(NODE_NAME)))
      {
         throw new PSProcessException(
               "Invalid root node element specified");
      }

      // process name
      String tmp = el.getAttribute(ATTR_NAME);
      if ((tmp == null) || (tmp.trim().length() < 1))
      {
         throw new PSProcessException(
            "Process name specifed as attribute ("
               + IPSProcess.ATTR_NAME
               + ") + of element ("
               + IPSProcess.NODE_NAME
               + ") may not be null or empty");
      }
      m_name = tmp.trim();

      // process type
      tmp = el.getAttribute(ATTR_TYPE);
      if ((tmp == null) || (tmp.trim().length() < 1))
      {
         throw new PSProcessException(
            "Process type specifed as attribute ("
               + IPSProcess.ATTR_TYPE
               + ") + of element ("
               + IPSProcess.NODE_NAME
               + ") may not be null or empty");
      }
      m_type = tmp.trim();

      // Process Definitions
      m_processDefs.clear();

      NodeList nl = el.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node node = nl.item(i);
         if (node instanceof Element)
         {
            Element defEl = (Element) node;
            if (!defEl.getTagName().equals(PSProcessDef.NODE_NAME))
            {
               throw new PSProcessException(
                  "Invalid child element ("
                     + defEl.getTagName()
                     + ") of root element ("
                     + PSProcessDef.NODE_NAME
                     + ")");
            }
            PSProcessDef def = new PSProcessDef(defEl);
            int[] supportedOS = def.getSupportedOS();
            for (int j = 0; j < supportedOS.length; j++)
               m_processDefs.put(new Integer(supportedOS[j]), def);
         }
      }
   }

   // see interface
   public String getName()
   {
      return m_name;
   }

   // see interface
   public String getType()
   {
      return m_type;
   }

   // see interface
   public PSProcessDef getProcessDef()
   {
      int os = PSProcessManager.getOS();
      return (PSProcessDef) m_processDefs.get(new Integer(os));
   }

   // see interface
   public PSProcessAction start(Map ctx) throws PSProcessException
   {
      if (ctx == null)
         throw new IllegalArgumentException("process context may not be null");
      PSProcessDef pdef = getProcessDef();
      String[] cmdArray = getCommand(pdef, ctx);
      String[] envp = getEnv(pdef, ctx);
      File dir = getWorkingDir(pdef, ctx);

      return new PSProcessAction(cmdArray, envp, dir);
   }

   /**
    * Returns the resolved working directory for this process.
    *
    * @param pdef the process defintion for the current OS, may not be
    * <code>null</code>
    *
    * @param ctx a {@link Map map}, contains data for executing the
    * process, may not be <code>null</code>
    *
    * @return the resolved working directory for this process, may be
    * <code>null</code> if no working directory is defined.
    *
    * @throws PSProcessException if any error occurs resolving the working
    * directory
    */
   protected File getWorkingDir(PSProcessDef pdef, Map ctx)
      throws PSProcessException
   {
      if (pdef == null)
         throw new IllegalArgumentException("process definition may not be null");

      if (ctx == null)
         throw new IllegalArgumentException("process context may not be null");

      return pdef.getWorkingDir(ctx);
   }

   /**
    * Returns the resolved command line arguments for this process definition.
    * The first argument in the returned array is the executable for this
    * process.
    *
    * @param pdef the process defintion for the current OS, may not be
    * <code>null</code>
    *
    * @param ctx a {@link java.util.Map map}, contains data for executing the
    * process, may not be <code>null</code>
    *
    * @return the resolved executable and resolved command line arguments,
    * never <code>null</code> or empty. The first argument representing the
    * executable is always present.
    *
    * @throws PSProcessException if any error occurs resolving the executable
    * or the command parameters
    */
   protected String[] getCommand(PSProcessDef pdef, Map ctx)
      throws PSProcessException
   {
      if (pdef == null)
         throw new IllegalArgumentException("process definition may not be null");

      if (ctx == null)
         throw new IllegalArgumentException("process context may not be null");

      String[] cmdParams = pdef.getCommandParams(ctx);
      int len = ((cmdParams == null) ? 1 : (cmdParams.length + 1));

      String[] cmdArray = new String[len];
      cmdArray[0] = pdef.getExecutable(ctx);

      if (cmdParams != null)
      {
         for (int i = 0; i < cmdParams.length; i++)
         {
            cmdArray[i + 1] = cmdParams[i];
         }
      }
      return cmdArray;
   }

   /**
    * Returns the resolved environmental parameters for this process.
    * Each element of the returned array has environment variable settings in
    * the format "name=value".
    *
    * @param pdef the process defintion for the current OS, may not be
    * <code>null</code>
    *
    * @param ctx a {@link java.util.Map map}, contains data for executing the
    * process, may not be <code>null</code>
    *
    * @return the resolved environmental parameters, may be <code>null</code>
    * if no environment setting is specified.
    *
    * @throws PSProcessException if any error occurs resolving the environment
    * parameters
    */
   protected String[] getEnv(PSProcessDef pdef, Map ctx)
      throws PSProcessException
   {
      if (ctx == null)
         throw new IllegalArgumentException("process context may not be null");

      if (pdef == null)
         throw new IllegalArgumentException("process definition may not be null");

      return pdef.getEnvParams(ctx);
   }

   /**
    * Stores the name of the function, initialized in the <code>fromXml</code>
    * method, never modified after that, never <code>null</code> or empty
    * after initialization.
    */
   protected String m_name = null;
   
   /**
    * Map for storing the process definition (<code>PSProcessDef</code>) as
    * value and the supported OS (as <code>Integer</code>) as key.
    * Never <code>null</code>.
    */
   protected Map m_processDefs = new HashMap();

   /**
    * Stores the type of the function, initialized in the <code>fromXml</code>
    * method, never modified after that, never <code>null</code> or empty
    * after initialization.
    */
   protected String m_type = null;
}
