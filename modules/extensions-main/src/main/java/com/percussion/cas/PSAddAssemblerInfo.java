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
package com.percussion.cas;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSRelationshipData;
import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.cms.objectstore.PSSite;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extensions.general.PSAddPluginProperties;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.tmxdom.IPSTmxDtdConstants;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.GlobalTemplateUsage;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.util.PSHtmlParameters;
import com.percussion.util.PSParseUrlQueryString;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.util.PSUrlUtils;
import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This post exit adds information needed in content assembler stylesheets
 * to the result document. A XML document conforming to the DTD
 * sys_AssemblerInfo.dtd is created and inserted into the result document as
 * its first child.
 * In the second part the exit modifies the assembly style sheets to add extra
 * links to edit the related content in preview or WYSIWYG mode.
 *
 * TO DO:
 * the style sheet transformation is now performed every time the assembly page
 * requested for edit-preview. It would be required to cache thisusing the
 * datetime stamps on the original style sheet files and transform only if the
 * destination stylesheets do not exist or source files are modfied.
 */
public class PSAddAssemblerInfo implements IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor#canModifyStyleSheet()
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   // see IPSExtensionDef#init(IPSExtensionDef, File)
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      if (ms_paramCount == NOT_INITIALIZED)
      {
         ms_paramCount = 0;

         Iterator iter = extensionDef.getRuntimeParameterNames();
         while (iter.hasNext())
         {
            iter.next();
            ms_paramCount++;
         }
      }
   }

   /**
    * This will make an internal request to the
    * sys_ceSupport/AssemblerProperties resource to get all assembler
    * properties defined for the siteid and contextid provided. All returned
    * property name/value pairs will be added to the result document so they
    * are available for the rendering stylesheet.
    *
    * @param params no parameters are expected.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      request.printTraceMessage("PSAddAssemblerInfo: entering");

      if (doc == null || doc.getDocumentElement() == null)
      {
         request.printTraceMessage("PSAddAssemblerInfo: result doc is null" +
            " or empty, exiting with nothing added.");
         return doc;
      }

      //Determine if the assembly is recursive. 
      boolean recursion = isAssemblyRecursive(request);

      //Store the origination application name for future use.
      String originalAppName = request.getCurrentApplicationName();
      String requestFileURL = request.getRequestFileURL();

      IPSInternalRequest propertiesReq = null;
      IPSInternalRequest relatedReq = null;
      try
      {
         //Add user locale string to the root element (for i18n purpose)
         String lang = request.getUserContextInformation(
            PSI18nUtils.USER_CONTEXT_VAR_SYS_LANG,
            PSI18nUtils.DEFAULT_LANG).toString();
         doc.getDocumentElement().setAttribute(
            IPSTmxDtdConstants.ATTR_XML_LANG, lang);

         //Make sure the siteid is present in the request
         Object obj = request.getParameter(IPSHtmlParameters.SYS_SITEID);
         String siteid = null;
         if (obj == null || obj.toString().length() < 1)
            siteid = DEFAULT_SITEID;
         else
            siteid = obj.toString().trim();
         request.setParameter(IPSHtmlParameters.SYS_SITEID, siteid);

         //Make sure the original siteid is present in the request which 
         //defaults to siteid
         obj = request.getParameter(IPSHtmlParameters.SYS_ORIGINALSITEID);
         if (obj == null || obj.toString().trim().length() < 1)
            request.setParameter(IPSHtmlParameters.SYS_ORIGINALSITEID, siteid);

         /*
          * Convert to standard HTML parameter names and add default siteid
          * if not there yet.
          */
         Map htmlParams =
            PSHtmlParameters.createStandardParams(request.getParameters());
         htmlParams.put(IPSHtmlParameters.SYS_SESSIONID,
            request.getUserSessionId());
         
         String variantid = request.getParameter(IPSHtmlParameters.SYS_VARIANTID);
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         IPSAssemblyTemplate template = asm.loadUnmodifiableTemplate(variantid);
         
         // get the global template
         String globalTemplate = null;
         List sites = PSSite.getSites(request);
         // Only lookup a global template if the variant is not set to use a
         // velocity global template, i.e. a value other than legacy.[]
         if (!sites.isEmpty()
               && template.getGlobalTemplateUsage().equals(
                     GlobalTemplateUsage.Legacy))
         {
            // there is always maximum one site
            PSSite site = (PSSite) sites.get(0);
            globalTemplate = site.getGlobalTemplateName();
            
            PSLocator itemLocator = new PSLocator(
               htmlParams.get(IPSHtmlParameters.SYS_CONTENTID).toString(), 
               htmlParams.get(IPSHtmlParameters.SYS_REVISION).toString());
            
            String globalTemplateOverride = getGlobalTemplateOverride(
                  request, itemLocator, site);
            if (globalTemplateOverride != null)
               globalTemplate = globalTemplateOverride;
         }
         
         // create the sys_AssemblerInfo XML element
         Element assemblerInfo = doc.createElement(ASSEMBLER_INFO_ELEM);
         assemblerInfo.setAttribute(PREVIEWURL_ATTR, requestFileURL);
         if (globalTemplate != null)
            assemblerInfo.setAttribute("psxglobaltemplate", globalTemplate);
         Iterator attrs = htmlParams.entrySet().iterator();
         while (attrs.hasNext())
         {
            Map.Entry attr = (Map.Entry) attrs.next();
            assemblerInfo.setAttribute((String) attr.getKey(),
               (String) attr.getValue());
         }
         
         /*
          * Insert the assembler info as the first child of the result
          * document.
          */
         Element firstChild =
            (Element) doc.getDocumentElement().getFirstChild();
         if (firstChild != null)
            doc.getDocumentElement().insertBefore(assemblerInfo, firstChild);
         else
            doc.getDocumentElement().appendChild(assemblerInfo);
         /*
          * Add all related item links, nothing will be added if no authtype 
          * was provided.
          */
         String authtype = (String) htmlParams.get(
            IPSHtmlParameters.SYS_AUTHTYPE);
         if (authtype != null)
         {
            if (recursion)
            {
               String msg = request.getRequestFileURL()
                  + request.getParameters() + " is being assembled recursively";
               request.printTraceMessage(msg);
               ms_log.debug(msg);
            }
            else
            {
               // get all related content info and append it to the
               // AssemblerInfo
               relatedReq = request.getInternalRequest(RELATEDCONTENT,
                  htmlParams, false);
               if (relatedReq != null)
               {
                  Document relatedDoc = relatedReq.getResultDoc();

                  Element relatedElem = doc.createElement(RELATED_CONTENT_ELEM);
                  assemblerInfo.appendChild(relatedElem);

                  NodeList children = relatedDoc.getElementsByTagName(INFOURLS);
                  if (children != null && children.getLength() > 0)
                  {
                     Node importNode = doc.importNode(children.item(0), true);
                     relatedElem.appendChild(importNode);
                  }

                  children = relatedDoc.getElementsByTagName(RELATEDLINKURL);
                  int childCount = children.getLength();
                  for (int i = 0; i < childCount; i++)
                  {
                     Element child = (Element) children.item(i);
                     Node text = child.getFirstChild();
                     if (text instanceof Text)
                     {
                        NamedNodeMap attributes = child.getAttributes();
                        String urlValue = ((Text) text).getNodeValue();
                        urlValue = (urlValue == null) ? "" : urlValue;

                        /*
                         * Replace the siteid and folderid parameters in the url
                         * if required. The ones from the relationship
                         * properties must override those in the URL.
                         */
                        String tgtSiteid = child
                           .getAttribute(IPSHtmlParameters.SYS_SITEID);
                        String tgtFolderid = child
                           .getAttribute(IPSHtmlParameters.SYS_FOLDERID);

                        if (tgtSiteid.length() > 0)
                        {
                           urlValue = PSParseUrlQueryString.replaceParam(
                              urlValue, IPSHtmlParameters.SYS_SITEID, 
                              tgtSiteid);
                        }
                        if (tgtFolderid.length() > 0)
                        {
                           urlValue = PSParseUrlQueryString.replaceParam(
                              urlValue, IPSHtmlParameters.SYS_FOLDERID, 
                              tgtFolderid);
                        }     
                        urlValue = appendNewAssemblyLevelParam(request,
                           urlValue);
                        //
                        Element linkurl = doc.createElement(RELATEDLINKURL);
                        relatedElem.appendChild(linkurl);

                        if (attributes != null)
                        {
                           for (int j = 0; j < attributes.getLength(); j++)
                           {
                              Attr attribute = (Attr) attributes.item(j);
                              if (attribute != null)
                                 linkurl.setAttribute(attribute.getName(),
                                    attribute.getValue());
                           }
                        }
                        Element value = doc.createElement("Value");
                        value.setAttribute("current", urlValue);
                        linkurl.appendChild(value);
                     }
                  }

               }
            }

            // creates parameters with only sys_siteid and sys_context, both
            // will be used to construct the cache-key for caching the
            // "sys_casSupport/AssemblerProperties". The cache flag is enabled
            // for
            // the resource.
            Map assemblyParams = new HashMap(2);
            assemblyParams.put(IPSHtmlParameters.SYS_CONTEXT, htmlParams
               .get(IPSHtmlParameters.SYS_CONTEXT));
            assemblyParams.put(IPSHtmlParameters.SYS_SITEID, htmlParams
               .get(IPSHtmlParameters.SYS_SITEID));
            // get all assembler properties and append them to the AssemblerInfo
            propertiesReq = request.getInternalRequest(ASSEMBLERPROPERTIES,
               assemblyParams, false);
            if (propertiesReq != null)
            {
               Document propertyDoc = propertiesReq.getResultDoc();
               if ((propertyDoc != null)
                  && (propertyDoc.getDocumentElement() != null))
               {
                  Node root = propertyDoc.getDocumentElement();
                  Node importNode = doc.importNode(root, true);
                  assemblerInfo.appendChild(importNode);
               }
            }

            // append the InlineLink element
            Map paramMap = new HashMap(2);
            paramMap.put(IPSHtmlParameters.SYS_SESSIONID, request
               .getUserSessionId());
            paramMap.put(IPSHtmlParameters.SYS_CONTEXT, htmlParams
               .get(IPSHtmlParameters.SYS_CONTEXT));

            // make an internal rhythmyx link
            URL url = PSUrlUtils.createUrl("127.0.0.1", new Integer(request
               .getServerListenerPort()), PUBLICATIONURL, paramMap.entrySet()
               .iterator(), null, request);

            Element inlineLink = doc.createElement(INLINE_LINK_ELEM);
            inlineLink.setAttribute(URL_ATTR, url.toString());
            assemblerInfo.appendChild(inlineLink);

            // append the varianturl element
            Map vurlParamMap = new HashMap(3);
            vurlParamMap.put(IPSHtmlParameters.SYS_SESSIONID, request
               .getUserSessionId());
            vurlParamMap.put(IPSHtmlParameters.SYS_CONTEXT, htmlParams
               .get(IPSHtmlParameters.SYS_CONTEXT));
            vurlParamMap.put(IPSHtmlParameters.SYS_AUTHTYPE, htmlParams
               .get(IPSHtmlParameters.SYS_AUTHTYPE));

            // make an internal rhythmyx link
            url = PSUrlUtils.createUrl("127.0.0.1", new Integer(request
               .getServerListenerPort()), VARIANTURL, vurlParamMap.entrySet()
               .iterator(), null, request);

            Element varianturl = doc.createElement(VARIANT_URL_ELEM);
            varianturl.setAttribute(URL_ATTR, url.toString());
            assemblerInfo.appendChild(varianturl);

            // Fix all types of inline links, viz. links, images and snippets
            // for
            // correct version and revision numbers.
            // extract all body field nodes from the full document
            List bodyFields = new ArrayList();
            NodeList nl = doc.getElementsByTagName("*");
            int nodeCount = nl.getLength();
            // Collect all rich text fields if the assembly process is not
            // recursing.
            for (int i = 0; nl != null && !recursion && i < nodeCount; i++)
            {
               if (!(nl.item(i) instanceof Element))
                  continue;
               Element elem = (Element) nl.item(i);
               Node nd = elem.getFirstChild();
               if (nd instanceof Text)
               {
                  String content = ((Text) nd).getData();
                  if (content.indexOf("rxbodyfield") != -1)
                     bodyFields.add((Text) nd);
               }
            }

            // Now processs all content nodes to fix the content
            int count = bodyFields.size();
            if (count > 0)
            {
               PSRelationshipData inlineLinkRelData = PSSingleValueBuilder
                  .buildRelationshipData(request, null);
               for (int i = 0; i < count; i++)
               {
                  try
                  {
                     Text txtContent = (Text) bodyFields.get(i);
                     String content = (txtContent).getData();
                     Document contentDoc = PSXmlDocumentBuilder
                        .createXmlDocument(new StringReader(content), false);
                     // Check whether the rxbodyfield is class attributes
                     // value of the root element.
                     String classattr = contentDoc.getDocumentElement()
                        .getAttribute("class");
                     String rootelemname = contentDoc.getDocumentElement()
                        .getTagName();
                     if (rootelemname.equals("div")
                        && classattr.equals("rxbodyfield"))
                     {
                        PSSingleValueBuilder.processVariant(contentDoc
                           .getDocumentElement(), request, inlineLinkRelData);
                        txtContent.setData(PSXmlDocumentBuilder.toString(
                           contentDoc, PSXmlDocumentBuilder.FLAG_NO_INDENT));
                     }
                  }
                  catch (Exception e)
                  {
                     /*This should never happen as the Content is already an 
                      *xml document.
                      *Just write the trace in case if it happens...
                      */
                     request.printTraceMessage(e.getMessage());
                  }
               }
            }
         }
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(
            e.getErrorCode(), e.getErrorArguments());
      }
      catch (Throwable e)
      {
         PSConsole.printMsg("Extension", e);
      }
      finally
      {
         if (propertiesReq != null)
            propertiesReq.cleanUp();

         if (relatedReq != null)
            relatedReq.cleanUp();
      }

     /*
      * Call the main method that modifies the required stylesheets for editing
      * the related content in WYSIWYG mode.
      */
      modifyAssemblyStyleSheets(request, doc, originalAppName);
      
      // Add plugin info attributes
      request.printTraceMessage(
         "PSAddAssemblerInfo: adding plugin info attributes to result document");
      PSAddPluginProperties.addPluginInfo(request, doc);

      request.printTraceMessage("PSAddAssemblerInfo: leaving");
      return doc;
   }
   
   /**
    * Get the global template override from the folder paths of the supplied 
    * item locator.
    * 
    * @param request the request context, assume not <code>null</code>.
    * @param locator the item locator, assumed not <code>null</code>.
    * @param site the site for which to lookup the global template override,
    *    assumed not <code>null</code>.
    * 
    * @return the global template override or <code>null</code> if no override
    *    was found.
    * 
    * @throws PSCmsException for any error.
    */
   private String getGlobalTemplateOverride(IPSRequestContext request, 
      PSLocator locator, PSSite site) throws PSCmsException
   {
      try
      {
         // get the folder id for the site folder
         PSRelationshipProcessor relProcessor = PSRelationshipProcessor.getInstance();
         String sfroot = site.getFolderRoot();
         if (sfroot == null || sfroot.length() == 0)
            return null;

         PSServerFolderProcessor fprocessor = PSServerFolderProcessor.getInstance();
         int siteFolderId = fprocessor.getIdByPath(sfroot);

         if (siteFolderId == -1)
            return null; // cannot find the folder id for the site.
         
         // get the locator paths for the supplied item
      
         List folderPaths = fprocessor.getFolderLocatorPaths(locator);
         
         boolean foundSiteTree = false;
         List locatorPath = null;
         Iterator walker = folderPaths.iterator();
         while (!foundSiteTree && walker.hasNext())
         {
            locatorPath = (List) walker.next();
            foundSiteTree = isPathForSite(locatorPath, siteFolderId);
         }
         
         if (foundSiteTree)
         {
            String globalTemplate;
            PSLocator flocator;
            /*
             * Walk the path from the bottom up to find the global template 
             * override with the highest precedence.
             */ 
            ListIterator walkPath = locatorPath.listIterator();
            while (walkPath.hasNext())
            {
               flocator = (PSLocator) walkPath.next();
               globalTemplate = fprocessor.getGlobalTemplateProperty(flocator
                     .getId());
               if (globalTemplate != null && globalTemplate.trim().length() > 0)
               {
                  return globalTemplate;
               }
            }
         }
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
      
      return null;
   }
   
   /**
    * Test if the supplied folder tree is for the provided site.
    * 
    * @param locatorPath the locator path. The 1st entry is the locator of the
    *    bottom folder and the last entry is the root locator.
    * @param siteFolderId the content id of the site folder.
    * 
    * @return <code>true</code> if the supplied locator path contains the
    *    provided site id, <code>false</code> otherwise.
    */
   private boolean isPathForSite(List locatorPath, int siteFolderId)
   {
      PSLocator folder;
      for (int i=0; i<locatorPath.size(); i++)
      {
         folder = (PSLocator) locatorPath.get(i);
         if (folder.getId() == siteFolderId)
            return true;
      }
      
      return false;
   }


   /**
    * Add the {@link #ASSEMBLY_LEVEL assembly level} parameter to the supplied
    * variant URL. Does not validate the URL. Just appends the parameter
    * (name=value) at the end of the URL string with an appropriate parameter
    * separator(? or &amp;). Does not check if the parameter already exists in
    * the URL. The new assembly parameter value is the current assembly level
    * value read from the request (see
    * {@link #readCurrentAssemblyLevel(IPSRequestContext)}) incremented by 1.
    * 
    * @param request request context, must not be <code>null</code>.
    * @param assemblyUrlString the unencoded URL string to append the assembly
    * level parameter, may be <code>null</code> or empty.
    * @return the assembly URL value after appending the new assembly level as a
    * special parameter. May be <code>null</code> or empty if supplied URL
    * sting is <code>null</code> or empty.
    */
   public static String appendNewAssemblyLevelParam(IPSRequestContext request,
      String assemblyUrlString)
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must nopt be null");
      }
      if (assemblyUrlString != null && assemblyUrlString.length() > 0)
      {
         char separator = '&';
         if (assemblyUrlString.indexOf('?') == -1)
         {
            separator = '?';
         }
         assemblyUrlString += separator + ASSEMBLY_LEVEL + "="
            + (readCurrentAssemblyLevel(request) + 1);
      }
      return assemblyUrlString;
   }

   /**
    * Determine if the assembly is recursive. The assembly is considered to be
    * recursive if a snippet was already assembled during a previous level of
    * assembly.
    * 
    * @param request request context from which the assembly recursion map is
    * read as request private object and the assembly depth is read as request
    * parameter. Assumed not <code>null</code>.
    * @return <code>true</code> if the assembly is recursive,
    * <code>false</code> otherwise.
    */
   private boolean isAssemblyRecursive(IPSRequestContext request)
   {
      String recursionKey = makeKeyFromRequestContext(request);
      request.printTraceMessage("recursionKey = " + recursionKey);
      Map assembleRecursionMap = (Map) request
         .getPrivateObject(ASSEMBLY_RECURSION_MAP_KEY);
      Set levels = (Set) assembleRecursionMap.get(recursionKey);
      int currentAssemblyLevel = readCurrentAssemblyLevel(request);
      boolean recursion = false;
      if (levels == null)
      {
         levels = new HashSet();
         levels.add("" + currentAssemblyLevel);
         assembleRecursionMap.put(recursionKey, levels);
      }
      else
      {
         if (levels.contains("" + currentAssemblyLevel))
         {
            //If the current level already exits, it must have at least one 
            //more level to indicate it is recursion.
            if (levels.size() > 1)
               recursion = true;
         }
         else
         {
            //If current level does not exist, existsence of a previous 
            //level indicates it is recursion.
            if (levels.size() > 0)
               recursion = true;
         }
      }
      return recursion;
   }

   /**
    * Read and return the assembly depth by reading the special HTML parameter
    * {@link #ASSEMBLY_LEVEL} from the request context.
    * 
    * @param request request context object, assumed not <code>null</code>
    * @return assembly depth which will be 0 (default) or higher.
    */
   private static int readCurrentAssemblyLevel(IPSRequestContext request)
   {
      String sDepth = request.getParameter(ASSEMBLY_LEVEL);
      sDepth = (sDepth == null || sDepth.length() == 0) ? "0" : sDepth;
      int depth = 0;
      try
      {
         depth = Integer.parseInt(sDepth);
      }
      catch(NumberFormatException e){}
      
      return depth;
   }

   /**
    * Make the assembly recursion parameter key from request context. This is
    * the string concatenation of the requet page URL and the requets parameter
    * map (toString()). The special parameter {@link #ASSEMBLY_LEVEL} used in
    * assembly recursion detection is always excluded from the request
    * parameters.
    * 
    * @param request request context object, assumed not <code>null</code>.
    * The requets page URL and the parameters to make the key are taken from
    * this object.
    * 
    * @return assembly recursion parameter key as explained above, never
    * <code>null</code> or empty.
    */
   static private String makeKeyFromRequestContext(IPSRequestContext request)
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      Map params = new HashMap(request.getParameters());
      params.remove(ASSEMBLY_LEVEL);
      return request.getRequestFileURL() + params;
   }

   /**
    * This method (is as good as a post exit) modifies the assembly related
    * stylesheet files (the actual assembly stylesheet and the included ones)
    * to include the additional links to edit related content in WYSIWYG mode.
    * This done by locating the actual style sheet file by making an internal
    * request to a Rx application, modifying it using a transformation style
    * sheet that includes the required links, saving this file to new location
    * and then modifying the result document to use this style sheet rather than
    * the original one. The method looks for an HTML parameter
    * sys_command='editrc' which is a flag to transform the style sheets.
    * Transformation is also performed on the slot definition style sheets,
    * namely, sys_Slots and rx_Slots located in sys_resources and rx_resources
    * applications.
    *
    * @param request as explained in the method
    * <code>processResultDocument</code> above.
    *
    * @param resultDoc as explained in the method
    * <code>processResultDocument</code> above.
    *
    * @param appName - original application name that the request came for,
    * shall be  <code>null</code> or <code>empty</code>
    * @return modifyed XML result document, never <code>null</code>.
    * @throws PSParameterMismatchException 
    * @throws PSExtensionProcessingException 
    *
    */
   private Document modifyAssemblyStyleSheets(IPSRequestContext request, 
      Document resultDoc, String appName)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if(command == null)
         return resultDoc;
      //do not proceed further if the sys_command is not 'editrc'
      if(!command.equalsIgnoreCase(SYS_COMMAND_EDITRC))
      {
         return resultDoc;
      }
      String contentid = PSHtmlParameters.get(
         IPSHtmlParameters.SYS_CONTENTID, request);
      String variantid = PSHtmlParameters.get(
         IPSHtmlParameters.SYS_VARIANTID, request);

      //Get the activeitems contentid
      String activecontentid = "";
      String activeitemid = PSHtmlParameters.get(
         IPSHtmlParameters.SYS_ACTIVEITEMID, request);
      String trail = PSHtmlParameters.get("sys_trail", request);

      String parentid = "";
      String childid = "";
      String parent = PSHtmlParameters.get("parentPage", request);
      //parentPage parameter will be available in the request 
      //only when we are assembling the parent item. If it is available
      //You are working on parent and we initialize parentid with the contentid.
      if(parent!=null)
         parentid = contentid;
      
      List ridList = new ArrayList();
      if(activeitemid!=null && activeitemid.trim().length()>0)
      {
         ridList.add(activeitemid.trim());
      }
      if(trail!=null && trail.trim().length()>0 && !trail.equals(activeitemid))
      {
         StringTokenizer tok = new StringTokenizer(trail, ":");
         ridList.add(tok.nextToken());
      }
      if(ridList.size()>0)
      {
         Map ridMap = new HashMap();
         ridMap.put(IPSHtmlParameters.SYS_ACTIVEITEMID,ridList);
         IPSInternalRequest intReq = request.getInternalRequest(
               "sys_rcSupport/activeitemdetails", ridMap, false);
         try
         {
            Document doc = intReq.getResultDoc();
            NodeList nl = doc.getElementsByTagName("rid");
            if(nl!=null&&nl.getLength()>0)
            {
               Element elem = (Element) nl.item(0);
               childid = elem.getAttribute("contentid");
               parentid = elem.getAttribute("parentcontentid");
               if(nl.getLength()>1)
               {
                  elem = (Element) nl.item(1);
                  if(elem.getAttribute("rid").equals(activeitemid))
                     childid = elem.getAttribute("contentid");
                  else
                     parentid = elem.getAttribute("parentcontentid");
               }
            }
         }
         catch (PSInternalRequestCallException e1)
         {
            request.printTraceMessage(e1.getMessage());
         }
      }
