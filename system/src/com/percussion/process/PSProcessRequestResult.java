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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.process;

import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is the object representation of the dtd defined in 
 * PSXProcessRequestResult.dtd. It is immutable.
 *
 * @author paulhoward
 */
public class PSProcessRequestResult
{
   /**
    * One of the allowed values for the status. See {@link #getStatus()} for
    * details. Used to indicated that the process started and completed within
    * the allowed wait time. 
    */
   public static final int STATUS_FINISHED = 0;

   /**
    * One of the allowed values for the status. See {@link #getStatus()} for
    * details. Used to indicated that the request document was invalid in some
    * way, or that the the process couldn't run (e.g. def not found). If the 
    * process started, but had to be forcibly stopped, the {@link 
    * #STATUS_TERMINATED} code is used instead. Note that even if a process 
    * returns an error code, that this is not an error condition.
    */
   public static final int STATUS_ERROR = 1;

   /**
    * One of the allowed values for the status. See {@link #getStatus()} for
    * details. Used to indicated that the the process started successfully, but 
    * did not complete within the allowed time. 
    */
   public static final int STATUS_TERMINATED = 2;
   
   /**
    * One of the allowed values for the status. See {@link #getStatus()} for
    * details. Used to indicated that the the process started successfully,
    * while it is not known whether it has finished yet or when it will finish.
    * Returned from the processes that have a long life span like daemons.  
    */
   public static final int STATUS_STARTED = 3;
   
   
   /**
    * Convenience ctor that calls {@link 
    * #PSProcessRequestResult(String, int, String, int, int) 
    * this(name, resultCode, resultText, status, -1)}.
    */
   public PSProcessRequestResult(String name, int resultCode, String resultText,
         int status)
   {
      this(name, resultCode, resultText, status, -1);
   }
   
   /**
    * Construct a response from all the pieces.
    * 
    * @param name See {@link #getName()} for desc. Never <code>null</code> or
    * empty.
    * 
    * @param resultCode The numeric value returned after the process has 
    * completed, or -1 for any other error conditions.
    * 
    * @param resultText If the process is started, the console output. 
    * Otherwise, some message describing the error condition. May be <code>
    * null</code> or empty, in which case a default message indicating this
    * is used. All control codes are stripped from the string before being
    * saved. This prevents problems when the object is serialized to xml.
    * 
    * @param status One of the STATUS_xxx values.
    * 
    * @param actionHandle See {@link #getActionHandle()} for details. If
    * <code>status</code> is not <code>STATUS_STARTED</code>, this value is
    * ignored (treated as -1). Any value < 1 is treated as -1;
    */
   public PSProcessRequestResult(String name, int resultCode, String resultText,
         int status, int actionHandle)
   {
      if (null == name || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }
      if (status < 0 || status >= STATUS_VALUES.length)
      {
         throw new IllegalArgumentException(
               "status must be one of the STATUS_xxx values");
      }
      if (status != STATUS_STARTED || actionHandle < 1)
         actionHandle = -1;
      
      setName(name);
      m_resultCode = resultCode;
      setResultText(resultText); 
      m_status = status; 
      m_actionHandle = actionHandle;
   }
   
