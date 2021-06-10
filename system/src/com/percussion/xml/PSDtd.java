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

package com.percussion.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLContentSpec;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.impl.dtd.models.CMAny;
import org.apache.xerces.impl.dtd.models.CMNode;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * This class represents the DTD object. For usage of this class, see the
 * <code>main</code> method. First create the <code>PSDtdParser</code> object,
 * then call the appropriate <code>parseDtd</code> method of the newly
 * created object. To obtain the <code>PSDtd</code> object representing the
 * parsed DTD call the <code>getDtd</code> method of the DTD parser object.
 */
public class PSDtd extends DTDGrammar
{
   private static final Logger log = LogManager.getLogger(PSDtd.class);
   /**
    * Main method - used for tesing.
    * Usage is :
    * -d (DTD_FILE_PATH)
    * -x (XML_FILE_PATH)
    * For DTD files, it will parse the DTD and print debug information to the
    * console. For XML files, it will first create the DTD and follow the same
    * process as explained for DTD files.
    *
    * @param argv -d|x (DTD_OR_XML_FILE_PATH)
    */
   public static void main(String argv[])
   {
      if (argv.length != 2)
      {
         log.info("Invalid number of arguments.");
         printUsage();
         System.exit(1);
      }

      String cmd = argv[0];
      if (!((cmd.equalsIgnoreCase("-d")) || (cmd.equalsIgnoreCase("-x"))))
      {
         log.info("Invalid first argument.");
         printUsage();
         System.exit(1);
      }

      String filePath = argv[1];
      File file = new File(filePath);
      if (!file.isFile())
      {
         log.info("Invalid file path: {} ", filePath);
         System.exit(1);
      }

      PSDtdParser dtdParser = new PSDtdParser();
      try
      {
         if (cmd.equalsIgnoreCase("-d"))
            dtdParser.parseDtd(filePath);
         else if(cmd.equalsIgnoreCase("-x"))
            dtdParser.parseXmlForDtd(filePath, true);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         System.exit(1);
      }

      PSDtd psdtd = dtdParser.getDtd();
      if (psdtd == null)
      {
         log.info("DTD Parser returned null");
         System.exit(1);
      }
      PSDtd.printDtd(psdtd);
      //
      try
      {
         PSDtdTree tree = new PSDtdTree(psdtd);
         String strDtd = tree.toDTD(false);
         log.info(strDtd);
      }
      catch (Exception e)
      {
         log.error("Error : {} ", e.getMessage());
         log.debug(e.getMessage(),e);
      }
   }

