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
package com.percussion.services.assembly.impl.plugin;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.WarningLevel;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.XmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiCssCompressor;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.impl.PSTrackAssemblyError;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.utils.jexl.PSVelocityUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.timing.PSStopwatchStack;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import java.io.File;
import java.io.FileFilter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * This assembler uses a Velocity template to create HTML output text. This uses
 * the <code>RuntimeServices</code> from velocity as the velocity engine does
 * not allow access to everything required to build templates from our own
 * resource loader. The templates are cached using a <code>WeakHashMap</code>
 * which keys on the entire template text. Despite the seeming inefficiency,
 * this still takes a very small amount of time compared to the rest of the
 * process.
 * <p>
 * Note, any changes to the (system or user) Velocity macro files need to send a
 * notification to the instance of this class via
 * {@link IPSNotificationService#notifyEvent(PSNotificationEvent)}, so that the
 * the changes will be able to take effect right away.
 * <p>
 * This assembler will cache all compiled templates by default. However, 
 * <b>caching compiled templates may consume too much memory</b> for some 
 * templates. This behavior can be turn off with the following options:
 * <ul>
 *  <li>
 *      Specify <code>"-Dno_cache_templates=*"</code> as one of the java options,
 *      which will turn off the caching behavior for all templates.
 *  </li>
 *  <li>
 *      Specify <code>"-Dno_cache_templates=template-1,template-2"</code> as one of 
 *      the java options, which will turn off the caching behavior for only the
 *      specified templates.
 *  </li>
 *  <li>
 *      Specify a binding variable <code>"$sys.noCacheTemplate=true"</code> on 
 *      a template, which will turn off the caching behavior for current template.
 *  </li>
 * </ul> 
 * If a template caching is turned off, then any (snippet or child) templates used by 
 * the template may inherit the none-cache behavior.
 * 
 * @author dougrand
 */
public class PSVelocityAssembler extends PSAssemblerBase
      implements
         IPSNotificationListener
{
   private Object lock1 = new Object();
   
   /**
    * Start PI fragment for field and slot extraction end markup
    */
   private static final String PSX_END = "<?psx-end-";

   /**
    * Start PI fragment for field and slot extraction start markup
    */
   private static final String PSX_START = "<?psx-start-";

   /**
    * End PI fragment
    */
   private static final String END_PI = "?>";

   /**
    * Precalculated expression for JEXL
    */
   private static final IPSScript SYS_MIMETYPE = PSJexlEvaluator
         .createStaticExpression("$sys.mimetype");

   /**
    * Precalculated expression for JEXL
    */
   private static final IPSScript SYS_CHARSET = PSJexlEvaluator
         .createStaticExpression("$sys.charset");

   /**
    * Precalculated expression for JEXL
    */
   private static final IPSScript SYS_TEMPLATE = PSJexlEvaluator
         .createStaticExpression("$sys.template");

   /**
    * Logger for this class
    */
   static Log ms_logger = LogFactory.getLog(PSVelocityAssembler.class);

   /**
    * Engine for templating
    */
   protected static RuntimeServices ms_rs = null;


   protected static StringResourceRepository ms_repository = null;
   /**
    * This is used to determine if an instance of this class has been registered
    * to listen on any file changes, see {@link #PSVelocityAssembler()} for 
    * detail. Default to <code>false</code>.
    */
   private static boolean ms_isListenOnFileChange = false;
   
   /**
    * This is used to set the {@link RuntimeConstants#VM_LIBRARY} property of
    * the template engine {@link #ms_rs}. Default to <code>null</code>. It
    * is set by {@link #init(IPSExtensionDef, File)}.
    * 
    */
   protected String m_libraries = null;

   /**
    * This is used to set the {@link RuntimeConstants#VM_LIBRARY_AUTORELOAD} and
    * {@link RuntimeConstants#FILE_RESOURCE_LOADER_CACHE} properties of the
    * template engine {@link #ms_rs}. Default to <code>null</code>. It is
    * set by {@link #init(IPSExtensionDef, File)}.
    * <p>
    * Note, the above properties seem have no effect.
    */
   protected String m_reload = null;

   /**
    * The system template macro path. Default to <code>null</code>. It is set
    * by {@link #getSysTemplateMacroPath()} and never modified after that.
    */
   private static String ms_sysTemplatePath = null;

   /**
    * The local template macro path. Default to <code>null</code>. It is set
    * by {@link #getLocalTemplateMacroPath()} and never modified after that.
    */
   private static String ms_localTemplatePath = null;

   /**
    * Default constructor. This constructor must be called by the extended 
    * classes.
    */
   public PSVelocityAssembler() 
   {
      // listen to any file changes that care to send a notification
      // for invoking {@link #initVelocity()} in case changing a macro file.
      //
      // however, there is no need to add another listener if there is already
      // an instance of this class that initialized Velocity Engine (ms_rs)
      // and assumed to listen on the file changes.
      if (! ms_isListenOnFileChange)
      {
         IPSNotificationService notifyService = PSNotificationServiceLocator
               .getNotificationService();
         notifyService.addListener(EventType.FILE, this);
         
         ms_isListenOnFileChange = true;
      }
   }

   @Override
   protected IPSAssemblyResult doAssembleSingle(IPSAssemblyItem item)
         throws Exception
   {
      // Discover whether this is a partial assembly
      String part = item.getParameterValue(IPSHtmlParameters.SYS_PART, null);
      IPSAssemblyResult work = (IPSAssemblyResult) item;
      PSJexlEvaluator eval = new PSJexlEvaluator(work.getBindings());

      eval.bind("$sys.part.render", true);
      eval.bind("$sys.part.end", true);

      if (StringUtils.isBlank(part))
      {
         eval.bind("$sys.part.active", false);
         return super.doAssembleSingle(item);
      }
      else
      {
         /*
          * In a partial assembly, no global template is expanded. The 
          * underlying macros add PIs to the output document that help the
          * code here separate the text from the surrounding formatting. The
          * macros also use the information, if supplied, to suppress the 
          * rendering of unneeded fields and slots.
          */
         if (item.hasNode())
            ms_logger.debug("Partially Assemble item "
                  + item.getNode().getUUID());
         else
            ms_logger.debug("Partially Assemble item "
                  + item.getParameterValue(IPSHtmlParameters.SYS_CONTENTID,
                        "unknown"));
         String pieces[] = part.split(":");
         if (pieces.length != 2)
         {
            ms_logger.warn("Bad sys_part parameter: " + part);
         }
         else
         {
            eval.bind("$sys.part.type", pieces[0]);
            eval.bind("$sys.part.name", pieces[1]);
            eval.bind("$sys.part.active", true);
         }
         IPSAssemblyResult rval = assembleSingle(item);

         // Extract part
         if (pieces.length == 2)
         {
            Charset cset = PSStringUtils.getCharsetFromMimeType(rval
                  .getMimeType());
            String parttext = new String(rval.getResultData(), cset.name());
            String partstart = PSX_START + pieces[0] + " " + pieces[1]
                  + END_PI;
            String partend = PSX_END + pieces[0] + " " + pieces[1] + END_PI;
            int start = parttext.indexOf(partstart);
            if (start >= 0)
            {
               int end = parttext.indexOf(partend, start);
               if (end >= 0)
               {
                  parttext = parttext
                        .substring(start + partstart.length(), end);
                  rval.setResultData(parttext.getBytes(cset.name()));
               }
            }
         }

         ms_logger.debug("Partial Result is of type " + rval.getMimeType());
         return rval;
      }
   }

   /**
    * The name of the binding variable, used to turn off the compiled templates
    * See detail at the class description.
    */
   private static final String SYS_NO_CACHE_TEMPLATE = "$sys.noCacheTemplate";

   /**
    * The jexl expression object of the {@link #SYS_NO_CACHE_TEMPLATE}.
    */
   private static final IPSScript SYS_NO_CACHE_TEMPLATE_EXP = PSJexlEvaluator
         .createStaticExpression(SYS_NO_CACHE_TEMPLATE);

   /**
    * Contains a set of template names whose compiled templates will not be
    * cached. If the 1st element is <code>*</code>, then the system will not
    * cache any of the compiled templates.
    */
   private volatile static Set<String> ms_noCacheTemplates = null;
   
   /**
    * Determines if the system will cache the compiled template of the
    * specified template.
    *  
    * @param templateName the name of the template in question, assumed not
    * <code>null</code>. 
    * @param eval the jexl evaluator, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the compiled template will not be cached.
    */
   private boolean isCacheTemplateOff(String templateName, PSJexlEvaluator eval)
   {
      boolean result = false;

      Set<String> templates = getNoCacheTemplates();
      if (templates.contains("*"))
      {
         result = true;
      }
      else if (!templates.contains(templateName))
      {
         result = getNoCacheTemplateValue(eval);
      }
      else
      {
         eval.bind(SYS_NO_CACHE_TEMPLATE, Boolean.TRUE);
         result = true;
      }

      if (ms_logger.isDebugEnabled())
      {
         if (result)
            ms_logger.debug("Cache compiled velocity is off for template '" + templateName + "'.");
         else
            ms_logger.debug("Cache compiled velocity is on for template '" + templateName + "'.");
      }

      return result;
   }

   /**
    * Gets the value of the binding variable, {@link #SYS_NO_CACHE_TEMPLATE},
    * and return its value if exist; otherwise return <code>false</code> if
    * the binding variable does not exist.
    * 
    * @param eval the jex evaluator, used to retrieve and evaluate the 
    * binding variable, assumed not <code>null</code>.
    * 
    * @return the value described above.
    */
   private boolean getNoCacheTemplateValue(PSJexlEvaluator eval)
   {
      try
      {
         Object isCacheObj = eval.evaluate(SYS_NO_CACHE_TEMPLATE_EXP);
         if (isCacheObj == null)
            return false;

         return ((Boolean) isCacheObj).booleanValue();
      }
      catch (Exception e)
      {
         return false;
      }
   }

   /**
    * Gets the template names, whose compiled template objects will not
    * be cached. 
    * <br>
    * The template names are specified through a system property
    * "no_cache_templates", see the class description on the possible
    * values of the property.  
    * 
    * @return the template names or "*". It may be empty if the system
    * property is not specified, never <code>null</code>.
    */
   private Set<String> getNoCacheTemplates()
   {
      if (ms_noCacheTemplates != null)
         return ms_noCacheTemplates;
      
      String nameProperty = System.getProperty("no_cache_templates");
      if (StringUtils.isBlank(nameProperty))
         return Collections.emptySet();

      Set<String> templateNames = new HashSet<String>();
      if (nameProperty.trim().equals("*"))
      {
         templateNames.add("*");
         ms_logger.info("Cache compiled template is off for all templates");
         ms_noCacheTemplates = templateNames;
         return templateNames;
      }

      String[] names = nameProperty.split(",");
      for (String name : names)
      {
         if (isBlank(name))
            continue;
         templateNames.add(name);
      }

      ms_logger.info("Cache compiled template is off for: "
            + templateNames.toString());

      ms_noCacheTemplates = templateNames;

      return templateNames;
   }
   
   @Override
   public IPSAssemblyResult assembleSingle(IPSAssemblyItem item)
   {
      if (item.getParameterValue("sys_reinit", "false")
            .equalsIgnoreCase("true"))
      {
         try
         {
            initVelocity();
            // Change so we don't do this twice
            item.setParameterValue("sys_reinit", "false");
         }
         catch (Exception e)
         {
            ms_logger.error("Problem reinitializing velocity", e);
         }
      }

      PSJexlEvaluator eval = new PSJexlEvaluator(item.getBindings());
      String template = null;

      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start("velocityassemble");
      try
      {
         try
         {
            template = (String) eval.evaluate(SYS_TEMPLATE);
         }
         catch (Exception e)
         {
            return getFailureResult(item, "exception retrieving template "
                  + e.getLocalizedMessage());
         }

         // Add currentslot map
         eval.bind("$sys.currentslot", new HashMap<String, Object>());

         if (StringUtils.isBlank(template))
         {
            return getFailureResult(item, "no velocity template present");
         }

         VelocityContext ctx = PSVelocityUtils.getContext(item
               .getBindings());
         
         // Add $sys.ctx bindings so the velocity context is accessible for
         // some velocity tools
         eval.bind("$sys.ctx", ctx);


        String scriptName = item.getTemplate().getName();

        Template t = null;
          // Use template code itself as key to cache for template object.  Underlying cache size limits will handle memory size
          //  Cache does not need modify check this way also.
          if (ms_repository == null)
              ms_repository = StringResourceLoader.getRepository("string");
          try {
              if (ms_repository.getStringResource(template) != null)
                  t = ms_rs.getTemplate(template);
              else {
                  ms_repository.putStringResource(template, template);

                  t = ms_rs.getTemplate(template);
                  
                  }
              if (t == null) {
                  throw new IllegalArgumentException("Cannot find template in cache" + item.getTemplate().getName() + ":" + template);
            }

            StringWriter writer = new StringWriter(4000);
            t.merge(ctx, writer);
            writer.close();
            String result = writer.toString();

             String mtype = (String) eval.evaluate(SYS_MIMETYPE);
             if (StringUtils.isBlank(mtype))
                mtype = "text/html";

             if(PSServer.getProperty("compressOutput","false").equals("true")){
                if(mtype.equals("text/html")) {
                   result = compressHtml(result);
                }else if(mtype.equals("text/xml")){
                   result = compressXML(result);
                }
             }

            String charset = (String) eval.evaluate(SYS_CHARSET);
              if (!StringUtils.isBlank(charset)) {
               // Canonicalize the charset
               Charset cset = Charset.forName(charset);
               charset = cset.name();
               item.setResultData(result.getBytes(charset));
              } else {
                 item.setResultData(result.getBytes());
              }

             if (!StringUtils.isBlank(charset))
                mtype += ";charset=" + charset;
             else
                mtype += ";charset=utf-8";
             item.setMimeType(mtype);

             item.setStatus(Status.SUCCESS);
            return (IPSAssemblyResult) item;
         }
         catch (Throwable ae)
         {

            String message = getErrorMsgForItem(item);

            PSTrackAssemblyError.addProblem(message, ae);

            // Create clone for response
            IPSAssemblyItem work = (IPSAssemblyItem) item.clone();
            work.setStatus(Status.FAILURE);
            work.setMimeType("text/html");
            StringBuilder results = new StringBuilder();
            results.append("<html><head></head><body>");
            results
                    .append("<div style=\"border: 2px solid red; background-color: #FFEEEE; width:100%; padding:5px; margin:1px; \">");
            results.append("<h2>");
            results.append(message);
            results.append(" \"");
            results.append("</h2><p>");
            results.append(message +" : " +ae.toString());
            results.append("</p></div></body></html>");
            try
            {
               work.setResultData(results.toString().getBytes("UTF8"));
            }
            catch (UnsupportedEncodingException e)
            {
               //UTF8 should always be available
               ms_log.error("UTF-8 not supported",e);
            }

            return (IPSAssemblyResult) work;


         }
      } finally {
         sws.stop();
      }
   }

   private String compressXML(String result) {
      XmlCompressor compressor = new XmlCompressor();

      compressor.setEnabled(true);

      String ret = result;
      try {
         ret = compressor.compress(result);
      }catch(Exception e){
         ms_log.warn(e.getMessage(),e);
      }
      return ret;

   }

   private String compressHtml(String html) {

      HtmlCompressor compressor = new HtmlCompressor();


      compressor.setEnabled(true);                   //if false all compression is off (default is true)
      compressor.setRemoveComments(true);            //if false keeps HTML comments (default is true)
      compressor.setRemoveMultiSpaces(true);         //if false keeps multiple whitespace characters (default is true)
      compressor.setRemoveIntertagSpaces(true);      //removes iter-tag whitespace characters
      compressor.setRemoveQuotes(true);              //removes unnecessary tag attribute quotes
      compressor.setSimpleDoctype(true);             //simplify existing doctype
      compressor.setRemoveScriptAttributes(true);    //remove optional attributes from script tags
      compressor.setRemoveStyleAttributes(true);     //remove optional attributes from style tags
      compressor.setRemoveLinkAttributes(true);      //remove optional attributes from link tags
      compressor.setRemoveFormAttributes(true);      //remove optional attributes from form tags
      compressor.setRemoveInputAttributes(true);     //remove optional attributes from input tags
      compressor.setSimpleBooleanAttributes(true);   //remove values from boolean tag attributes
      compressor.setRemoveJavaScriptProtocol(true);  //remove "javascript:" from inline event handlers
      compressor.setRemoveHttpProtocol(true);        //replace "http://" with "//" inside tag attributes
      compressor.setRemoveHttpsProtocol(true);       //replace "https://" with "//" inside tag attributes
      compressor.setPreserveLineBreaks(true);        //preserves original line breaks
      compressor.setRemoveSurroundingSpaces("br,p"); //remove spaces around provided tags

      compressor.setCompressCss(false);               //compress inline css
      compressor.setCompressJavaScript(false);        //compress inline javascript


      List<Pattern> preservePatterns = new ArrayList<Pattern>();
      preservePatterns.add(HtmlCompressor.PHP_TAG_PATTERN); //<?php ... ?> blocks
      preservePatterns.add(HtmlCompressor.SERVER_SCRIPT_TAG_PATTERN); //<% ... %> blocks
      preservePatterns.add(HtmlCompressor.SERVER_SIDE_INCLUDE_PATTERN); //<!--# ... --> blocks
      preservePatterns.add(Pattern.compile("<jsp:.*?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)); //<jsp: ... > tags
      compressor.setPreservePatterns(preservePatterns);

      String ret = html;
      try {
         ret = compressor.compress(html);
      }catch(Exception e){
         ms_log.warn(e.getMessage(),e);
         return html;
      }

      YuiCssCompressor cssCompressor = new YuiCssCompressor();
      cssCompressor.setLineBreak(1); //  start from new line of every css class declaration
      compressor.setCssCompressor(cssCompressor);
      compressor.setCompressCss(true);

      try {
         ret = compressor.compress(ret);

      }catch(Exception e){
         ms_log.warn(e.getMessage(),e);
      }


      compressor.setYuiCssLineBreak(80);             //--line-break param for Yahoo YUI Compressor
      compressor.setYuiJsDisableOptimizations(true); //--disable-optimizations param for Yahoo YUI Compressor
      compressor.setYuiJsLineBreak(1);              //--line-break param for Yahoo YUI Compressor
      compressor.setYuiJsNoMunge(true);              //--nomunge param for Yahoo YUI Compressor
      compressor.setYuiJsPreserveAllSemiColons(true);//--preserve-semi param for Yahoo YUI Compressor
      compressor.setCompressJavaScript(true);        //compress inline javascript

      try {
         ret = compressor.compress(ret);

      }catch(Exception e){
         ms_log.warn(e.getMessage(),e);
      }
      return ret;
   }

   /**
    * Gets error message when failed to assemble the specified item.
    * @param item the item, assumed not <code>null</code>.
    * @return the error message, not blank.
    */
   private String getErrorMsgForItem(IPSAssemblyItem item)
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = cms.loadComponentSummary(item.getId().getUUID());
      
      return "Problem assembling output for item (name=\"" + summary.getName()
            + "\", id=" + item.getId().toString() + ") with template: "
            + item.getTemplate().getName() + ".";
   }
   
   @Override
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      try
      {

         m_reload = def
               .getInitParameter("com.percussion.extension.assembly.autoReload");

         // no need to init Velocity Engine again if it has been initialized.
         if (ms_rs != null)
            return;

         initVelocity();
      }
      catch (Exception e)
      {
         throw new PSExtensionException("Java", e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.notification.IPSNotificationListener#notifyEvent(com.percussion.services.notification.PSNotificationEvent)
    */
   public void notifyEvent(PSNotificationEvent event)
   {
      if (event.getType() == EventType.FILE
            && event.getTarget() instanceof File)
      {
         File cFile = (File) event.getTarget();
         if (isTemplateMacroPath(cFile.getAbsolutePath()))
         {
            initVelocity();
         }
      }
   }

   /**
    * Determines if the specified file path is a system or local template macro
    * path.
    * 
    * @param path the file path in question. It may be <code>null</code> or
    *           empty.
    * 
    * @return <code>true</code> if the specified file path is a system or
    *         local template macro path; otherwise return <code>false</code>.
    */
   private static boolean isTemplateMacroPath(String path)
   {
      if (StringUtils.isBlank(path))
         return false;

      return (path.startsWith(getSysTemplateMacroPath()) || path
            .startsWith(getLocalTemplateMacroPath()));
   }

   /**
    * Lazy load the system template macro path.
    * 
    * @return the system template macro path.
    */
   private static String getSysTemplateMacroPath()
   {
      if (ms_sysTemplatePath == null)
         ms_sysTemplatePath = PSServer.getRxFile("sys_resources/vm");
      return ms_sysTemplatePath;
   }

   /**
    * Lazy load the local template macro path.
    * 
    * @return the local template macro path.
    */
   private static String getLocalTemplateMacroPath()
   {
      if (ms_localTemplatePath == null)
         ms_localTemplatePath = PSServer.getRxFile("rx_resources/vm");
      return ms_localTemplatePath;
   }

   /**
    * Gets all macro files from the specified directories.
    * 
    * @param sysDir the system directory that contains the velocity macros from 
    * the core system, assumed not <code>null</code>.
    * @param localDir the directory contains the user specific velocity macros. 
    * 
    * @return all the files with ".vm" extension (case insensitive) in the
    * specified directories, never <code>null</code>, may be empty.
    */
   private String getMacroFiles(String sysDir, String localDir)
   {
      StringBuffer buffer = new StringBuffer();
      getMacroFiles(buffer, sysDir);
      getMacroFiles(buffer, localDir);
      
      return buffer.toString();
   }
   
   /**
    * Gets all macro files from the specified directory
    *
    * @param buffer the buffer for collecting all macro files and separated
    * with comma.
    * @param location the directory that contains the velocity macros, assumed 
    * not <code>null</code>.
    */
   private void getMacroFiles(StringBuffer buffer, String location)
   {
      FileFilter filter = new VMFileFilter();
      File dir = new File(location);
      for (File f : dir.listFiles(filter))
      {
         if (buffer.length() > 0)
            buffer.append(",");
         
         buffer.append(f.getName());
      }
   }

   /**
    * Filter the file names, only accept files with ".vm" extension.
    */
   private class VMFileFilter implements FileFilter
   {
      /*
       * //see base interface method for details
       */
      public boolean accept(File pathname)
      {
         String name = pathname.getName();
         if (StringUtils.isBlank(name))
            return false;
         
         int i = name.lastIndexOf('.');
         if ( i > 0 && (name.length() - i) == 3)
         {
            String suffix = name.substring(i);
            return suffix.equalsIgnoreCase(".vm");
         }
         return false;
      }
   }
   
   /**
    * Initialize velocity engine
    */
   private void initVelocity()
   {
      ms_logger.info("Velocity reinitialized");
      
      String sys_templates = getSysTemplateMacroPath();
      String rx_templates = getLocalTemplateMacroPath();
      m_libraries = getMacroFiles(sys_templates, rx_templates);
      
      ms_rs = new RuntimeInstance();

      ms_rs.addProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
      ms_rs.addProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
      ms_rs.setProperty("resource.loader","file, string");
      
      ms_rs.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
            rx_templates);
      ms_rs.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
            sys_templates);
      
      // Add String resource loader to cache template scripts
      ms_rs.setProperty("resource.manager.defaultcache.size","250");
      ms_rs.setProperty("string.resource.loader.description","Velocity StringResource loader");
      ms_rs.setProperty("string.resource.loader.class",StringResourceLoader.class.getName());
      ms_rs.setProperty("string.resource.loader.cache","true");
      ms_rs.setProperty("string.resource.loader.repository.class","org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl");
      ms_rs.setProperty("string.resource.loader.repository.name","string");

      // Turn off modification check as we use template for resource key will be immutable
      //ms_rs.setProperty("string.resource.loader.modificationCheckInterval","1");

      ms_logger.debug("Sys path: " + sys_templates);
      ms_rs.addProperty(RuntimeConstants.VM_LIBRARY, m_libraries);
      ms_logger.debug("Velocity libraries: " + m_libraries);
      ms_rs.addProperty(RuntimeConstants.VM_PERM_ALLOW_INLINE, "true");
      ms_rs.addProperty(RuntimeConstants.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL,
            "true");
      ms_rs.addProperty(RuntimeConstants.VM_MESSAGES_ON, "true");
      ms_rs.addProperty(RuntimeConstants.VM_PERM_INLINE_LOCAL, "true");
      /*
       * ms_rs.addProperty(RuntimeConstants.UBERSPECT_CLASSNAME,
       * "com.percussion.services.assembly.impl.plugin" +
       * ".PSVelocityAssemblerUberspect");
       */
      File logpath = new File(PSServer.getRxDir(), "jetty/base/logs/velocity.log");
      ms_rs.addProperty(RuntimeConstants.RUNTIME_LOG, logpath
                  .getAbsolutePath());
      if (m_reload != null && m_reload.equalsIgnoreCase("yes"))
      {
         ms_logger.debug("Reload is on");
         ms_rs.addProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, "true");
         ms_rs.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "false");
      }
      else
      {
         ms_logger.debug("Reload is off, caching is on");
         ms_rs.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
      }
      try
      {
         ms_rs.init();
      }
      catch (Exception e)
      {
         ms_logger.error("Problem initializing Velocity assembler", e);
         throw new RuntimeException("Problem initializing Velocity assembler",
               e);
      }

      }
   
}
