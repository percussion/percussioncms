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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.util.PSCharSets;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * PSMergeXMLAction will merge 2 xml documents.  The destination xml
 * document needs to be installed already. The source xml document needs
 * to be added to the source tree package and installed.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="mergeXMLAction"
 *              class="com.percussion.ant.install.PSMergeXMLAction"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to merge two xml files.
 *
 *  <code>
 *  &lt;mergeXMLAction copyElement="PSXmlElement"
 *                     destinationDoc="rxconfig/Server/config.xml"
 *                     parent="Parent1,Parent2"
 *                     sourceDoc="C:\Rhythmyx\installerTemp\source_tree\1.doc"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSMergeXMLAction extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      if(m_strSourceDoc != null &&
            m_strSourceDoc.length() > 0 &&
            m_strDestinationDoc != null &&
            m_strDestinationDoc.length() > 0 &&
            m_strCopyElement != null &&
            m_strCopyElement.length() > 0)
      {
         //get the root dir
         String strRootDir = getRootDir();
         Writer outWriter = null;

         if(strRootDir != null)
         {
            String strDestinationPath = strRootDir + File.separator +
            m_strDestinationDoc;

            strDestinationPath = Paths.get(strRootDir,strDestinationPath).normalize().toAbsolutePath().toString();

            File destFile = new File(strDestinationPath);
            if(destFile.exists())
            {
               //get the source resource file
               URL urlSource = null;
               InputStream inSource = null;
               try
               {
                  urlSource = getResource(m_strSourceDoc);
                  inSource = urlSource.openStream();

                  //get the Document objects
                  Document srcDoc = null;
                  Document destDoc = null;

                  DocumentBuilder db =
                     PSXmlDocumentBuilder.getDocumentBuilder(false);
                  srcDoc = db.parse(new InputSource(inSource));
                  File destXmlFile = new File(strDestinationPath);
                  if (destXmlFile.exists())
                     destDoc = db.parse(destXmlFile);
                  if ((srcDoc == null) || (destDoc == null)) // should not get here now
                     return;

                  //get the destination element for the copy
                  Element destElement = destDoc.getDocumentElement();
                  if(m_strParent != null)
                  {
                     for(int iParent = 0; destElement != null &&
                     iParent < m_strParent.length;
                     ++iParent)
                     {
                        String strParent = m_strParent[iParent];
                        NodeList parents =
                           destElement.getElementsByTagName(
                                 strParent);
                        if(parents != null &&
                              parents.getLength() > 0 &&
                              parents.item(0) instanceof Element)
                           destElement = (Element)parents.item(0);
                     }
                  }

                  //get the copy elements from the source doc
                  NodeList copyNodes =
                     srcDoc.getElementsByTagName(m_strCopyElement);

                  if(copyNodes != null && destElement != null)
                  {
                     for(int iCopyNode = 0; iCopyNode < copyNodes.getLength();
                     ++iCopyNode)
                     {
                        Node copyNode = copyNodes.item(iCopyNode);
                        if(copyNode != null)
                        {
                           //import the node into destDoc
                           Node importedNode = destDoc.importNode(
                                 copyNode, true);
                           //copy the node in
                           if (importedNode != null)
                              destElement.appendChild(importedNode);
                        }
                     }
                  }

                  //now write the dest xml doc
                  try(FileOutputStream os = new FileOutputStream(destFile)) {
                     outWriter = new OutputStreamWriter(os, PSCharSets.rxJavaEnc());

                     PSXmlDocumentBuilder.write(destDoc, outWriter);
                  }
                  inSource.close();
                  inSource = null;

                  outWriter.close();
                  outWriter = null;

               }
               catch (IOException io)
               {
                  PSLogger.logInfo(ERROR + io.getMessage());
                  PSLogger.logInfo(io);
               }
               catch(SAXException e)
               {
                  PSLogger.logInfo(ERROR + e.getMessage());
                  PSLogger.logInfo(e);
               }
               finally
               {
                  try
                  {
                     if(inSource != null)
                        inSource.close();

                     if (outWriter!=null)
                        outWriter.close();

                  }
                  catch (IOException io)
                  {
                     PSLogger.logInfo(ERROR + io.getMessage());
                     PSLogger.logInfo(io);
                  }
               }
            }
         }
      }
   }


   /*****************************************************************************
    * Bean properties
    ****************************************************************************/


   /**
    *  Mutator for parent.
    */
   public void setParent(String strParent)
   {
      m_strParent = convertToArray(strParent);
   }

   /**
    *  Accesssor for parent.
    */
   public String[] getParent()
   {
      return m_strParent;
   }

   /**
    *  Mutator for copy element.
    */
   public void setCopyElement(String strCopy)
   {
      m_strCopyElement = strCopy;
   }

   /**
    *  Accesssor for copy element.
    */
   public String getCopyElement()
   {
      return m_strCopyElement;
   }

   /**
    *  Mutator for source doc.
    */
   public void setSourceDoc(String strSourceDoc)
   {
      m_strSourceDoc = strSourceDoc;
   }

   /**
    *  Accesssor for source doc.
    */
   public String getSourceDoc()
   {
      return m_strSourceDoc;
   }

   /**
    *  Mutator for Dest doc.
    */
   public void setDestinationDoc(String strDestDoc)
   {
      m_strDestinationDoc = strDestDoc;
   }

   /**
    *  Accesssor for Dest doc.
    */
   public String getDestinationDoc()
   {
      return m_strDestinationDoc;
   }

   /**
    * The parent element which contains the elements to copy.
    * The list will be used to create a hier name.
    */
   private String[] m_strParent = new String[0];

   /**
    * The name of the elements to copy.
    */
   private String m_strCopyElement = null;

   /**
    * The destination xml document file. This file must exist on the
    * file system. It must be relative to the root directory.
    */
   private String m_strDestinationDoc = null;

   /**
    * The source xml document file.  This file must be in the resouces.
    * You can add a file to the resources through <code>RxISResourceFiles</code>.
    */
   private String m_strSourceDoc = null;


   private static final String ERROR = "ERROR: ";
}
