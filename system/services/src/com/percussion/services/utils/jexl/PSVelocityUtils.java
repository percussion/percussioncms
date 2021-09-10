/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.services.utils.jexl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.server.PSServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Various convenient methods for Velocity Template.
 *
 * @author Yu-Bing Chen
 */
public class PSVelocityUtils
{
	public static final String VELOCITY_AUTOCORRECTFILE = "velocity-precompile.properties";
    private static final String ADDTRAILINGSPACEFORLASTPOUND = "addTrailingSpaceForLastPound";
	private static final String AUTOCORRECT = "autocorrect";
	private static final String PATTERN = "pattern";
	private static final String REPLACE = "replace";
	private static final String AUTO_FIX_MACRO_FILES = "autoFixMacroFiles";

	private static final Logger log = LogManager.getLogger(PSVelocityUtils.class);
	private static Properties autocorrectPatterns;

   /**
    * Not to expose the default constructor.
    */
   private PSVelocityUtils()
   {
	   initProperties();

   }


private static void initProperties() {
	String preCompilerFile  = PSServer.getRxConfigDir() + File.separatorChar+ VELOCITY_AUTOCORRECTFILE;

	 loadPreCompilerConfig(preCompilerFile);

	}

    public static void preProcessTemplateFile(File f) {

       if(autocorrectPatterns == null || autocorrectPatterns.isEmpty()){
          initProperties();
       }

       String fixMacros = (String)autocorrectPatterns.getOrDefault(AUTO_FIX_MACRO_FILES, "true");
       if (!(fixMacros.equalsIgnoreCase("true") || fixMacros.equalsIgnoreCase("yes")))
       {
          log.debug("Property {} set to {} Skipping VM File fixup", AUTO_FIX_MACRO_FILES, fixMacros);
          return;
       }
       File folder = f.getParentFile();

        if (!folder.exists())
            throw new IllegalArgumentException("Folder "+folder.getAbsolutePath() +" does not exist for vm files");

        String origTemplate = null;
        try {
            origTemplate = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("Cannot read velocity template {} Error: {}", f.getAbsolutePath(),
                    PSExceptionUtils.getMessageForLog(e));
            log.debug( e);
            return;
        }
        String fileName = f.getName();
            String processed = preProcessTemplate(origTemplate, fileName);

            if (!origTemplate.equals(processed)) {

                try {
                    File backup = getBackupFile(folder, f);
                    FileUtils.copyFile(f, backup);

                    try (OutputStreamWriter writer =
                                 new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
                        writer.write(processed);
                    } catch (FileNotFoundException e) {
                       log.error("File not found exception for file Error: {}", 
                               PSExceptionUtils.getMessageForLog(e));
                       log.debug( e);
                       return;

                    } catch (IOException e) {
                        log.error("IOException writing processed velocity macro to {} Error: {}", f.getAbsolutePath(), 
                                PSExceptionUtils.getMessageForLog(e));
                        log.debug(e);
                        return;
                    }

                    log.info("Updated Velocity macro file {} backup written to {}", f.getAbsolutePath(), backup.getAbsolutePath());
                } catch (Exception e)
                {
                    log.error("Cannot backup or write fixed up velocity template Error: {}", PSExceptionUtils.getMessageForLog(e));
					log.debug(e);
                    return;
                }
   
            }

    }

    private static File getBackupFile(File folder, File f) {
        return getBackupFile(folder,f,1);
    }


    private static File getBackupFile(File folder, File f, int index) {
        String fileName = f.getName();
        String filePrefix = StringUtils.substringBefore(fileName,".vm");
        FastDateFormat formatter = FastDateFormat.getInstance("yyyyMMdd");

        File backupFile = new File(folder, filePrefix + "_" + formatter.format(new Date()) + "_" + (index) + ".bak");
        if (backupFile.exists())
            backupFile = getBackupFile(folder,f,++index);
        return backupFile;
    }


   /**
    * A resource loader for creating templates from runtime data
    * 
    * @author dougrand
    *
    */
   private static class StringResourceLoader extends ResourceLoader
   {
      private String m_templateSource = null;
      
      /**
       * Ctor
       * @param templateSource the source, never <code>null</code> or empty
       */
      public StringResourceLoader(String templateSource)
      {
         if (StringUtils.isBlank(templateSource))
         {
            throw new IllegalArgumentException("templateSource may not be null or empty");
         }
         m_templateSource = templateSource;
      }

      @Override
      public void init(ExtProperties configuration) {
         // do nada
      }

      @Override
      public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException {
         InputStream inStream =  new ByteArrayInputStream(m_templateSource.getBytes(StandardCharsets.UTF_8));
         return new InputStreamReader(inStream);
      }

      @Override
      public boolean isSourceModified(@SuppressWarnings("unused") Resource arg0)
      {
         return false;
      }

