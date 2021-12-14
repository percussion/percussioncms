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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a process definition. This can be used to obtain
 * the following:
 * <ul>
 *    <li>working directory of the process</li>
 *    <li>executable name/path</li>
 *    <li>command line arguments of the process</li>
 *    <li>environment settings for the process</li>
 * </ul>
 */
public class PSProcessDef
{
   /**
    * Constructs the process definition from the specified element
    *
    * @param el the element containing the process definition, may not be
    * <code>null</code>. See {@link #fromXml(Element)} for more details.
    */
   public PSProcessDef(Element el) throws PSProcessException
   {
      fromXml(el);
   }

   /**
    * Constructs the process definition from the specified element,
    *
    * @param el the element containing the process definition, may not be
    * <code>null</code>. Must conform to the PSXProcessDef element as 
    * defined in sys_processes.dtd.
    *
    * @throws PSProcessException if any error occurs constructing the state
    * from the specified element.
    */
   private void fromXml(Element el) throws PSProcessException
   {
      if (el == null)
         throw new IllegalArgumentException("element may not be null");

      String tagName = el.getTagName();
      if (!(tagName.equals(NODE_NAME)))
         throw new IllegalArgumentException(
            "Invalid node element specified: (" + el.getTagName()
            + ") Expected (" + NODE_NAME + ")");

      // supported OS
      String tmp = el.getAttribute(ATTR_OS);
      if (tmp.trim().length() < 1)
         throw new PSProcessException("supported OS may not be null or empty");

      List list = new ArrayList();
      StringTokenizer st = new StringTokenizer(tmp.trim(), ",", false);
      while (st.hasMoreTokens())
         list.add(st.nextToken().trim());

      m_supportedOS = new int[list.size()];
      Iterator it = list.iterator();
      int index = 0;
      while (it.hasNext())
      {
         String strOS = (String)it.next();
         int iOS = PSProcessManager.getOSType(strOS);
         m_supportedOS[index] = iOS;
         index++;
      }
      
      NodeList nl = el.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node node = nl.item(i);
         if (node instanceof Element)
         {
            Element childEl = (Element)node;
            String childTagName = childEl.getTagName();
            
            if (childTagName.equals(EL_DIR))
            {
               // working directory
               m_dirValue = new PSResolvableValue(childEl);
            }
            else if (childTagName.equals(EL_EXECUTABLE))
            {
               m_execValue = new PSResolvableValue(childEl);
            }
            else if (childTagName.equals(EL_PARAMS))
            {
               // params
               m_cmdParams = createParams(childEl);
            }
            else if (childTagName.equals(EL_ENV))
            {
               // environment
               m_envParams = createParams(childEl);
            }
            else
            {
               throw new PSProcessException(
                  "Invalid child element: (" + childTagName + ") of element ("
                  + NODE_NAME + ")");
            }
         }
      }
   }

   /**
    * Returns the resolved working directory for this process definition.
    *
    * @param ctx a {@link java.util.Map map}, which contains data for 
    * executing the process, may not be <code>null</code>
    *
    * @return the resolved working directory for this process, may be
    * <code>null</code> if no working directory is defined.
    *
    * @throws PSResolveException if any error occurs resolving the working
    * directory
    * @throws PSProcessException if the resolver cannot be obtained
    */
   public File getWorkingDir(Map ctx) throws PSResolveException, 
      PSProcessException
   {
      if (ctx == null)
         throw new IllegalArgumentException("process context may not be null");

      File dir = null;
      
      if (m_dirValue != null)
      {  
         dir = new File(getResolvedValue(m_dirValue, ctx));
      }

      return dir;
   }

   /**
    * Returns the resolved executable for this process definition.
    *
    * @param ctx a {@link java.util.Map map}, contains data for executing the
    * process, may not be <code>null</code>
    *
    * @return the resolved executable for this process, never <code>null</code>
    *
    * @throws PSResolveException if any error occurs resolving the executable
    * @throws PSProcessException if the resolver cannot be obtained
    */
   public String getExecutable(Map ctx) throws PSResolveException, 
      PSProcessException
   {
      if (ctx == null)
         throw new IllegalArgumentException("process context may not be null");

      return getResolvedValue(m_execValue, ctx);
   }

   /**
    * Returns the resolved command line arguments for this process definition.
    *
    * @param ctx a {@link Map map}, contains data for executing the
    * process, may not be <code>null</code>
    *
    * @return The resolved command parameters, never <code>null</code>, may be
    * empty.
    *
    * @throws PSResolveException if any error occurs resolving the command
    * parameters
    * @throws PSProcessException if the resolver cannot be obtained
    */
   public String[] getCommandParams(Map ctx)
      throws PSResolveException, PSProcessException
   {
      if (ctx == null)
         throw new IllegalArgumentException("process context may not be null");

      if (m_cmdParams == null)
         return null;

      List cmdList = new ArrayList();
      String param = "";
      boolean isGroup = false;
      for (int i = 0; i < m_cmdParams.length; i++) 
      {
         PSParamDef def = m_cmdParams[i];
         
         // skip any with ifdef not found in ctx
         if (def.getIfDefinedName() != null && !ctx.containsKey(
            def.getIfDefinedName()))
         {
            // if the begin of the group param is skipped, handle it here to start 
            // marking the group.
            // Likewise, the end of the group param may be skipped, in which case
            // handle the group termination
            if ( def.isBeginGroup() )
               isGroup = true;
            if ( def.isEndGroup() )
            {
               isGroup = false;
               cmdList.add(param);
               param = "";
            }
            continue;
         }
         
         if (def.isBeginGroup())
            isGroup = true;
         
         // add to cmd list
         String name = def.getName() == null ? null : "-" + def.getName();
         String val = getResolvedValue(def.getValue(), ctx);
         
         if (name != null && def.getSeparator() != null)
         {
            // handle non-default separator
            String pair = (name + def.getSeparator() + val);
            if (isGroup)
               param += pair; //accumulate params that belong to same group
            else
               cmdList.add(pair); //single param
         }
         else
         {
            // add as separate params
            if (name != null)
            {
               if (isGroup)           
                  param += name;
               else
                  cmdList.add(name); //add single name      
            }
            
            if (val.trim().length() > 0)
            {
               if (isGroup)
                  param += (((name != null) ? " " : "") + val);
               else
                  cmdList.add(val); //add single value
            }
         }
         
         if (def.isEndGroup())
         {
            isGroup = false;
            cmdList.add(param);
            param = "";
         }
         else if (isGroup)
         {
            param += " ";
         }
      }

      // premature end of group case: where we saw the beginning, but not the end
      if ( isGroup )
      {
         isGroup = false;
         cmdList.add(param);
         param = "";
      }
      
      // Create return array and fill it      
      String [] rval = (String[])cmdList.toArray(new String[cmdList.size()]);
      
      return rval;
   }

   /**
    * Returns the resolved environmental parameters for this process definition.
    * Each element of the returned array has environment variable settings in
    * the format "name=value".
    *
    * @param ctx a {@link java.util.Map map}, which contains data for 
    * executing the process, may not be <code>null</code>
    *
    * @return the resolved environmental parameters, may be <code>null</code>
    * if no environment setting is specified.
    *
    * @throws PSResolveException if any error occurs resolving the environment
    * parameters
    * @throws PSProcessException if a resolver cannot be obtained.
    */
   public String[] getEnvParams(Map ctx)
      throws PSResolveException, PSProcessException
   {
      if (ctx == null)
         throw new IllegalArgumentException("process context may not be null");

      List cmdList = new ArrayList();
      String [] cmdArray = null;
      if (m_envParams != null)
      {
         for (int i = 0; i < m_envParams.length; i++)
         {
            // ignore any params with no name
            String name = m_envParams[i].getName();
            if (name != null)
            {
               cmdList.add(m_envParams[i].getName() + "=" + 
                  getResolvedValue(m_envParams[i].getValue(), ctx));
            }
         }
         cmdArray = (String[])cmdList.toArray(new String[cmdList.size()]);   
      }

      return cmdArray;
   }
   
   /**
    * Resolves the value of the supplied resolvable value using its defined
    * resolver.
    * 
    * @param val The value to resolve, assumed not <code>null</code>.
    * @param ctx The context to use, assumed not <code>null</code>, may be
    * emtpy.
    * 
    * @return The resolved value, never <code>null</code>, may be empty.
    * 
    * @throws PSResolveException If there is an error resolving the value.
    * @throws PSProcessException If there is an error creating the resolver.
    */
   private String getResolvedValue(PSResolvableValue val, Map ctx) 
      throws PSProcessException
   {  
      IPSVariableResolver resolver = createResolver(val);
      return resolver.getValue(val.getValue(), ctx);      
   }

   /**
    * Creates the variable resolver specified by the resolvable value.
    *
    * @param val the value specifying the resolver to be created,
    * assumed not <code>null</code>
    *
    * @return the variable resolver, never <code>null</code>
    *
    * @throws PSProcessException if the specified resolver cannot be 
    * instantiated.
    */
   private IPSVariableResolver createResolver(PSResolvableValue val)
       throws PSProcessException
   {
      String resolverClass = val.getResolver();

      if (resolverClass == null)
         resolverClass = DEFAULT_RESOLVER;

      try
      {
         
         IPSVariableResolver varResolver = 
            (IPSVariableResolver)ms_resolvers.get(resolverClass);
         if (varResolver == null)
         {
            varResolver =
               (IPSVariableResolver)Class.forName(resolverClass).newInstance();
            ms_resolvers.put(resolverClass, varResolver);
         }
            
         return varResolver;
      }
      catch (ClassNotFoundException cls)
      {
         throw new PSProcessException(
            "Class not found exception: " + cls.getLocalizedMessage());
      }
      catch (IllegalAccessException iae)
      {
         throw new PSProcessException(
            "Illegal access exception: " + iae.getLocalizedMessage());
      }
      catch (InstantiationException ins)
      {
         throw new PSProcessException(
            "Class not found exception: " + ins.getLocalizedMessage());
      }
   }

   /**
    * Creates the param defs specified by the element
    * <code>childEl</code>. Creates a def for every 
    * {@link PSParamDef#XML_NODE_NAME} child element of the specified element.
    *
    * @param childEl the element specifying the params to be created,
    * assumed not <code>null</code>
    *
    * @return the param defs, never <code>null</code>, may be empty
    *
    * @throws PSProcessException if the element is invalid
    */
   private PSParamDef[] createParams(Element childEl)
       throws PSProcessException
   {
      List list = new ArrayList();
      NodeList nl = childEl.getChildNodes();
      int length = nl.getLength();;
      for (int i = 0; i < length; i++)
      {
         Node node = nl.item(i);
         if (node instanceof Element)
         {
            Element el = (Element)node;
            String childTagName = el.getTagName();
            if (childTagName.equals(PSParamDef.XML_NODE_NAME))
            {
               PSParamDef param = new PSParamDef(el);
              
               list.add(param);
            }
            else if (childTagName.equals(EL_GROUP))
            {
               PSParamDef[] groupedParams = createParams(el);
               
               if (groupedParams.length >= 1)
               {
                  groupedParams[0].setBeginGroup();
                  groupedParams[groupedParams.length-1].setEndGroup();
               }
               
               list.addAll(Arrays.asList(groupedParams));
            }
         }
      }

      return (PSParamDef[]) list.toArray(new PSParamDef[list.size()]);
   }

   /**
    * Returns the supported operating systems.
    *
    * @return the array containing <code>PSProcessManager.OS_XXX</code>
    * constants for the operating systems for which this process is defined.
    */
   public int[] getSupportedOS()
   {
      return m_supportedOS;
   }

   /**
    * Stores the supported operating systems, initialized in the
    * <code>fromXml</code> method, never modified or <code>null</code> after
    * that.
    */
   private int[] m_supportedOS = null;
   
   /**
    * Stores the resolvable value used to resolve the working directory,
    * initialized in the <code>fromXml</code> method, may be <code>null</code>
    * if no working directory is set.
    */
   private PSResolvableValue m_dirValue;
   
   /**
    * Stores the resolvable value used to resolve the executable,
    * initialized in the <code>fromXml</code> method, never <code>null</code>
    * or modified after initialization.
    */
   private PSResolvableValue m_execValue;
 
   /**
    * Stores the param defs used to resolve the process command parameters,
    * initialized in the <code>fromXml</code> method, may be <code>null</code>
    * if no process parameters are set.
    */
   private PSParamDef[] m_cmdParams;
   
   /**
    * Stores the param defs used to resolve the environment to set
    * when executing the process, initialized in the <code>fromXml</code>
    * method, may be <code>null</code> if no environment setting is specified.
    */
   private PSParamDef[] m_envParams;
   
   /**
    * Map of cached resolvers, key is the class name as a <code>String</code>
    * and value is the <code>IPSVariableResolver</code>.  Never 
    * <code>null</code>, entries are added by calls to 
    * {@link #createResolver(PSResolvableValue)}.
    */
   private static Map ms_resolvers = new ConcurrentHashMap();

   /**
    * Constant for the default resolver class.
    */
   public static final String DEFAULT_RESOLVER =
      "com.percussion.process.PSLiteralResolver";

   // Constants for XML element and attribute names
   public static final String NODE_NAME = "PSXProcessDef";
   
   private static final String EL_DIR = "dir";
   private static final String EL_EXECUTABLE = "executable";
   private static final String EL_GROUP = "group";
   private static final String EL_PARAMS = "params";
   private static final String EL_ENV = "env";
   private static final String ATTR_OS = "os";   

}

