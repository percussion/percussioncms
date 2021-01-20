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

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;


/**
 * This class provides an implementation for {@link IPSCommandHandler} to
 * a remote server. It accomplishes most of its work by using the 
 * <code>sendMessage</code> method in the {@link PSProcessDaemon} class.
 * <p>Instances of this class are immutable.
 *
 * @author paulhoward
 */
public class PSRemoteCommandHandler implements IPSCommandHandler
{
   /**
    * Creates an immutable instance of this class.
    * 
    * @param server The name of the machine where the remote daemon is running.
    * Never <code>null</code> or empty.
    * 
    * @param port The port on the remote machine where the remote daemon is
    * running. Must be &gt;0.
    * 
    * @param relPath All paths supplied to any method in this class are 
    * interpreted relative to this path. This path is stripped from the 
    * supplied path and the resulting path becomes the virtual path used on 
    * the remote server. All paths are normalized to use forward slash as the
    * path separator. Never <code>null</code>.
    */
   public PSRemoteCommandHandler(String server, int port, File relPath)
   {
      if (null == server || server.trim().length() == 0)
      {
         throw new IllegalArgumentException("server cannot be null or empty");
      }
      if (port <= 0)
      {
         throw new IllegalArgumentException("port must be > 0");
      }
      if (null == relPath)
      {
         throw new IllegalArgumentException("relPath cannot be null");
      }
         
      m_server = server;
      m_port = port;
      m_relPath = relPath.getAbsolutePath().replace('\\', '/').toLowerCase();
   }
   
   //see base class method for details
   public PSProcessRequestResult executeProcess(
      String procName,
      Map extraParams,
      int wait,
      boolean terminate)
      throws PSProcessException
   {
      Document request = null;
      Document resultDoc = null;
      try
      {
         request = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.replaceRoot(request, new PSProcessRequest(
               procName, wait, terminate, extraParams).toXml(request));
         
         StringBuffer result = new StringBuffer();
         List params = new ArrayList();
         params.add(PSXmlDocumentBuilder.toString(request));
         PSProcessDaemon.sendCommand(m_server, m_port,
               PSProcessDaemon.CMD_EXEC_PROCESS, params, result);
         resultDoc = PSXmlDocumentBuilder.createXmlDocument(
               new StringReader(result.toString()), false);
         return new PSProcessRequestResult(resultDoc.getDocumentElement());
      }
      catch (Exception e)
      {
         /* There's no reason to catch all exceptions individually because 
          * the same action would be taken in all cases.
          */
         throw new PSProcessException(e.getLocalizedMessage());
      }
   }


   //see base class method for details
   public PSProcessRequestResult waitOnProcess(int handle, int wait) 
      throws PSProcessException 
   {
      Document request = null;
      Document resultDoc = null;
      try
      {
         StringBuffer result = new StringBuffer();
         List params = new ArrayList();
         params.add(String.valueOf(handle));
         params.add(String.valueOf(wait));
         PSProcessDaemon.sendCommand(m_server, m_port,
               PSProcessDaemon.CMD_WAIT_FOR_PROCESS, params, result);
         resultDoc = PSXmlDocumentBuilder.createXmlDocument(
               new StringReader(result.toString()), false);
         return new PSProcessRequestResult(resultDoc.getDocumentElement());
      }
      catch (Exception e)
      {
         /* There's no reason to catch all exceptions individually because 
          * the same action would be taken in all cases.
          */
         throw new PSProcessException(e.getLocalizedMessage());
      }
   }

