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
package com.percussion.extension;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An extension handler configuration stores the extension def for each
 * of a handler's extensions. The configuration object can be used by
 * any kind of handler.
 * <p>
 * A configuration object does not "own" or keep track of what file or
 * document (if any) it was loaded from.
 * <p>
 * This class is not thread safe. For thread safety, manually synchronize
 * on the instance.
 */
class PSExtensionHandlerConfiguration
{
   /**
    * Convenience constructor that calls 
    * {@link #PSExtensionHandlerConfiguration(File, IPSExtensionDefFactory, 
    * boolean) PSExtensionHandlerConfiguration(configFile, defFactory, false)}.
    */
   public PSExtensionHandlerConfiguration(File configFile,
      IPSExtensionDefFactory defFactory) throws PSExtensionException
   {
      this(configFile, defFactory, false);
   }

   /**
    * Constructs a new configuration object from the given file, optionally
    * creating the file if it does not exist. A newly created file will contain
    * the barest shell of configuration data.
    * 
    * @param configFile The file from which configuration data is to be read.
    *    Must not be <CODE>null</CODE>.
    * @param defFactory The extension def factory used to serialize and
    *    deserialize extension defs to and from XML. May be
    *    <CODE>null</CODE> to use the default.
    * @param createNew If <CODE>true</CODE> and the file does not exist, it
    *    will be created and the contents formatted as a configuration file.
    * @throws PSExtensionException If the configuration data could not be read
    *    from the file (didn't exist, was not readable, etc.), or if the
    *    configuration data was badly corrupted. Small inconsistencies in the 
    *    data that do not interfere with parsing shall not be treated 
    *    exceptionally.
    */
   public PSExtensionHandlerConfiguration(File configFile,
      IPSExtensionDefFactory defFactory, boolean createNew)
      throws PSExtensionException
   {
      if (defFactory != null)
         m_defFactory = defFactory;
      
      load(configFile, createNew);
   }

   /**
    * Constructs a new configuration object from the given document.
    * 
    * @param configDoc The document from which configuration data is to be
    * gathered. Must not be <CODE>null</CODE>.
    * @param defFactory The extension def factory used to serialize and
    * deserialize extension defs to and from XML. Must not be <CODE>null</CODE>.
    * @throws PSExtensionException If the configuration data could not be read
    * from the file (didn't exist, was not readable, etc.), or if the
    * configuration data was badly corrupted. Small inconsistencies in the data
    * that do not interfere with parsing shall not be treated exceptionally.
    */
   public PSExtensionHandlerConfiguration(Document configDoc,
      IPSExtensionDefFactory defFactory) throws PSExtensionException
   {
      load(configDoc);
   }

   /**
    * Private constructor to initalize member variables.
    */
   private PSExtensionHandlerConfiguration()
   {
   }

   /**
    * Returns <CODE>true</CODE> if and only if the extension with the given
    * name is defined.
    * <p>
    * Note: The fact that an extension has a valid definition doesn't imply
    * that the extension will function correctly or even be loadable.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    *
    * @return <CODE>true</CODE> iff the extension is defined.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public boolean isExtensionDefined(PSExtensionRef ref)
   {
      return (null != getExtensionDef(ref));
   }

   /**
    * Gets the names of all defined extensions across all contexts.
    *
    * @return An Iterator over 0 or more PSExtensionRef objects. Never
    * <CODE>null</CODE>.
    */
   @SuppressWarnings("unchecked")
   public Iterator getExtensionNames()
   {
      Collection names = new LinkedList();
      for (Iterator i = getExtensionContexts(); i.hasNext(); )
      {
         for (Iterator j = getExtensionNames((String)i.next()); j.hasNext(); )
         {
            names.add(j.next());
         }
      }
      return names.iterator();
   }

