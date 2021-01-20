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

package com.percussion.data;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * This class is used to provide context info to our
 * PSSqlXXXBuilder classes.
 */
class PSSqlBuilderContext
{
   /**
    * Construct a new builder context using a PSStatementBlock as the
    * initial block.
    */
   PSSqlBuilderContext()
   {
      super();
      m_curGroup = null;
      m_curBlock = new PSStatementBlock(true);
      m_blocks = new ArrayList();
      m_buf = new ByteArrayOutputStream();
      m_text = new PrintWriter(m_buf);

      m_blocks.add(m_curBlock);
   }

   /**
    * Add the specified text to the current block.
    *
    * @param str the text to add to the current block, may be <code>null</code>
    * or empty. If <code>null</code> then "null" text is added.
    *
    * @param closeRun <code>true</code> if no more text is to be added to the
    * current statement block, <code>false</code> otherwise.
    */
   void addText(String str, boolean closeRun)
   {
      m_text.print(str);
      if (closeRun)
         closeTextRun();
   }

   /**
    * Convenience method that calls
    * {@link #addText(String, boolean) addText(str, false)}
    */
   void addText(String str)
   {
      addText(str, false);
   }

   /**
    * Creates a new block of the specified type.
    *
    * @param type the type of block to create, should be one of the
    * <code>BLOCK_TYPE_XXX</code> vallues
    *
    * @param isStaticBlock <code>true</code> if this block should be omitted
    * if <code>null</code>, <code>false</code> otherwise
    *
    * @param addToList if this block should be added to the internal list of
    * blocks, <code>false</code> otherwise. If <code>true</code> then this
    * block will be contained in the array returned by <code>getBlocks()</code>
    * method, otherwise not
    */
   private void newBlock(int type, boolean isStaticBlock, boolean addToList)
   {
      closeTextRun();   // make sure there's no stray data

      verifyBlockType(type);
      /* start a new block for this clause */
      switch (type)
      {
         case BLOCK_TYPE_STATEMENT:
            m_curBlock = new PSStatementBlock(isStaticBlock);
            break;

         case BLOCK_TYPE_FUNCTION:
            m_curBlock = new PSFunctionBlock(isStaticBlock);
            break;
      }
      if (addToList)
         m_blocks.add(m_curBlock);
   }

   /**
    * Convenience method that calls
    * {@link #newBlock(int, boolean, boolean)
    * newBlock(BLOCK_TYPE_STATEMENT, isStaticBlock, addToList)}
    */
   private void newBlock(boolean isStaticBlock, boolean addToList)
   {
      newBlock(BLOCK_TYPE_STATEMENT, isStaticBlock, addToList);
   }

   /**
    * Convenience method that calls
    * {@link #newBlock(int, boolean, boolean)
    * newBlock(type, isStaticBlock, true)}
    */
   void newBlock(int type, boolean isStaticBlock)
   {
      newBlock(type, isStaticBlock, true);
   }

   /**
    * Convenience method that calls
    * {@link #newBlock(int, boolean)
    * newBlock(BLOCK_TYPE_STATEMENT, isStaticBlock)}
    */
   void newBlock(boolean isStaticBlock)
   {
      newBlock(BLOCK_TYPE_STATEMENT, isStaticBlock);
   }

   /**
    * Convenience method that calls
    * {@link #newBlock(int, boolean) newBlock(type, true)}
    */
   void newBlock(int type)
   {
      newBlock(type, true);
   }

   /**
    * Convenience method that calls
    * {@link #newBlock(int) newBlock(BLOCK_TYPE_STATEMENT)}
    */
   void newBlock()
   {
      newBlock(BLOCK_TYPE_STATEMENT);
   }

   /**
    * Creates a new statement group (<code>PSStatementGroup</code>). A
    * statement group can contain other statements or statement groups.
    *
    * @param prefix the prefix to be added before the string form of the
    * statement group (for example - "WHERE"), may be <code>null</code> or
    * empty if no string is to be prepended to the statement group
    */
   void newGroup(String prefix)
   {
      closeTextRun();      // make sure there's no stray data
      m_curBlock = null;   // when a group starts, it has no block until
                           // newGroupBlock is called

      // we need to convert the right block to a group
      m_curGroup = new PSStatementGroup(prefix, null, null, null);
      m_blocks.add(m_curGroup);
   }