   /**
    * See the {@link #PSProcessRequestResult(String, int, String, int) other 
    * ctor} for description.
    * 
    * @param src Never <code>null</code>.
    * @throws Exception If the supplied element doesn't conform the the dtd
    * found in PSXProcessRequestResult.dtd.
    */
   public PSProcessRequestResult(Element src)
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
      m_status = PSXMLDomUtil.checkAttributeEnumerated(src, STATUS_ATTR, 
            STATUS_VALUES, false);
      String resultCode = 
         PSXMLDomUtil.checkAttribute(src, RESULT_CODE_ATTR, true);
      m_resultCode = Integer.parseInt(resultCode);
      if (m_status == STATUS_STARTED)
      {
         String actionHandle = 
            PSXMLDomUtil.checkAttribute(src, ACTION_HANDLE_ATTR, false);
         m_actionHandle = actionHandle.trim().length() == 0 ? -1 : 
               Integer.parseInt(actionHandle);
      }
      PSXmlTreeWalker walker = new PSXmlTreeWalker(src);
      setResultText(walker.getElementData(RESULT_ELEM));
   }
   
   /**
    * The name passed in the request as the process definition name. This must
    * match one of the entries in the process definition file defined for the
    * daemon.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * An indication of the results of attempting to handle the request. A
    * string representation of the of the <code>STATUS_xxx</code> codes.
    * @return Never <code>null</code> or empty.
    */
   public String getStatus()
   {
      return STATUS_VALUES[m_status];
   }
   
   /**
    * See {@link #getStatus()} for details.
    * @return One of the <code>STATUS_xxx</code> values.
    */
   public int getStatusCode()
   {
      return m_status;
   }

   /**
    * The numeric value returned as a result of executing the requested
    * process.
    * 
    * @return For errors, -1, otherwise the value returned by the process.
    */
   public int getResultCode()
   {
      return m_resultCode;
   }
   
   /**
    * The text associated w/ handling the request, dependent on the status.
    * For errors, it is the error message. For the other 2 status values, it
    * is the console output from the process. If there was no output, the 
    * message states this (e.g. "-- no result text --").
    * 
    * @return Never <code>null</code>.
    */
   public String getResultText()
   {
      return m_resultText;
   }
   
   /**
    * If the process is allowed to continue running after the action is 
    * executed, this handle can be used to get information about
    * the process at a later time. -1 is used by default and means the process
    * terminated before the action execution code returned, otherwise, it is
    * a positive value. 
    * <p>If the {@link #getStatusCode()} is not <code>STATUS_STARTED</code>,
    * this value will be -1.
    * 
    * @return -1 if the associated process was terminated, otherwise a positive
    * value that can be used with the command handler.
    */
   public int getActionHandle()
   {
      return m_actionHandle;
   }
   
   /**
    * Builds the xml representation of this object according to the 
    * dtd defined in PSXProcessRequestResult.dtd.
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
      root.setAttribute(STATUS_ATTR, STATUS_VALUES[m_status]);
      root.setAttribute(RESULT_CODE_ATTR, String.valueOf(m_resultCode));
      root.setAttribute(ACTION_HANDLE_ATTR, String.valueOf(m_actionHandle));
      PSXmlDocumentBuilder.addElement(doc, root, RESULT_ELEM, m_resultText);
      return root;
   }
   
   /**
    * Trims the name before saving it. See {@link #getName()} for details.
    * @param name Assumed not <code>null</code> or empty.
    */
   private void setName(String name)
   {
      m_name = name.trim();
   }
   
   /**
    * If the supplied string is <code>null</code> or empty, sets it to 
    * indicate that no text is present. See {@link #getResultText()} for more 
    * details. Strips out all control codes.
    * 
    * @param text May be <code>null</code> or empty.
    */
   private void setResultText(String text)
   {
      boolean empty = text == null || text.trim().length() == 0;
      m_resultText = empty ? "-- No output --" : text;
      //strip out any non-Xml conforming characters
      StringBuffer buf = new StringBuffer(m_resultText);
      StringBuffer cleanBuf = new StringBuffer(m_resultText.length());
      for (int i = 0; i < buf.length(); i++)
      {
         char c = buf.charAt(i);
         if (!Character.isISOControl(c) || Character.isWhitespace(c))
            cleanBuf.append(c);
      }
      m_resultText = cleanBuf.toString();  
   }
   
   /**
    * The string representations of the <code>STATUS_xxx</code> codes. The 
    * code is an index into this array.
    */
   private String[] STATUS_VALUES = 
   {
      // the default must be first in the array
      "finished",
      "error",
      "terminated",
      "started"
   };

   /**
    * See {@link #getName()} for description. Set in ctor, then never changed.
    * Never <code>null</code> or empty.
    */
   private String m_name;

   /**
    * See {@link #getStatusCode()} for description. Set in ctor, then
    * never changed. 
    */
   private int m_status;

   /**
    * See {@link #getResultCode()} for description. Set in ctor, then never 
    * changed. 
    */
   private int m_resultCode;

   /**
    * See {@link #getResultText()} for description. Set in ctor, then never 
    * changed. Never <code>null</code> or empty.
    */
   private String m_resultText;
   
   /**
    * See {@link #getActionHandle()} for description. Set in ctor, then never 
    * changed. 
    */
   private int m_actionHandle = -1;
   
   //xml element/attribute constants
   private static final String XML_NODE_NAME = "PSXProcessRequestResult";
   private static final String RESULT_ELEM = "Result";
   private static final String NAME_ATTR = "procName";
   private static final String STATUS_ATTR = "status";
   private static final String RESULT_CODE_ATTR = "resultCode";
   private static final String ACTION_HANDLE_ATTR = "actionHandle";
}