   //see base class method for details
   public boolean fileSystemObjectExists(File path) throws PSProcessException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      try
      {
         StringBuffer result = new StringBuffer();
         List params = new ArrayList();
         String virtualPath = convertPath(path);
         params.add(virtualPath);
         int resultCode = PSProcessDaemon.sendCommand(m_server, m_port, 
               PSProcessDaemon.CMD_FS_OBJ_EXISTS, params, result);
         if (resultCode != 0)
            throw new PSProcessException(result.toString());
         return result.toString().equals("1");
      }
      catch (IOException e)
      {
         throw new PSProcessException(e.getLocalizedMessage());
      }
   }

   //see base class method for details
   public void removeFileSystemObject(File path) 
      throws PSProcessException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      try
      {
         StringBuffer result = new StringBuffer();
         List params = new ArrayList();
         String virtualPath = convertPath(path);
         params.add(virtualPath);
         int resultCode = PSProcessDaemon.sendCommand(m_server, m_port, 
               PSProcessDaemon.CMD_REMOVE_FS_OBJ, params, result);
         if (resultCode != 0)
            throw new PSProcessException(result.toString());
      }
      catch (IOException e)
      {
         throw new PSProcessException(e.getLocalizedMessage());
      }
   }

   //see base class method for details
   public void makeDirectories(File path) 
      throws PSProcessException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      try
      {
         StringBuffer result = new StringBuffer();
         List params = new ArrayList();
         String virtualPath = convertPath(path);
         params.add(virtualPath);
         int resultCode = PSProcessDaemon.sendCommand(m_server, m_port, 
               PSProcessDaemon.CMD_MAKE_DIRS, params, result);
         if (resultCode != 0)
            throw new PSProcessException(result.toString());
      }
      catch (IOException e)
      {
         throw new PSProcessException(e.getLocalizedMessage());
      }
   }

   //see base class method for details
   public void saveTextFile(File path, String content)
      throws IOException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      StringBuffer result = new StringBuffer();
      List params = new ArrayList();
      String virtualPath = convertPath(path);
      params.add(virtualPath);
      if (content == null)
         content = "";
      params.add(content);
      int resultCode = PSProcessDaemon.sendCommand(m_server, m_port, 
            PSProcessDaemon.CMD_SAVE_FILE, params, result);
      if (resultCode != 0)
         throw new IOException(result.toString());
   }

   //see base class method for details
   public void saveBinaryFile(File path, InputStream src)
      throws IOException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      StringBuffer result = new StringBuffer();
      List params = new ArrayList();
      String virtualPath = convertPath(path);
      params.add(virtualPath);
      if (src == null)
         src = new ByteArrayInputStream(new byte[0]);
      params.add(src);
      int resultCode = PSProcessDaemon.sendCommand(m_server, m_port, 
            PSProcessDaemon.CMD_SAVE_BINARY_FILE, params, result);
      if (resultCode != 0)
         throw new IOException(result.toString());
   }

   //see base class method for details
   public String getTextFile(File path) 
      throws IOException
   {
      if (null == path)
      {
         throw new IllegalArgumentException("path cannot be null");
      }
      StringBuffer result = new StringBuffer();
      List params = new ArrayList();
      String virtualPath = convertPath(path);
      params.add(virtualPath);
      int resultCode = PSProcessDaemon.sendCommand(m_server, m_port, 
            PSProcessDaemon.CMD_GET_FILE, params, result);
      if (resultCode != 0)
         throw new IOException(result.toString());
      return result.toString();      
   }

   /**
    * Modifies the supplied path based on information supplied in the ctor.
    * 
    * @param path Assumed not <code>null</code>.
    * 
    * @return The <code>String</code> representation of the supplied path 
    * translated to a virtual path. Never <code>null</code> or empty. The
    * returned path always uses the / as the path separator.
    * 
    * @throws IOException If the supplied path does not begin w/ the string
    * found in <code>m_relPath</code>.
    */
   private String convertPath(File path)
      throws IOException
   {
      String origPath = path.getAbsolutePath().replace('\\', '/');
      if (origPath.toLowerCase().startsWith(m_relPath))
      {
         return origPath.substring(m_relPath.length());
      }
      throw new  IOException("Invalid path supplied '" 
            + path.getAbsoluteFile() 
            + "', must begin with '"
            + m_relPath 
            + "'.");
   }

   /**
    * See ctor <code>port</code> param for description. 
    * Set in ctor, then always &gt; 0 and never modified.
    */
   private int m_port;

   /**
    * See ctor <code>server</code> param for description. 
    * Set in ctor, never <code>null</code>, empty or modified after that.
    */
   private String m_server;
   
   /**
    * See ctor <code>relPath</code> param for description. Set in ctor, then 
    * never changed or modified after that. Stored with path separator as a 
    * forward slash and lower-cased.
    */
   private String m_relPath;
}
