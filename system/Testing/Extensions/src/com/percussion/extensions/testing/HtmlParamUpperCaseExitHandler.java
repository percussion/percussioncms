/******************************************************************************
 *
 * [ HtmlParamUpperCaseExitHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.server.IPSRequestContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This sample exit is used to upper case all HTML parameter values.
 */
public class HtmlParamUpperCaseExitHandler implements IPSRequestPreProcessor
{
   /**
    * Default constructor, as required for use by IPSExitHandler.
    */
   public HtmlParamUpperCaseExitHandler()
   {
      super();
   }

   public void init(IPSExtensionDef def, java.io.File f)
   {
      // nothing to do here
   }

   /**
    * This is the work horse. Here we'll take the input
    * request object to locate the HashMap of HTML parameters.
    * We can then iterate through the String based params and
    * upper case them.
    * 
    * @param request the current request context
    * @param params the input parameters for our call (should be none)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
   {
      HashMap htmlParams = null;
      if (request != null)
         htmlParams = request.getParameters();
      if (htmlParams == null)
         return;

      Iterator ite = htmlParams.entrySet().iterator();
      while (ite.hasNext())
      {
         java.util.Map.Entry e = (java.util.Map.Entry)ite.next();
         Object o = e.getValue();
         if (o instanceof String)
         {
            e.setValue(((String)o).toUpperCase());
         }
         else if (o instanceof List)
         {
            List paramList = (List)o;
            int size = paramList.size();
            for (int i = 0; i < size; i++)
            {
               o = paramList.get(i);
               if (o instanceof String)
               {
                  paramList.set(i, ((String)o).toUpperCase());
               }
            }
         }
      }
   }

   /** The fully qualified name (including package) of this class. */
   private static final String ms_className = "HtmlParamUpperCaseExitHandler";

   /** The name of this function. */
   private static final String ms_name = "upperCaseHtmlValues";
   
   /** The description of this function. */
   private static final String ms_description = "upper cast html parameters";

   /** The version number of this function. */
   private static final String ms_version = "1.0";

   /** The extension definition for this function. */
//   private static IPSExtensionDef ms_extensionDef = null;

//   static{
//      try{
//         ms_extensionDef = new PSJavaExtensionDef(ms_name, ms_className);
//         ms_extensionDef.setDescription(ms_description);
//         ms_extensionDef.setVersion(ms_version);
//         ms_extensionDef.setType(IPSExtensionDef.EXT_TYPE_REQUEST_PRE_PROC);
//
//         PSExtensionParamDef[] params = null;  // no parameter defined
//
//         ms_extensionDef.setParamDefs(params);
//      } catch (PSIllegalArgumentException e){
//      }
//   }
}
