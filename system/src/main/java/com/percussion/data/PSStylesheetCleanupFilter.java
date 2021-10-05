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
package com.percussion.data;

import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A filter that contains a set of matching rules that determine what namespace
 * declarations, elements, and attributes are allowed. The matching expressions
 * use Glob type patterns for matching.
 * <p>
 * 
 * <pre>
 *    Wildcard symbols:
 *   
 *     * = 0 or more of any character
 *     ? = 1 instance of any character
 * </pre>
 * 
 * </p>
 */
public class PSStylesheetCleanupFilter
{
   private static final Logger log = LogManager.getLogger(PSStylesheetCleanupFilter.class);

   /**
    * Private ctor. This class is a singleton. Use {@link #getInstance(Element)}
    * to get an object instance.
    */
   private PSStylesheetCleanupFilter() {
      // no-op
   }

   /**
    * Returns the Singleton instance of the <code>PSStylesheetFilter</code>
    * class. The filter object is created from the stylesheetCleanupFilter.xml
    * file that resides under rxconfig/Server. If the file does not exist then a
    * hard coded default filter is used.
    * 
    * <pre>
    *     &lt;?xml version=\&quot;1.0\&quot; encoding=\&quot;UTF-8\&quot; ?&gt;
    *     &lt;stylesheetCleanupFilter&gt;
    *        &lt;allowedNamespace name=\&quot;\&quot; declAllowed=\&quot;true \&quot; declValue=\&quot;*xhtml*\&quot;&gt;
    *           &lt;allowedElement name=\&quot;*\&quot;/&gt;
    *           &lt;allowedAttribute name=\&quot;*\&quot;/&gt;
    *        &lt;/allowedNamespace&gt;
    *        &lt;allowedNamespace name=\&quot;xml\&quot; declAllowed=\&quot;false \&quot;&gt;
    *           &lt;allowedAttribute name=\&quot;lang\&quot;/&gt;
    *           &lt;allowedAttribute name=\&quot;space\&quot;/&gt;
    *        &lt;/allowedNamespace&gt;
    *     &lt;/stylesheetCleanupFilter&gt;
    * </pre>
    * 
    * @return the singleton instance for this class, never <code>null</code>
    */
   public static synchronized PSStylesheetCleanupFilter getInstance()
   {
      if (ms_instance == null)
      {
         File filterFile = new File(PSServer.getRxDir(), PSServer.SERVER_DIR
               + "/stylesheetCleanupFilter.xml");
         getInstance(filterFile);
      }
      return ms_instance;
   }

   /**
    * Call this to initialize the class for testing or other purposes where the
    * location may not be standard.
    * 
    * @param location the location, never <code>null</code>
    * @return the instance, never <code>null</code>
    */
   public static synchronized PSStylesheetCleanupFilter getInstance(
         File location)
   {
      if (location == null)
      {
         throw new IllegalArgumentException("location may not be null");
      }
      if (ms_instance == null)
      {
         ms_instance = new PSStylesheetCleanupFilter();
         Document doc = null;

         try
         {
            doc = getDefaultFilterDocument();

            if (location.exists() && location.isFile())
               doc = PSXmlDocumentBuilder.createXmlDocument(
                     new FileInputStream(location), false);
         }
         catch (Exception e)
         {
            log.error("Problem loading namespace configuration from file {}, error {} ", location, e.getMessage());
            log.debug(e);
         }
         ms_instance.fromXml(doc.getDocumentElement());
      }
      return ms_instance;
   }

   /**
    * Determines if the specified namespace declaration is allowed
    * 
    * @param ns the namespace, may be <code>null</code> or empty.
    * @param val the namespace declaration value to be checked
    * @return <code>true</code> if the declaration is allowed
    */
   public boolean isNSDeclarationAllowed(String ns, String val)
   {
      return hasMatch(ns, val, DECLARATION_INDEX);
   }

   /**
    * Determines if the specified namespace element is allowed
    * 
    * @param ns the namespace, may be <code>null</code> or empty.
    * @param elem the namespace element value to be checked
    * @return <code>true</code> if the element is allowed
    */
   public boolean isNSElementAllowed(String ns, String elem)
   {
      return hasMatch(ns, elem, ELEMENTS_INDEX);
   }

   /**
    * Determines if the specified namespace attribute is allowed
    * 
    * @param ns the namespace, may be <code>null</code> or empty.
    * @param attr the attribute to be checked
    * @return <code>true</code> if the attribute is allowed
    */
   public boolean isNSAttributeAllowed(String ns, String attr)
   {
      return hasMatch(ns, attr, ATTRIBUTES_INDEX);
   }

