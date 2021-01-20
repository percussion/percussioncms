/******************************************************************************
 *
 * [ TempFileWriterExtension.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.server.IPSRequestContext;
import java.io.File;
import java.io.FileOutputStream;
import org.w3c.dom.Document;

/**
 * This exit is used to test our sandbox can't write to files on disk.
 */
public class TempFileWriterExtension implements IPSUdfProcessor,
   IPSRequestPreProcessor, IPSResultDocumentProcessor
{
   /************ IPSRequestPreProcessor implementation ************/
   /**
    * This attempts to write to disk, which should fail sandbox security.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
   {
      writeToDisk();
   }

   /************ IPSResultDomumentProcessor implementation ************/
   /** Just return false. */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * This attempts to write to disk, which should fail sandbox security.
    */
   public Document processResultDocument(Object[] params, IPSRequestContext request,
                                          Document doc)
   {
      writeToDisk();
      return doc;
   }

   /************ IPSExtension and IPSUdfProcessor implementation ************/
   /**
    * No-op.
    */
   public void init( IPSExtensionDef def, File codeRoot )
   {}

   /**
    * This attempts to write to disk, which should fail sandbox security.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
   {
      writeToDisk();
      return "";
   }

   private void writeToDisk()
   {
      FileOutputStream out = null;
      try{
         File f = File.createTempFile("psx", ".txt");
         out = new FileOutputStream(f);
         out.write("I have defeated the sandbox!".getBytes());
      } catch (java.io.IOException e){
         throw new RuntimeException(e.toString());
      } finally{
         if (out != null){
            try { out.close(); }
            catch(Exception e){ /* ignore at this point */ }
         }
      }
   }
}