   /**
    * Gets the names of all extensions defined in the given context. If
    * the context does not exist, returns <CODE>null</CODE>.
    *
    * @param context The context to search in. Must not be <CODE>null</CODE>.

    * @return An Iterator over 0 or more PSExtensionRef objects. Could
    * be <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public Iterator getExtensionNames(String context)
   {
      if (!PSExtensionRef.isValidContext(context))
      {
         throw new IllegalArgumentException("\"" + context +
            "\" is not a valid context.");
      }

      Map extensionDefs = (Map)(m_extensionContexts.get(context));
      if (extensionDefs == null)
         return null;

      return extensionDefs.keySet().iterator();
   }

   /**
    * Gets the names of all contexts used in this handler.
    *
    * @return An Iterator over 0 or more String objects which represent
    * all context names used in this handler. Never <CODE>null</CODE>.
    *
    * @see PSExtensionRef#getContext
    */
   public Iterator getExtensionContexts()
   {
      return m_extensionContexts.keySet().iterator();
   }

   /**
    * Gets the extension definition for the extension with the given name.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    *
    * @return The extension definition, or <CODE>null</CODE> if no such
    * extension is defined.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   @SuppressWarnings("unchecked")
   public IPSExtensionDef getExtensionDef(PSExtensionRef ref)
   {
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      Map<PSExtensionRef,IPSExtensionDef> extensionDefs = getExtensionDefMap(ref);

      return (IPSExtensionDef)extensionDefs.get(ref);
   }

   /**
    * Removes the extension definition for the extension with the given name.
    * This method is idempotent - if no such extension exists, the method
    * will return successfully.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void removeExtensionDef(PSExtensionRef ref)
   {
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      Map<PSExtensionRef,IPSExtensionDef> extensionDefs = getExtensionDefMap(ref);
      if (extensionDefs != null)
      {
         extensionDefs.remove(ref);
      }
   }

   /**
    * Adds the extension definition, overwriting any other extension
    * definition of the same name.
    *
    * @param extensionDef The extension def to add. Must not be <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   @SuppressWarnings("unchecked")
   public void addExtensionDef(IPSExtensionDef extensionDef)
   {
      if (extensionDef == null)
         throw new IllegalArgumentException("extensionDef cannot be null");

      PSExtensionRef ref = extensionDef.getRef();
      Map<PSExtensionRef,IPSExtensionDef> extensionDefs = getExtensionDefMap(ref);

      extensionDefs.put(ref, extensionDef);
   }

   
   private Map<PSExtensionRef,IPSExtensionDef> getExtensionDefMap(PSExtensionRef ref)
   {
      Map<PSExtensionRef,IPSExtensionDef> extensionDefs = m_extensionContexts.get(ref.getContext());
      if (extensionDefs == null)
      {
         synchronized (this)
         {
            extensionDefs = m_extensionContexts.get(ref.getContext());
            if (extensionDefs == null)
            {
               extensionDefs = new ConcurrentHashMap<>(8, 0.9f, 1);
         m_extensionContexts.put(ref.getContext(), extensionDefs);
      }
         }

      }
      return extensionDefs;
   }
   /**
    * Registers this file or directory for pending removal. Generally, any
    * pending removals will be processed when an extension handler is
    * initialized, but this depends on the handler implementation.
    * <p>
    * <B>CAUTION</B>: While handlers should ensure that only the proper files
    * and directories are removed, please note that entire directories can
    * be recursively deleted.
    *
    * @param toBeRemoved The file or directory that should be removed. If
    * it is a directory, then its all files and subdirectories will be removed
    * recursively before the directory itself is removed. Must not be
    * <CODE>null</CODE> or empty.
    *
    * @throws IOException If the canonical version of the file or directory
    * name could not be generated.
    *
    * @throws IllegalArgumentException If any param is invalid.
    *
    * @see java.io.File#getCanonicalFile
    */
   @SuppressWarnings("unchecked")
   public void setPendingRemoval(File toBeRemoved)
      throws IOException
   {
      if (toBeRemoved == null || toBeRemoved.toString().length() == 0)
         throw new IllegalArgumentException("toBeRemoved cannot be null/empty");
      
      m_pendingRemovals.add(toBeRemoved.getCanonicalFile());
   }

