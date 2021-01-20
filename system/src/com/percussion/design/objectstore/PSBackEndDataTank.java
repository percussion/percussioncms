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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Hashtable;
import java.util.Objects;


/**
 * The PSBackEndDataTank class is used to define the back-end data stores
 * used in a PSPipe object. When multiple back-end tables are permitted,
 * the back-end data tank is also used to define the relationships (joins)
 * between the tables.
 *
 * @see PSPipe
 * @see PSPipe#getBackEndDataTank
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSBackEndDataTank extends PSComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSBackEndDataTank(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a back-end data tank object. The object can be used to
    * allow multiple tables (which must be joined for query).
    */
   public PSBackEndDataTank()
   {
      super();
      m_tables = new PSCollection(
         com.percussion.design.objectstore.PSBackEndTable.class);
      m_joins = new PSCollection(
         com.percussion.design.objectstore.PSBackEndJoin.class);
   }

   /**
    * Get the back-end tables associated with this data tank.
    *
    * @return      a collection containing the back-end tables
    *             (PSBackEndTable objects)
    *
    * @see         PSBackEndTable
    */
   public com.percussion.util.PSCollection getTables()
   {
      return m_tables;
   }

   /**
    *this routine compares the passed char against an series of posible matches
    *
    *@param ch the <code> char </code> to match
    *
    *@param scMatchingSet the <code> String containg the posible matches
    *
    *@return true if ch is on the set
    *
    */
   private boolean isInSet(char ch,String scMatchingSet)
   {
      boolean bFound=false;
      int length=scMatchingSet.length();
      if( length > 0 )
      {
         char pChar[]=scMatchingSet.toCharArray();
         //scan all the matchs
         for(int nLoop=0;nLoop<length; nLoop++)
         {
            if( ch  == pChar[nLoop] )
            {
               bFound=true;
               break;
            }
         }
      }
      return(bFound);
   }

   /**
   *Scan a string ( from the end towards the begining ) for posible matches
   *
   *@param str the <code> String </code> to be searched
   *
   *@param scMatchingSet the <code> String </code> containing the matching set
   *
   *@return -1 not match was found, else the position of the first char on str
   * that matches
   */
   private int findChars(String str,String scMatchingSet)
   {
      int iRet=-1;

      int strLength=str.length();
      // is not an empty string ?
      if( strLength > 0 )
      {
         // get the chars from str
         char pStr[]=str.toCharArray();
         // is the last char in the set
         if( isInSet(pStr[strLength-1],scMatchingSet) == true )
         {
            // scan it
            for(int index=strLength-2; index >=0; index-- )
            {
               // did found at char that do not matches
               if( isInSet(pStr[index],scMatchingSet) == false )
               {
                  iRet=index+1; // set the position
                  break; // and break the loop
               }
            }
         }
      }
      return(iRet);
   }

   /**
    * Overwrite the back-end tables with the specified collection.
    * If you only want to modify certain back-end table, add a new one, etc.
    * use getTables to get the existing collection and modify the
    * returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSBackEndDataTank object. Any subsequent changes made to the object
    * by the caller will also effect the data tank.
    *
    * @param tables   the new back-end tables
    *
    * @see            #getTables
    *
    * @see            PSBackEndTable
    */
   public void setTables(com.percussion.util.PSCollection tables)
   {
      if ((tables == null) || (tables.size() == 0))
         throw new IllegalArgumentException("back-end databank tables empty");

      /* is this the right collection type */
      if (!com.percussion.design.objectstore.PSBackEndTable.class.isAssignableFrom(
         tables.getMemberClassType())) {
         throw new IllegalArgumentException("coll bad content type: Back-end Table "
            + tables.getMemberClassName());
      }

      /* check for duplicate table aliases
       *
       * NOTE: back-end tables can be duplicated as long as the alias
       * is different. This is permitted for complex joins.
       */
      int size;
    // check if we have 1+ tables
      if ( (size = tables.size()) > 1)
    {
         Hashtable tableHash = new Hashtable();
         PSBackEndTable table;


      // the number to be appended to the alias
        int iTableAliasRepeatIndex=0;

      // walk trough all the tables
      for (int i = 0; i < size; i++)
      {
         // get the table
             table = (PSBackEndTable)tables.get(i);
         // get the alias
          String tableAlias=table.getAlias();
          // add to the hash
             if (tableHash.put(tableAlias, "") != null)
         {
            // if we are here means that we found two tables with the same alias

            // the new alias index
            String indexString=new String();

            // the starting alias number ( always start at 1)
            iTableAliasRepeatIndex=1;

            // controls the loop
            boolean bFound=true;
            while( bFound )
            {
                 // set the value
                 // make an integer
                 Integer val=new Integer(iTableAliasRepeatIndex);
                 // get the table alias
                 String newAlias=new String(tableAlias);
                 // get the string representation
                 indexString=val.toString();
                 // make a tmp alias
                 newAlias+=indexString;

                 // control flag set to false
                 bFound=false;

                 // scan all the tables searching for a table with the same
                 // alias
                 for (int count = 0; count < size; count++)
                 {

                    // get the table
                        PSBackEndTable scanedTable= (PSBackEndTable)tables.get(count);
                    // get this table alias
                    String oldAlias=scanedTable.getAlias();
                    // this alias matches an existing alias?
                     if( oldAlias.equals(newAlias) )
                    {
                        //yes find the number
                        int  startPos=findChars(oldAlias,"1234567890");
                        if( startPos != -1 )
                        {
                           // get the value
                           String value=oldAlias.substring(startPos,oldAlias.length());

                           // make an integer of it
                           val=new Integer(value);
                           // get the integer rep
                           iTableAliasRepeatIndex=val.intValue();

                           // increment it
                           iTableAliasRepeatIndex++;

                           // convert it back to string
                           val=new Integer(iTableAliasRepeatIndex);
                           indexString=val.toString();  // here is done
                           bFound=true;   // continue the loop
                           break;  // break the for loop
                       }
                    } // if( oldAlias.equals(newAlias) )
                 } //  for (int count = 0; count < size; count++)
                 // if no alias were found
             }//    while( bFound )
             // make the new alias
             tableAlias+=indexString;
             table.setAlias(tableAlias);
             tableHash.put(tableAlias,"");
         } //        if (tableHash.put(name, "") != null)
      } //    for (int i = 0; i < size; i++)
    }//   if ( (size = tables.size()) > 1)
      m_tables = tables;
   }

   /**
    * Get the joins defined between the back-end tables. If more than one
    * table is defined in this data tank, joins between the tables must
    * be defined.
    *
    * @return      a collection containing the joins (PSBackEndJoin objects)
    *
    * @see         PSBackEndJoin
    */
   public com.percussion.util.PSCollection getJoins()
   {
      return m_joins;
   }

   /**
    * Overwrite the joins with the specified collection.
    * If you only want to modify certain joins, add a new one, etc. use
    * getJoins to get the existing collection and modify the
    * returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSBackEndDataTank object. Any subsequent changes made to the object
    * by the caller will also effect the data tank.
    *
    * @param joins   the new joins (may be <code>null</code>)
    *
    * @see            #getJoins
    * @see            PSBackEndJoin
    */
   public void setJoins(com.percussion.util.PSCollection joins)
   {
      if (joins != null) {
         /* is this the right collection type */
         if (!com.percussion.design.objectstore.PSBackEndJoin.class.isAssignableFrom(
            joins.getMemberClassType()))
         {
            Object[] args =
               { "Back-end Join", "PSBackEndJoin", joins.getMemberClassName() };
            throw new IllegalArgumentException("coll bad content type: Back-end Join "
               + joins.getMemberClassName());
         }
      }

      // the join verification happens later at runtime...
      m_joins = joins;
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param tank a valid PSBackEndDataTank. 
    */
   public void copyFrom(PSBackEndDataTank tank)
   {
      super.copyFrom(tank);
      m_tables = new PSCollection(tank.getTables().getMemberClassType());
      m_tables.addAll(tank.getTables());
      m_joins = new PSCollection(tank.getJoins().getMemberClassType());
      m_joins.addAll(tank.getJoins());
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXBackEndDataTank XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXBackEndDataTank is used to define the back-end data stores
    *       used in a PSXPipe object. When multiple back-end tables are
    *       permitted, the back-end data tank is also used to define the
    *       relationships (joins) between the tables.
    *
    *       Object References:
    *
    *       PSXBackEndTable - the back-end table(s) associated with this
    *       data tank.
    *
    *       PSXBackEndJoin - the joins defined between the back-end tables.
    *       If more than one table is defined in this data tank, joins
    *       between the tables must be defined.
    *    --&gt;
    *    &lt;!ELEMENT PSXBackEndDataTank   (PSXBackEndTable+, PSXBackEndJoin*)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXBackEndDataTank XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //Create backEndTables from XML elementsilds to that
      PSComponent   entry;
      int size = (m_tables == null) ? 0 : m_tables.size();
      for (int i=0; i < size; i++)
      {
         entry = (PSComponent)m_tables.get(i);
         root.appendChild(entry.toXml(doc));
      }

      //Create backEndJoins parent node and add all joins as childs to that
      size = (m_joins == null) ? 0 : m_joins.size();
      for (int i = 0; i < size; i++)
      {
         entry = (PSComponent)m_joins.get(i);
         root.appendChild(entry.toXml(doc));
      }

      return root;
   }

   /**
   * This method is called to populate a PSBackEndDataTank Java object
   * from a PSXBackEndDataTank XML element node. See the
   * {@link #toXml(Document) toXml} method for a description of the XML object.
   *
   * @exception   PSUnknownNodeTypeException if the XML element node is not
   *                                        of type PSXBackEndDataTank
   */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try
      {
         if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

         if (false == ms_NodeType.equals (sourceNode.getNodeName()))
         {
            Object[] args = { ms_NodeType, sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
               throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         //Get the PSXBackEndTables, parent of back end tables collection
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         org.w3c.dom.Node cur = tree.getCurrent(); //cur=curNodeType=<PSXBackEndDataTank>

         m_tables.clear();

         if (tree.getNextElement(PSBackEndTable.ms_NodeType, firstFlags) != null)
         {
            do
            {
               PSBackEndTable table = new PSBackEndTable(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
               m_tables.add(table);
            }
            while (tree.getNextElement(
               PSBackEndTable.ms_NodeType, nextFlags) != null);
         }

         // pop back up
         tree.setCurrent(cur);

         // now do the joins
         m_joins.clear();

         if (tree.getNextElement(PSBackEndJoin.ms_NodeType, firstFlags) != null)
         {
            //Collect all the back end joins
            do
            {
               PSBackEndJoin join = new PSBackEndJoin(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
               m_joins.add(join);
            }
            while (tree.getNextElement(
               PSBackEndJoin.ms_NodeType, nextFlags) != null);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSBackEndDataTank)) return false;
      if (!super.equals(o)) return false;
      PSBackEndDataTank that = (PSBackEndDataTank) o;
      return Objects.equals(m_tables, that.m_tables) &&
              Objects.equals(m_joins, that.m_joins);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_tables, m_joins);
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
      return;

      int joinSize = (m_joins == null) ? 0 : m_joins.size();
      int tabSize = (m_tables == null) ? 0 : m_tables.size();

      // validate the tables
      if (tabSize == 0)
         cxt.validationError(this, IPSObjectStoreErrors.BE_TABLE_NULL, null);
      else
      {
         cxt.pushParent(this);
         try
         {
            for (int i = 0; i < m_tables.size(); i++)
            {
               Object o = m_tables.get(i);
               PSBackEndTable tab = (PSBackEndTable)o;
               tab.validate(cxt);
            }
         }
         finally
         {
            cxt.popParent();
         }
      }

      cxt.pushParent(this);
      try
      {
         for (int i = 0; i < joinSize; i++)
         {
            Object o = m_joins.get(i);
            PSBackEndJoin join = (PSBackEndJoin)o;
            join.validate(cxt);
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

  /**
   * Creates a deep copy of this <tt>PSBackEndDataTank</tt> instance
   * @return a clone of this <tt>PSBackEndDataTank</tt>
   */
   public Object clone()
   {
      PSBackEndDataTank copy = (PSBackEndDataTank) super.clone();
         if(m_tables != null)
            copy.m_tables = (PSCollection)m_tables.clone();
         if(m_joins != null)
            copy.m_joins = (PSCollection) m_joins.clone();
      return copy;
   }


   public         PSCollection        m_tables = null;
   private         PSCollection        m_joins = null;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXBackEndDataTank";
}
