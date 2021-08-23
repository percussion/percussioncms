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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.ant;

import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.StringOutputSegment;
import au.id.jericho.lib.html.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Creates a help hints xml file using the specified help mappings and
 * base help path.
 */
public class PSHelpHintFileCreator
{

   /**
    * Ctor
    * @param helpMappings path of the help mappings file, cannot be 
    * <code>null</code>.
    * @param helpPath base help path, cannot be <code>null</code>.
    * @param target the target xml file path, cannot be <code>null</code>.
    */
   public PSHelpHintFileCreator(File helpMappings, File helpPath, File target)
   {
     if(helpMappings == null)
        throw new IllegalArgumentException("helpMappings cannot be null.");
     if(helpPath == null)
        throw new IllegalArgumentException("helpPath cannot be null.");
     if(target == null)
        throw new IllegalArgumentException("target cannot be null.");
     m_helpMappings = helpMappings;
     m_helpPath = helpPath;
     m_target = target;
   }
   
   /**
    * Create the help hint file
    */
   public void createFile() throws IOException
   {
      validatePaths();
      Properties mappings = loadMappings();
      final Map<String, String> sortedMappings = new TreeMap<String, String>();
      Iterator it = mappings.keySet().iterator();
      while(it.hasNext())
      {
         String key = (String) it.next();
         sortedMappings.put(key, (String) mappings.get(key));
      }
      
      PrintStream out = null;
      try
      {
         out = new PrintStream(new FileOutputStream(m_target, false));
         out.println(XML_HEADER);
         out.println(makeStartTag(ELEM_ROOT, null, false));
         createFileBody(out, sortedMappings);
         out.println(makeEndTag(ELEM_ROOT));
         
      }
      finally
      {
         out.close();
      }
   }
   
   /**
    * Set debug on or off
    * @param on
    */
   public void setDebug(boolean on)
   {
      m_debug = on;
   }
   
   /**
    * @return iterator of all unique parsed images. Never <code>null</code>,
    * may be empty.
    */
   public Iterator getImages()
   {
      return m_images.keySet().iterator();
   }
   
   /**
    * Creates the actual "meat" of the xml file.
    * @param out assumed not <code>null</code>.
    */
   private void createFileBody(PrintStream out, Map<String, String> mappings)
      throws IOException
   {
      Iterator it = mappings.keySet().iterator();
      while(it.hasNext())
      {
         String key = (String)it.next();
         if(key.startsWith(EDITOR_PKG) || key.startsWith(WIZARD_PKG))
         {
            final String filename = mappings.get(key);
            if(filename != null && filename.trim().length() > 0)
            {               
               File helpFile = new File(m_helpPath, filename);
               if(helpFile.exists() && helpFile.isFile())
               {
                  parseAndWriteHints(out, helpFile, key);
               }
            }
         }
      }
   }
   
   /**
    * Parses the messages from the files and then writes the appropriate
    * help hint entry in the output xml file.
    * @param out
    * @param helpFile
    * @param key
    * @throws IOException
    */
   private void parseAndWriteHints(PrintStream out, File helpFile, String key)
      throws IOException
   {
      String text = getHelpFileText(helpFile);      
      Source source = new Source(text);      
      List descTags = getAllFieldDescStartTags(source);
      if(descTags.isEmpty())
         return;
      for(int i = 0; i < descTags.size(); i++)
      {
         String content = null;
         StartTag tagA = (StartTag)descTags.get(i);
         if(i == descTags.size() - 1)
         {
            Tag lastTag = tagA.findEndTag();
            while(true)
            {
               Tag nextTag = source.findNextTag(lastTag.getEnd());
               if(nextTag == null)
                  break;   
               lastTag = nextTag;
               if(nextTag instanceof StartTag)
               {
                  if(isRelatedItemTable(text, (StartTag)nextTag))
                     break;                  
               }
               else if(ms_stop_tags.contains(nextTag.getName()))
               {
                  break;
               }
               
            }
            content = text.substring(tagA.getBegin(), lastTag.getBegin());
            
         }
         else
         {            
            StartTag tagB = (StartTag)descTags.get(i + 1);
            content = text.substring(tagA.getBegin(), tagB.getBegin());
         }
         
         // Write the hint entry
         String fieldname = getFieldName(content);
         if(fieldname == null)
            continue;
         String theKey = key + "." + fieldname.toLowerCase().trim().replace(' ', '_');
         System.out.println("Creating help hint entry for: " + theKey);
         final Map<String, String> attribs =
               Collections.singletonMap(ATTR_KEY, theKey); 
         StringBuilder sb = new StringBuilder();
         sb.append(INDENT);
         sb.append(makeStartTag(ELEM_HINT, attribs, false));
         sb.append(NEWLINE);
         sb.append(INDENT);
         sb.append(CDATA_BEGIN);
         sb.append(NEWLINE);
         sb.append(INDENT);
         sb.append(INDENT);
         sb.append(handleAnchors(handleImages(content)));
         sb.append(NEWLINE);
         sb.append(INDENT);
         sb.append(CDATA_END);
         sb.append(NEWLINE);
         sb.append(INDENT);
         sb.append(makeEndTag(ELEM_HINT));
         sb.append(NEWLINE);
         out.print(sb.toString());
         
      }
   }
   