   /**
    * Clears a pending-removal entry from the list of pending removals. This
    * method is idempotent - if the file was not pending removal, nothing
    * will happen.
    *
    * @param toBeCleared The file whose pending-removal entry is to be
    * cleared. Must not be <CODE>null</CODE> or empty.
    *
    * @return <CODE>true</CODE> if and only if the given file was pending
    * removal.
    *
    * @throws IOException If the canonical version of the file or directory
    * name could not be generated.
    *
    * @throws IllegalArgumentException If any param is invalid.
    *
    * @see java.io.File#getCanonicalFile
    * @see #setPendingRemoval
    */
   public boolean clearPendingRemoval(File toBeCleared)
      throws IOException
   {
      if (toBeCleared == null || toBeCleared.toString().length() == 0)
         throw new IllegalArgumentException("toBeRemoved cannot be null/empty");


      return m_pendingRemovals.remove(toBeCleared.getCanonicalFile());
   }

   /**
    * Gets the pending file/directory removals.
    *
    * @return A non-<CODE>null</CODE> Iterator over 0 or more
    * non-<CODE>null</CODE> canonical File objects.
    *
    * @see #setPendingRemoval
    */
   @SuppressWarnings("unchecked")
   public Iterator getPendingRemovals()
   {
      /* Need to copy the hashset to a new list and return an iterator 
         from this new list.  If we don't, when clearPendingRemoval() is 
         called, it will invalid the iterator.  
      */
      if (m_pendingRemovals == null)
         return IteratorUtils.EMPTY_LIST_ITERATOR;
      
      ArrayList tmpList = new ArrayList(m_pendingRemovals.size());
      Iterator i = m_pendingRemovals.iterator();
      while (i.hasNext())
         tmpList.add(i.next());
         
      return tmpList.iterator();
   }

   /**
    * Stores the extension configuration in the given file. The file will be
    * created if it does not exist. It will be overwritten if it does exist.
    *
    * @param file The file in which the configuration will be stored. Must not
    *    be <CODE>null</CODE>.
    * @param excludeMethods <code>true</code> to exclude the extension methods
    *    from serialization, <code>false</code> to include.
    * @throws PSExtensionException 
    * @throw IOException If an I/O error occurred while storing the
    *    configuration.
    */
   public void store(File file, boolean excludeMethods) throws IOException, PSExtensionException
   {
      if (file == null)
         throw new IllegalArgumentException("file cannot be null");
 
      String filename=file.getName();
      int  extSep = filename.lastIndexOf(".");
      String baseName = filename;
      String ext="";
      if (extSep>0)
      {
         baseName=filename.substring(0,extSep);
         ext = filename.substring(extSep);
      }
     
      File tempFile = new File(file.getParentFile(), baseName + "_temp" + ext);
      File oldFile = new File(file.getParentFile(), baseName + "_bak" + ext);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      store(doc, excludeMethods);
      
      OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
      
      try
      {
         PSXmlDocumentBuilder.write(doc, out);
      }
      finally
      {
         if (out!=null){
            try{out.close();}catch(Exception e){/*ignore*/};
         }
      }

      if (file.exists())
      {
         copyFile(file,oldFile);
      }

      if (tempFile.exists())
      {
         moveFile(tempFile,file);
      }
      
   }

   private void moveFile(File fromFile, File toFile) throws PSExtensionException
   {
      copyFile(fromFile,toFile);
      fromFile.delete();
   }

   private void copyFile(File fromFile, File toFile) throws PSExtensionException
   {
      InputStream is = null;
      OutputStream os = null;
      try {
          is = new FileInputStream(fromFile);
          os = new FileOutputStream(toFile);
          byte[] buffer = new byte[1024];
          int length;
          while ((length = is.read(buffer)) > 0) {
              os.write(buffer, 0, length);
          }
      }
      catch (FileNotFoundException e)
      {
         throw new PSExtensionException(IPSExtensionErrors.EXT_INSTALL_UPDATE_ERROR,"Cannot find extension file "+fromFile.getAbsolutePath()+" or folder for output "+toFile.getAbsolutePath() +": " +e.getLocalizedMessage());
      }
      catch (IOException e)
      {
         throw new PSExtensionException(IPSExtensionErrors.EXT_INSTALL_UPDATE_ERROR,"Cannot wite to Extension file "+toFile.getAbsolutePath()+ ": "+e.getLocalizedMessage());
      } finally {
          if (is!=null)try{is.close();}catch(Exception e){/*ignore*/};
          if (os!=null)try{os.close();}catch(Exception e){/*ignore*/};
      }
      
   }

