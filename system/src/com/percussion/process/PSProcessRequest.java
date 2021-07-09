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

import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is the object representation of the dtd defined in 
 * sys_PSXProcessRequest.dtd. It is immutable.
 *
 * @author paulhoward
 */
public class PSProcessRequest
{
   /**
    * Creates an object whose <code>toXml</code> can be used to generate a
    * request document for the remote process daemon.
    * 
    * @param name See {@link #setName(String)} for details.
    * 
    * @param wait See {@link #setWait()} for details.
    *
    * @param terminate See {@link #setTerminate()} for details.
    *  
    * @param processEnv See {@link #setParams(Map)} for details.
    */
   public PSProcessRequest(String name, int wait, boolean terminate,
      Map processEnv)
   {
      if (null == name || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }
      
      setName(name);
      setWait(wait);
      setTerminate(terminate);
      setParams(processEnv);
   }
   
   /**
    * See {@link #PSProcessRequest(String, int,Map) other ctor} for description.
    * @param src Never <code>null</code>.
    * @throws Exception If the supplied element doesn't conform the the dtd
    * found in PSXProcessRequest.dtd.
    */
   public PSProcessRequest(Element src)
      throws Exception
   {
      if (null == src)
      {
         throw new IllegalArgumentException("source element cannot be null");
      }
      if (!src.getNodeName().equals(XML_NODE_NAME))
      {
         throw new Exception("Expected node " + XML_NODE_NAME + " but got " 
               + src.getNodeName());
      }
      
      setName(PSXMLDomUtil.checkAttribute(src, NAME_ATTR, true));
      setWait(PSXMLDomUtil.checkAttributeInt(src, WAIT_ATTR, false));
      setTerminate(
            PSXMLDomUtil.checkAttributeBool(src, TERMINATE_ATTR, false, 
            String.valueOf(true)));
      
      PSXmlTreeWalker walker = new PSXmlTreeWalker(src);
      Element paramEl = walker.getNextElement(PARAM_ELEM);
      
      while (null != paramEl)
      {
         String name = paramEl.getAttribute(PARAM_NAME_ATTR);
         if (name == null || name.trim().length() == 0)
            throw new Exception("Param missing name.");
         String value = PSXmlTreeWalker.getElementData(paramEl);
         if (value == null || value.trim().length() == 0)
            throw new Exception("Param missing value.");
         m_params.put(name, value);
         paramEl = walker.getNextElement(PARAM_ELEM);
      }
   }

   /**
    * The name of the process definition to execute. 
    * @return The name supplied in the ctor. Never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;  
   }
   
   /**
    * Maximum time to wait (in milliseconds) for the process to complete 
    * (and thus return). If not finished after this period has elapsed, what 
    * to do w/ the process is determined by the {@link #isTerminate()} flag.
    */
   public int getWait()
   {
      return m_wait;
   }

   /**
    * Before returning, if the process has not finished, should it be 
    * forcefully ended or left running?.
    * @return <code>true</code> means end the process, <code>false</code> means 
    * leave it running.
    */
   public boolean isTerminate()
   {
      return m_terminate;
   }
   
   /**
    * See {@link #isTerminate()} for description.
    * 
    * @param terminate <code>true</code> means end the process before 
    * returning, <code>false</code> means leave it running.
    */
   private void setTerminate(boolean terminate)
   {
      m_terminate = terminate;
   }

   /**
    * The set of parameters for the process manager. If parameters supplied
    * here and those supplied in the daemon config file have the same name,
    * the value supplied here will be used.
    *  
    * @return Each entry has a key as <code>String</code> and a value as 
    * <code>String</code>. Never <code>null</code>, may be empty. No 
    * <code>null</code> or empty keys or values.
    */
   public Map getParams()
   {
      return m_params;
   }

   /**
    * Builds the xml representation of this object according to the 
    * dtd defined in PSXProcessRequest.dtd.
    * 
    * @param doc The context in which the element is created. Never <code>
    * null</code>.
    * 
    * @return Never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(WAIT_ATTR, String.valueOf(m_wait));
      root.setAttribute(TERMINATE_ATTR, String.valueOf(m_terminate));
      if (m_params.size() > 0)
      {
         Element paramsEl = 
               PSXmlDocumentBuilder.addElement(doc, root, PARAMS_ELEM, null);
         Iterator params = m_params.keySet().iterator();
         while (params.hasNext())
         {
            String name = (String) params.next();
            Element paramEl = PSXmlDocumentBuilder.addElement(doc, paramsEl, 
                  PARAM_ELEM, (String) m_params.get(name));
            paramEl.setAttribute(PARAM_NAME_ATTR, name);
         }
      }
      return root;
   }
   
   /**
    * See {@link #getWait()} for description.
    * 
    * @param howLong Any value &lt;0 will be treated as 0.
    */
   private void setWait(int howLong)
   {
      if (howLong < 0)
         howLong = 0;
      m_wait = howLong;
   }

   /**
    * See {@link #getName()} for description.
    * 
    * @param string Assumed not <code>null</code> or empty.
    */
   private void setName(String name)
   {
      m_name = name.trim();
      
   }
   
   /**
    * Name/value pairs that will be passed to the process
    * invocation engine to be used to resolve macros within the definition.
    * Validates the supplied params and copies them to the map owned 
    * by this object.
    * 
    * @param params  May be <code>null</code> or empty. Does a <code>toString
    * </code> on each key and value and validates that they are not empty.
    * <code>null</code> keys and values are skipped.
    */
   private void setParams(Map params)
   {
      if (null == params || params.size() == 0)
         return;
      Iterator paramsIt = params.keySet().iterator();
      while (paramsIt.hasNext())
      {
         Object o = paramsIt.next();
         if (null != o)
         {
            String name = o.toString();
            o = params.get(name);
            if (null != o)
            {
               m_params.put(name, o.toString());
            }
         }
      }
   }
   
   /**
    * See {@link #getName()} for description. Set in ctor, then never changed.
    */
   private String m_name;
   
   /**
    * See {@link #getWait()} for description. Set in ctor, then never changed.
    */
   private int m_wait;
   
   /**
    * See {@link #isTerminate()} for description. Set in ctor,
    * then never changed. Defaults to <code>false</code>.
    */
   private boolean m_terminate = false;
   

   /**
    * Container for parameter defs. Each entry is a non-<code>null</code>, non-
    * empty <code>String</code>, <code>String</code>. Entries added in ctor
    * then never modified.
    */
   private Map m_params = new HashMap();

   //xml element/attribute constants
   static final String XML_NODE_NAME = "PSXProcessRequest";
   static final String PARAMS_ELEM = "Params";
   static final String PARAM_ELEM = "Param";
   static final String PARAM_NAME_ATTR = "name";
   private static final String NAME_ATTR = "procName";
   private static final String WAIT_ATTR = "waitMillis";
   private static final String TERMINATE_ATTR = "terminate";
}