   /**
    * Finds all image tags and adds the resource base token to the source
    * attribute. Also records each unique image file path for later use.
    * @param content assumed not <code>null</code>.
    * @return the modified string, never <code>null</code>.
    */
   private String handleImages(String content)
   {
      Source source = new Source(content);
      OutputDocument outputDoc = new OutputDocument(content);
      for(Iterator it = source.findAllStartTags(ELEM_IMAGE).iterator(); it.hasNext();)
      {
         StartTag sTag = (StartTag)it.next();
         Attributes attrs = sTag.getAttributes();
         Attribute attr = attrs.get(ATTR_SOURCE);
         String value = attr.getValue();
         m_images.put(value, "1");
         StringBuilder sb = new StringBuilder();
         sb.append(ATTR_SOURCE);
         sb.append("=");
         sb.append(QUOTE);
         sb.append(RESOURCE_BASE_TOKEN);
         sb.append("\\");
         sb.append(value);
         sb.append(QUOTE);
         outputDoc.add(new StringOutputSegment(attr.getBegin(), attr.getEnd(), sb.toString()));
      }
      return outputDoc.toString();
      
   }
   
   /**
    * Finds all anchor tags and adds the resource base token to the href
    * attribute if needed. Also records each unique image file path for later use.
    * @param content assumed not <code>null</code>.
    * @return the modified string, never <code>null</code>.
    */
   private String handleAnchors(String content)
   {
      Source source = new Source(content);
      OutputDocument outputDoc = new OutputDocument(content);
      for(Iterator it = source.findAllStartTags(ELEM_ANCHOR).iterator(); it.hasNext();)
      {
         StartTag sTag = (StartTag)it.next();
         Attributes attrs = sTag.getAttributes();
         Attribute attr = attrs.get(ATTR_HREF);
         String value = attr.getValue();
         if(value.trim().startsWith("http://"))
            continue;
         StringBuilder sb = new StringBuilder();
         sb.append(ATTR_HREF);
         sb.append("=");
         sb.append(QUOTE);
         sb.append(RESOURCE_BASE_TOKEN);
         sb.append("\\");
         sb.append(value);
         sb.append(QUOTE);
         outputDoc.add(new StringOutputSegment(attr.getBegin(), attr.getEnd(), sb.toString()));
      }
      return outputDoc.toString();
      
   }
   
   /**
    * Strips all tags from content
    * @param content assumed not <code>null</code>.
    * @return cleaned content, never <code>null</code>.
    */
   private String stripTags(String content)
   {
      Source source = new Source(content);
      OutputDocument outputDoc = new OutputDocument(content);
      for(Iterator it = source.getNextTagIterator(0); it.hasNext();)
      {
         Tag tag = (Tag)it.next();
         outputDoc.add(new StringOutputSegment(tag.getBegin(), tag.getEnd(), ""));
      }
      return outputDoc.toString();
   }
   
