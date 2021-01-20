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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the metadata for a content editor control, as defined by
 * the &lt;psxctl:ControlMeta&gt; node in <code>sys_LibraryControlDef.dtd</code>
 */
public class PSControlMeta extends PSComponent
{
   /**
    * Initializes a newly created <code>PSControlMeta</code> object, from
    * an XML representation.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode the XML element node to construct this object from.
    *    Cannot be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format
    */
   public PSControlMeta(Element sourceNode)
         throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode cannot be null.");
      fromXml(sourceNode, null, null);
   }


   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode element with name specified by {@link #XML_NODE_NAME}
    * @param parentDoc ignored.
    * @param parentComponents ignored.
    * @throws PSUnknownNodeTypeException  if an expected XML element is missing,
    *    or <code>null</code>
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      validateElementName(sourceNode, XML_NODE_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      m_name = getRequiredElement(tree, XML_NAME_ATTR);

      m_deprecated = getEnumeratedAttribute(
         tree, XML_DEPRECATE_ATTR, XML_DEPRECATE_ENUM);

      m_deprecatedReplacement = getImpliedAttribute(tree, XML_REPLACEWITH_ATTR);

      setDisplayName(tree.getElementData(XML_DISPLAYNAME_ATTR));
      m_dimension = getEnumeratedAttribute(tree, XML_DIMENSION_ATTR,
            XML_DIMENSION_ENUM);
      m_choiceSet = getEnumeratedAttribute(tree, XML_CHOICESET_ATTR,
            XML_CHOICESET_ENUM);
      m_params.clear();
      m_dependencies.clear();
      m_description = "";

      // move on to the children
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      Element child = tree.getNextElement(firstFlags);
      while (child != null)
      {
         String childName = child.getTagName();

         if (childName.equals(XELEM_DESCRIPTION))
            setDescription(tree.getElementData(child));
         else if (childName.equals(XELEM_PARAMLIST))
         {
            PSXmlTreeWalker paramTree = new PSXmlTreeWalker(child);
            final String paramNodeName = PSControlParameter.XML_NODE_NAME;
            for (Element el = paramTree.getNextElement(paramNodeName, firstFlags);
                 null != el;
                 el = paramTree.getNextElement(paramNodeName, nextFlags))
            {
               m_params.add(new PSControlParameter(el));
            }
         }
         else if (childName.equals(XELEM_DEPENDENCIES))
         {
            PSXmlTreeWalker paramTree = new PSXmlTreeWalker(child);
            final String dependencyNode = PSDependency.XML_NODE_NAME;
            for (Element el = paramTree.getNextElement(dependencyNode, firstFlags);
                 null != el;
                 el = paramTree.getNextElement(dependencyNode, nextFlags))
            {
               m_dependencies.add(new PSDependency(el));
            }

         }
         else if (childName.equals(XELEM_FILES))
         {
            PSXmlTreeWalker filesTree = new PSXmlTreeWalker(child);
            final String fileNode = PSFileDescriptor.XML_NODE_NAME;
            for (Element el = filesTree.getNextElement(fileNode, firstFlags);
                 null != el;
                 el = filesTree.getNextElement(fileNode, nextFlags))
            {
               PSFileDescriptor fileDesc = new PSFileDescriptor(el);
               m_files.add(fileDesc);
            }

         }

         child = tree.getNextElement(nextFlags);
      }
   }

   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * <pre><code>
    * &lt;!ELEMENT psxctl:ControlMeta (psxctl:Description?, psxctl:ParamList?,
    *    psxctl:AssociatedFileList?, psxctl:Dependencies?)>
    * &lt;!ATTLIST psxctl:ControlMeta
    *    name CDATA #REQUIRED
    *    displayName CDATA #IMPLIED
    *    dimension (single | array | table) "single"
    *    choiceset (none|required|optional) "none"
    * >
    * &lt;!ELEMENT psxctl:Description (#PCDATA)>
    * &lt;!ELEMENT psxctl:ParamList (psxctl:Param+)>
    * &lt;!ELEMENT psxctl:AssociatedFileList (psxctl:FileDescriptor+)>
    * &lt;!ELEMENT psxctl:Dependencies (psxctl:Dependency+)>
    * </code></pre>
    *
    * @param doc The XML document being constructed, needed to create new
    *    elements.  Cannot be <code>null</code>.
    * @return    the newly created XML element node
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("Must provide a valid Document");
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_NAME_ATTR, m_name);
      if (m_displayName.length() > 0)
         root.setAttribute(XML_DISPLAYNAME_ATTR, m_displayName);
      root.setAttribute(XML_DIMENSION_ATTR, m_dimension);
      root.setAttribute(XML_CHOICESET_ATTR, m_choiceSet);
      if (m_description.length() > 0)
         PSXmlDocumentBuilder.addElement(doc, root, XELEM_DESCRIPTION,
               m_description);

      // psxctl:ParamList
      if (m_params.size() > 0)
      {
         Element paramList = doc.createElement( XELEM_PARAMLIST );
         for (Iterator iter = m_params.iterator(); iter.hasNext();)
         {
            PSControlParameter param = (PSControlParameter) iter.next();
            paramList.appendChild(param.toXml(doc));
         }
         root.appendChild(paramList);
      }

      // psxctl:AssociatedFileList
      if (m_files.size() > 0)
      {
         Element files = doc.createElement( XELEM_FILES );
         for (Iterator iter = m_files.iterator(); iter.hasNext();)
         {
            PSFileDescriptor file = (PSFileDescriptor) iter.next();
            files.appendChild( file.toXml( doc ) );
         }
         root.appendChild(files);
      }

      // psxctl:Dependencies
      if (m_dependencies.size() > 0)
      {
         Element dependencies = doc.createElement( XELEM_DEPENDENCIES );
         for (Iterator iter = m_dependencies.iterator(); iter.hasNext();)
         {
            PSDependency dependency = (PSDependency) iter.next();
            dependencies.appendChild( dependency.toXml( doc ) );
         }
         root.appendChild(dependencies);
      }

      return root;
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component.
    *
    * @param source object to be shallow copied; may not be <code>null</code>
    */
   public void copyFrom(PSComponent source)
   {
      if (source instanceof PSControlMeta)
      {
         PSControlMeta control = (PSControlMeta) source;
         try
         {
            super.copyFrom( source );
         } catch (IllegalArgumentException e) { } // cannot happen
         m_name = control.m_name;
         m_choiceSet = control.m_choiceSet;
         m_description = control.m_description;
         m_dependencies = control.m_dependencies;
         m_dimension = control.m_dimension;
         m_displayName = control.m_displayName;
         m_params = control.m_params;
         m_files = control.m_files;
      }
      else
         throw new IllegalArgumentException( "INVALID_OBJECT_FOR_COPY" );
   }


   /**
    * @return  a string representation of the object, the display name (if set)
    *          or the internal name
    */
   public String toString()
   {
      if (m_displayName.length() > 0)
         return m_displayName;
      else
         return m_name;
   }

   /**
    * Returns <code> if the controls is deprecated, otherwise returns <code>
    * false</code>.
    * <p />
    * @return <code>true</code> if the control is deprecated otherwise <code>
    * false</code>
    */
   public boolean isDeprecated()
   {
      if (m_deprecated.equals("yes"))
         return true;

      return false;
   }

   /**
    * If the control is deprecated, it may have a name of the control to use in
    * its place.  This will return the name of the control to use instead of
    * the deprecated control.
    * <p />
    * @return the name of the control that will replace the deprecated control
    * may return <code>null</code> or empty.
    */
   public String getDeprecatedReplacementName()
   {
      return m_deprecatedReplacement;
   }

   /**
    * @return the internal name of this control; never empty or <code>null
    *         </code>
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * @return The display name of this control; may be empty, never <code>null
    *         </code>
    */
   public String getDisplayName()
   {
      return m_displayName;
   }


   /**
    * @return A description of the type of data this control takes.
    *         Never empty or <code>null</code>
    */
   public String getDimension()
   {
      return m_dimension;
   }


   /**
    * @return A hint as to whether this control uses a set of choices.
    *         Never empty or <code>null</code>
    */
   public String getChoiceSet()
   {
      return m_choiceSet;
   }


   /**
    * @return A full description of the behavior and parameters for this
    *         control. May be empty, never <code>null</code>
    */
   public String getDescription()
   {
      return m_description;
   }


   /**
    * @return A list of zero-or-more <code>PSControlParameter</code>
    * objects which enumerates the parameters supported by this control;
    * never <code>null</code>.
    */
   public List getParams()
   {
      return m_params;
   }


   /**
    * @return A list of zero-or-more <code>PSDependency</code>
    * objects which enumerates the dependencies required by this control;
    * never <code>null</code>.
    */
   public List getDependencies()
   {
      return m_dependencies;
   }

   /**
    * Get the list of associated files.
    *
    * @return A list of zero-or-more <code>PSFileDescriptor</code>
    * objects, never <code>null</code>.
    */
   public List getAssociatedFiles()
   {
      return m_files;
   }


   /**
    * Sets the description member variable, enforcing the assertion that this
    * variable will never be <code>null</code>.
    *
    * @param description The value to assign. If <code>null</code>, the empty
    *        string is used instead.
    */
   private void setDescription(String description)
   {
      if (null == description)
         m_description = "";
      else
         m_description = description;
   }


   /**
    * Sets the display name member variable, enforcing the assertion that this
    * variable will never be <code>null</code>.
    *
    * @param displayName The value to assign. If <code>null</code>, the empty
    *        string is used instead.
    */
   private void setDisplayName(String displayName)
   {
      if (null == displayName)
         m_displayName = "";
      else
         m_displayName = displayName;
   }


   /**
    * Name of parent XML element
    */
   public static final String XML_NODE_NAME         = "psxctl:ControlMeta";

   /**
    * Name of the XML attribute that contains the name
    */
   private static final String XML_NAME_ATTR        = "name";

   /**
    * Name of the XML attribute that contains the dimension
    */
   private static final String XML_DIMENSION_ATTR   = "dimension";

   /**
    * Name of the XML attribute that contains the choiceset
    */
   private static final String XML_CHOICESET_ATTR   = "choiceset";

   /**
    * Name of the XML attribute that contains the display name
    */
   private static final String XML_DISPLAYNAME_ATTR = "displayName";

   /**
    * Name of the XML attribute that contains the deprecate value
    */
   private static final String XML_DEPRECATE_ATTR = "deprecate";

   /**
    * Name of the XML attribute that contains the replacewith value
    */
   private static final String XML_REPLACEWITH_ATTR = "replacewith";

   /**
    * Name of child XML element that contains the description
    */
   private static final String XELEM_DESCRIPTION = "psxctl:Description";

   /**
    * Name of child XML element that contains the parameter list
    */
   private static final String XELEM_PARAMLIST = "psxctl:ParamList";

   /**
    * Name of child XML element that contains the dependency list
    */
   private static final String XELEM_DEPENDENCIES = "psxctl:Dependencies";

   /**
    * Name of child XML element that contains the associated file list
    */
   private static final String XELEM_FILES = "psxctl:AssociatedFileList";

   /**
    * An array of legal values for the dimension XML attribute.  The value at
    * index 0 is the default.
    */
   private static final String[] XML_DIMENSION_ENUM = {
      "single", "array", "table"
   };

   /**
    * An array of legal values for the deprecate XML attribute.  The value at
    * index 0 is the default.
    */
   private static final String[] XML_DEPRECATE_ENUM = {
      "no", "yes"
   };

   /** Constant that indicates this control may not define a choice set */
   public static final String CHOICES_NONE = "none";

   /** Constant that indicates this control may define a choice set */
   public static final String CHOICES_OPTIONAL = "optional";

   /** Constant that indicates this control must define a choice set */
   public static final String CHOICES_REQUIRED = "required";

   /**
    * An array of legal values for the choiceset XML attribute.  The value at
    * index 0 is the default.
    */
   private static final String[] XML_CHOICESET_ENUM = {
      CHOICES_NONE, CHOICES_REQUIRED, CHOICES_OPTIONAL
   };

   /** Constant for controls that expect two-dimensional data */
   public final static String TABLE_DIMENSION = "table";

   /** Constant for controls that expect one-dimensional data */
   public final static String ARRAY_DIMENSION = "array";

   /** Constant for controls that expect a single value as data */
   public final static String SINGLE_DIMENSION = "single";

   /**
    * Internal name of this control. Never <code>null</code> or empty after
    * construction.
    */
   private String m_name;

   /**
    * Value of the deprecate attribute. May be <code>null</code> or empty after
    * construction.
    */
   private String m_deprecated;

   /**
    * Value of the replacewith attribute. May be <code>null</code> or empty after
    * construction.
    */
   private String m_deprecatedReplacement;

   /**
    * Display name of this control. Never <code>null</code> after construction.
    */
   private String m_displayName;

   /**
    * A description of the type of data this control takes. Never <code>null
    * </code> after construction.  See {@link #XML_DIMENSION_ENUM} for a list
    * of legal values.
    */
   private String m_dimension;

   /**
    * A hint as to whether this control uses a set of choices. Never <code>null
    * </code> after construction.  See {@link #XML_CHOICESET_ENUM} for a list
    * of legal values.
    */
   private String m_choiceSet;

   /**
    * A full description of the behavior and parameters for this control.
    * Never <code>null</code>.
    */
   private String m_description  = "";

   /**
    * Contains any parameters this control supports as
    * <code>PSControlParameter</code> objects.  May be empty.
    */
   private List m_params = new ArrayList();

   /**
    * Contains any dependencies defined for this control, as <code>
    * PSDependency</code> objects.  May be empty.
    */
   private List m_dependencies = new ArrayList();

   /**
    * Contains any associated files defined for this control, as <code>
    * PSFileDescriptor</code> objects.  Never <code>null</code>, may be empty.
    */
   private List m_files = new ArrayList();

}

