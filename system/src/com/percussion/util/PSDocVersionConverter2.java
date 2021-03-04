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
package com.percussion.util;

import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This doc version converter is used to convert Rhythmyx 1.1 application
 * documents to Rhythmyx 2.0 application documents.
 */
public class PSDocVersionConverter2
{
   private static final Logger log = LogManager.getLogger(PSDocVersionConverter2.class);
   /**
    * The main method for command-line testing.
    */
   public static void main(String[] args)
   {
      try
      {
         InputStream in = null;
         OutputStream out = null;
         try
         {
            File inFile = new File(args[0]);
            in = new BufferedInputStream(new FileInputStream(inFile));
            Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            in.close();
            in = null;

            File extensionsDir = new File(args[1]);
            PSExtensionInstallTool tool = new PSExtensionInstallTool(extensionsDir);
            tool.installJavaHandler();
            tool.installJavaScriptHandler();
            convertApplicationDocument(tool.getExtensionManager(), doc);
            out = new FileOutputStream(inFile);
            PSXmlDocumentBuilder.write(doc, out);
            out.close();
            out = null;
         }
         finally
         {
            if (in != null)
            {
               try { in.close(); } catch (IOException e) { /* ignore */ }
            }
            if (out != null)
            {
               try { out.close(); } catch (IOException e) { /* ignore */ }
            }
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }

   /**
    * Converts a Rhythmyx 1.1 application document to Rhythmyx 2.0. The
    * conversion process will translate all extension references and
    * calls into the new extension model. Conversion will also process
    * all JavaScript UDFs defined directly in the application and convert
    * them into JavaScript extensions which are stored with the central
    * extension manager.
    *
    * @param mgr The extension manager with which UDFs will be registered.
    * Can be <CODE>null</CODE>, in which case the conversion will convert
    * all extension references but it will not install any newly converted
    * extensions with a manager. This may result in an application that
    * depends on extensions which are not installed.
    *
    * @param inDoc The document to convert. If the document is an application
    * with a version other than 1.1, this method will return
    * <CODE>false</CODE>. Otherwise, the document will be modified in place.
    *
    * @return <CODE>true</CODE> if and only if the document was converted
    * successfully from 1.1 to 2.0.
    *
    * @throws PSExtensionException
    * @throws PSNonUniqueException
    * @throws PSNotFoundException
    */
   public static boolean convertApplicationDocument(
      IPSExtensionManager mgr,
      Document inDoc
      )
      throws PSExtensionException,
         PSNonUniqueException,
         PSNotFoundException
   {
      PSDocVersionConverter2 converter = new PSDocVersionConverter2(mgr);

      return converter.convertOneOneToTwoZero(inDoc);
   }

   /**
    * Converts a Rhythmyx 1.1 application file to Rhythmyx 2.0. The
    * conversion process will translate all extension references and
    * calls into the new extension model. Conversion will also process
    * all JavaScript UDFs defined directly in the application and convert
    * them into JavaScript extensions which are stored with the central
    * extension manager.
    *
    * @param mgr The extension manager with which UDFs will be registered.
    * Can be <CODE>null</CODE>, in which case the conversion will convert
    * all extension references but it will not install any newly converted
    * extensions with a manager. This may result in an application that
    * depends on extensions which are not installed.
    *
    * @param appFile The file containing the application to convert. If the
    * file represents an application with a version other than 1.1, this
    * method will return <CODE>false</CODE>. Otherwise, the file will be
    * modified in place.
    *
    * @return <CODE>true</CODE> if and only if the file was converted
    * successfully from 1.1 to 2.0.
    *
    * @throws PSExtensionException
    * @throws PSNonUniqueException
    * @throws PSNotFoundException
    * @throws IOException
    * @throws SAXException
    */
   public static boolean convertApplicationFile(
      IPSExtensionManager mgr,
      File appFile
      )
      throws PSExtensionException,
         PSNonUniqueException,
         PSNotFoundException,
         SAXException,
         IOException
   {
      return convertApplicationFile(mgr, appFile, appFile);
   }

   /**
    * Converts a Rhythmyx 1.1 application file to Rhythmyx 2.0 and
    * stores the resulting document in the given file. The original
    * file will be unchanged. The conversion process will translate all
    * extension references and calls into the new extension model. Conversion
    * will also process all JavaScript UDFs defined directly in the
    * application and convert them into JavaScript extensions which are stored
    * with the central extension manager.
    *
    * @param mgr The extension manager with which UDFs will be registered.
    * Can be <CODE>null</CODE>, in which case the conversion will convert
    * all extension references but it will not install any newly converted
    * extensions with a manager. This may result in an application that
    * depends on extensions which are not installed.
    *
    * @param appFileIn The file containing the application to convert. If the
    * file represents an application with a version other than 1.1, this
    * method will return <CODE>false</CODE>. Otherwise, the file will be
    * modified in place.
    *
    * @param appFileOut The file to which the converted application will
    * be saved. Any existing file content will be overwritten.
    *
    * @return <CODE>true</CODE> if and only if the file was converted
    * successfully from 1.1 to 2.0.
    *
    * @throws PSExtensionException
    * @throws PSNonUniqueException
    * @throws PSNotFoundException
    * @throws IOException
    * @throws SAXException
    */
   public static boolean convertApplicationFile(
      IPSExtensionManager mgr,
      File appFileIn,
      File appFileOut
      )
      throws PSExtensionException,
         PSNonUniqueException,
         PSNotFoundException,
         SAXException,
         IOException
   {
      PSDocVersionConverter2 converter = new PSDocVersionConverter2(mgr);
      return converter.convertOneOneToTwoZero(appFileIn, appFileOut);
   }


   /**
    * Constructs a doc version converter for the default version.
    *
    * @param manager The extension manager instance, which must have
    * been properly initialized with the JavaScript handler installed.
    */
   private PSDocVersionConverter2(IPSExtensionManager manager)
   {
      this(manager, "2.0");
   }

   /**
    * Constructs a doc version converter for the specified version.
    *
    * @param manager The extension manager instance, which must have
    * been properly initialized with the JavaScript handler installed.
    * It can be <CODE>null</CODE>.
    *
    * @param newVersion The new version. Must be "2.0".
    */
   private PSDocVersionConverter2(IPSExtensionManager manager,
      String newVersion
      )
   {
      m_extMgr = manager;

      if (!newVersion.equals("2.0"))
      {
         throw new IllegalArgumentException("input version " +
           newVersion + " not supported");
      }

      m_newVersion = newVersion;
      m_extNames = new HashMap();
   }

   /**
    * Converts app with version 1.1 to app with version 2.0. If the
    * app file does not have version 1.1, then no conversion will
    * be done.
    *
    * @param app The application XML file. May be <CODE>null</CODE>,
    * in which case nothing will happen.
    *
    * @return <CODE>true</CODE> if conversion from 1.1 was done,
    * false otherwise.
    *
    * @throws IOException If an I/O error occurred during conversion.
    * File <CODE>outApp</CODE> will be in an unknown state.

    * @throws PSExtensionException If the extension manager reported
    * an error. File <CODE>outApp</CODE> will be untouched.
    *
    * @throws SAXException If <CODE>app</CODE> is not a valid application
    * XML file. File <CODE>outApp</CODE> will be untouched.
    */
   private boolean convertOneOneToTwoZero(File app, File outApp)
      throws IOException, PSExtensionException, SAXException,
         PSNotFoundException, PSNonUniqueException
   {
      if (app == null)
         return false;

      InputStream in = null;
      OutputStream out = null;

      try
      {
         in = new BufferedInputStream(new FileInputStream(app));
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         in.close();
         in = null;

         boolean converted = convertOneOneToTwoZero(doc);
         if (converted)
         {
            out = new BufferedOutputStream(new FileOutputStream(outApp));
            PSXmlDocumentBuilder.write(doc, out);
            out.close();
            out = null;
         }

         return converted;
      }
      finally
      {
         if (in != null)
         {
            try { in.close(); } catch (IOException e) { /* ignore */ }
         }

         if (out != null)
         {
            try { out.close(); } catch (IOException e) { /* ignore */ }
         }
      }
   }

   /**
    * Converts app document with version 1.1 to app with version 2.0. If the
    * app file does not have version 1.1, then no conversion will
    * be done.
    *
    * @param app The application XML document. Must not be <CODE>null</CODE>.
    *
    * @return The document, which may or may not have been converted
    * (depending on whether conversion was necessary).
    *
    * @throws PSExtensionException If the extension manager reported
    * an error. The document will be in an unknown state.
    */
   private boolean convertOneOneToTwoZero(Document app)
      throws PSExtensionException,
         PSNonUniqueException,
         PSNotFoundException
   {
      if (app == null)
         throw new IllegalArgumentException("App document cannot be null");

      // TODO: determine application version and return if necessary
      Element appEl = app.getDocumentElement();
      if (appEl == null || !appEl.getTagName().equals("PSXApplication"))
      {
         throw new IllegalArgumentException("PSXApplication element required.");
      }

      String version = appEl.getAttribute("version");
      if (version == null || !version.equals("1.1"))
      {
         return false; // no conversion can be (or needs to be) done
      }

      // generate and set the application context in the appExtensionContext element
      m_appContext = generateApplicationContext();
      PSXmlDocumentBuilder.addElement(app, appEl, "appExtensionContext",
         m_appContext);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(appEl);

      // convert all server UDF references
      tree.setCurrent(appEl);
      Element serverRefs = tree.getNextElement("ServerUdfReferences", firstFlag);
      if (serverRefs != null)
      {
         convertServerUDFRefs(serverRefs);
         serverRefs.getParentNode().removeChild(serverRefs);
      }

      // convert all server exit references
      tree.setCurrent(appEl);
      serverRefs = tree.getNextElement("ServerExitReferences", firstFlag);
      if (serverRefs != null)
      {
         convertServerExitRefs(serverRefs);
         serverRefs.getParentNode().removeChild(serverRefs);
      }

      // convert all Application UDFs
      tree.setCurrent(appEl);
      Element appUDFs = tree.getNextElement("ApplicationUdfs", firstFlag);
      if (appUDFs != null)
      {
         convertApplicationUDFs(appUDFs);
         appUDFs.getParentNode().removeChild(appUDFs);
      }

      // now convert all extension calls to use new names
      convertExtensionCalls(appEl);

      tree.setCurrent(appEl);

      Element requestorEl = tree.getNextElement("PSXRequestor", firstFlag);
      if (requestorEl != null)
      {
         String mimeType = requestorEl.getAttribute("outputMimeType");
         if ((mimeType != null) && (mimeType.length() > 0))
         {
            Element outputMimeNode = PSXmlDocumentBuilder.addEmptyElement(app, requestorEl, "outputMimeType");
            PSTextLiteral lit = new PSTextLiteral(mimeType);
            outputMimeNode.appendChild(((IPSComponent) lit).toXml(app));
            requestorEl.setAttribute("directDataStream", "yes");
            requestorEl.setAttribute("outputMimeType", null);
         }
      }

      tree.setCurrent(appEl);

      appEl.setAttribute("version", "2.0");
      return true;
   }

   private void convertServerUDFRefs(Element refsElement)
   {
      // NOT NECESSARY: Server UDFs are copied into the application.
   }

   private void convertServerExitRefs(Element refsElement)
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(refsElement);

      for (Element e = tree.getNextElement("PSXJavaExtensionDef", firstFlag); e != null;)
      {
         // get handler name element
         Element handlerElement = tree.getNextElement(
            "PSXExtensionHandlerDef", true, true);

         if (handlerElement == null)
         {
            throw new IllegalArgumentException("PSXExtensionHandlerDef is required");
         }

         Element handlerNameElement = tree.getNextElement("name", true, true);
         if (handlerNameElement == null)
         {
            throw new IllegalArgumentException("Extension handler name is required");
         }

         String handlerName = PSXmlTreeWalker.getElementData(handlerNameElement);
         if (handlerName == null || handlerName.trim().length() == 0)
         {
            throw new IllegalArgumentException("Extension handler name is required");
         }

         // get extension name
         Element nameElement = tree.getNextElement("name", false, true);
         if (nameElement == null)
         {
            throw new IllegalArgumentException("extension name is required.");
         }

         tree.setCurrent(e);
         String name = PSXmlTreeWalker.getElementData(nameElement);
         if (name == null || name.trim().length() == 0)
         {
            throw new IllegalArgumentException("extension name is required.");
         }

         PSExtensionRef ref = new PSExtensionRef(
            handlerName,
            getServerExitContext( name ),
            name);

         m_extNames.put(name, ref);
         Element oldE = e;
         e = tree.getNextElement("PSXJavaExtensionDef", nextFlag);
         oldE.getParentNode().removeChild(oldE);
      }
   }

   private String getServerExitContext( String extName )
   {
      HashMap exits = new HashMap();
      exits.put( "setCookie", null );
      exits.put( "setEmptyXmlStyleSheet", null );
      exits.put( "modifyXmlHierarchy", null );

      if ( exits.containsKey( extName ))
         return "global/percussion/exit/";
      else
         return "global/";
   }

   private void convertApplicationUDFs(Element appUDFs)
      throws PSExtensionException,
         PSNonUniqueException,
         PSNotFoundException
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(appUDFs);
      Node cur = tree.getCurrent();
      for (Element UDF = tree.getNextElement("PSXScriptExtensionDef", firstFlag); UDF != null;)
      {
         IPSExtensionDef def = defineApplicationUDFExtension(UDF);
         String strKey = def.getInitParameter("com.percussion.user.oldName");
         if (!m_extNames.containsKey(strKey))
         {
            if (m_extMgr != null)
            {
               log.info("Installing JS UDF {}", def.getRef().toString());
               m_extMgr.installExtension(def, PSIteratorUtils.emptyIterator());
            }
            m_extNames.put(strKey, def.getRef());
         }

         Element oldUDF = UDF;
         UDF = tree.getNextElement("PSXScriptExtensionDef", nextFlag);
         oldUDF.getParentNode().removeChild(oldUDF);
      }

      tree.setCurrent( cur );
      for (Element UDF = tree.getNextElement("PSXJavaExtensionDef", firstFlag); UDF != null;)
      {
         IPSExtensionDef def = defineApplicationJavaUDFExtension(UDF);
         /* We don't install the extension because it is already installed.
            Just add the def to the list of found extensions so we can use it
            when converting calls. */
         if ( isGlobalJavaUdf( def ))
            m_extNames.put(def.getInitParameter("com.percussion.user.oldName"),
               def.getRef());

         Element oldUDF = UDF;
         UDF = tree.getNextElement("PSXJavaExtensionDef", nextFlag);
         oldUDF.getParentNode().removeChild(oldUDF);
      }

   }

