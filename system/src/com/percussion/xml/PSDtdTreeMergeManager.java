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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class PSDtdTreeMergeManager
{
   /**
    * Constructor needs a master PSDtdTree for all incoming slave trees.
    * Re-create a UTDtdMergeManager if you want to restart the merge without
    * the previously merged instances.
    *
    * @param masterTree The DTD tree provided by OSPageDatatank. If masterTree is
   * <CODE>null</CODE> or if masterTree has no root, an IllegalArgumentException
   * will be thrown.
    *
    * @throws IllegalArgumentException if masterTree is null.
    *
         * @see PSDtdTree
    */
   public PSDtdTreeMergeManager( PSDtdTree masterTree )
   {
      if ( null == masterTree )
         throw new IllegalArgumentException("masterTree cannot be null!");

      m_masterTree = masterTree;
   }

   /**
    * for testing purposes only.
    */

    /*
   public static void main( String[] args )
   {
      if (args.length < 1)
      {
         System.out.println("No args");
         return;
      }

      PSDtdTree masterTree = null;
      PSDtdTree slaveTree = null;

      try
      {
      if (args.length < 2)
      {
               System.out.println("master and slave DTD filenames required");
               return;
      }

         String masterDtd = args[0];
      String slaveDtd = args[1];

         // setting up master tree
      String docType = masterDtd.substring( 0, masterDtd.indexOf('.') );
      java.io.File file = new java.io.File(masterDtd);
      java.io.InputStream in = new java.io.BufferedInputStream(
                                         new java.io.FileInputStream(file));
      masterTree = new PSDtdTree(in, docType);

         // setting up slave tree
      docType = slaveDtd.substring( 0, slaveDtd.indexOf('.') );
      file = new java.io.File(slaveDtd);
      in = new java.io.BufferedInputStream( new java.io.FileInputStream(file) );
      slaveTree = new PSDtdTree(in, docType);

// test using: java -Djava.compiler=none com/percussion/xml/PSDtdTreeMergeManager test.dtd test1.dtd
      Iterator iterator = masterTree.elementKeyIterator();
         System.out.println( "---------- Master Tree -------------" );
      while ( iterator.hasNext() )
      {
        String key = (String)iterator.next();
        System.out.println( "     "+key );
      }

         PSDtdTreeMergeManager mergeObj = new PSDtdTreeMergeManager( masterTree );
      mergeObj.mergeDtdTree( slaveTree );

      iterator = masterTree.elementKeyIterator();
      System.out.println( "---------- Master Tree Result -------------" );
      while ( iterator.hasNext() )
         {
        String key = (String)iterator.next();
        System.out.println( "     "+key );
      }

      //masterTree = mergeObj.getMergedTree();
      System.out.println( "------------ Master Tree Result (RELATION) ---------------" );
         java.util.List cat = masterTree.getCatalog("/", "@");
         for (java.util.Iterator i = cat.iterator(); i.hasNext(); )
         {
            System.out.println(i.next().toString());
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
  }
  */

  public static void main( String[] args )
  {
      if (args.length < 1)
      {
         System.out.println("No args");
         return;
      }

      PSDtdTree masterTree = null;
    PSDtdTree slaveTree = null;

      try
      {
      if (args.length < 2)
      {
               System.out.println("master and slave DTD filenames required");
               return;
      }

         String masterDtd = args[0];
      String slaveDtd = args[1];

      // setting up master tree
         String docType = masterDtd.substring( 0, masterDtd.indexOf('.') );
      java.io.File file = new java.io.File(masterDtd);
      java.io.InputStream in = new java.io.BufferedInputStream(
                                         new java.io.FileInputStream(file));
      masterTree = new PSDtdTree(in, docType);

      // setting up slave tree
         docType = slaveDtd.substring( 0, slaveDtd.indexOf('.') );
      file = new java.io.File(slaveDtd);
      in = new java.io.BufferedInputStream( new java.io.FileInputStream(file) );
      slaveTree = new PSDtdTree(in, docType);

// test using: java -Djava.compiler=none com/percussion/xml/PSDtdTreeMergeManager test.dtd test1.dtd
         Iterator iterator = masterTree.elementKeyIterator();
//      System.out.println( "---------- Master Tree -------------" );
//         System.out.println( masterTree.toDTD(false) );
//         System.out.println( "------------------------------------" );

         PSDtdTreeMergeManager mergeObj = new PSDtdTreeMergeManager( masterTree );
         masterTree = mergeObj.updateTreeForUserMod( masterTree, slaveTree );

//         System.out.println( masterTree.toDTD(false) );
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
  }



  /**
   * @return Returns the master result tree.
   */
  public PSDtdTree getMergedTree()
   {
    return m_masterTree;
  }

   /**
    * For every element entry in the slave
    * tree, if an entry that has the same path is found in the master tree,
    * the occurrence setting of that node in the master tree is changed to match
    * the occurrence setting in the slave tree. Modifications are done on a
    * clone of the master tree.
    * This method treats all children of an element as if the groupings were not
    * present, i.e. occurrence settings for 'groups' (e.g. (a | b | c)
    * is a group) are not modified, only the individual elements within the group.
    *
    * @param master The tree to compare element entries against for occurrence
    * settings variations.
    *
    * @param slave The source of the occurrence settings.
    *
    * @return A cloned master tree with the occurrence settings of the slave
    * tree.
    *
    * @todo This could be optimized quite a bit.
    */
   public PSDtdTree updateTreeForUserMod( PSDtdTree master, PSDtdTree slave )
   {
      List paths = slave.getCatalog( null, null );

      PSDtdTree workingTree = null;

      /* We could defer cloning until we know we have to (returning the original
         if no changes were made), but that would require us to make a check w/in
         the innermost loop below. It is assumed that most master trees will be
         modified. */
      try
      {
         workingTree = (PSDtdTree) master.clone();
      }
      catch (CloneNotSupportedException e) { /* Ignore, shouldn't happen unless tree stops cloning */}

      // just in case the path results are null; this usually mean that we are
      // trying to update the tree for an updateResource, which does not have a
      // valid root (very bad)
      if ( null == paths )
         return workingTree;

      /* The returned catalog only has leaf entries, but we need intermediate
         nodes as well. To get these, we break down each path and add the
         components to a hash. The resulting hash will include all of the sub-paths,
         in addition to the full paths. */
      HashMap allPaths = new HashMap( 50 );
      Iterator iter = paths.listIterator();
      while ( iter.hasNext())
      {
         String path = (String) iter.next();
         if ( !PSDtdTree.isAttributePath( path, PSDtdTree.CANONICAL_ATTRIBUTE_PREFIX ))
         {
            allPaths.put( path, null );
            int lastOccurrence = path.lastIndexOf(PSDtdTree.CANONICAL_PATH_SEP);
            while ( -1 != lastOccurrence )
            {
               path = path.substring( 0, lastOccurrence );
               allPaths.put( path, null );
               lastOccurrence = path.lastIndexOf(PSDtdTree.CANONICAL_PATH_SEP);
            }
         }
         else
            allPaths.put( path, null );
      }

      Set finalPaths = allPaths.keySet();

      // reset the iterator for the complete path list
      iter = finalPaths.iterator();
      while ( iter.hasNext())
      {
         String path = (String) iter.next();
         if ( !PSDtdTree.isAttributePath( path, PSDtdTree.CANONICAL_ATTRIBUTE_PREFIX ))
         {
            PSDtdElementEntry masterEntry = workingTree.getEntryForName( path );
            if ( null != masterEntry )
            {
               PSDtdElementEntry slaveEntry = slave.getEntryForName( path );
               masterEntry.setOccurrences( slaveEntry.getOccurrenceType());
            }
         }
      }
      return workingTree;
   }


   /**
    * Performs the merge between the slaveTree passed in and the master tree. The
    * merge will base on the merging algorithm.
    *
    * Attach algorithm here!!!
    *
    * @param slaveTree The slave tree that will be merged into the master tree.
    * Method will return immediately if <CODE>null</CODE>. A PSDtdTree with no
    * root is passed in will cause the method to return immediately as well.
    */
   public void mergeDtdTree( PSDtdTree slaveTree )
   {
      if ( null == slaveTree || null == slaveTree.getRoot() )
         return;

      try
      {
         slaveTree = (PSDtdTree)slaveTree.clone();
      }
      catch ( CloneNotSupportedException e )
      { /* ignore, won't happen unless interface changes */ }

      // get the Root PSDtdElement object to keep while we replace the Name
      String rootName = m_masterTree.getRoot().getElement().getName();
      slaveTree.setRootName( rootName );
      m_slaveTree = slaveTree;

//      System.out.println( "---------- Slave Tree -------------" );
      Iterator iterator = m_slaveTree.elementKeyIterator();
      while ( iterator.hasNext() )
      {
         String key = (String)iterator.next();
//         System.out.println( "     "+key );
         mergeElement( key );
      }
   }

   /**
    * insert attribute occurrence merge chart here.
    *
    * @param master The attribute occurrence setting in the master tree.
    * @param slave The attribute occurrence setting in the slave tree.
    * @return int The result attribute occurrence setting from the merge.
    */
   private int mergeAttributeOccurrence( int master, int slave )
   {
      int result = master;

      if ( PSDtdAttribute.REQUIRED == master || PSDtdAttribute.REQUIRED ==slave )
         result = PSDtdAttribute.REQUIRED;
      else if ( PSDtdAttribute.IMPLIED == master )
         result = slave;

      return result;
   }

   /**
    * Merges the slave attribute to the master attribute by finding an attribute
    * in the master tree (for this element) that has the same name.
    *
    * @param master The master PSDtdElement which may or may not have attributes.
    * The merge will do nothing if master is <CODE>null</CODE>.
    * @param slave The slave PSDtdElement which may or may not have attibutes.
    * The merge will do nothing if slave is <CODE>null</CODE>.
    */
   private void mergeAttributes( String key )
   {
      PSDtdElement master = m_masterTree.getElement( key );
      PSDtdElement slave = m_slaveTree.getElement( key );

      if ( null == slave ) // case 1 & 4
         return;

      if ( 0 == master.getNumAttributes() ) // case 2
      {
         /*
         if ( null == master )
            master = new PSDtdElement( key );
         */
         for (int i = 0; i < slave.getNumAttributes(); i++)
         {
            PSDtdAttribute sAttrib = slave.getAttribute( i );
            PSDtdAttribute mAttrib = new PSDtdAttribute( sAttrib.getName() );
            mAttrib.setPossibleValues( sAttrib.getPossibleValues() );
            mAttrib.setOccurrence( sAttrib.getOccurrence() );
            mAttrib.setType( sAttrib.getType() );

            master.addAttribute( mAttrib );
         }
      }
      else // case 3
      {
         // put all master list of attributes
         HashMap map = new HashMap();
         for ( int i = 0; i < master.getNumAttributes(); i++ )
         {
            PSDtdAttribute attribute = master.getAttribute( i );
            map.put( attribute.getName(), attribute );
         }

         // now traverse the slave list of attribute to merge matching entries or
         // add new entries
         for ( int i = 0; i < slave.getNumAttributes(); i++ )
         {
            PSDtdAttribute attribute = slave.getAttribute( i );
            String name = attribute.getName();
            if ( map.containsKey( name ) ) // if attribute already exists, merge
            {
               PSDtdAttribute mAttribute = (PSDtdAttribute)map.get( name );
               int occur = mergeAttributeOccurrence( mAttribute.getOccurrence(),
                                                     attribute.getOccurrence() );
               mAttribute.setOccurrence( occur );
               map.put( mAttribute.getName(), mAttribute );
            }
            else // simply add new attribute to list
               map.put( attribute.getName(), attribute );
         }
         master.resetAttributes();
         Iterator iterator = map.values().iterator();
         while ( iterator.hasNext() )
         {
            master.addAttribute( (PSDtdAttribute)iterator.next() );
         }
      }
      // update element in m_masterTree
      m_masterTree.addElement( key, master );
   }

  /**
   * insert element occurrence merge chart here.
   *
   * @param master The element occurrence setting in the master tree.
   * @param slave The element occurrence setting in the slave tree.
   * @return int The result element occurrence setting from the merge.
   */
  private int mergeElementOccurrence( int master, int slave )
  {
    return (master | slave);
  }

  /**
   * Merges the slave PSDtdElement to the master PSDtdElement. The result is set
   * into the member master tree (m_masterTree).
   *
   * (!) Add merge algorithm here!
   *
   * @param key The String key that is the name of the Element that we are
   * currently merging. This key is used by both the master tree and the slave
   * tree and retrieved by calling <tree instance>.getElement() method
   * (essentially a HashMap.get() call).
   * @todo Merging of inner NodeLists ALWAYS removes all nested parenthesis. Must
   * add code to accurately preserve the nested NodeLists.
   */
   private PSDtdElement mergeElement( String key )
   {
      PSDtdElement sElement = m_slaveTree.getElement( key );
      PSDtdElement mElement = m_masterTree.getElement( key );

      if ( null == sElement )
         return null;

      PSDtdNode mContent = null;

      if ( null != mElement )
         mContent = mElement.getContent();

      PSDtdNode sContent = sElement.getContent();

      // setup complete, ready to start recursive merge

      // if slave CM is a PCDATA, copy to master CM immediately
      if ( null == mContent )
      {
         if ( sContent instanceof PSDtdDataElement )
         {
            // System.out.println("       "+sElement.getName() + " m=null; s=Data");
            mContent = new PSDtdDataElement();
         }
         else if ( sContent instanceof PSDtdElementEntry )
         {
            //   System.out.println("       "+sElement.getName() + " m=null; s=Entry");

            PSDtdElementEntry entry = (PSDtdElementEntry)sContent;
            String elementName = entry.getElement().getName();
            PSDtdElement tempElement = m_masterTree.getElement( elementName );
            // recurse down slaveTree until a copy-able element CM is reached
            if ( null == tempElement )
               tempElement = mergeElement( elementName );

            entry.setElement( tempElement );
            mContent = entry;
         }
         else if ( sContent instanceof PSDtdNodeList )
         {
            //System.out.println("       "+sElement.getName() + " m=null; s=List");

            PSDtdNodeList nodeList = (PSDtdNodeList)sContent;
            PSDtdNodeList tempList = new PSDtdNodeList( nodeList.getType(),
                                                        nodeList.getOccurrenceType() );
            for (int i = 0; i < nodeList.getNumberOfNodes(); i++)
            {
               PSDtdNode innerNode = nodeList.getNode( i );
               if ( innerNode instanceof PSDtdDataElement )
               {
                  // do nothing since mContent is null
                  innerNode = new PSDtdDataElement();
                  tempList.add( innerNode );
               }
               else if ( innerNode instanceof PSDtdElementEntry )
               {
                  // check if ElementEntry.element exists in masterTree
                  PSDtdElementEntry entry = (PSDtdElementEntry)innerNode;
                  String elementName = entry.getElement().getName();
                  PSDtdElement tempElement = m_masterTree.getElement( elementName );
                  // recurse down slaveTree until a copy-able element CM is reached
                  if ( null == tempElement )
                     tempElement = mergeElement( elementName );

                  entry.setElement( tempElement );

                  tempList.add( entry );
               }
               else // innerNode instanceof PSDtdNodeList
               {
                  mergeInnerNodes( (PSDtdNodeList)innerNode, tempList );
               }
            }
            mContent = tempList;
         }
      }
      else if ( mContent instanceof PSDtdDataElement )
      {
         if ( sContent instanceof PSDtdDataElement )
         {
            // do nothing
            // System.out.println("       "+sElement.getName() + " m=Data; s=Data");
         }
         else if ( sContent instanceof PSDtdElementEntry )
         {
            // System.out.println("       "+sElement.getName() + " m=Data; s=Entry");

            PSDtdElementEntry entry = (PSDtdElementEntry)sContent;
            String elementName = entry.getElement().getName();
            PSDtdElement tempElement = m_masterTree.getElement( elementName );
            // recurse down slaveTree until a copy-able element CM is reached
            if ( null == tempElement )
               tempElement = mergeElement( elementName );

            PSDtdNodeList nodeList = new PSDtdNodeList( PSDtdNodeList.OPTIONLIST,
                                                        PSDtdNodeList.OCCURS_ANY );
            entry.setElement( tempElement );
            nodeList.add( new PSDtdDataElement() );
            entry.setOccurrences( PSDtdNode.OCCURS_ONCE );
            nodeList.add( entry );

            mContent = nodeList;
         }
         else if ( sContent instanceof PSDtdNodeList )
         {
            // System.out.println("       "+sElement.getName() + " m=Data; s=List");

            PSDtdNodeList sNodeList = (PSDtdNodeList)sContent;
            //PSDtdNodeList tempList = new PSDtdNodeList( nodeList.getType(),
            //                                            nodeList.getOccurrenceType() );
            PSDtdNodeList tempList = new PSDtdNodeList( PSDtdNodeList.OPTIONLIST,
                                                        PSDtdNodeList.OCCURS_ANY );

            tempList.add( new PSDtdDataElement() );
            for (int i = 0; i < sNodeList.getNumberOfNodes(); i++)
            {
               PSDtdNode innerNode = sNodeList.getNode( i );
               if ( innerNode instanceof PSDtdElementEntry )
               {
                  // check if ElementEntry.element exists in masterTree
                  PSDtdElementEntry entry = (PSDtdElementEntry)innerNode;
                  String elementName = entry.getElement().getName();
                  PSDtdElement tempElement = m_masterTree.getElement( elementName );
                  // recurse down slaveTree until a copy-able element CM is reached
                  if ( null == tempElement )
                     tempElement = mergeElement( elementName );

                  entry.setElement( tempElement );
                  entry.setOccurrences( PSDtdNode.OCCURS_ONCE );
                  tempList.add( entry );
               }
               else // innerNode instanceof PSDtdNodeList
               {
                  mergeInnerNodes( (PSDtdNodeList)innerNode, tempList );
               }
            }
            mContent = tempList;
         }
      }
      else if ( mContent instanceof PSDtdElementEntry )
      {
         PSDtdElementEntry mEntry = (PSDtdElementEntry)mContent;
         if ( sContent instanceof PSDtdDataElement )
         {
            // System.out.println("       "+sElement.getName() + " m=Data; s=Entry");

            PSDtdNodeList tempList = new PSDtdNodeList( PSDtdNodeList.OPTIONLIST,
                                                        PSDtdNodeList.OCCURS_ANY );
            // preserving the strict mixed-model of DTDs; PCDATA always first
            tempList.add( new PSDtdDataElement() );
            mContent.setOccurrences( PSDtdNode.OCCURS_ONCE );
            tempList.add( mContent );
            mContent = tempList;
         }
         else if ( sContent instanceof PSDtdElementEntry )
         {
            // System.out.println("       "+sElement.getName() + " m=Entry; s=Entry");

            PSDtdElementEntry sEntry = (PSDtdElementEntry)sContent;
            String elementName = sEntry.getElement().getName();
            PSDtdElement tempElement = m_masterTree.getElement( elementName );
            // recurse down slaveTree until a copy-able element CM is reached
            if ( null == tempElement )
               tempElement = mergeElement( elementName );

            sEntry.setElement( tempElement );
            // mNode and sNode share the same name; so merge the 2 nodes
            if ( elementName.equals( mEntry.getElement().getName() ) )
            {
               PSDtdNode sNode = sEntry.getElement().getContent();
               if ( !(null == mContent || null == sNode) )
               {
                  int occur = mergeElementOccurrence( mContent.getOccurrenceType(),
                                                      sContent.getOccurrenceType() );
                  sEntry.setOccurrences( occur );
               }
               mContent = sEntry;
            }
            // mNode and sNode have different names; so add the 2 nodes into a
            // NodeList
            else
            {
               PSDtdNodeList tempList = new PSDtdNodeList( PSDtdNodeList.SEQUENCELIST,
                                                           PSDtdNode.OCCURS_ONCE );
               tempList.add( mContent );
               tempList.add( sEntry );

               mContent = tempList;
            }
         }
         else if ( sContent instanceof PSDtdNodeList )
         {
            // System.out.println("       "+sElement.getName() + " m=Entry; s=List");

            PSDtdNodeList sNodeList = (PSDtdNodeList)sContent;
            PSDtdNodeList tempList = new PSDtdNodeList( sNodeList.getType(),
                                                        sNodeList.getOccurrenceType() );
            boolean bNeedFix = false;
            for (int i = 0; i < sNodeList.getNumberOfNodes(); i++)
            {
               PSDtdNode innerNode = sNodeList.getNode( i );
               if ( innerNode instanceof PSDtdDataElement )
               {
                  innerNode = new PSDtdDataElement();
                  tempList.add( innerNode );
                  bNeedFix = true;
               }
               else if ( innerNode instanceof PSDtdElementEntry )
               {
                  // check if ElementEntry.element exists in masterTree
                  PSDtdElement element = null;
                  PSDtdElementEntry sEntry = (PSDtdElementEntry)innerNode;
                  String elementName = sEntry.getElement().getName();
                  if ( elementName.equals( mEntry.getElement().getName() ) )
                  {
                     element = mEntry.getElement();
                     PSDtdNode mNode = mEntry.getElement().getContent();
                     PSDtdNode sNode = sEntry.getElement().getContent();

                     if ( !( null == mNode || null == sNode ) )
                     {
                        int occur = mergeElementOccurrence( mEntry.getOccurrenceType(),
                                                            sEntry.getOccurrenceType() );
                        sEntry.setOccurrences( occur );
                     }
                     m_bNodeInList = true;
                  }
                  else
                  {
                     element = m_masterTree.getElement( elementName );
                     // recurse down slaveTree until a copy-able element CM is reached
                     if ( null == element )
                        element = mergeElement( elementName );
                  }
                  sEntry.setElement( element );
                  tempList.add( sEntry );
               }
               else // innerNode instanceof PSDtdNodeList
               {
                  // TODO: need to add m_bNodeInList to verify no inner-inner merge
                  bNeedFix = mergeInnerNodes( (PSDtdNodeList)innerNode, tempList );
               }
            }

            // this step is needed in case mContent did not exist in the NodeList.
            // add mContent to the beginning of the NodeList
            if ( !m_bNodeInList )
            {
               PSDtdNodeList newList = new PSDtdNodeList( sNodeList.getType(),
                                                          sNodeList.getOccurrenceType() );
               newList.add( mContent );
               for ( int i = 0; i < tempList.getNumberOfNodes(); i++)
                  newList.add( tempList.getNode( i ));

               tempList = newList;
            }

            //mContent = fixMixedModelNodeList( tempList );
            if ( bNeedFix )
               mContent = fixMixedModelNodeList( tempList );
            else
               mContent = tempList;
         }
      }
      else if ( mContent instanceof PSDtdNodeList )
      {
         if ( sContent instanceof PSDtdDataElement )
         {
            // System.out.println("       "+sElement.getName() + " m=List; s=Data");
            PSDtdNodeList mNodeList = (PSDtdNodeList)mContent;
            PSDtdNodeList tempList = new PSDtdNodeList( PSDtdNodeList.OPTIONLIST,
                                                        PSDtdNodeList.OCCURS_ANY );
            if ( !(mNodeList.getNode( 0 ) instanceof PSDtdDataElement) )
            {
               tempList.add( new PSDtdDataElement() );
            }

            for ( int i = 0; i < mNodeList.getNumberOfNodes(); i++ )
            {
               PSDtdNode node = mNodeList.getNode( i );
               node.setOccurrences( PSDtdNode.OCCURS_ONCE );
               tempList.add( node );
            }
            mContent = tempList;
         }
         else if ( sContent instanceof PSDtdElementEntry )
         {
            // System.out.println("       "+sElement.getName() + " m=List; s=Entry");

            PSDtdElementEntry sEntry = (PSDtdElementEntry)sContent;
            String entryName = sEntry.getElement().getName();

            PSDtdNodeList mNodeList = (PSDtdNodeList)mContent;
            PSDtdNodeList tempList = new PSDtdNodeList( mNodeList.getType(),
                                                        mNodeList.getOccurrenceType() );

            boolean bNeedFix = false;
            for (int i = 0; i < mNodeList.getNumberOfNodes(); i++)
            {
               PSDtdNode innerNode = mNodeList.getNode( i );
               if ( innerNode instanceof PSDtdDataElement )
               {
                  innerNode = new PSDtdDataElement();
                  tempList.add( innerNode );
                  bNeedFix = true;
               }
               else if ( innerNode instanceof PSDtdElementEntry )
               {
                  // check if ElementEntry.element exists in masterTree
                  PSDtdElementEntry mEntry = (PSDtdElementEntry)innerNode;
                  String elementName = mEntry.getElement().getName();

                  if ( elementName.equals( entryName ) )
                  {
                     PSDtdNode mNode = mEntry.getElement().getContent();
                     PSDtdNode sNode = sEntry.getElement().getContent();

                     if ( !( null == mNode || null == sNode ) )
                     {
                        int occur = mergeElementOccurrence( mEntry.getOccurrenceType(),
                                                            sEntry.getOccurrenceType() );
                        mEntry.setOccurrences( occur );
                     }
                     m_bNodeInList = true;
                  }

                  tempList.add( mEntry );
               }
               else // innerNode instanceof PSDtdNodeList
               {
                  // TODO: need to add m_bNodeInList to verify no inner-inner merge
                  bNeedFix = mergeInnerNodes( (PSDtdNodeList)innerNode, tempList );
               }
            }

            // this step is needed in case sContent did not exist in the NodeList.
            // add sContent to the end of the NodeList
            if ( !m_bNodeInList )
            {
               PSDtdNodeList newList = new PSDtdNodeList( mNodeList.getType(),
                                                          mNodeList.getOccurrenceType() );
               for ( int i = 0; i < tempList.getNumberOfNodes(); i++)
                  newList.add( tempList.getNode( i ));

               PSDtdElement element = m_masterTree.getElement( entryName );
               if ( null == element )
                  element = mergeElement( entryName );

               sEntry.setElement( element );
               newList.add( sEntry );
               tempList = newList;
            }

            if ( bNeedFix )
               mContent = fixMixedModelNodeList( tempList );
            else
               mContent = tempList;
         }
         else if ( sContent instanceof PSDtdNodeList )
         {
            // System.out.println("       "+sElement.getName() + " m=List; s=List");

            PSDtdNodeList mNodeList = (PSDtdNodeList)mContent;
            PSDtdNodeList sNodeList = (PSDtdNodeList)sContent;

            boolean bNeedFix = false;
            // store nodes from master nodelist into a hashmap
            HashMap mMap = new HashMap();
            for ( int i = 0; i < mNodeList.getNumberOfNodes(); i++ )
            {
               PSDtdNode node = mNodeList.getNode( i );
               String nodeName = null;
               if ( node instanceof PSDtdDataElement )
               {
                  nodeName = PSDtdDataElement.PCDATA_STRING;
                  bNeedFix = true;
               }
               else if ( node instanceof PSDtdElementEntry )
                  nodeName = ((PSDtdElementEntry)node).getElement().getName();
               else // node instanceof PSDtdNodeList
                  nodeName = null;

               mMap.put( nodeName, node );
            }

            bNeedFix = addNodeListIntoMap( mMap, sNodeList ) || bNeedFix;

            // this step is needed in case mContent did not exist in the NodeList.
            // add mContent to the beginning of the NodeList
            PSDtdNodeList tempList = null;
            if ( !m_bNodeInList )
            {
               tempList = new PSDtdNodeList( mNodeList.getType(),
                                             mNodeList.getOccurrenceType() );
               Iterator iterator = mMap.values().iterator();
               while ( iterator.hasNext() )
                  tempList.add( (PSDtdNode)iterator.next() );
               //mContent = tempList;
            }

            if ( bNeedFix )
               mContent = fixMixedModelNodeList( tempList );
            else
               mContent = tempList;

            //////////////////////////////////////////////////
            /*
            if (key.equals("Name"))
            {
               System.out.println(" bNeedFix: "+bNeedFix);
               for ( int i = 0; i < ((PSDtdNodeList)mContent).getNumberOfNodes(); i++ )
               {
                  System.out.println(i+": "+ ((PSDtdNodeList)mContent).getNode( i ) );
               }
            }
            */
            ///////////////////////////////////////////////////
         }
      }

      m_bNodeInList = false;
      // this element does not exist in master yet, so create one and add it
      if ( null == mElement )
         mElement = new PSDtdElement( key );

      mElement.setContent( mContent, mElement.isAny() );
      m_masterTree.addElement( key, mElement );
      // merge elements' attributes first
      mergeAttributes( key );
      return mElement;
   }

  /**
   * Used by mergeElement(String) method to specifically merge inner NodeLists.
   *
   * @param innerList The inner NodeList that we will be traversing and used to
   * merge into ownerList.
   * @param ownerList The NodeList to merge into.
   * @return boolean <CODE>true</CODE> if any of the inner nodes has PCDATA.
   */
   private boolean mergeInnerNodes( PSDtdNodeList innerList,
                                    PSDtdNodeList ownerList )
   {
      boolean bHasPCDATA = false;
      for ( int i = 0; i < innerList.getNumberOfNodes(); i++ )
      {
         PSDtdNode innerNode = innerList.getNode( i );
         if ( innerNode instanceof PSDtdElementEntry )
         {
            PSDtdElementEntry entry = ((PSDtdElementEntry)innerNode);
            String innerNodeName = entry.getElement().getName();
            PSDtdElement tempElement = m_masterTree.getElement( innerNodeName );
            // recurse down slaveTree until a copy-able element CM is reached
            if ( null == tempElement )
               tempElement = mergeElement( innerNodeName );

            entry.setElement( tempElement );
            ownerList.add( entry );
         }
         else if ( innerNode instanceof PSDtdNodeList )
         {
            boolean b = mergeInnerNodes( (PSDtdNodeList)innerNode, ownerList );
            bHasPCDATA = b || bHasPCDATA;
         }
         else
         {
            ownerList.add( new PSDtdDataElement() );
            bHasPCDATA = true;
         }
      }

      return bHasPCDATA;
   }


   /**
    * Recursively adds nodes in NodeLists to the storage HashMap.
    *
    * @param map The HashMap used for data storage, basically the a HashMap
    * version of the master NodeList. This should never be <CODE>null</CODE>.
    * @param nodeList The current node element (which is a PSDtdNodeList) that
    * is being traversed. This has to be the slave NodeList.
    * @return boolean <CODE>true</CODE> if contains a PCDATA.
    */
   private boolean addNodeListIntoMap( HashMap mMap, PSDtdNodeList sNodeList )
   {
      boolean bHasPCDATA = false;
      for (int i = 0; i < sNodeList.getNumberOfNodes(); i++)
      {
         PSDtdNode sInnerNode = sNodeList.getNode( i );
         if ( sInnerNode instanceof PSDtdDataElement )
         {
            if ( !mMap.containsKey( PSDtdDataElement.PCDATA_STRING ) )
            {
               sInnerNode = new PSDtdDataElement();
               mMap.put( PSDtdDataElement.PCDATA_STRING, sInnerNode );
               bHasPCDATA = true;
            }
            // else do nothing
         }
         else if ( sInnerNode instanceof PSDtdElementEntry )
         {
            // check if ElementEntry.element exists in masterTree
            PSDtdElementEntry sEntry = (PSDtdElementEntry)sInnerNode;
            String elementName = sEntry.getElement().getName();

            if ( mMap.containsKey( elementName ) )
            {
               PSDtdElementEntry mEntry = (PSDtdElementEntry)mMap.get( elementName );
               PSDtdNode mNode = mEntry.getElement().getContent();
               PSDtdNode sNode = sEntry.getElement().getContent();

               // in case the CM is EMPTY
               if ( !(null == mNode || null == sNode) )
               {
                  int occur = mergeElementOccurrence( mEntry.getOccurrenceType(),
                                                      sEntry.getOccurrenceType() );
                  mEntry.setOccurrences( occur );
               }
               mMap.put( elementName, mEntry );
            }
            else
            {
               PSDtdElement tempElement = m_masterTree.getElement( elementName );
               if ( null == tempElement )
                  tempElement = mergeElement( elementName );

               sEntry.setElement( tempElement );
               mMap.put( elementName, sEntry );
            }
         }
         else // sInnerNode instanceof PSDtdNodeList
         {
            boolean b = addNodeListIntoMap( mMap, (PSDtdNodeList)sInnerNode );
            bHasPCDATA = b || bHasPCDATA;
         }
      }

      return bHasPCDATA;
   }

   /**
    * Fixes up the NodeList to conform to the mixed-content model. A post-merge
    * NodeList may contain a DataElement (PCDATA) that is not in
    * the first element of the NodeList. This is considered a malformed DTD
    * NodeList. The correct mixed-content model for DTDs is ALWAYS have the
    * PCDATA (ONLY one in list) first in the list, and all else follow. All
    * other nodes will forced to changed to a repeat attribute of OCCURS_ONCE.
    *
    * @param list The NodeList that needs to be fixed up, which MUST be a
    * NodeList that needs to become a proper mixed-content model.
    * <CODE>null</CODE> is not a valid parameter, IllegalArgumentException will
    * be thrown.
    * @return Creates a new NodeList based on the list passed in, it is returned
    * after fix-up.
    * @throw IllegalArgumentException Only if list was <CODE>null</CODE>.
    * @see PSDtdNodeList
    * @see PSDtdDataElement
    */
   private PSDtdNodeList fixMixedModelNodeList( PSDtdNodeList list )
   {
      if ( null == list )
         throw new IllegalArgumentException( "list parameter is null!" );

      PSDtdNodeList newList = new PSDtdNodeList( PSDtdNodeList.OPTIONLIST,
                                                 PSDtdNodeList.OCCURS_ANY );

      newList.add( new PSDtdDataElement() );
      for ( int i = 0; i < list.getNumberOfNodes(); i++ )
      {
         if ( !(list.getNode( i ) instanceof PSDtdDataElement) )
         {
            PSDtdNode currentNode = list.getNode( i );
            currentNode.setOccurrences( PSDtdNode.OCCURS_ONCE );
            newList.add( currentNode );
         }
      }
      return newList;
   }


   /** This is the tree kept in memory for the slave trees to merge into. */
   private PSDtdTree m_masterTree = null;

   /** This is used as a hidden parameter for certain private methods */
   private PSDtdTree m_slaveTree = null;

   /**  */
   private boolean m_bNodeInList = false;

}