   /**
    * This method is used for debugging. It prints debug information, such as
    * elements and their attributes contained in the DTD, to the console.
    * @param psdtd the parsed DTD object, may not be <code>null</code>
    * @throw IllegalArgumentException if <code>psdtd<code>null</code>
    */
   public static void printDtd(PSDtd psdtd)
   {
      if (psdtd == null)
         throw new IllegalArgumentException("psdtd may not be null");
      String root = psdtd.getName();
      log.info("Root Element : {} ", root);
      Enumeration e = psdtd.getElementDeclarations();
      while (e.hasMoreElements())
      {
         PSXmlElementDecl elemDecl = (PSXmlElementDecl)e.nextElement();
         String elementName = elemDecl.getName();
         log.info("Content Model for : {} : ", elementName);
         log.info(psdtd.getContentModelAsString(elementName));
         String type = null;
         switch (elemDecl.getContentType())
         {
            case PSXmlElementDecl.TYPE_ANY :
               type = "TYPE_ANY";
               break;

            case PSXmlElementDecl.TYPE_CHILDREN :
               type = "TYPE_CHILDREN";
               break;

            case PSXmlElementDecl.TYPE_EMPTY :
               type = "TYPE_EMPTY";
               break;

            case PSXmlElementDecl.TYPE_MIXED :
               type = "TYPE_MIXED";
               break;

            case PSXmlElementDecl.TYPE_SIMPLE :
               type = "TYPE_SIMPLE";
               break;

            default :
               type = "Unknown Type";
               break;
         }
         log.info("");
         log.info("Element : {} ", elementName);
         log.info("Element Type : {} ", type);

         //get the attributes
         Enumeration en = psdtd.getAttributeDeclarations(elementName);
         while (en.hasMoreElements())
         {
            PSXmlAttributeDecl attrDecl = (PSXmlAttributeDecl)en.nextElement();
            String attDeclaredType = null;
            switch (attrDecl.getDeclaredType())
            {
               case PSXmlAttributeDecl.TYPE_CDATA :
                  attDeclaredType = "TYPE_CDATA";
                  break;

               case PSXmlAttributeDecl.TYPE_ENTITY :
                  attDeclaredType = "TYPE_ENTITY";
                  break;

               case PSXmlAttributeDecl.TYPE_ENUMERATION :
                  attDeclaredType = "TYPE_ENUMERATION";
                  break;

               case PSXmlAttributeDecl.TYPE_ID :
                  attDeclaredType = "TYPE_ID";
                  break;

               case PSXmlAttributeDecl.TYPE_IDREF :
                  attDeclaredType = "TYPE_IDREF";
                  break;

               case PSXmlAttributeDecl.TYPE_NMTOKEN :
                  attDeclaredType = "TYPE_NMTOKEN";
                  break;

               case PSXmlAttributeDecl.TYPE_NOTATION :
                  attDeclaredType = "TYPE_NOTATION";
                  break;

               case PSXmlAttributeDecl.TYPE_NAMED :
                  attDeclaredType = "TYPE_NAMED";
                  break;

               case PSXmlAttributeDecl.TYPE_IDREFS :
                  attDeclaredType = "TYPE_IDREFS";
                  break;

               case PSXmlAttributeDecl.TYPE_ENTITIES :
                  attDeclaredType = "TYPE_ENTITIES";
                  break;

               case PSXmlAttributeDecl.TYPE_NMTOKENS :
                  attDeclaredType = "TYPE_NMTOKENS";
                  break;

               case PSXmlAttributeDecl.TYPE_UNKNOWN :
                  attDeclaredType = "TYPE_UNKNOWN";
                  break;

               default :
                  attDeclaredType = "Unknown declared type";
                  break;
            }

            String attDefaultType = null;
            switch (attrDecl.getDefaultType())
            {
               case PSXmlAttributeDecl.DEFAULT_TYPE_DEFAULT :
                  attDefaultType = "DEFAULT_TYPE_DEFAULT";
                  break;

               case PSXmlAttributeDecl.DEFAULT_TYPE_FIXED :
                  attDefaultType = "DEFAULT_TYPE_FIXED";
                  break;

               case PSXmlAttributeDecl.DEFAULT_TYPE_IMPLIED :
                  attDefaultType = "DEFAULT_TYPE_IMPLIED";
                  break;

               case PSXmlAttributeDecl.DEFAULT_TYPE_REQUIRED :
                  attDefaultType = "DEFAULT_TYPE_REQUIRED";
                  break;

               case PSXmlAttributeDecl.DEFAULT_TYPE_NOFIXED :
                  attDefaultType = "DEFAULT_TYPE_NOFIXED";
                  break;

               default :
                  attDefaultType = "Unknown Default Type";
                  break;
            }

            log.info("Attribute : {} ", attrDecl.getName());
            log.info("Attribute Default Type : {} ", attDefaultType);
            log.info("Attribute Declared Type : {}", attDeclaredType);
            log.info("Attribute Default String Value : {} ", attrDecl.getDefaultStringValue());
            log.info("Attribute Size : {}",  attrDecl.size());
            Enumeration attEl = attrDecl.elements();
            int j = 0;
            while (attEl.hasMoreElements())
            {
               String token = (String)attEl.nextElement();
               log.info("Attribute Token [{}] : , {}", j, token);
               j ++;
            }
         }
      }
   }

   protected static void printUsage()
   {
      log.info("usage: java com.percussion.xml.PSDtd -d|x FILE_PATH");
      log.info("options:");
      log.info("  -d DTD_FILE_PATH");
      log.info("  -x XML_FILE_PATH");
   }