   /**
    * Adds all the matching rules from the xml passed in.
    * <p>
    * Uses the following DTD: <br>
    * 
    * <pre>
    *     &lt;!ELEMENT allowedAttribute EMPTY&gt;
    *     &lt;!ATTLIST allowedAttribute
    *      name CDATA #REQUIRED
    *     &gt;
    *   
    *     &lt;!ELEMENT allowedElement EMPTY&gt;
    *     &lt;!ATTLIST allowedElement
    *      name CDATA #REQUIRED
    *     &gt;
    *   
    *     &lt;!ELEMENT allowedNamespace (allowedElement*, allowedAttribute*)&gt;
    *     &lt;!ATTLIST allowedNamespace
    *      name CDATA #REQUIRED
    *      declAllowed CDATA #REQUIRED
    *      declValue CDATA #IMPLIED
    *     &gt;
    *   
    *     &lt;!ELEMENT stylesheetCleanupFilter (allowedNamespace+)&gt;
    * </pre>
    * 
    * @param elem
    */
   protected void fromXml(Element elem)
   {
      m_allowedNS.clear();
      NodeList nl = elem.getElementsByTagName(ELEM_ALLOWED_NAMESPACE);
      int len = nl.getLength();
      for (int i = 0; i < len; i++)
      {
         Element currentEl = (Element) nl.item(i);
         String namespaceName = currentEl.getAttribute(ATTR_NAME);
         String namespaceUri = currentEl.getAttribute(ATTR_URI);
         String declAllowed = currentEl.getAttribute(ATTR_DECLARATION_ALLOWED);
         String declValue = currentEl.getAttribute(ATTR_DECLARATION_VALUE);
         addNamespace(namespaceName, namespaceUri);
         if (declAllowed != null
               && (declAllowed.trim().equalsIgnoreCase("true") || declAllowed
                     .trim().equalsIgnoreCase("yes")))
         {
            addNSDeclaration(namespaceName, declValue);
         }
         // Handle children
         NodeList children = currentEl.getChildNodes();
         int childLen = children.getLength();
         for (int c = 0; c < childLen; c++)
         {
            String nodeName = children.item(c).getNodeName();
            String name = null;
            if (nodeName.equals(ELEM_ALLOWED_ELEMENT))
            {
               name = ((Element) children.item(c)).getAttribute(ATTR_NAME);
               addNSElement(namespaceName, name);
            }
            if (nodeName.equals(ELEM_ALLOWED_ATTR))
            {
               name = ((Element) children.item(c)).getAttribute(ATTR_NAME);
               addNSAttribute(namespaceName, name);
            }
         }
      }
   }

   /**
    * Adds a namespace to the allowed namespaces hash map. This creates an array
    * that holds three arrayLists and puts them into the allowed namespaces hash
    * map.
    * 
    * @param ns the namespace, may be <code>null</code> or empty.
    * @param namespaceUri the uri associated with this namespace, may be
    *           <code>null</code> or empty for bc
    */
   @SuppressWarnings("unchecked")
   private void addNamespace(String ns, String namespaceUri)
   {
      if (!StringUtils.isEmpty(namespaceUri))
         m_uris.put(ns, namespaceUri);
      if (!m_allowedNS.containsKey(ns))
      {
         List[] lists =
         {new ArrayList(), new ArrayList(), new ArrayList()};
         m_allowedNS.put(ns, lists);
      }
   }

   /**
    * Adds an element rule to the appropriate list under the specified
    * namespace.
    * 
    * @param ns the namespace, may be <code>null</code> or empty.
    * @param elem the element rule pattern
    */
   private void addNSElement(String ns, String elem)
   {
      if (!m_allowedNS.containsKey(ns))
         throw new IllegalStateException("Cannot add namespace element");
      List<String> elems = getList(ns, ELEMENTS_INDEX);
      if (!elems.contains(elem))
         elems.add(elem);
   }

   /**
    * Adds an attribute rule to the appropriate list under the specified
    * namespace.
    * 
    * @param ns the namespace, may be <code>null</code> or empty.
    * @param attr the attribute rule pattern
    */
   private void addNSAttribute(String ns, String attr)
   {
      if (!m_allowedNS.containsKey(ns))
         throw new IllegalStateException("Cannot add namespace element");
      List<String> attrs = getList(ns, ATTRIBUTES_INDEX);
      if (!attrs.contains(attr))
         attrs.add(attr);
   }

   /**
    * Adds a declaration rule to the appropriate list under the specified
    * namespace.
    * 
    * @param ns the namespace, may be <code>null</code> or empty.
    * @param dec the declaration value rule pattern
    */
   private void addNSDeclaration(String ns, String dec)
   {
      if (!m_allowedNS.containsKey(ns))
         throw new IllegalStateException("Cannot add namespace element");
      List<String> decs = getList(ns, DECLARATION_INDEX);
      if (!decs.contains(dec))
         decs.add(dec);
   }

   /**
    * Looks for a pattern match between the value passed in and the Glob type
    * expressions that exist in the specified namespace list.
    * 
    * @param ns the namespace string, may be <code>null</code> or empty.
    * @param val the value to be matched
    * @param listIndex the index for the specific namespace list to be used
    * @return <code>true</code> if a match is found
    */
   private boolean hasMatch(String ns, String val, int listIndex)
   {
      if (StringUtils.isBlank(ns))
         ns = "";
      if (val == null)
         val = "";
      if (!m_allowedNS.containsKey(ns))
         return false;
      List list = getList(ns, listIndex);
      // First try to find a "*" wildcard entry, if we find this
      // we can just return true and save some time by not iterating
      // the list
      if (list.contains("*"))
         return true;
      Iterator it = list.iterator();
      while (it.hasNext())
      {
         String pattern = (String) it.next();
         try
         {
            if (isMatch(val, pattern))
               return true;
         }
         catch (MalformedPatternException e)
         {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
         }
      }
      return false;
   }