      @Override
      public long getLastModified(@SuppressWarnings("unused") Resource arg0)
      {
         return 0;
      }
   }
      
   /**
    * Compile the specified Velocity Template.
    * 
    * @param content the template in question, may be <code>null</code> or empty.
    * @param name the name of the above template, may not be <code>null</code>
    *    or empty.
    * @param rs the runtime service of the Velocity Engine, never 
    *    <code>null</code>.
    *    
    * @return the compiled template, never <code>null</code>
    * 
    * @throws ResourceNotFoundException if failed to find resource.
    * @throws ParseErrorException if parse error occurred.
    */
   public static Template compileTemplate(String content, String name,
         RuntimeServices rs)
      throws ResourceNotFoundException, ParseErrorException
   {

      //Pre-process the template if auto correct is turned on
      content = preProcessTemplate(content, name);

      Template t = new Template();
      t.setRuntimeServices(rs);
      t.setResourceLoader(new StringResourceLoader(content));
      t.setEncoding("UTF-8");
      t.setName(name);
      t.process();

      return t;
   }
   
   /**
    * Get Velocity Context from a supplied bindings.
    * @param bindings the bindings, may not be <code>null</code>, may be empty.
    * @return the Velocity Context, never <code>null</code>.
    */
   public static VelocityContext getContext(Map<String, Object> bindings)
   {
      if (bindings == null)
         throw new IllegalArgumentException("bindings may not be null.");
      
      VelocityContext ctx = new VelocityContext();
      
      // Transfer bindings, removing the '$' from variable names
      // the '$' is understood by Velocity
      for (Map.Entry<String, Object> entry : bindings.entrySet())
      {
         String name = entry.getKey();
         if (name.startsWith("$"))
         {
            name = name.substring(1);
         }
         ctx.put(name, entry.getValue());
      }
      return ctx;
   }
   
   /***
    * Preprocess the template to auto-correct 1.x to 2.x incompatibilities.
    *
    * @param template
    * @return
    */
   public static String preProcessTemplate(String template, String name){
	   String ret = template;

	   try{
		   if(autocorrectPatterns == null || autocorrectPatterns.isEmpty()){
			   initProperties();
		   }

		   String autoCorrect = autocorrectPatterns.getProperty(AUTOCORRECT);
           String fixTrailingSpace = autocorrectPatterns.getProperty(ADDTRAILINGSPACEFORLASTPOUND, "true");
		   //Only execute patterns if auto correct feature is turned on or is missing
		   if(autoCorrect == null || autoCorrect.equalsIgnoreCase("true")){
			   int patternCounter = 1;

			   String pattern = autocorrectPatterns.getProperty(PATTERN + "." + patternCounter, null);
			   String replace = autocorrectPatterns.getProperty(REPLACE + "." + patternCounter, null);
			   while(pattern != null && replace !=null){
				   //Perform find and replace

				   ret = ret.replaceAll(pattern, replace);

				   //Increment the counter and get the next pair
				   patternCounter++;

				   pattern = autocorrectPatterns.getProperty(PATTERN + "." + patternCounter, null);
				   replace = autocorrectPatterns.getProperty(REPLACE + "." + patternCounter, null);

			   }

			   if (fixTrailingSpace.equalsIgnoreCase("true")) {
                   String lastChar = ret.substring(ret.length() - 1);
                   if (lastChar.equals("#") || lastChar.equals("$")) {
                       log.debug("Last character of template {} is {}. Appending space to fix parse error", name, lastChar);
                       ret = ret.substring(0, ret.length() - 1) + " ";
                   }
               }
		   }
	   }catch(Exception e){
		   log.error("An unexpected exception occurred while pre-compiling Template {} Error: {}", name, PSExceptionUtils.getMessageForLog(e));
		   log.debug( e);
	   }

	   return ret;
   }

   public static void loadPreCompilerConfig(InputStream preCompilerFile) {
	   Properties ret = new Properties();
	   try{
		   ret.load(preCompilerFile);
	   }catch(IOException e){
			log.error("Velocity pre-compiler disabled.  Unable to load configuration file from stream. Error: {}", PSExceptionUtils.getMessageForLog(e));
			log.debug( e);
	   }

	   autocorrectPatterns = ret;
   }
   public static void loadPreCompilerConfig(String preCompilerFile) {
	   Properties ret = new Properties();
			try (FileInputStream input = new FileInputStream(preCompilerFile)){
				ret.load(input);
			} catch (IOException e) {
				log.error("Velocity pre-compiler disabled.  Unable to load configuration file: {}. Error: {}" , preCompilerFile, PSExceptionUtils.getMessageForLog(e));
				log.debug( e);
			}

	   autocorrectPatterns = ret;
	}
}