   /**
    * Constructor.
    *
    * @param source the location of the entity which forms the starting point
    * of the grammar to be constructed, may not be <code>null</code>
    *
    * @param symbolTable The symbol table to use. May not be <code>null</code>.
    * For details, please see the Xerces javadoc of class
    * org.apache.xerces.util.org.apache.xerces.util
    *
    * @param desc stores information specific to DTD grammars.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>source</code> is
    * <code>null</code>
    */
   public PSDtd(XMLInputSource source, SymbolTable symbolTable,
      XMLDTDDescription desc)
   {
      super(symbolTable, desc);
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      m_source = source;
   }

   /**
    * Return an Enumeration instance of all attribute list declarations for the
    * specified elementName in this DTD's internal and external subsets.
    * @param elementName the Element name to match in the internal and external
    * DTD subsets, may not be <code>null</code> or empty
    * @return an enumeration of all attribute list declarations.
    * @throw IllegalArgumentException if elementName is <code>null</code>
    * or empty
    */
   public Enumeration getAttributeDeclarations(String elementName)
   {
      if ((elementName == null) || (elementName.trim().length() == 0))
         throw new IllegalArgumentException(
            "elementName may not be null or empty");
      Vector attrList = new Vector();
      int elementDeclIndex = getElementDeclIndex(elementName);
      if (elementDeclIndex != -1)
      {
         int attributeDeclIndex = getFirstAttributeDeclIndex(elementDeclIndex);
         while (attributeDeclIndex != -1)
         {
            PSXmlAttributeDecl attributeDecl = new PSXmlAttributeDecl();
            if (getAttributeDecl(attributeDeclIndex, attributeDecl))
               attrList.addElement(attributeDecl);
            attributeDeclIndex = getNextAttributeDeclIndex(
               attributeDeclIndex);
         }
      }
      return attrList.elements();
   }

   /**
    * Returns this DTD's name. This value is also known as the DOCTYPE
    * and the root Element Name.
    *
    * To determine the root of the DTD the following logic is used:
    *
    * 1> If Xerces gives a non-<code>null</code> or non-empty DTD root, return
    * it
    * 2> If the list of possible root elements contains only one element, then
    * return the single element contained in the list.
    * 3> From the list of possible root elements, create a new list of elements
    * which have a valid content model. If there is only one element in this
    * new list return it.
    * 4> In this new list, eliminate all elements which are contained in
    * any other element's content model. If the new list now has only one
    * element, then return it.
    * 5> If any element with same name as the DTD file name exists, then
    * return it as root.
    * 6> Finally just return the first declared element as root.
    *
    * @return This DTD's name, or <code>null</code> if no name.
    */
   public String getName()
   {
      if (m_dtdRoot != null)
         return m_dtdRoot;

      String root = null;
      do
      {
         // see if xerces gives the root element
         XMLGrammarDescription gramDesc = getGrammarDescription();
         if (gramDesc != null)
         {
            if (gramDesc instanceof XMLDTDDescription)
            {
               root = ((XMLDTDDescription)gramDesc).getRootName();
               if (!((root == null) || (root.trim().length() == 0)))
                  break;
            }
         }

         // If only one possible root exists, return it.
         if (m_possibleRootElements.size() == 1)
         {
            root = (String)m_possibleRootElements.get(0);
            break;
         }

         // check if the name of the file matches in which this DTD is defined
         // matches an element declaration, then the matching element
         // will be returned as root if no root can be sucessfully obtained.
         String dtdFileName = null;
         boolean dtdFileNameElementExists = false;
         String systemId = m_source.getSystemId();
         if ((systemId != null) && (systemId.trim().length() > 0))
         {
            File dtdFile = new File(systemId);
            dtdFileName = dtdFile.getName();
            int index = dtdFileName.lastIndexOf(".");
            if (index != -1)
               dtdFileName = dtdFileName.substring(0, index);
         }

         // make a new list of all the elements (obtained from the list of
         // possible roots) which have a valid content model
         List possibleRoots = new ArrayList();
         Iterator it = m_possibleRootElements.iterator();
         while (it.hasNext())
         {
            String elemName = (String)it.next();
            String contentModel = getContentModelAsString(elemName);
            if (contentModel != null)
            {
               possibleRoots.add(elemName);
               if ((dtdFileName != null) && (elemName.equalsIgnoreCase(dtdFileName)))
               {
                  dtdFileName = elemName;
                  dtdFileNameElementExists = true;
               }
            }
         }

         // If only one possible root left, return it.
         if (possibleRoots.size() == 1)
         {
            root = (String)possibleRoots.get(0);
            break;
         }

         // try to get the root by eliminating the elements which are contained
         // in any other element's content model
         List roots = new ArrayList();
         roots.addAll(possibleRoots);
         if (possibleRoots.size() > 0)
         {
            for (int i = 0; i < possibleRoots.size(); i++)
            {
               String contentModel = getContentModelAsString((String)possibleRoots.get(i));
               if (contentModel != null)
               {
                  StringTokenizer st = new StringTokenizer(contentModel, " \t\n\r\f?*+(),");
                  while (st.hasMoreTokens())
                  {
                     String token = st.nextToken();
                     if (roots.contains(token))
                        roots.remove(token);
                  }
               }
            }
         }

         if (roots.size() == 1)
         {
            root = (String)roots.get(0);
            break;
         }

         // failed to get the root of the DTD. if any element with same name as
         // the DTD file name exists, then return it as root.
         if (dtdFileNameElementExists)
         {
            root = dtdFileName;
            break;
         }

         // finally just return the first declared element as root
         int index = getFirstElementDeclIndex();
         if (index != -1)
         {
            PSXmlElementDecl elemDecl = getElementDeclaration(index);
            if (elemDecl != null)
               root = elemDecl.getName();
         }
      }
      while (false);

      m_dtdRoot = root;
      return m_dtdRoot;
   }

