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

package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

      Document resultDoc = null;
      try(FileInputStream in = new FileInputStream(mapFile)){
         resultDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
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


         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element mapEl = map.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, mapEl);

         PSDeploymentHandler.DBMSMAP_DIR.mkdirs();         
         File mapFile = getFileFromServerName(map.getSourceServer());

         try(FileOutputStream out = new FileOutputStream(mapFile)){
            PSXmlDocumentBuilder.write(doc, out);
         }
         catch (Exception e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
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
      StringBuilder fileName = new StringBuilder();
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