   /**
    * Determines that the passed in tag is a table and is the 
    * related items table.
    * @param text assumed not <code>null</code>.
    * @param tag assumed not <code>null</code>.
    * @return <code>true</code> if this is a related items table.
    */
   private boolean isRelatedItemTable(String text, StartTag tag)
   {
      if(!tag.getName().equals(ELEM_TABLE))
         return false;
      EndTag eTag = tag.findEndTag();
      Source source = new Source(text.substring(tag.getBegin(), eTag.getEnd()));
      for(Iterator it = source.findAllStartTags(ELEM_P).iterator(); it.hasNext();)
      {
         StartTag sTag = (StartTag)it.next();
         Attributes attrs = sTag.getAttributes();
         Attribute attr = attrs.get(ATTR_CLASS);
         if(attr != null && attr.getValue().equals(RELATEDHEADING))
            return true;         
      }
      return false;
   }
   
   /**
    * Finds all field description p tags
    * @param source assumed not <code>null</code>.
    * @return list of all the field description p tags, never
    * <code>null</code>, may be empty.
    */
   private List getAllFieldDescStartTags(Source source)
   {
      List tags = new ArrayList();
      for(Iterator it = source.findAllStartTags(ELEM_P).iterator(); it.hasNext();)
      {
         StartTag sTag = (StartTag)it.next();
         if(isFieldDescription(sTag))
         {
            tags.add(sTag);
         }
      }
      return tags;
   }
   
   
   /**
    * Parses the fieldname from the content string passed in
    * @param content assumed not <code>null</code>
    * @return field name or <code>null</code> if not found.
    */
   private String getFieldName(String content)
   {
      Source source = new Source(content);
      for(Iterator it = source.findAllStartTags(ELEM_STRONG).iterator(); it.hasNext();)
      {
         StartTag sTag = (StartTag)it.next();
         Attributes attrs = sTag.getAttributes();
         Attribute attr = attrs.get(ATTR_CLASS);
         if(attr != null && attr.getValue().equals(FIELDNAME))
         {
            EndTag eTag = sTag.findEndTag();
            return stripTags(content.substring(sTag.getEnd(), eTag.getBegin()));
         }
      }
      return null;
   }
   
   /**
    * Determine if the passed in tag is a field description
    * tag.
    * @param tag assumed not <code>null</code>.
    * @return <code>true</code> if this is a field description p tag.
    */
   private boolean isFieldDescription(StartTag tag)
   {
      if(!tag.getName().toLowerCase().equals(ELEM_P))
         return false;
      Attributes attrs = tag.getAttributes();
      Attribute attr = attrs.get(ATTR_CLASS);
      if(attr != null && attr.getValue().equals(FIELDDESC))
         return true;
      return false;
   }
   
   /**
    * Loads the contents of the help file into a string
    * @param helpFile
    * @return the contents of the help text file
    * @throws IOException upon any error
    */
   private String getHelpFileText(File helpFile)
      throws IOException
   {
      debug(helpFile.getAbsolutePath());
      FileInputStream is = null;
      try
      {
         StringBuilder sb = new StringBuilder();
         is = new FileInputStream(helpFile);
         int cursor = -1;
         while((cursor = is.read()) != -1)
         {
            sb.append((char)cursor);
         }
         return sb.toString();
      }
      finally
      {
         if(is != null)
            is.close();
      }
   }
   