   /**
    * Return an Enumeration instance of all element declarations in this
    * DTD's internal and external subsets.
    * @return an enumeration of all element declarations.
    * Never <code>null</code>. May be empty.
    */
   public Enumeration getElementDeclarations()
   {
      Vector elementList = new Vector();
      int index = getFirstElementDeclIndex();
      while (index != -1)
      {
         PSXmlElementDecl elemDecl = getElementDeclaration(index);
         if (elemDecl != null)
            elementList.addElement(elemDecl);
         index = getNextElementDeclIndex(index);
      }
      return elementList.elements();
   }

   /**
    * Return an PSXMLElementDecl instance that matches the specified elementName
    * in this DTD's internal and external subsets.
    * @param elementName the Element name to match in the internal and
    * external DTD subsets, may not be <code>null</code> or empty
    * @return The matching element definition, or <code>null</code> if no match.
    * @throw IllegalArgumentException if elementName is <code>null</code>
    * or empty
    */
   public PSXmlElementDecl getElementDeclaration(String elementName)
   {
      if ((elementName == null) || (elementName.trim().length() == 0))
         throw new IllegalArgumentException(
            "elementName may not be null or empty");
      int elementIndex = getElementDeclIndex(elementName);
      return getElementDeclaration(elementIndex);
   }

   /**
    * Returns the Content Model for the specified elementName in this DTD's
    * internal and external subset.
    * @param elementName the name of element definition to check in this DTD's
    * internal and external subset, may not be <code>null</code> or empty
    * @return the element definition's content model, or <code>null</code>
    * if the element definition does not exist.
    * @throw IllegalArgumentException if elementName is <code>null</code>
    * or empty
    */
   public CMNode getContentModelNode(String elementName)
   {
      if ((elementName == null) || (elementName.trim().length() == 0))
         throw new IllegalArgumentException(
            "elementName may not be null or empty");

      int elementDeclIndex = getElementDeclIndex(elementName);
      int contentSpecIndex = -1;
      Object oContentSpecIndex = m_contentSpecIndexList.get(
         new Integer(elementDeclIndex));
      if (oContentSpecIndex != null)
         contentSpecIndex = ((Integer)oContentSpecIndex).intValue();
      if (contentSpecIndex != -1)
      {
         XMLContentSpec contentSpec = new XMLContentSpec();
         return buildSyntaxTree(contentSpecIndex, contentSpec);
      }
      return null;
   }

