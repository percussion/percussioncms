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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * The PSDtdNode interface is extended by classes defining
 * a node in an internal DTD tree.
 *
 * @see        PSDtdElementEntry
 * @see        PSDtdNodeList
 * @see        PSDtdDataElement
 * @see         PSDtdElement
 * @see         PSDtdTree
 *
 * @author     David Gennaco
 * @version    1.0
 * @since      1.0
 */
public class PSDtdNode implements Serializable {

   private static final Logger log = LogManager.getLogger(PSDtdNode.class);

   /**
    *   Default Constructor.
    *
    */
   PSDtdNode()
   {
      m_occurrenceType = OCCURS_ONCE;
   }

   /**
    *   Construct with occurrences.
    *
    */
   PSDtdNode(int occurrence)
   {
      m_occurrenceType = occurrence;
   }


   /**
    *   Return the parent.
    *
    *   @return            The parent node, null if this is the root
    */
   public PSDtdNode getParentNode()
   {
      return m_parent;
   }

   /**
    *   Return the parent element.
    *
    *   @return            The closest parent element up the tree
    */
   public PSDtdElementEntry getParentElement()
   {
      PSDtdNode node = m_parent;

      while ((node != null) && !(node instanceof PSDtdElementEntry))
      {
         node = node.getParentNode();
      }

      return (PSDtdElementEntry) node;
   }

   /**
    *   Return the occurences setting for this node.
    *
    *   @return   <code>OCCURS_ONCE</code> if the node is required;
    *               <code>OCCURS_OPTIONAL</code> if the node is optional; 
    *               <code>OCCURS_ANY</code> if the node can occur 0 or more times; 
    *               <code>OCCURS_ATLEASTONCE</code> if the node can occur 1 or more times; 
    */
   public int getOccurrenceType()
   {
      return m_occurrenceType;
   }

   /**
    *   Set the occurrence setting for this node.
    *
    *   @param     occurrence_type
    *               <code>OCCURS_ONCE</code> if the node is required;
    *               <code>OCCURS_OPTIONAL</code> if the node is optional; 
    *               <code>OCCURS_ANY</code> if the node can occur 0 or more times; 
    *               <code>OCCURS_ATLEASTONCE</code> if the node can occur 1 or more times; 
    */
   public void setOccurrences(int occurrence_type)
   {
      if ((occurrence_type < MIN_OCCURRENCE) || (occurrence_type > MAX_OCCURRENCE))
         throw new IllegalArgumentException("occurrence_type must be between " 
         + MIN_OCCURRENCE + " and " + MAX_OCCURRENCE);
      else
         m_occurrenceType = occurrence_type;
   }
   
   /**
    *   Is this node the root node?
    *
    *      @return                <code>true</code>
    *                              <code>false</code>
    */
   public boolean isRoot()
   {
      return (m_parent == null);
   }
   
   /**
    *  Get the DTD character associated with this occurrence setting
    *
    *      @return   The character used in the DTD, 'U' for unknown, ' ' for once
    */
   public static char getOccurrenceCharacter(int occurrenceType)
   {
      switch (occurrenceType)
      {
         case OCCURS_ONCE:
            return ' ';
         case OCCURS_OPTIONAL:
            return '?';
         case OCCURS_ANY:
            return '*';
         case OCCURS_ATLEASTONCE:
            return '+';
      }

      return 'U';
   }

   /**
    *   Set the parent
    *
    *      @param      node      The parent node
    */
   public void setParent(PSDtdNode node)
   {
      m_parent = node;
   }

   /**
    *   print is defined for internal debugging, and should be
    *      overridden by classes extending this class
    */
   public void print(String tab)
   {
      log.info(tab + "Undefined node");
   }

   /**
    * Add this node to the catalog list.
    *
    * This function should be overridden for all extended classes.
    *
    *   @param   stack         the recursion detection stack
    *
    *   @param   catalogList   the catalog list being built
    *
    *   @param   cur         the current name to expand on
    *
    *   @param   sep         the element separator string
    *
    *   @param   attribId      the string used to identify an attribute entry
    *
    */
   public void catalog(HashMap stack, List catalogList, String cur,
                     String sep, String attribId)
   {
      return;
   }

   public Object acceptVisitor(PSDtdTreeVisitor visitor, Object data)
   {
      return visitor.visit(this, data);
   }

   public Object childrenAccept(PSDtdTreeVisitor visitor, Object data)
   {
      // default implementation is a no-op...this function is
      // overridden by PSDtdNodeList because it actually has children
      return null;
   }

