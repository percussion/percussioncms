/******************************************************************************
 *
 * [ PermissionsTestExitHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;

import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

import com.percussion.extension.PSExtensionProcessingException;

import org.w3c.dom.Document;

import com.percussion.data.PSConversionException;

import com.percussion.extension.IPSExtensionDef;

import java.util.HashMap;
import java.util.Iterator;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import java.net.Authenticator;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.security.Provider;
import java.security.Security;

/**
 *
 *    This class will test the various permissions with the
 *    exit handler for sandbox security violations.
 *    The class will be set up to try one permission for
 *    udfExit handling and post-document processing
 *
 *    For pre-processing, the following html parameters (case-sensitive!)
 *    will be accepted to choose the kinds of permissions that are attempted
 *    to test
 *
 *    AWT *
 *    Net *
 *    Prop
 *    Reflect *
 *    Runtime
 *    Security
 *    Serializable *
 *    File
 *    Socket
 *
 *    * - not currently supported by this test (no-ops)
 *
 *    When any of these parameters is "on" (case-insensitive)
 *    an action which tests the associated permission will be attempted.
 *
 *    If any of these parameters is omitted or set to "off" (case-insensitive)
 *    the associated action will not be performed.
 *
 *   The default value for a parameter is "off", so any other value
 *    will be considered off.
 *
 *    WARNING!  If any one of these parameters is specified more than
 *       once with different values, the behavior is undefined.
 *
 *    Example:
 *
 *    http://ServerRoot/RhythmyxRoot/TestRoot/query.html?AWT=on
 *
 **/

