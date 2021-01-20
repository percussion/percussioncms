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

package com.percussion.deployer.server;

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * PSDbmsMapManager handles saving and retrieving <code>PSDbmsMap</code>
 * objects from and to the file system.
 */
public class PSDbmsMapManager
{

   /**
    * Get a server's <code>PSDbmsMap</code> object from the file system if
    * exist, otherwise get a newly created one.
    *
    * @param server The name of the source server, it may not be empty or
    * <code>null</code>.
    *
    * @return The server's <code>PSDbmsMap</code> object from the file system
    * if exist; otherwise, return a newly created <code>PSDbmsMap</code>
    * object if not exist.
    *
    * @throws IllegalArgumentException If any param is invalid.
    * @throws PSDeployException if there are other errors.
    */
   public static PSDbmsMap getDbmsMap(String server) throws PSDeployException
   {
      if ( server == null || server.trim().length() == 0 )
         throw new IllegalArgumentException("server may not be null or empty");

      PSDbmsMap result = null;

      File mapFile = getFileFromServerName(server);

      if (!mapFile.exists())
      {
         result = new PSDbmsMap(server);
      }
      else
      {
         try
         {
            Document doc = getDbmsMapDoc(server);
            Element root = doc.getDocumentElement();
            result = new PSDbmsMap(root);
         }
         catch (Exception e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
         }
      }
      return result;
   }

   /**
    * Get the <code>Document</code>, which contains the specified
    * <code>PSDbmsMap</code> object from disk.
    *
    * @param serverName The name of the server, assumed not <code>null</code>
    * or empty, and it exist on the file system.
    *
    * @return The doc, never <code>null</code>.
    *
    * @throws PSDeployException if there is an error while getting the
    * <code>Document</code>.
    */
   private static Document getDbmsMapDoc(String serverName) throws PSDeployException
   {      
      File mapFile = getFileFromServerName(serverName);

      FileInputStream in = null;
      Document resultDoc = null;
      try
      {
         in = new FileInputStream(mapFile);
         resultDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (in != null)
            try {in.close();} catch(IOException ex){}
      }
      return resultDoc;
   }


   /**
    * Saving a specified <code>PSDbmsMap</code> object to the file system.
    *
    * @param map The <code>PSDbmsMap</code> object what need to be saved,
    * it may not be <code>null</code>
    *
    * @throws IllegalArgumentException If <code>map</code> is <code>null</code>.
    * @throws PSDeployException if there is an error while saving to disk.
    */
   public static void saveDbmsMap(PSDbmsMap map) throws PSDeployException
   {
      if ( map == null )
         throw new IllegalArgumentException("map may not be null");

      FileOutputStream out = null;
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element mapEl = map.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, mapEl);

         PSDeploymentHandler.DBMSMAP_DIR.mkdirs();         
         File mapFile = getFileFromServerName(map.getSourceServer());

         out = new FileOutputStream(mapFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (out != null)
            try {out.close();} catch(IOException ex){}
      }
   }
   
   /**
    * Get a file name from a server name. Replace all ':' characters 
    * with '_' if there is any in the source server name. This is because
    * the character ':' is an invalid character for a file name on Windows.
    * 
    * @param serverName The server name, assume not <code>null<code> or empty.
    * 
    * @return The (transfered) file name, it will never be <code>null</code> or
    * empty.
    * 
    * @throws IllegalArgumentException If <code>serverName</code> is 
    * <code>null</code> or empty.
    */
   static private File getFileFromServerName(String serverName)
   {
      StringTokenizer toks = new StringTokenizer(serverName, ":");
      StringBuffer fileName = new StringBuffer();
      while (toks.hasMoreTokens()) 
      {
         if (fileName.length() > 0)
            fileName.append("_");
            
         fileName.append(toks.nextToken());
      }

      File mapFile = new File(PSDeploymentHandler.DBMSMAP_DIR, 
         fileName.toString() + ".xml");

      return mapFile;
   }

}