   /**
    *
    *   Get the maximum merged occurrence setting for the two 
    *      supplied occurrence types, as defined by the following tables.
    *
    * <TABLE BORDER="1">
    * <TR><TH></TH><TH COLSPAN="5" ALIGN="CENTER">Sequence List</TH></TR>
    * <TR><TH ROWSPAN="5" VALIGN="CENTER">Node</TH>
    *     <TH>    </TH><TH>OPT</TH><TH>REQ</TH><TH>ANY</TH><TH>1+ </TH></TR>
    * <TR><TH>OPT </TH><TH>OPT</TH><TH>OPT</TH><TH>ANY</TH><TH>ANY</TH></TR>
    * <TR><TH>REQ </TH><TH>OPT</TH><TH>REQ</TH><TH>ANY</TH><TH>1+ </TH></TR>
    * <TR><TH>ANY </TH><TH>ANY</TH><TH>ANY</TH><TH>ANY</TH><TH>ANY</TH></TR>
    * <TR><TH>1+  </TH><TH>ANY</TH><TH>1+ </TH><TH>ANY</TH><TH>1+ </TH></TR>
    * </TABLE>
    * <P>
    * <TABLE BORDER="1">
    * <TR><TH></TH><TH COLSPAN="5" ALIGN="CENTER">Option List</TH></TR>
    * <TR><TH ROWSPAN="5" VALIGN="CENTER">Node</TH>
    *     <TH>    </TH><TH>OPT</TH><TH>REQ</TH><TH>ANY</TH><TH>1+ </TH></TR>
    * <TR><TH>OPT </TH><TH>OPT</TH><TH>OPT</TH><TH>ANY</TH><TH>ANY</TH></TR>
    * <TR><TH>REQ </TH><TH>OPT</TH><TH>OPT</TH><TH>ANY</TH><TH>ANY</TH></TR>
    * <TR><TH>ANY </TH><TH>ANY</TH><TH>ANY</TH><TH>ANY</TH><TH>ANY</TH></TR>
    * <TR><TH>1+  </TH><TH>ANY</TH><TH>ANY</TH><TH>ANY</TH><TH>ANY</TH></TR>
    * </TABLE>
    */
   protected int getMaxMergedOccurrenceSetting(int occurrenceType)
   {
      if (occurrenceType == OCCURS_UNKNOWN || occurrenceType == OCCURS_ANY)
         return occurrenceType;

      int myOccurrence = m_occurrenceType;

      if (myOccurrence == OCCURS_UNKNOWN || myOccurrence == OCCURS_ANY)
         return myOccurrence;

      if (myOccurrence != occurrenceType) {

         if ((this instanceof PSDtdNodeList) && (((PSDtdNodeList) this).getType() == PSDtdNodeList.OPTIONLIST))
         {
            if (occurrenceType == OCCURS_ATLEASTONCE)
               return OCCURS_ANY;
            else if (myOccurrence == OCCURS_ATLEASTONCE)
               return OCCURS_ANY;
         }

         /* Merge myOccurrence (modified for optionlist) and occurrenceType */
         if (occurrenceType == OCCURS_ATLEASTONCE) {
            if (myOccurrence == OCCURS_OPTIONAL)
               return OCCURS_ANY;
            else
               occurrenceType = OCCURS_ATLEASTONCE;
         }
         else if (myOccurrence == OCCURS_ATLEASTONCE) {
            if (occurrenceType == OCCURS_OPTIONAL)
               return OCCURS_ANY;
            else
               occurrenceType = OCCURS_ATLEASTONCE;
         } else {
            occurrenceType = OCCURS_OPTIONAL;
         }
      }

      PSDtdNode parent = getParentNode();      

      if ((parent instanceof PSDtdElementEntry) || (parent == null))
         return occurrenceType;

      return parent.getMaxMergedOccurrenceSetting(occurrenceType);
   }

   PSDtdNode m_parent;

   int m_occurrenceType;

   static final public int MIN_OCCURRENCE         = 0;
   static final public int OCCURS_ONCE            = 0;  /* default - required */
   static final public int OCCURS_OPTIONAL      = 1;  /* 0 or 1 */
   static final public int OCCURS_ANY            = 3;  /* 0 or more */
   static final public int OCCURS_ATLEASTONCE    = 2;    /* 1 or more */
   static final public int MAX_OCCURRENCE         = 3;
   static final public int OCCURS_UNKNOWN         = 4;
}