   /**
    * Checks if the supplied def is one of the Java UDFs shipped w/ Rx.
    *
    * @param def Must be a def for a Java UDF.
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   private boolean isGlobalJavaUdf( IPSExtensionDef def )
   {
      HashMap udfs = new HashMap();
      udfs.put( "PSSimpleJavaUdf_add", null );
      udfs.put( "PSSimpleJavaUdf_subtract", null );
      udfs.put( "PSSimpleJavaUdf_multiply", null );
      udfs.put( "PSSimpleJavaUdf_divide", null );
      udfs.put( "PSSimpleJavaUdf_concat", null );
      udfs.put( "PSSimpleJavaUdf_dateAdjust", null );
      udfs.put( "PSSimpleJavaUdf_dateFormat", null );
      udfs.put( "PSSimpleJavaUdf_literal", null );
      udfs.put( "PSSimpleJavaUdf_replace", null );
      udfs.put( "PSSimpleJavaUdf_toLowerCase", null );
      udfs.put( "PSSimpleJavaUdf_toProperCase", null );
      udfs.put( "PSSimpleJavaUdf_toUpperCase", null );

      String classname = def.getInitParameter( "className" );
      int pos = classname.lastIndexOf( '.' );
      if ( pos > 0 )
         classname = classname.substring( pos + 1 );
      return udfs.containsKey( classname );
   }


   private void convertExtensionCalls(Element root)
   {
      NodeList calls = root.getElementsByTagName("PSXExtensionCall");
      for (int i = 0; i < calls.getLength(); i++)
      {
         Element callElement = (Element)calls.item(i);
         PSExtensionCall call = buildExtensionCall(callElement);
         Element newElement = call.toXml(callElement.getOwnerDocument());
         callElement.getParentNode().insertBefore(newElement, callElement);
         callElement.getParentNode().removeChild(callElement);
         // at this point, calls.getLength() is the same as it ever was
      }
   }

   /**
    * Converts the given PSXScriptExtensionDef element into an
    * extension def. The element is <STRONG>not</STRONG> modified or removed.
    */
   private IPSExtensionDef defineApplicationUDFExtension(Element scriptDefEl)
      throws PSExtensionException
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(scriptDefEl);