   /**
    * Creates a new block and adds it to the current statement group.
    *
    * @param type the type of block to create, should be one of the
    * <code>BLOCK_TYPE_XXX</code> vallues
    *
    * @param isStaticBlock <code>true</code> if this block should be omitted
    * if <code>null</code>, <code>false</code> otherwise
    *
    * @param separator the statement block sepator (for example, AND, OR etc),
    * may be <code>null</code> or empty
    */
   void newGroupBlock(int type, boolean isStaticBlock, String separator)
   {
      // create the new block
      newBlock(type, isStaticBlock, false);

      // and place it in the appropriate place
      if (m_curGroup.getLeftBlock() == null)
      {
         m_curGroup.setLeftBlock(m_curBlock);
         m_curGroup.setBlockSeparator(separator);
      }
      else
      {
         // right block == null
         // we need to create the right block as a group
         // we may end up with one group with no right side in the end,
         // but it's the cleanest way to deal with possible mixtures of
         // separators, etc.
         PSStatementGroup grp = new PSStatementGroup(
            null, m_curBlock, separator, null);
         m_curGroup.setRightBlock(grp);
         m_curGroup = grp;
      }
   }

   /**
    * Convenience method that calls
    * {@link #newGroupBlock(int, boolean, String)
    * newGroupBlock(BLOCK_TYPE_STATEMENT, isStaticBlock, separator)}
    */
   void newGroupBlock(boolean isStaticBlock, String separator)
   {
      newGroupBlock(BLOCK_TYPE_STATEMENT, isStaticBlock, separator);
   }

   /**
    * Closes the current group and current block. Once closed, no more text
    * or replacement value can be added to the block. And no more statement
    * block or group can be added to the group.
    */
   void closeGroup()
   {
      closeTextRun();   // make sure there's no stray data
      m_curGroup = null;
      m_curBlock = null;
   }

   /**
    * Used to flush the text string created using the <code>addText()</code>
    * methods to the current statement block.
    */
   void closeTextRun()
   {
      m_text.flush();
      if (m_buf.size() > 0)
      {
         if (m_curBlock != null)
            m_curBlock.addText(m_buf.toString());
         m_buf.reset();
      }
   }

   /**
    * Conenience method which calls
    * {@link #addReplacementField(value, datatype, col, null)}
    * See that method for details.
    */
   void addReplacementField(IPSReplacementValue value,
      int datatype, PSBackEndColumn col)
   {
      addReplacementField(value, datatype, col, null);
   }

   /**
    * Add a replacement field to the current statement block.
    *
    * @param   value    The replacement value.  Never <code>null</code>.
    *
    * @param   datatype The jdbc datatype for the replacement value.
    *
    * @param   col      The back end column associated with this replacement
    *                   field.
    *
    * @param   lci      The Lob column initializer to be used when dealing
    *                   with LOB-based column types.  Can be <code>null</code>.
    *
    * @throws IllegalStateException if this context is in an invalid state
    * such that the current block is <code>null</code>
    */
   void addReplacementField(IPSReplacementValue value, int datatype,
      PSBackEndColumn col, IPSLobColumnInitializer lci)
   {
      Object[]params = new Object[]{new Integer(datatype), col, lci};
      addReplacementField(value, params);
   }

   /**
    * Add a replacement field to the current block.
    *
    * @param value the replacement value to add to the block, may not be
    * <code>null</code>.
    *
    * @param params current block specific implementation parameters, may be
    * <code>null</code> or empty if the implementation does not require these
    * parameters
    *
    * @throws IllegalStateException if this context is in an invalid state
    * such that the current block is <code>null</code>
    */
   public void addReplacementField(IPSReplacementValue value, Object[]params)
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");

      if (m_curBlock == null)
         throw new IllegalStateException(
            "Invalid state. Current block is null");

      // close the preceding text run before adding fields
      closeTextRun();
      m_curBlock.addReplacementField(value, params);
   }

   /**
    * Returns all the statement blocks in this context.
    *
    * @return the statement blocks contained in this context, never
    * <code>null</code>, may be empty
    */
   IPSStatementBlock[] getBlocks()
   {
      closeTextRun();   // make sure there's no stray data

      // then create the array and return it
      IPSStatementBlock[] ret = new IPSStatementBlock[m_blocks.size()];
      m_blocks.toArray(ret);
      return ret;
   }

   /**
    * Verify the type of block.
    *
    * @param blockType should be one of <code>BLOCK_TYPE_XXX</code> values.
    */
   public void verifyBlockType(int blockType)
   {
      if (!((blockType == BLOCK_TYPE_STATEMENT) ||
         (blockType == BLOCK_TYPE_FUNCTION)))
      {
         throw new IllegalArgumentException("Invalid block type specified");
      }
   }

   private PSStatementGroup      m_curGroup;
   private IPSStatementBlock      m_curBlock;
   private ArrayList               m_blocks;
   private ByteArrayOutputStream   m_buf;
   private PrintWriter            m_text;

   /**
    * block is an instance of <code>PSStatementBlock</code>
    */
   public static final int BLOCK_TYPE_STATEMENT = 0;

   /**
    * block is an instance of <code>PSFunctionBlock</code>
    */
   public static final int BLOCK_TYPE_FUNCTION = 1;

}