//    if active item id is not present, parent item becomes the active item
      if(activeitemid == null)
      {
         activecontentid = contentid;
      }
      else
      {
         NodeList nl = resultDoc.getElementsByTagName(RELATEDLINKURL);
         for(int i=0; nl!=null && i<nl.getLength(); i++)
         {
            if(((Element)nl.item(i)).getAttribute(ATTR_REALTEDITEMID).equals(
               activeitemid))
            {
               activecontentid = ((Element)nl.item(i)).getAttribute(
                  ATTR_CONTENTID);
               break;
            }
         }
      }
      //if activeitems contentid exists then get the details of the item.
      if(activecontentid != null && activecontentid.length() > 0)
      {
         Map chkoutMap = new HashMap();
         chkoutMap.put(IPSHtmlParameters.SYS_CONTENTID,activecontentid);
         chkoutMap.put(IPSHtmlParameters.SYS_COMMAND,WORKFLOW_COMMAND_NAME);
         chkoutMap.put(PSWorkFlowUtils.DEFAULT_ACTION_TRIGGER_NAME,
            WORKFLOW_CHECKOUT);
         try
         {
            Document contentdetailsdoc = getContentDetails(
               activecontentid, request);
            if(contentdetailsdoc != null)
            {
               Element root = contentdetailsdoc.getDocumentElement();
               String contenteditorurl = root.getAttribute(
                  ATTR_CONTENTEDITORURL);
               //checkout the item if the item is not checked out by anybody
               // and if the item is not in public state and if the content
               // editor url is not empty
               if (!root.getAttribute(
                  ATTR_CONTENTVALID).equalsIgnoreCase("y") &&
                  root.getAttribute(
                     ATTR_CHECKOUTUSERNAME).trim().length() == 0 &&
                  contenteditorurl.trim().length() > 0)
               {
                  IPSInternalRequest iReq1 = request.getInternalRequest(
                     contenteditorurl, chkoutMap, false);
                  iReq1.cleanUp();
               }
            }
         }
         catch(Exception e)
         {
            request.printTraceMessage(e.getMessage());
         }
      }

      
      try
      {
         String appRoot = request.getRequestRoot();
         //Make an internal request to get the stylesheet name and location.
         Map temp = new HashMap();
         temp.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
         temp.put(IPSHtmlParameters.SYS_VARIANTID, variantid);
         IPSInternalRequest iReq = request.getInternalRequest(
            "sys_rcSupport/variantstylesheetlookup", temp, false);
         Document doc = iReq.getResultDoc();
         iReq.cleanUp();
         Element elem = doc.getDocumentElement();
         elem = getChildElement(elem, "Variant");
         String type = elem.getAttribute("type");
         String outputformat = elem.getAttribute("outputformat");
         /*
          * Add these two values as attributes to the assembler info - may be 
          * useful!
          */
         NodeList nl = resultDoc.getElementsByTagName(ASSEMBLER_INFO_ELEM);
         if(nl != null && nl.getLength() > 0)
         {
            Element elemAssembly = (Element)nl.item(0);
            elemAssembly.setAttribute("type", type);
            elemAssembly.setAttribute("outputformat", outputformat);
         }

         //if the variant type is VARIANT_TYPE_SYSTEM (system), do not modify
         //the stylesheet
         if(type.trim().equals(VARIANT_TYPE_SYSTEM))
         {
            return resultDoc;
         }
         if (!contentid.equals(parentid) && !contentid.equals(childid)
               && !outputformat.equals("1"))
         {
            return resultDoc;
         }

         elem = getChildElement(elem, "StylesheetName");
         String styleSheet = getElementData(elem).trim();
         //The new location

         //Transform the main assembly style sheet
         transformStylesheet(STYLESHEETTRANSFORM, appName + "/" + styleSheet,
            false, false);

         //Transform sys_Slots style sheet
         transformStylesheet(STYLESHEETTRANSFORM, STYLESHEETSYSSLOTS, true,
            true);

         //Transform rx_Slots style sheet
         transformStylesheet(STYLESHEETTRANSFORM, STYLESHEETRXSLOTS, true, 
            true);

         //Transform sys_Slots style sheet
         transformStylesheet(STYLESHEETTRANSFORM, STYLESHEETSYSGLOBALS, true,
            true);

         //Transform rx_Slots style sheet
         transformStylesheet(STYLESHEETTRANSFORM, STYLESHEETRXGLOBALS, true,
            true);

         //Transform rx_GlobalTemplates style sheet
         transformStylesheet(STYLESHEETTRANSFORM, STYLESHEETRXGLOBALTEMPLATES,
            true, true);

         //Transform sys_GlobalTemplates style sheet
         transformStylesheet(STYLESHEETTRANSFORM, STYLESHEETSYSGLOBALTEMPLATES,
            true, true);

         //Now modify the process instruction to use the new style sheets.
         java.net.URL url = new java.net.URL("file:"
            + buildEditFilePath(styleSheet));

         String urlText = null;

         if (request.getRequestPageType() == IPSRequestContext.PAGE_TYPE_HTML)
            urlText = PSApplicationHandler.getLocalizedURL(appName, url)
               .toExternalForm();
         else
            urlText = PSApplicationHandler.getExternalURLString(appName,
               appRoot, url);

         ProcessingInstruction pi = resultDoc.createProcessingInstruction(
            "xml-stylesheet",
            ("type=\"text/xsl" + "\" href=\"" + urlText + "\""));

         Element root = (resultDoc == null) ? null : resultDoc
            .getDocumentElement();
         if (root != null)
            resultDoc.insertBefore(pi, root);
         else
            resultDoc.appendChild(pi);
      }
      catch(Exception e)
      {
         PSConsole.printMsg("Extension", e);
      }
      return resultDoc;
   }

   /**
    * This helper method gets the contenitems details for
    * the given contentid.
    * @param contentid assumed not <code>null<code>
    * @param request IPSRequestContext assumed not <code>null<code>
    * @return contentdetails document
    */
   private Document getContentDetails(String contentid, IPSRequestContext request)
   {
      Document doc = null;
      IPSInternalRequest iReq = null;
      try
      {
         Map params = new HashMap();
         params.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
         iReq = request.getInternalRequest(CONTENT_DETAILS_URL, params, false);
         doc = iReq.getResultDoc();
      }
      catch(Exception e)
      {
         return null;
      }
      finally
      {
         if(iReq != null)
            iReq.cleanUp();
      }
      if(doc == null)
         return null;

      String ceurl = doc.getDocumentElement().getAttribute(
         ATTR_CONTENTEDITORURL);
      String result = "";
      //assumes at least one parameter i.e. contentid is part of the url
      int loc = ceurl.indexOf(".htm");
      if(loc > 0)
      {
         ceurl = ceurl.substring(0, loc);
         String ceresource = "";
         loc = ceurl.lastIndexOf('/');
         if(loc > 0)
         {
            ceresource = ceurl.substring(loc+1);
            ceurl = ceurl.substring(0, loc);
            loc = ceurl.lastIndexOf('/');
            if(loc > 0)
            {
               ceurl = ceurl.substring(loc+1);
               result = ceurl + "/" + ceresource;
            }
         }
      }
      doc.getDocumentElement().setAttribute(ATTR_CONTENTEDITORURL,result);
      return doc;
   }


    /**
     * Given the file names for transformation XSL, source XSL and the
     * destination XSL files, this method transforms the source stylesheet to
     * destination style sheet using the transformation stylesheet.
     * to a File. All files are read and written from or to disk.
     *
     * @param transformXsl - the transformation style sheet file name, must
     * not be <code>null</code> or <code>empty</code>
     * @param sourceXsl - the source stylesheet file name (or XML file name) to
     * transform, must not be <code>null</code> or <code>empty</code>
     * @param recurse if <code>true</code>, processes the inlcuded/imported 
     * (xsl:include/xsl:import) style sheets too.
     * @param modifyHrefs If <code>true</code> modifies the XSL stylesheet as 
     * described in {@link #modifyHref(Element)}.
     * @return the file path name of the transformed XSL stylesheet, 
     * <code>null</code> if the file needed no transformation.
     * @throws TransformerException if the transformation fails.
     * @throws TransformerConfigurationException if the transformation fails.
     */
   private String transformStylesheet(String transformXsl, String sourceXsl,
         boolean recurse, boolean modifyHrefs) throws TransformerException,
         TransformerConfigurationException
   {
      String destXsl = buildEditFilePath(sourceXsl);
      Logger l = Logger.getLogger(getClass());
      
      File fileTransform = null;
      File fileSrc = null;
      File fileDest = null;
      File tempSrcFile = null;
      
      try
      {
         fileTransform = new File(PSServer.getRxDir(),transformXsl);
         fileSrc = new File(PSServer.getRxDir(),sourceXsl);
         fileDest = new File(PSServer.getRxDir(),destXsl);
         
         /*
          * if the transform style sheet file is modified or the source xsl is
          * modifed create the new results xsl. This is part of the optimization
          * by avoiding unncessary transformation when the files are not modfied.
          */
         boolean bFileTransformModified = isModified(fileTransform);
         boolean bFileSrcModified = isModified(fileSrc);

         if(!bFileTransformModified && !bFileSrcModified)
            return null;

         //When transformation stylesheet is modifed, we must empty previous
         //records
         if(bFileTransformModified)
            recordModified(fileTransform, true);

         //When the source stylesheet is modifed, we do not need to empty previous
         //records
         if(bFileSrcModified)
            recordModified(fileSrc, false);          
         
         // The following code modifies the stylesheet removing any
         // attributes in the <html> tag and then writes it to a
         // temp file that will be used during transformation. 
         // This is needed to allow active assembly to work when the
         // xhtml namespace declaration is present in the <html>
         // tag. If left in active assembly breaks.
         BufferedWriter bwriter = null;
         BufferedReader breader = null;
         try
         {
            tempSrcFile = new PSPurgableTempFile("AAX", ".xsl", null);
            bwriter =
               new BufferedWriter(new FileWriter(tempSrcFile));
            breader = 
               new BufferedReader(new FileReader(fileSrc));
            String line = null; 
            while((line = breader.readLine()) != null)
            {
               int sPos = line.toLowerCase().indexOf(HTMLTAG_PREFIX);
              
               if(sPos != -1)
               {
                  int ePos = line .indexOf('>', sPos);
                  if((line.charAt(ePos - 1)) != '/')
                     line = line.substring(0, sPos + HTMLTAG_PREFIX.length()) 
                        + line.substring(ePos);
               }
               bwriter.write(line + "\n");              
            }
            bwriter.flush();
         }
         catch (IOException e)
         {
            if(tempSrcFile != null)
            {
               ((PSPurgableTempFile)tempSrcFile).release();
               tempSrcFile = null;
            }
            PSConsole.printMsg(
               this.getClass().getName(), e.getLocalizedMessage());
         }
         finally
         {
            try
            { 
               if(bwriter != null)
                  bwriter.close();
               if(breader != null)
                  breader.close();       
            }
            catch(IOException ignore){}
         }
         // If the modification to the XSL failed then we
         // default to using the original unmodified XSL
         if(tempSrcFile != null)
            fileSrc = tempSrcFile;

         // Create a transform factory instance.
         TransformerFactory tfactory = TransformerFactory.newInstance();

         // Create a transformer for the stylesheet.
         Transformer transformer =
               tfactory.newTransformer(new StreamSource(fileTransform));

         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
         transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

         FileOutputStream fos = null;
         OutputStreamWriter destWriter = null;
         InputStream is = null;
         Reader reader = null;
         
         try
         {
            is = new FileInputStream(fileSrc);
            reader = new InputStreamReader(is, PSCharSetsConstants
                  .rxJavaEnc());
            // Make sure the destination's parent directory
            // exists, or if not then create it
            File destDir = new File(fileDest.getParent());
            if(!destDir.exists())
            {
               destDir.mkdirs();
            }
            
            // Create file writer with appropriate encoding
            fos = new FileOutputStream(fileDest);
            destWriter = new OutputStreamWriter(fos, 
               PSCharSetsConstants.rxJavaEnc());
            // Transform the source XSL to dest file
            transformer.transform(
                  new StreamSource(reader, fileSrc.toURL().toExternalForm()),
                  new StreamResult(destWriter));
            
            if (modifyHrefs)
            {
               File f = new File(PSServer.getRxDir(),destXsl);
               OutputStreamWriter writer = null;
               InputStream dis = null;
               Reader dr = null;
               
               try
               {
                  dis = new FileInputStream(f);
                  dr = new InputStreamReader(dis, PSCharSetsConstants
                        .rxJavaEnc());
                  Document doc =
                     PSXmlDocumentBuilder.createXmlDocument(dr, false);
                  Element[] elems = getImportAndInlcudeElems(doc);
                  for (int i = 0; i < elems.length; i++)
                     modifyHref(elems[i]);
                  writer = new OutputStreamWriter(new FileOutputStream(f), 
                     PSCharSetsConstants.rxJavaEnc());
                  PSXmlDocumentBuilder.write(doc, writer);
               }
               finally
               {
                  if (dr != null) dr.close();
                  if (dis != null) dis.close();
                  if (writer != null)
                  {
                     writer.close();
                  }
               }
            }
            if (!recurse)
               return destXsl;

            Document srcXslDoc =
               PSXmlDocumentBuilder.createXmlDocument(
                  new FileReader(fileSrc),
                  false);

            //Process all xsl:import and xsl:include elements
            Element[] elems = getImportAndInlcudeElems(srcXslDoc);
            for (int i = 0; i < elems.length; i++)
            {
               Element elem = elems[i];
               String file = elem.getAttribute("href");
               if (!file.startsWith("file:"))
                  continue;
               file = file.substring("file:".length());
               transformStylesheet(transformXsl, file, false, true);
            }
         }
         //Any exception should not terminate the process here. If we throw 
         //an exception, the assembly will not be performed. We just display 
         //the message to server console.
         catch (FileNotFoundException e)
         {
            l.error(e.getLocalizedMessage());
         }
         catch (IOException e)
         {
            l.error(e.getLocalizedMessage());
         }
         catch (SAXException e)
         {
            l.error(e.getLocalizedMessage());
         }
         finally
         {
            fileTransform = null;
            fileSrc = null;
            fileDest = null;
            if(tempSrcFile != null)
            {
               ((PSPurgableTempFile)tempSrcFile).release();
               tempSrcFile = null;
            }
            try
            {
               if (reader != null) reader.close();
               if (is != null) is.close();
               if (destWriter != null)
               {
                  destWriter.close();
               }
            }
            catch (IOException e1)
            {
               l.error("Trouble closing writer", e1);
            }
            
         }
         
         return destXsl;
      }
      finally
      {
         fileTransform = null;
         fileSrc = null;
         fileDest = null;
      }
   }
   
   /**
    * Collect all xsl:import and xsl:include elements and return them as an 
    * array of DOM elements.
    * 
    * @param doc DOM document, assumed not <code>null</code>.
    * @return an array of DOM Elements, never <code>null</code> but may be 
    * empty.
    */
   private Element[] getImportAndInlcudeElems(Document doc)
   {
      NodeList imports = doc.getElementsByTagName("xsl:import");
      NodeList includes = doc.getElementsByTagName("xsl:include");
      Element elems[] = new Element[imports.getLength()+includes.getLength()];
      int index = 0;
      for (int i = 0; i < imports.getLength(); i++)
         elems[index++] = (Element) imports.item(i);
      for (int i = 0; i < includes.getLength(); i++)
         elems[index++] = (Element) includes.item(i);
         
      return elems;
   }
   
   /**
    * Modifies the href attribute of the element specified (xsl:import or 
    * xsl:include) to point to a location that includes an end folder "edit". 
    * @param elem DOM element that has an attribute "href", typically 
    * xsl:import or xsl:include, assumed not <code>null</code>.
    */
   private void modifyHref(Element elem)
   {
      String file = elem.getAttribute("href");
      if (file == null || !file.startsWith("file:"))
         return;
      elem.setAttribute("href", buildEditFilePath(file));
   }
   
   /**
    * Builds the new path to include "edit" as the last part of the folder.
    * For example, if the supplied path is xx/yy/zz/ww.xsl, the new path 
    * returned will be xx/yy/zz/edit/ww.xsl.
    * @param path path to be modified, assumed not <code>null</code>.
    * @return new path built based on the scheme above, never 
    * <code>null</code>.
    */
   private String buildEditFilePath(String path)
   {
      int index = path.lastIndexOf('/');
      String left = "";
      String right = path;
      if(index > -1)
      {
         left = path.substring(0, index+1);
         right = path.substring(index+1);
      }
      return (left + "edit/" + right);
   }

   /**
    * Helper function to compute if a file is modified or not. The last modifed
    * date for the file is taken in memory Properties file and
    * compared with the current modified date.
    * @param file, Java File object, assumed not <code>null</code>
    * @return <code>true</code> if the file is modified, <code>false</code>
    * otherwise even during any exception.
    */
   private boolean isModified(File file)
   {
      try
      {
         String modDateOld = ms_props.getProperty(file.getPath(), "0");
         String modDateCurrent = Long.toString(file.lastModified());
         /*throw <code>true</code> only if modified dates match. All other
          * cases return <code>false</code>
          */
         if(modDateOld.equals(modDateCurrent))
         {
            return false;
         }
      }
      catch(Throwable t) //Any exception return <code>true</code>
      {
      }
      return true;
   }

   /**
    * Helper function to record and empty (when required) previous recording of
    * modified dates for the transformation stylesheet files.
    * @param file, file object of interest, assumed not <code>null</code>.
    * @param bResetPrevious, flag to tell the function that all previous
    * recording has to be cleared. This is done when the transformation
    * stylesheet is modified.
    */
   private void recordModified(File file, boolean bResetPrevious)
   {
      if(bResetPrevious)
      {
         Set entries = ms_props.entrySet();
         if(entries != null)
            entries.clear();
      }
      String modDateCurrent = Long.toString(file.lastModified());
      ms_props.setProperty(file.getPath(), modDateCurrent);
   }

   /**
    * Helper function to return the first child element with given name of a
    * given parent.
    * @param parent, parent element - may be <code>null</code> in which case 
    * the return value will be <code>null</code>
    * @param child, child element name may be <code>null</code> in which case 
    * the return value will be <code>null</code>
    * @return Child element as described above, may be <code>null</code>.
    */
   public static Element getChildElement(Element parent, String child)
   {
      if(parent == null)
         return null;

      if(child == null || child.trim().length() < 1)
         return null;

      NodeList nl = parent.getElementsByTagName(child);
      if(nl == null || nl.getLength() < 1)
         return null;
      return (Element)nl.item(0);
   }

   /**
    * Helper function to get the text data of a given element
    * @param elem - Elelemnt to extract data of - may be <code>null</code>.
    * @return Element data as String. Never <code>null</code> may be empty.
    */
   public static String getElementData(Element elem)
   {
      if(elem == null)
         return "";
      Node node = elem.getFirstChild();
      if(node != null && node instanceof Text)
      {
         return ((Text)node).getData();
      }
      return "";
   }

   /**
    * The number of parameters provided for this exit. Initially set to
    * NOT_INITIALIZED to reflect that the #init(IPSExtensionDef, File) method
    * has not been called yet. Is set during the first call to #init(
    * IPSExtensionDef, File).
    */
   public static int ms_paramCount = NOT_INITIALIZED;

   /**
    * The siteid used if non was provided, never <code>null</code>.
    */
   private static final String DEFAULT_SITEID = "0";

   /**
    * The element name for the assembler info added to the result document.
    */
   public static final String ASSEMBLER_INFO_ELEM = "sys_AssemblerInfo";

   /**
    * The attribute name for previewurl that is the request URL for this page
    */
   public static final String PREVIEWURL_ATTR = "previewurl";

   /**
    * The element name for the element wrapping all related content
    * information.
    */
   public static final String RELATED_CONTENT_ELEM = "RelatedContent";

   /**
    * The element name for the element wrapping the variant url.
    */
   public static final String VARIANT_URL_ELEM = "VariantURL";

   /**
    * The element name for the element wrapping the inline link info.
    */
   public static final String INLINE_LINK_ELEM = "InlineLink";

   /**
    * The attribute name for the InlineLink element.
    */
   public static final String URL_ATTR = "url";

   /**
    * The element name returned from the rx_casSupport application for
    * related link URL's.
    */
   private static final String RELATEDLINKURL = "linkurl";

   /**
    * The system support application containing all assembler system
    * resources, never <code>null</code>.
    */
   private static final String SYS_CAS_SUPPORT = "sys_casSupport";

   /**
    * The resource to get the assembler properties from, never
    * <code>null</code>.
    */
   private static final String ASSEMBLERPROPERTIES = SYS_CAS_SUPPORT +
      "/AssemblerProperties";

   /**
    * The resource to get the publication url from, never
    * <code>null</code>.
    */
   private static final String PUBLICATIONURL = "../" + SYS_CAS_SUPPORT +
      "/PublicationUrl.xml";

   /**
    * The resource to get the variant url, never
    * <code>null</code>.
    */
   private static final String VARIANTURL =
      "../sys_ceInlineSearch/varianturl.xml";

   /**
    * The resource to get all information for related content from, never
    * <code>null</code>.
    */
   private static final String RELATEDCONTENT = SYS_CAS_SUPPORT +
      "/casSupport";

   /**
    * name XML element that stores the URLs for the related content info. This
    * element shall be the child element of the RelatedContent element in the
    * assembler information element.
    */
   static private final String INFOURLS = "infourls";
   
   /**
    * Prefix of an html tag (i.e &lt;html)
    */
   private static final String HTMLTAG_PREFIX = "<html";

   /**
    * The transformation stylesheet file name
    */
   static private final String STYLESHEETTRANSFORM =
      "sys_resources/stylesheets/assemblyedit.xsl";

   /**
    * sys_Slots stylesheet file path name
    */
   static private final String STYLESHEETSYSSLOTS =
      "sys_resources/stylesheets/assemblers/sys_Slots.xsl";

   /**
    * rx_Slots stylesheet file path name
    */
   static private final String STYLESHEETRXSLOTS =
      "rx_resources/stylesheets/assemblers/rx_Slots.xsl";

   /**
    * sys_Globals stylesheet file path name
    */
   static private final String STYLESHEETSYSGLOBALS =
      "sys_resources/stylesheets/assemblers/sys_Globals.xsl";

   /**
    * rx_Globals stylesheet file path name
    */
   static private final String STYLESHEETRXGLOBALS =
      "rx_resources/stylesheets/assemblers/rx_Globals.xsl";

   /**
    * sys_GlobalTemplates stylesheet file path name
    */
   static private final String STYLESHEETSYSGLOBALTEMPLATES = 
      "sys_resources/stylesheets/assemblers/sys_GlobalTemplates.xsl";
   
   /**
    * rx_GlobalTemplates stylesheet file path name
    */
   static private final String STYLESHEETRXGLOBALTEMPLATES =
      "rx_resources/stylesheets/assemblers/rx_GlobalTemplates.xsl";

   /**
    * The parameter sys_command value for editing the related content.
    */
   public static final String SYS_COMMAND_EDITRC = "editrc";

   /**
    * The in memory Properties object. This is used to read and recorde the file
    * modified dates
    */
   private static Properties ms_props = new Properties();

   /**
    * Constant value for normal variant type
    */
   public static final String VARIANT_TYPE_NORMAL = "0";

   /**
    * Constant value for autoindex type of variant.
    */
   public static final String VARIANT_TYPE_AUTO = "1";

   /**
    * Constant value for system type of variant. These include all variants that
    * produce not HTML outputs (binaries, JS files etc.).
    */
   public static final String VARIANT_TYPE_SYSTEM = "2";

   /**
    * Constant for workflow sys command name
    */
   public static final String WORKFLOW_COMMAND_NAME = "workflow";

   /**
    * Constant for workflow action
    */
   public static final String WORKFLOW_CHECKOUT =  "checkout";

   /**
    * Constant for linkurl's relateditemid attribute
    */
   public static final String ATTR_REALTEDITEMID =  "relateditemid";

   /**
    * Constant for linkurl's relateditemid attribute
    */
   public static final String ATTR_CONTENTID = "contentid";

   /**
    * Constant for contentdetails resource name
    */
   public static final String CONTENT_DETAILS_URL =
      "sys_ceSupport/contentdetails";
   /**
    * Constant for linkurl's relateditemid attribute
    */
   public static final String ATTR_CONTENTEDITORURL = "contenteditorurl";

   /**
    * Constant for linkurl's relateditemid attribute
    */
   public static final String ATTR_CONTENTVALID = "contentvalid";

   /**
    * Constant for linkurl's relateditemid attribute
    */
   public static final String ATTR_CHECKOUTUSERNAME = "checkoutusername";


   /**
    * String constant to retrieve and modify the map of previously assembled
    * snippets and their parent contexts. The key and value both follow the
    * pattern described in {@link #makeKeyFromRequestContext(IPSRequestContext)}.
    */
   public static final String ASSEMBLY_RECURSION_MAP_KEY = 
      "sys_assemblyRecursionMapKey";

   /**
    * String constant for the special parameter name that holds the value of the
    * assmbly recursion depth. The value of this parameter is passed to child
    * snipptes after incrementing by one during assembly process.
    */
   private static final String ASSEMBLY_LEVEL = "sys_assemblylevel";

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static Logger ms_log = Logger.getLogger(PSAddAssemblerInfo.class);
}