   /**
    * Matches the passed in string against the passed in Glob type expression.
    * 
    * @param str the string to match against the GlobType expression
    * @param gExp the globtype expression
    * @return <code>ture</code> if a match is found
    * @throws MalformedPatternException upon pattern compilation error
    */
   private boolean isMatch(String str, String gExp)
         throws MalformedPatternException
   {
      PatternCompiler compiler = new GlobCompiler();
      PatternMatcher matcher = new Perl5Matcher();

      // may throw MalformedPatternException
      Pattern pattern = compiler.compile(gExp);

      return matcher.matches(str, pattern);
   }

   /**
    * Returns list as specified by the index passed in.
    * 
    * @param ns the namespace to which this list belongs.
    * @param index the appropriate list index
    * @return the specified list or <code>null</code> if the namespace does
    *         not exist.
    */
   private List<String> getList(String ns, int index)
   {
      List<String> list = null;
      if (ns == null)
         ns = "";
      if (!m_allowedNS.containsKey(ns))
         return list;
      return ((List<String>[]) m_allowedNS.get(ns))[index];
   }

   /**
    * Get the namespace associated with the given prefix
    * 
    * @param namespaceName prefix, may be <code>null</code> or empty, in which
    *           case the default namespace is used
    * @return the namespace uri, may be <code>null</code> or empty for some
    *         namespaces (default and xml)
    */
   public String getNSUri(String namespaceName)
   {
      if (StringUtils.isBlank(namespaceName))
         namespaceName = "";

      return m_uris.get(namespaceName);
   }

   /**
    * Get the iterator on the prefixes
    * 
    * @return the iterator, never <code>null</code>
    */
   public Iterator<String> getPrefixes()
   {
      return m_allowedNS.keySet().iterator();
   }

   /**
    * Returns the default stylesheet cleanup filter as an xml document.
    * 
    * @return the filter as an xml document
    * @throws IOException upon io error
    * @throws SAXException upon xml parsing errors
    */
   private static Document getDefaultFilterDocument() throws IOException,
         SAXException
   {

      try(ByteArrayInputStream bis = new ByteArrayInputStream(DEFAULT_FILTER_XML.getBytes(StandardCharsets.UTF_8)))
      {
         return PSXmlDocumentBuilder.createXmlDocument(bis, false);
      }
   }

   /**
    * Map to hold pattern rules for all allowed namespaces. The data structure
    * is a map that has an entry for each allowed namespace and the value of
    * each entry is 3 Lists (elements, attributes, namespace declaration values)
    * Namespace devlaration values really doesn't need to be a list as it is
    * only a single value.
    */
   private Map<String, List<String>[]> m_allowedNS = new HashMap<String, List<String>[]>();

   /**
    * A map that associates namespace prefixes with their corresponding uris.
    */
   private Map<String, String> m_uris = new HashMap<String, String>();

   /**
    * The singleton instance of this class, initialized in
    * {@link #getInstance(Element)}, never <code>null</code> after that.
    */
   private static PSStylesheetCleanupFilter ms_instance;

   // List indexes
   private static final int ELEMENTS_INDEX = 0;

   private static final int ATTRIBUTES_INDEX = 1;

   private static final int DECLARATION_INDEX = 2;

   // Xml element constants
   private static final String ELEM_ALLOWED_NAMESPACE = "allowedNamespace";

   private static final String ELEM_ALLOWED_ELEMENT = "allowedElement";

   // Xml attribute constants
   private static final String ELEM_ALLOWED_ATTR = "allowedAttribute";

   private static final String ATTR_NAME = "name";

   private static final String ATTR_URI = "uri";

   private static final String ATTR_DECLARATION_ALLOWED = "declAllowed";

   private static final String ATTR_DECLARATION_VALUE = "declValue";

   /**
    * Default stylesheet cleanup filter. This is used as a fallback if no
    * stylesheetCleanFilter.xml file exists. Which in most cases should never
    * happen.
    */
   private static final String DEFAULT_FILTER_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
         + "<stylesheetCleanupFilter>"
         + "<allowedNamespace name=\"\" declAllowed=\"true \" declValue=\"*xhtml*\">"
         + "<allowedElement name=\"*\"/>"
         + "<allowedAttribute name=\"*\"/>"
         + "</allowedNamespace>"
         + "<allowedNamespace name=\"xml\" uri=\"http://www.w3.org\" declAllowed=\"false \">"
         + "<allowedAttribute name=\"lang\"/>"
         + "<allowedAttribute name=\"space\"/>"
         + "</allowedNamespace>"
         + "</stylesheetCleanupFilter>";

}