   /**
    * Returns the content model as a string.
    * @param elementName the element whose content model is to be obtained,
    * may not be <code>null</code> or empty
    * @return the content model as a string, may be<code>null</code> if the
    * element declaration does not exist in the DTD.
    * Never empty, if not <code>null</code>.
    * @throw IllegalArgumentException if elementName is <code>null</code>
    * or empty.
    */
   public String getContentModelAsString(String elementName)
   {
      if ((elementName == null) || (elementName.trim().length() == 0))
         throw new IllegalArgumentException(
            "elementName may not be null or empty");
      int elementDeclIndex = getElementDeclIndex(elementName);
      if (elementDeclIndex != -1)
         return getContentSpecAsString(elementDeclIndex);
      return null;
   }

   /**
    * Returns an PSXMLElementDecl instance that is at the element index of
    * element declarations array
    * @param elementIndex the element index of element declarations array
    * @return The matching element definition, or <code>null</code> if
    * elementIndex is equal to -1 or exceeds the size of element declarations
    * array.
    */
   protected PSXmlElementDecl getElementDeclaration(int elementDeclIndex)
   {
      if (elementDeclIndex == -1)
         return null;
      PSXmlElementDecl elemDecl = new PSXmlElementDecl();
      if (getElementDecl(elementDeclIndex, elemDecl))
         return elemDecl;
      return null;
   }

