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

package com.percussion.server.compare;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.process.PSProcessAction;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSPersistentProperty;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSOsTool;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class implements a loadable request handler interface
 * <code>IPSLoadableRequestHandler</code> for comparing the two contentitems or
 * two different revisions of a contentitem. When the request handler is called
 * with proper contentid, revision and variantids for the tow items, it returns
 * the compared document using the comparision engine DocuComp.
 * The values for DocuComp options can be supplied through the compare.xml file.
 * The process method first gets the assembly pages as strings for
 * the documents that need to be compared and then runs docucomp engines docuRun
 * to compare these strings.
 *
 */
public class PSCompareRequestHandler implements IPSLoadableRequestHandler
{
   // see the IPSLoadableRequestHandler interface for method javadocs
   @SuppressWarnings("rawtypes")
   public void init(Collection requestRoots, InputStream cfgFileIn)
   {
      if (requestRoots == null || requestRoots.size() == 0)
         throw new IllegalArgumentException(
                 "must provide at least one request root");

      // validate that requestRoots contains only Strings
      for (Iterator<String> iter = requestRoots.iterator(); iter.hasNext();)
      {
         if (!(iter.next() instanceof String))
            throw new IllegalArgumentException(
                    "request roots collection may only contain String objects");
      }
      m_requestRoots = requestRoots;
      Document configDoc = null;
      if (cfgFileIn != null)
      {
         try
         {
            configDoc = PSXmlDocumentBuilder.createXmlDocument(cfgFileIn, true);
         }
         catch (IOException ioe)
         {
            PSConsole.printMsg(HANDLER, ioe.getMessage());
         }
         catch (SAXException sxe)
         {
            PSConsole.printMsg(HANDLER, sxe.getMessage());
         }

      }
      if (configDoc == null)
      {
         PSConsole
                 .printMsg(HANDLER, "configDoc is null, using default options");
      }

      PSConsole.printMsg(HANDLER,
              "Compare request handler initialization completed.");
   }

   // see IPSRootedHandler for documentation
   public String getName()
   {
      return HANDLER;
   }

   // see IPSRootedHandler for documentation
   @SuppressWarnings("rawtypes")
   public Iterator getRequestRoots()
   {
      return m_requestRoots.iterator();
   }

   /**
    * Performs the document comparision using the docucomp engine. Creates two
    * <code>PSCompare</code> objects one for each document which needs to be
    * compared. Gets the assembly page for each document and compares them. The
    * result then will be send through response object.
    *
    * @param request the request to process, must not be <code>null</code>.
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      final PSResponse response = request.getResponse();
      String res;
      try
      {
         PSCompare compDoc1 = new PSCompare(request, FIRST_SET);
         PSCompare compDoc2 = new PSCompare(request, SECOND_SET);

         String assemblyPage1 = compDoc1.getAssemblyPage(request);
         String assemblyPage2 = compDoc2.getAssemblyPage(request);
         PSUserSession session = request.getUserSession();
         String lang = "en-US";
         if(session != null ){
            Collection userProps = session.getUserProperties();
            if(userProps != null){
               Iterator<PSPersistentProperty> userPropsItr = userProps.iterator();
               while (userPropsItr.hasNext()){
                  PSPersistentProperty prop = userPropsItr.next();
                  if(PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG.equals(prop.getName())){
                     String lg = prop.getValue();
                     if(lg != null && !lg.isEmpty()){
                        lang = lg;
                     }
                     break;

                  }

               }
            }

         }
         res = getComparisionResult(assemblyPage1, assemblyPage2, lang);
      }
      catch (Exception e)
      {
         PSConsole.printMsg(HANDLER,
         ExceptionUtils.getFullStackTrace(e) );
         res = e.getMessage();
      }


      byte[] bytes = null;
      try
      {
         // get the result bytes for the rhythmyx standard encoding
         bytes = res.getBytes(PSCharSets.rxStdEnc());
      }
      catch (UnsupportedEncodingException e)
      {
         // use the platforms default encoding if the rhythmyx standard fails
         bytes = res.getBytes();
      }

      response.setContent(new ByteArrayInputStream(bytes), bytes.length,
         "text/html");
   }

   /**
    * These strings are the html pages that are needed to be compared.
    * The comparision results will be returned as a string.
    *
    * @param newStr The new string for the comparision, assumed not <code>null
    * </code>.
    *
    * @param oldStr The old String for the comparision, assumed not <code>null
    * </code>.
    * @param lang user's login locale
    *
    * @return Comparision results as <code>String</code>. May return <code>
    *    empty</code> string but not <code>null</code>.
    *
    * @throws PSCompareException if error occurs in docuRun at the time of
    *    Initialization or at the time of comparision
    */
   protected static String getComparisionResult(String newStr, String oldStr,
      String lang) {
      String java, classpath, libpath, configUri;

      if (PSOsTool.isWindowsPlatform()) {
         java = PSServer.getRxFile("JRE/bin/java.exe");
      }
      else {
         java = PSServer.getRxFile("JRE/bin/java");
      }

      libpath = PSServer.getRxFile("bin");
      String jarDirPath =  "bin"; //"AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib";
      classpath = PSServer.getRxFile(jarDirPath + "/docucomp.jar") +
              File.pathSeparator +
              PSServer.getRxFile(jarDirPath + "/rxdocucomp.jar");

      configUri = new File(PSServer.getRxFile("rxconfig/Server/requestHandlers/compare.xml")).toURI().toString();

      PSPurgableTempFile oldFile;
      PSPurgableTempFile newFile;
      PSPurgableTempFile outputFile;

      try
      {
         oldFile = new PSPurgableTempFile("cmp", null, null);
         newFile = new PSPurgableTempFile("cmp", null, null);
         outputFile = new PSPurgableTempFile("cmp", null, null);

         FileUtils.writeStringToFile(oldFile, oldStr, "UTF-8");
         FileUtils.writeStringToFile(newFile, newStr, "UTF-8");
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to do compare", e);
      }

      String[] cmds = new String[] {
              java,
              "-Xmx512m",
              "-Djava.library.path=" + libpath,
              "-cp",
              classpath,
              "com.percussion.docucomp.PSMain",
              oldFile.toURI().toString(),
              newFile.toURI().toString(),
              outputFile.toURI().toString(),
              lang == null ? "en" : lang,
              configUri
      };
      try
      {
         PSProcessAction action = new PSProcessAction(cmds, null, null);
         if (action.waitFor() != 0) {
            throw new RuntimeException(action.getStdErrorText() + "\n" + action.getStdOutText());
         }
         return FileUtils.readFileToString(outputFile, "UTF-8");
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to run compare tool", e);
      }
   }



   // see interface for method javadoc
   public void shutdown()
   {
      // nothing to do
   }

   /**
    * Name of the subsystem used to dump messages to server console.
    */
   public static final String HANDLER = "Compare";
   
   /**
    * Storage for the request roots, initialized in the <code>init</code>
    * method, never <code>null</code> or empty after. Contains
    * <code>String</code> objects.
    */
   private Collection<String> m_requestRoots = null;


   /**
    * request will have parameters like sys_contentid1, sys_revision1 for
    * first document that need to be considered for the comparison and
    * sys_contentid2, sys_revision2 for second document. The following constant
    * is used to add 1 to the regualr IPSHtmlParameters constants like
    * SYS_CONTENTID(sys_contentid).
    */
   public static final String FIRST_SET = "1";

   /**
    * The following constant is used to add 2 to the regualr IPSHtmlParameters
    * constants like SYS_CONTENTID(sys_contentid).
    */
   public static final String SECOND_SET = "2";

}