public class PermissionsTestExitHandler implements
   IPSRequestPreProcessor,
   IPSUdfProcessor,
   IPSResultDocumentProcessor
{
   /*  IPSRequestPreProcessor interface */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws
         com.percussion.security.PSAuthorizationException,
         com.percussion.server.PSRequestValidationException,
         PSExtensionProcessingException
   {
      /* Run Through the HTML params looking for our stuff */
      HashMap reqParams = request.getParameters();
      Iterator i = reqParams.keySet().iterator();
      int processFlags = 0;

      while (i.hasNext())
      {
         Object key = i.next();
         if (ms_HtmlParamValues.containsKey(key))
         {
            /* Turn the flag on if it
               is defined as on */
            Object setting = reqParams.get(key); 
            if ((setting != null) && (setting.toString().equalsIgnoreCase("ON")))
            {
               // Turn the flag on
               processFlags |= ((Integer) ms_HtmlParamValues.get(key)).intValue();
            }
         }
      }
      runPermissionTests(processFlags);
   }

   public void init(IPSExtensionDef def, java.io.File f)
   {
      // Nada
   }

   /* IPSResultDocumentProcessor interface */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws
         PSExtensionProcessingException
   {
      /* Run Through the HTML params looking for our stuff */
      HashMap reqParams = request.getParameters();
      Iterator i = reqParams.keySet().iterator();
      int processFlags = 0;

      while (i.hasNext())
      {
         Object key = i.next();
         if (ms_HtmlParamValues.containsKey(key))
         {
            /* Turn the flag on if it
               is defined as on */
            Object setting = reqParams.get(key); 
            if ((setting != null) && (setting.toString().equalsIgnoreCase("ON")))
            {
               // Turn the flag on
               processFlags |= ((Integer) ms_HtmlParamValues.get(key)).intValue();
            }
         }
      }
      runPermissionTests(processFlags);

      return resultDoc;
   }

   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /* IPSUdfProcessor interface */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException 
   {
      /* Run Through the HTML params looking for our stuff */
      HashMap reqParams = request.getParameters();
      Iterator i = reqParams.keySet().iterator();
      int processFlags = 0;

      while (i.hasNext())
      {
         Object key = i.next();
         if (ms_HtmlParamValues.containsKey(key))
         {
            /* Turn the flag on if it
               is defined as on */
            Object setting = reqParams.get(key); 
            if ((setting != null) && (setting.toString().equalsIgnoreCase("ON")))
            {
               // Turn the flag on
               processFlags |= ((Integer) ms_HtmlParamValues.get(key)).intValue();
            }
         }
      }

      try {
         runPermissionTests(processFlags);
      } catch (PSExtensionProcessingException e) {
         // Shouldn't happen!
         throw new PSConversionException(e.getErrorCode(), e.getErrorArguments());
      }
      return new Integer(47);
   }


   /* IPSExtension interface */
//   public IPSExtensionDef getExtensionDef()
//   {
//      return ms_ExtensionDef;
//   }

   private void runPermissionTests(int permissionFlags)
      throws PSExtensionProcessingException
   {
      /* Check the flags and attempt to run something which hits
         the specified permission */
      if ((permissionFlags & CHECK_AWT) == CHECK_AWT)
      {
         // Something GUI...
      }
      if ((permissionFlags & CHECK_NET) == CHECK_NET)
      {
         java.net.Authenticator.setDefault(null);
      }
      if ((permissionFlags & CHECK_PROP) == CHECK_PROP)
      {
         String OS = System.getProperty("os.name");
      }
      if ((permissionFlags & CHECK_REFLECT) == CHECK_REFLECT)
      {
         // Not sure
      }
      if ((permissionFlags & CHECK_RUNTIME) == CHECK_RUNTIME)
      {
         SecurityManager sm = System.getSecurityManager();
         System.setSecurityManager(sm);
      }
      if ((permissionFlags & CHECK_SECURITY) == CHECK_SECURITY)
      {
         String prop = Security.getProperty("Rhythmyx Test");
      }
      if ((permissionFlags & CHECK_SERIALIZE) == CHECK_SERIALIZE)
      {
         // Something about subclassing ObjectOutput/InputStream
      }
      if ((permissionFlags & CHECK_FILE) == CHECK_FILE)
      {
         try {
            FileInputStream fi = new FileInputStream("server.properties");
            FileOutputStream fo = new FileOutputStream("fred.test");
            fo.write("Fred rules!".getBytes());
            fo.close();
         } catch (IOException e) {
            // Ignore, we got thru, at least, to get this error
         }
      }
      if ((permissionFlags & CHECK_SOCKET) == CHECK_SOCKET)
      {
         try {
            InetAddress addr = InetAddress.getByName("Hermes");
            Socket sock = new Socket(addr, 80);
            sock.close();
         } catch (UnknownHostException e)
         {
            throw new PSExtensionProcessingException(ms_name, e);
         } catch (IOException ioE)
         {
            // ignore
         }
      }
   }

   private static int CHECK_AWT        = 0x00000001;
   private static int CHECK_NET        = 0x00000002;
   private static int CHECK_PROP       = 0x00000004;
   private static int CHECK_REFLECT    = 0x00000008;
   private static int CHECK_RUNTIME    = 0x00000010;
   private static int CHECK_SECURITY   = 0x00000020;
   private static int CHECK_SERIALIZE  = 0x00000040;
   private static int CHECK_FILE       = 0x00000080;
   private static int CHECK_SOCKET     = 0x00000100;

   private static String ms_name = "PermissionsTester";
   private static String ms_className = "PermissionsTestExitHandler";
   private static String ms_version = "1.0";
   private static String ms_description = "Sandbox security test class";
   private static IPSExtensionDef ms_ExtensionDef;

   private static HashMap ms_HtmlParamValues = new HashMap();

   static {
      ms_HtmlParamValues.put("AWT", new Integer(CHECK_AWT));
      ms_HtmlParamValues.put("Net", new Integer(CHECK_NET));
      ms_HtmlParamValues.put("Prop", new Integer(CHECK_PROP));      
      ms_HtmlParamValues.put("Reflect", new Integer(CHECK_REFLECT));
      ms_HtmlParamValues.put("Runtime", new Integer(CHECK_RUNTIME));      
      ms_HtmlParamValues.put("Security", new Integer(CHECK_SECURITY));
      ms_HtmlParamValues.put("Serializable", new Integer(CHECK_SERIALIZE));
      ms_HtmlParamValues.put("File", new Integer(CHECK_FILE));
      ms_HtmlParamValues.put("Socket", new Integer(CHECK_SOCKET));

//      try {
//         ms_ExtensionDef = 
//            new PSJavaExtensionDef(ms_name, ms_className);
//         ms_ExtensionDef.setParamDefs(null);
//         ms_ExtensionDef.setType(
//               IPSExtensionDef.EXT_TYPE_REQUEST_PRE_PROC|IPSExtensionDef.EXT_TYPE_RESULT_DOC_PROC|IPSExtensionDef.EXT_TYPE_UDF_PROC);
//         ms_ExtensionDef.setVersion(ms_version);
//         ms_ExtensionDef.setDescription(ms_description);
//      }  catch (com.percussion.error.PSIllegalArgumentException e)
//      {
//         System.out.println("This should not be happening! "+e.toString());
//      }
   }
}