   /**
    * Initializes this configuration object from a file, optionally creating
    * the file if it does not exist.
    *
    * @param configFile The file from which configuration data is to
    * be read. Must not be <CODE>null</CODE>.
    *
    * @param createNew If <CODE>true</CODE> and the file does not exist, it
    * will be created and the contents formatted as a configuration file.
    *
    * @throws PSExtensionException If the configuration data could not be
    * read from the file (didn't exist, was not readable, etc.), or if the
    * configuration data was badly corrupted. Small inconsistencies in the
    * data that do not interfere with parsing shall not be treated
    * exceptionally.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   private void load(File configFile, boolean createNew)
      throws PSExtensionException
   {
      if (createNew)
      {
         try
         {
            createShellConfigFile(configFile, "");
         }
         catch (IOException e)
         {
            throw new PSExtensionException(
               IPSExtensionErrors.EXT_MANAGER_INIT_FAILED, e.toString());
         }
      }

      InputStream in = null;
      
      try
      {
         in = new BufferedInputStream(new FileInputStream(
            configFile));

         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         load(doc);
      }
      catch (IOException e)
      {
         throw new PSExtensionException(
            IPSExtensionErrors.EXT_MANAGER_INIT_FAILED, e, "Error loading config xml for "+configFile.getAbsolutePath() +":"+ e.toString());
      }
      catch (SAXException e)
      {
         throw new PSExtensionException(
            IPSExtensionErrors.EXT_MANAGER_INIT_FAILED, e, e.toString());
      }
      finally
      {
         if (in != null)
         {
            try { in.close(); } catch (IOException e) { /* ignore */ }
         }
      }
   }

   /**
    * Initializes this configuration object from the given document.
    *
    * @param configDoc The document from which configuration data is to
    * be gathered. Must not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the configuration data could not be
    * read from the file (didn't exist, was not readable, etc.), or if the
    * configuration data was badly corrupted. Small inconsistencies in the
    * data that do not interfere with parsing shall not be treated
    * exceptionally.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   private void load(Document configDoc) throws PSExtensionException
   {
      // create a walker positioned on the root node
      PSXmlTreeWalker tree = new PSXmlTreeWalker(
         configDoc.getDocumentElement());

      final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      
      Element handlerConfig = tree.getNextElement(
         "PSXExtensionHandlerConfiguration");
      if (handlerConfig == null)
      {
         if (configDoc.getDocumentElement().getTagName().equals(
            "PSXExtensionHandlerConfiguration"))
         {
            handlerConfig = configDoc.getDocumentElement();
            tree.setCurrent(handlerConfig);
         }
         else
         {
            // TODO: i18n, determine code
            throw new PSExtensionException(0,
               "Could not find PSXExtensionHandlerConfiguration element");
         }
      }

      String handlerName = handlerConfig.getAttribute("handlerName");
      if (!PSExtensionRef.isValidExtensionName(handlerName))
      {
         // TODO: i18n, determine code
         throw new PSExtensionException(0,
            "\"" + handlerName + "\" is not a valid extension handler name.");
      }

      m_handlerName = handlerName;

      // get each pending removal
      Node cur = tree.getCurrent();
      for (Element e = tree.getNextElement("pendingRemoval", firstFlag); 
         e != null; e = tree.getNextElement("pendingRemoval", nextFlag))
      {
         String file = e.getAttribute("name");
         if (file == null || file.length() == 0)
         {
            // TODO: log a message that an empty pending-removal was skipped
            continue;
         }

         try
         {
            setPendingRemoval(new File(file));
         }
         catch (IOException ioe)
         {
            // TODO: i18n and code
            throw new PSExtensionException(0, ioe.toString());
         }
      }

      tree.setCurrent(cur);                               
      // get each extension def
      for (Element e = tree.getNextElement("Extension", firstFlag);
         e != null; e = tree.getNextElement("Extension", nextFlag) )
      {
         try
         {
            IPSExtensionDef def = m_defFactory.fromXml(e);
            if (isExtensionDefined(def.getRef()))
            {
               // TODO: log a message that this extension is multiply defined
               continue;
            }
            addExtensionDef(def);
            loadExtensionMethods(def);
         }
         catch (PSExtensionException pse)
         {
            // TODO: log invalid extension definition in config file
         }
      }
   }

   /**
    * Checks the number of actual parameters and its annotated parameters for the given 
    * method and class.
    * 
    * @param clazz the class of the method, assumed not <code>null</code>.
    * @param method the method of the parameters in question, assumed not <code>null</code>.
    * @param paramAnnotations the annotated parameters, assumed not <code>null</code>.
    */
   private void checkMethodParameters(Class clazz, Method method, IPSJexlParam[] paramAnnotations)
   {
      Class[] types = method.getParameterTypes();
      if (types.length == paramAnnotations.length)
         return;
      
      ms_log.warn("The number of annotated parameters(" + paramAnnotations.length 
            + ") do match the number of actual parameters(" + types.length + ") for method \"" 
            + method.getName() + "\" of class \"" + clazz.getName() + "\".");
   }
   
   /**
    * Load the method descriptions for all defined JEXL methods in the supplied
    * extension definition.
    * 
    * @param def the extension definition for which to load all method 
    *    descriptions, not <code>null</code>.
    * @throws PSExtensionException for any error.
    */
   private void loadExtensionMethods(IPSExtensionDef def) 
      throws PSExtensionException
   {
      if (def == null)
         throw new IllegalArgumentException("def cannot be null");
      
      try
      {
         if (def.isJexlExtension())
         {
            Class clazz = Class.forName(def.getInitParameter("className"));
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods)
            {
               IPSJexlMethod methodAnnotation = method.getAnnotation(
                  IPSJexlMethod.class);
               if (methodAnnotation == null)
                  continue;

               PSExtensionMethod extensionMethod = new PSExtensionMethod(
                  method.getName(), method.getReturnType().getName(), 
                  methodAnnotation.description());
               
               Class[] types = method.getParameterTypes();
               IPSJexlParam[] paramAnnotations = methodAnnotation.params();
               checkMethodParameters(clazz, method, paramAnnotations);
               int len = Math.min(types.length, paramAnnotations.length);
               for (int i=0; i<len; i++)
               {
                  String paramName = null;
                  String paramDescription = null;
                  if (paramAnnotations != null && paramAnnotations[i] != null)
                  {
                     paramName = paramAnnotations[i].name();
                     paramDescription = paramAnnotations[i].description();
                  }
                  
                  if (StringUtils.isBlank(paramName))
                     paramName = "param_" + i;
                  
                  PSExtensionMethodParam methodParam = 
                     new PSExtensionMethodParam(paramName, types[i].getName(), 
                        paramDescription);
                  
                  extensionMethod.addParameter(methodParam);
               }
               
               def.addExtensionMethod(extensionMethod);
            }
         }
      }
      catch (ClassNotFoundException e)
      {
         throw new PSExtensionException(IPSExtensionErrors.CLASS_NOT_FOUND, 
            def.getInitParameter("className"));
      }
   }

   /**
    * Creates the bare, correct structure of the configuration in
    * the given file.
    *
    * @param configFile The config filename to store the bare config
    * in. The file will be overwritten if it exists. Must not be
    * <CODE>null</CODE>.
    *
    * @param handlerName The handler name to use in the config file. Must
    * not be <CODE>null</CODE>.
    *
    * @throws IOException If an I/O error occurred when writing to
    * or opening the <CODE>configFile</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public static void createShellConfigFile(File configFile, String handlerName)
      throws IOException
   {
      if (configFile == null)
         throw new IllegalArgumentException("configFile cannot be null");

      if (handlerName == null)
         throw new IllegalArgumentException("handlerName cannot be null");

      // create the config file if it doesn't already exist
      boolean newFile = configFile.createNewFile();

      if (newFile) // write the doc out to the file
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(doc,
            "PSXExtensionHandlerConfiguration");

         root.setAttribute("handlerName", handlerName);
         OutputStream out = null;
         try
         {
            out = new FileOutputStream(configFile);
            PSXmlDocumentBuilder.write(doc, out);
         }
         finally
         {
            if (out != null)
            {
               try { out.close(); } catch (IOException e) { /* ignore */ }
            }
         }
      }
   }

   /**
    * Stores the extension configuration in the given document. Storing
    * will start at the root level, replacing the root element and all
    * of its children.
    *
    * @param doc The document in which the config will be stored. Must
    *    not be <CODE>null</CODE>.
    * @param excludeMethods <code>true</code> to exclude the extension methods
    *    from serialization, <code>false</code> to include.
    */
   private void store(Document doc, boolean excludeMethods)
   {
      Element root = doc.createElement("PSXExtensionHandlerConfiguration");
      PSXmlDocumentBuilder.replaceRoot(doc, root);
      store(root, excludeMethods);
   }

   /**
    * Stores the extension configuration under the given element. If the
    * element name is "PSXExtensionHandlerConfiguration", the configuration
    * will be stored directly beneath the given element. If not, a
    * "PSXExtensionHandlerConfiguration" element will be created beneath the
    * given element and storage will begin under the newly created element.
    *
    * @param root The element under which the configuration will be stored.
    *    Must not be <CODE>null</CODE>.
    * @param excludeMethods <code>true</code> to exclude the extension methods
    *    from serialization, <code>false</code> to include.
    * @return The element directly under which the configuration was stored.
    *    This may be the <CODE>root</CODE> param or an element created directly
    *    under it.
    */
   private Element store(Element root, boolean excludeMethods)
   {
      final Document doc = root.getOwnerDocument();

      // if we're not currently at the correct level, insert a level
      if (!root.getTagName().equals("PSXExtensionHandlerConfiguration"))
      {
         root = PSXmlDocumentBuilder.addEmptyElement(doc,
            root, "PSXExtensionHandlerConfiguration");
         store(root, excludeMethods);
      }

      if (m_handlerName != null)
      {
         root.setAttribute("handlerName", m_handlerName);
      }

      for (Iterator i = getPendingRemovals(); i.hasNext(); )
      {
         File f = (File)i.next();
         Element e = PSXmlDocumentBuilder.addEmptyElement(doc,
            root, "pendingRemoval");

         e.setAttribute("name", f.toString());
      }

      for (Iterator i = getExtensionNames(); i.hasNext(); )
      {
         IPSExtensionDef def = getExtensionDef((PSExtensionRef)i.next());
         m_defFactory.toXml(root, def, excludeMethods);
      }

      return root;
   }

   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }

   /** The handler name. Non-<CODE>null</CODE>.*/
   private String m_handlerName;

   /** 
    * The extension definition factory used for serializing to / from XML,
    * never <code>null</code>.
    */
   private IPSExtensionDefFactory m_defFactory = new PSExtensionDefFactory();

   /**
    * A map from extension contexts to corresponding maps from extension names 
    * to extension defs. Never <CODE>null</CODE>, may be empty.
    */
   private volatile Map<String,Map<PSExtensionRef,IPSExtensionDef>> m_extensionContexts = new ConcurrentHashMap<>(8, 0.9f, 1);

   /**
    * A set of files and dirs pending removal by the handler. Never
    * <CODE>null</CODE>, may be empty.
    */
   private volatile Set m_pendingRemovals = new HashSet();
   
   private static final Logger ms_log = LogManager.getLogger("PSExtensionHandlerConfiguration");
}