   /**
    * Helper method for creating xml tags
    * @param name
    * @param attribs
    * @param type
    * @return the newly created tag, never <code>null</code> or empty.
    */
   private String makeTag(String name, Map<String, String> attribs, String type)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<");
      if(type.equals("close"))
         sb.append("/");
      sb.append(name);
      if(attribs != null && !attribs.isEmpty())
      {
         for (final String key : attribs.keySet())
         {
            String value = attribs.get(key);
            sb.append(" ");
            sb.append(key);
            sb.append("=\"");
            sb.append(value);
            sb.append("\"");
         }
      }
      if(type.equals("selfclose"))
         sb.append("/");
      sb.append(">");
      return sb.toString();
   }
   
   /**
    * Helper method for creating xml start tags
    * @param name
    * @param attribs
    * @param isSelfClose
    * @return the newly created tag, never <code>null</code> or empty.
    */
   private String makeStartTag(String name, Map<String, String> attribs,
         boolean isSelfClose)
   {
      String type = isSelfClose ? "selfclose" : "start";
      return makeTag(name, attribs, type);
   }
   
   /**
    * Helper method for creating xml end tags
    * @param name
    * @return the newly created tag, never <code>null</code> or empty.
    */
   private String makeEndTag(String name)
   {
      return makeTag(name, null, "close");
   }
   
   /**
    * Validates the to make sure both the help mappings file and
    * @throws IOException upon error
    */
   private void validatePaths() throws IOException
   {
      debug("Validating help paths.");
      if(!m_helpMappings.exists() || !m_helpMappings.isFile())
      {
         throw new IOException("Help mappings file does not exist: [" +
            m_helpMappings.getAbsolutePath() + "]");
      }
      if(!m_helpPath.exists() || !m_helpPath.isDirectory())
      {
         throw new IOException("Help base directory does not exist: [" +
            m_helpPath.getAbsolutePath() + "]");
      }
   }
   
   /**
    * Helper method to load the help mappings and return them
    * as a set of properties.
    * @return never <code>null</code>, may be empty.
    * @throws IOException on any error
    */
   private Properties loadMappings() throws IOException
   {
      debug("Loading mappings");
      Properties props = new Properties();
      InputStream is = new FileInputStream(m_helpMappings);
      props.load(is);
      is.close();
      return props;      
   }
   
   
   
   /**
    * Write a debug string to console if debug mode is on
    * @param str the debug string to output.
    */
   private void debug(String str)
   {
      if(m_debug)
         System.out.println("[DEBUG] -> " + str);
   }
   
   /**
    * Main used for testing only
    * @param args
    */
   public static void main(String[] args) throws Exception
   {
      String base = "E:\\rxMain\\Designer\\ui\\default-config\\rxconfig\\Workbench\\";
      String helppath = 
         "P:\\Documentation\\Released Documents\\Rhythmyx\\6.0\\Online\\com.percussion.doc.workbench";
      String mappings = base + "WorkbenchHelpMappings.properties";
      String target = base + "PSXEditorHelpHints.xml";
      PSHelpHintFileCreator creator = 
         new PSHelpHintFileCreator(
            new File(mappings), new File(helppath), new File(target));
      //creator.setDebug(true);
      creator.createFile();
   }   
   
   /**
    * The path to the help mappings file
    */
   private File m_helpMappings;
   
   /**
    * The path to where the plugin help base directory is located
    */
   private File m_helpPath;
   
   /**
    * The target path for the file to be created.
    */
   private File m_target;
   
   /**
    * Flag indicating that debug is on
    */
   private boolean m_debug;
   
   /**
    * Map of all unique images parsed
    */
   private final Map<String, String> m_images = new HashMap<String, String>();   
   
   
   // Package constants
   private static final String EDITOR_PKG = "com.percussion.workbench.ui.editors.form.";
   private static final String WIZARD_PKG = "com.percussion.workbench.ui.editors.wizards.";
   
   // XML Attribute constants
   private static final String ATTR_CLASS = "class";
   private static final String ATTR_HREF = "href";
   private static final String ATTR_KEY = "key";
   private static final String ATTR_SOURCE = "src";
   
   // XML Element constants
   private static final String ELEM_ANCHOR = "a";
   private static final String ELEM_HINT = "helphint";
   private static final String ELEM_IMAGE = "img";
   private static final String ELEM_P = "p";
   private static final String ELEM_ROOT = "PSXEditorHelpHints";
   private static final String ELEM_TABLE = "table";
   private static final String ELEM_STRONG = "strong";
   
   // Various formatting and other constants
   private static final String CDATA_BEGIN = "<![CDATA[";
   private static final String CDATA_END = "]]>";
   private static final String FIELDDESC = "fielddescription";
   private static final String FIELDNAME = "fieldname";   
   private static final String NEWLINE = "\n";
   private static final String QUOTE = "\"";
   private static final String RELATEDHEADING = "relatedheading";
   private static final String SPACE = " ";
   private static final String RESOURCE_BASE_TOKEN = "@@RESOURCEBASE@@";
   private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
   private static final String INDENT = SPACE + SPACE + SPACE;   
   
   /**
    * List of tags that indicate that we have come to the end of 
    * the final field description.
    */
   private static final List<String> ms_stop_tags = new ArrayList<String>();
   static
   {
      ms_stop_tags.add("body");
      ms_stop_tags.add("html");
   }
   
  
}