   /**
    * Returns the Content Model node for the <code>Element<code> at the
    * specified index. The index can be obtained from
    * <code>m_contentSpecIndexList</code> member variable.
    * @param contentSpecIndex The index of the element in the <code>Map</code>
    * represented by the <code>m_contentSpecIndexList</code> member variable.
    * @return the content model for the <code>Element</code>
    */
   protected CMNode buildSyntaxTree(int contentSpecIndex,
      XMLContentSpec contentSpec)
   {
      // We will build a node at this level for the new tree
      CMNode nodeRet = null;
      if (!(getContentSpec(contentSpecIndex, contentSpec)))
         return null;
      // do a binary AND with 0x0f to obtain the type of node
      if ((contentSpec.type & 0x0f) == XMLContentSpec.CONTENTSPECNODE_ANY)
      {
         nodeRet = new CMAny(contentSpec.type,
            (String)contentSpec.otherValue,
            m_leafCount++);
      }
      else if ((contentSpec.type & 0x0f) ==
         XMLContentSpec.CONTENTSPECNODE_ANY_OTHER)
      {
         nodeRet = new CMAny(contentSpec.type,
            (String)contentSpec.otherValue,
            m_leafCount++);
      }
      else if ((contentSpec.type & 0x0f) ==
         XMLContentSpec.CONTENTSPECNODE_ANY_LOCAL)
      {
         nodeRet = new CMAny(contentSpec.type, null, m_leafCount++);
      }
      //
      //  If this node is a leaf, then its an easy one. We just add it
      //  to the tree.
      //
      else if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_LEAF)
      {
         //
         //  Create a new leaf node, and pass it the current leaf count,
         //  which is its DFA state position. Bump the leaf count after
         //  storing it. This makes the positions zero based since we
         //  store first and then increment.
         //
         m_qName.setValues(null, (String)contentSpec.value,
            (String)contentSpec.value, (String)contentSpec.otherValue);
         nodeRet = new PSCMLeaf(m_qName, m_leafCount++);
      }
      else
      {
         //
         //  Its not a leaf, so we have to recurse its left and maybe right
         //  nodes. Save both values before we recurse and trash the node.
         final int leftNode = ((int[])contentSpec.value)[0];
         final int rightNode = ((int[])contentSpec.otherValue)[0];

         if ((contentSpec.type == XMLContentSpec.CONTENTSPECNODE_CHOICE)
            ||  (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_SEQ))
         {
            //
            //  Recurse on both children, and return a binary op node
            //  with the two created sub nodes as its children. The node
            //  type is the same type as the source.
            //
            nodeRet = new PSCM2op(contentSpec.type,
                                    buildSyntaxTree(leftNode, contentSpec),
                                 buildSyntaxTree(rightNode, contentSpec));
         }
         else if (contentSpec.type ==
            XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE)
         {
            nodeRet = new PSCM1op( contentSpec.type, buildSyntaxTree(leftNode, contentSpec));
         }
         else if ((contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE)
            || (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE)
            || (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE))
         {
            nodeRet = new PSCM1op(contentSpec.type, buildSyntaxTree(leftNode, contentSpec));
         }
         else
         {
            throw new RuntimeException("ImplementationMessages.VAL_CST");
         }
      }
      // And return our new node for this level
      return nodeRet;
   }

   /**
    * Stores the content spec index corresponding to each element in a
    * <code>Map</code>
    * @param elementDeclIndex the index of the Element
    * @param contentSpecIndex the content spec index corresponding to the element
    */
   protected void setContentSpecIndex(int elementDeclIndex,
      int contentSpecIndex)
   {
      super.setContentSpecIndex(elementDeclIndex, contentSpecIndex);
      m_contentSpecIndexList.put(new Integer(elementDeclIndex),
         new Integer(contentSpecIndex));
   }

   /**
    * This method is overriden so that we ignore any element declared in
    * parameter entities.
    *
    * @see org.apache.xerces.impl.dtd.DTDGrammar
    */
    public void startParameterEntity(String name,
      XMLResourceIdentifier identifier, String encoding, Augmentations augs)
      throws XNIException
   {
      m_bAddPossibleRootElement = false;
      super.startParameterEntity(name, identifier, encoding, augs);
   }

   /**
    * This method is overriden so that we ignore any element declared in
    * parameter entities.
    *
    * @see org.apache.xerces.impl.dtd.DTDGrammar
    */
   public void endParameterEntity(String name, Augmentations augs)
      throws XNIException
   {
      m_bAddPossibleRootElement = true;
      super.endParameterEntity(name, augs);
   }

   /**
    * This method is overriden so that we can add element declarations
    * to the list of possible root elements. This is a callback method and
    * name is guaranteed to be non-<code>null</code> and non-empty
    *
    * @see org.apache.xerces.impl.dtd.DTDGrammar
    */
   public void elementDecl(String name, String contentModel, Augmentations augs)
      throws XNIException
   {
      if (m_bAddPossibleRootElement)
         m_possibleRootElements.add(name);

      super.elementDecl(name, contentModel, augs);
   }

   /**
    * If <code>true</code> any element declaration is added to the list of
    * possible root elements <code>m_possibleRootElements</code>, otherwise
    * the element is not added to this list. Initialized to <code>true</code>.
    */
   private boolean m_bAddPossibleRootElement = true;

   /**
    * List of possible root elements as <code>String</code> objects,
    * never <code>null</code>, may be empty.
    */
   private List m_possibleRootElements = new ArrayList();

   /**
    * used in the buildSyntaxTree method.
    */
   protected int m_leafCount = 0;

   /**
    * Temporary qualified name of an element. Used in the buildSyntaxTree method.
    */
    protected QName m_qName = new QName();

   /**
    * Map that stores the content spec index of an element corresponding to the
    * element's index.
    */
    protected Map m_contentSpecIndexList = new HashMap();

    /**
     * the location of the entity which forms the starting point of the grammar
     * to be constructed, never <code>null</code>, initialized in the
     * constructor, never modified after that.
     */
    private XMLInputSource m_source = null;

    /**
     * The root of the DTD represented by this object, initialized to
     * <code>null</code>, set in the first call to the <code>getName()</code>
     * method, never modified after that.
     */
    private String m_dtdRoot = null;

}
