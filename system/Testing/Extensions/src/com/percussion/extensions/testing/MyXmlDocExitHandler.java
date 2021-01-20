/******************************************************************************
 *
 * [ MyXmlDocExitHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This exit is used to create an XML doc with a different structure.
 */
public class MyXmlDocExitHandler implements IPSResultDocumentProcessor
{
   /**
    * Default constructor, as required for use by IPSExitHandler.
    */
   public MyXmlDocExitHandler()
   {
      super();
   }

   public void init(IPSExtensionDef def, java.io.File f)
   {
      // Nothing here!
   }
   /** Get this unique pre-defined UDF */
//   public IPSExtensionDef getExtensionDef()
//   {
//      return ms_extensionDef;
//   }

   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * This attempts to write to disk, which should fail sanbox security.
    */
   public Document processResultDocument(Object[] params,IPSRequestContext request,Document doc)
   {
      doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "MyDoc");
      PSXmlDocumentBuilder.addElement(doc, root, "myValue", "1");
      return doc;
   }

   /** The fully qualified name (including package) of this class. */
   private static final String ms_className = "MyXmlDocExitHandler";

   /** The name of this function. */
   private static final String ms_name = "myXmlDoc";
   
   /** The description of this function. */
   private static final String ms_description = "my xml document";

   /** The version number of this function. */
   private static final String ms_version = "1.0";

   /** The extension definition for this function. */
//   private static IPSExtensionDef ms_extensionDef = null;

//   static{
//      try{
//         ms_extensionDef = new PSJavaExtensionDef(ms_name, ms_className);
//         ms_extensionDef.setDescription(ms_description);
//         ms_extensionDef.setVersion(ms_version);
//         ms_extensionDef.setType(IPSExtensionDef.EXT_TYPE_RESULT_DOC_PROC);
//
//         PSExtensionParamDef[] params = null;  // no parameter defined
//
//         ms_extensionDef.setParamDefs(params);
//      } catch (PSIllegalArgumentException e){
//      }
//   }
}