      // get handler name element
      Element handlerElement = tree.getNextElement(
         "PSXExtensionHandlerDef", true, true);

      if (handlerElement == null)
      {
         throw new IllegalArgumentException("PSXExtensionHandlerDef is required");
      }

      Element handlerNameElement = tree.getNextElement("name", true, true);
      if (handlerNameElement == null)
      {
         throw new IllegalArgumentException("Extension handler name is required");
      }

      String handlerName = PSXmlTreeWalker.getElementData(handlerNameElement);
      if (handlerName == null || handlerName.trim().length() == 0)
      {
         throw new IllegalArgumentException("Extension handler name is required");
      }

      // get extension name
      Element nameElement = tree.getNextElement("name", false, true);
      if (nameElement == null)
      {
         throw new IllegalArgumentException("extension name is required.");
      }

      tree.setCurrent(scriptDefEl);
      String name = PSXmlTreeWalker.getElementData(nameElement);
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("extension name is required.");
      }

      // get extension type(s)
      tree.setCurrent(scriptDefEl);
      Element typeElement = tree.getNextElement(
         "type", true, true);

      if (typeElement == null)
      {
         throw new IllegalArgumentException("extension type is required");
      }

      String typeStr = PSXmlTreeWalker.getElementData(typeElement);
      int type = Integer.parseInt(typeStr);
      Collection interfaces = new ArrayList();
      if (0 != (type & EXT_TYPE_UDF_PROC))
      {
         interfaces.add("com.percussion.extension.IPSUDFProcessor");
      }

      if (0 != (type & EXT_TYPE_REQUEST_PRE_PROC))
      {
         interfaces.add("com.percussion.extension.IPSRequestPreProcessor");
      }

      if (0 != (type & EXT_TYPE_RESULT_DOC_PROC))
      {
         interfaces.add("com.percussion.extension.IPSResultDocumentProcessor");
      }

      // get extension body
      tree.setCurrent(scriptDefEl);
      Element bodyElement = tree.getNextElement("body", true, true);
      if (bodyElement == null)
      {
         throw new IllegalArgumentException("extension body is required.");
      }

      String body = PSXmlTreeWalker.getElementData(bodyElement);
      if (body == null || body.trim().length() == 0)
      {
         throw new IllegalArgumentException("extension body is required.");
      }

      // get extension description
      tree.setCurrent(scriptDefEl);
      Element descriptionElement = tree.getNextElement("description", true, true);
      String description = null; // optional
      if (descriptionElement != null)
      {
         description = PSXmlTreeWalker.getElementData(descriptionElement);
      }

      PSExtensionRef ref = new PSExtensionRef(
         handlerName,
         m_appContext,
         name);

      // build init params
      Properties initParams = new Properties();
      initParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");
      initParams.setProperty(IPSExtensionDef.INIT_PARAM_FACTORY,
         "com.percussion.extension.PSExtensionDefFactory");
      initParams.setProperty("com.percussion.user.oldName",
         name);

      if (description != null)
      {
         initParams.setProperty(IPSExtensionDef.INIT_PARAM_DESCRIPTION,
            description);
      }

      initParams.setProperty("scriptBody", body);

      // build runtime parameters
      tree.setCurrent(scriptDefEl);
      Collection runtimeParams = new ArrayList();
      for (Element paramDefEl = tree.getNextElement("PSXExtensionParamDef", firstFlag);
         paramDefEl != null;)
      {
         String paramName = tree.getElementData("name", false);
         String dataType = tree.getElementData("dataType", false);
         String desc = tree.getElementData("description", false);
         PSExtensionParamDef paramDef = new PSExtensionParamDef(paramName,
            dataType);

         paramDef.setDescription(desc);
         runtimeParams.add(paramDef);

         paramDefEl = tree.getNextElement("PSXExtensionParamDef", nextFlag);
      }

      PSExtensionDef def = new PSExtensionDef(
         ref, // name
         interfaces.iterator(), // interfaces
         null, // resource URLs
         initParams,
         runtimeParams.iterator() // runtime params
         );

      return def;
   }


   /**
    * Converts the given PSXJavaExtensionDef element into an
    * extension def. The element is <STRONG>not</STRONG> modified or removed.
    */
   private IPSExtensionDef defineApplicationJavaUDFExtension(Element scriptDefEl)
      throws PSExtensionException
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(scriptDefEl);

      // get handler name element
      Element handlerElement = tree.getNextElement(
         "PSXExtensionHandlerDef", true, true);

      if (handlerElement == null)
      {
         throw new IllegalArgumentException("PSXExtensionHandlerDef is required");
      }

      Element handlerNameElement = tree.getNextElement("name", true, true);
      if (handlerNameElement == null)
      {
         throw new IllegalArgumentException("Extension handler name is required");
      }

      String handlerName = PSXmlTreeWalker.getElementData(handlerNameElement);
      if (handlerName == null || handlerName.trim().length() == 0)
      {
         throw new IllegalArgumentException("Extension handler name is required");
      }

      // get extension name
      Element nameElement = tree.getNextElement("name", false, true);
      if (nameElement == null)
      {
         throw new IllegalArgumentException("extension name is required.");
      }

      tree.setCurrent(scriptDefEl);
      String name = PSXmlTreeWalker.getElementData(nameElement);
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("extension name is required.");
      }

      // get extension type(s)
      tree.setCurrent(scriptDefEl);
      Element typeElement = tree.getNextElement(
         "type", true, true);

      if (typeElement == null)
      {
         throw new IllegalArgumentException("extension type is required");
      }

      String typeStr = PSXmlTreeWalker.getElementData(typeElement);
      int type = Integer.parseInt(typeStr);
      Collection interfaces = new ArrayList();
      if (0 != (type & EXT_TYPE_UDF_PROC))
      {
         interfaces.add("com.percussion.extension.IPSUDFProcessor");
      }

      if (0 != (type & EXT_TYPE_REQUEST_PRE_PROC))
      {
         interfaces.add("com.percussion.extension.IPSRequestPreProcessor");
      }

      if (0 != (type & EXT_TYPE_RESULT_DOC_PROC))
      {
         interfaces.add("com.percussion.extension.IPSResultDocumentProcessor");
      }

      // get extension class
      tree.setCurrent(scriptDefEl);
      Element bodyElement = tree.getNextElement("className", true, true);
      if (bodyElement == null)
      {
         throw new IllegalArgumentException("class name is required.");
      }

      String classname = PSXmlTreeWalker.getElementData(bodyElement);
      if (classname == null || classname.trim().length() == 0)
      {
         throw new IllegalArgumentException("class name is required.");
      }

      // get extension description
      tree.setCurrent(scriptDefEl);
      Element descriptionElement = tree.getNextElement("description", true, true);
      String description = null; // optional
      if (descriptionElement != null)
      {
         description = PSXmlTreeWalker.getElementData(descriptionElement);
      }

      PSExtensionRef ref = new PSExtensionRef(
         handlerName,
         "global/percussion/udf/",
         name);

      // build init params
      Properties initParams = new Properties();
      initParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");
      initParams.setProperty(IPSExtensionDef.INIT_PARAM_FACTORY,
         "com.percussion.extension.PSExtensionDefFactory");
      initParams.setProperty("com.percussion.user.oldName",
         name);

      if (description != null)
      {
         initParams.setProperty(IPSExtensionDef.INIT_PARAM_DESCRIPTION,
            description);
      }

      initParams.setProperty("className", classname);

      // build runtime parameters
      tree.setCurrent(scriptDefEl);
      Collection runtimeParams = new ArrayList();
      for (Element paramDefEl = tree.getNextElement("PSXExtensionParamDef", firstFlag);
         paramDefEl != null;)
      {
         String paramName = tree.getElementData("name", false);
         String dataType = tree.getElementData("dataType", false);
         String desc = tree.getElementData("description", false);
         PSExtensionParamDef paramDef = new PSExtensionParamDef(paramName,
            dataType);

         paramDef.setDescription(desc);
         runtimeParams.add(paramDef);

         paramDefEl = tree.getNextElement("PSXExtensionParamDef", nextFlag);
      }

      PSExtensionDef def = new PSExtensionDef(
         ref, // name
         interfaces.iterator(), // interfaces
         null, // resource URLs
         initParams,
         runtimeParams.iterator() // runtime params
         );

      return def;
   }


   /**
    * This method is called to populate a new PSExtensionCall Java object
    * from an old-style PSXExtensionCall XML element node.
    *
    */
   private PSExtensionCall buildExtensionCall(Element sourceNode)
   {
      // this method is a duplicate of PSExtensionCall.fromXml() in
      // revision 1.4 of that file (before new extension model).

      // TODO: do this with straight XML

      if (sourceNode == null)
         throw new IllegalArgumentException("PSXExtensionCall: XML element is null");

      if (false == "PSXExtensionCall".equals (sourceNode.getNodeName()))
      {
         throw new IllegalArgumentException("PSXExtensionCall: wrong type XML element");
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      // find the new exit name for this exit (use the map we built)
      String exit_name = tree.getElementData("name", false);
      PSExtensionRef ref = null;
      if (exit_name != null)
         ref = (PSExtensionRef)m_extNames.get(exit_name);

      if (ref == null)
      {
         ref = new PSExtensionRef("UnsupportedConversion",
            "global/",
            exit_name);
      }

      Collection params = new ArrayList();

      String curNodeType = "PSXExtensionParamValue";
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      for (   Element curNode = tree.getNextElement(curNodeType, firstFlags);
            curNode != null;
            curNode = tree.getNextElement(curNodeType, nextFlags))
      {
         try
         {
            params.add(new PSExtensionParamValue(
               (Element)tree.getCurrent(), null, null));
         }
         catch (PSUnknownNodeTypeException e)
         {
            throw new IllegalArgumentException("PSXExtensionCall: unknown node type exception");
         }
      }

      PSExtensionParamValue values[] = new PSExtensionParamValue[params.size()];
      params.toArray(values);
      return new PSExtensionCall(ref, values);
   }

   private String generateApplicationContext()
   {
      return "application/ext" + new Date().getTime();
   }

   /** The extension manager. Can be <CODE>null</CODE>. */
   private IPSExtensionManager m_extMgr;

   /** The new doc version. */
   private String m_newVersion;

   /** Map from old extension names (String) to new extension names (PSExtensionRef) */
   private Map m_extNames;

   /** The unique context for application extensions. */
   private String m_appContext;

   /**
    * This extension is a user defined function (UDF) processor.
    */
   private static final int EXT_TYPE_UDF_PROC            = 0x01;

   /**
    * This extension is a request pre-processor.
    */
   private static final int EXT_TYPE_REQUEST_PRE_PROC      = 0x02;

   /**
    * This extension is a result document processor.
    */
   private static final int EXT_TYPE_RESULT_DOC_PROC      = 0x04;


   private static final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
      | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

   private static final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
      | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
}
