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
package com.percussion.search;

import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;


/**
 * This is a wrapper class on PSEditorChangeEvent class that has extra
 * information about whether to commit the changes or not. While indexing items
 * we commit for every set of items read from the database.  We do this to
 * avoid doing unnecessary commits that will slow down the indexing while
 * making sure we do not allow the number of events to commit to get 
 * to large.  Also we do a commit when the queue empties.
 */
public class PSSearchEditorChangeEvent extends PSEditorChangeEvent
{
   /**
    * {@link PSEditorChangeEvent#PSEditorChangeEvent(int, int, int, long)} for
    * class and all params except commit.
    * 
    * @param actionType
    * @param contentId
    * @param revisionId
    * @param contentTypeId
    * @param commit A flag to indicate whether the index needs to be committed
    * after indexing this event.
    */
   public PSSearchEditorChangeEvent(int actionType, int contentId,
         int revisionId, long contentTypeId, boolean commit)
   {
      super(actionType, contentId, revisionId, contentTypeId);
      doCommit = commit;
   }

   /**
    * {@link PSEditorChangeEvent#PSEditorChangeEvent(int, int, int, int, int, long)}
    * for class and all params except commit.
    * 
    * @param actionType
    * @param contentId
    * @param revisionId
    * @param childId
    * @param childRowId
    * @param contentTypeId
    * @param commit A flag to indicate whether the index needs to be commited
    * after indexing this event.
    */
   public PSSearchEditorChangeEvent(int actionType, int contentId,
         int revisionId, int childId, int childRowId, long contentTypeId,
         boolean commit)
   {
      super(actionType, contentId, revisionId, childId, childRowId,
            contentTypeId);
      doCommit = commit;
   }

   /**
    * {@link PSEditorChangeEvent#PSEditorChangeEvent(Element)} for
    * class and source param description.
    * @param source
    * @param commit A flag to indicate whether the index needs to be commited
    * after indexing this event.
    * @throws PSUnknownNodeTypeException
    */
   public PSSearchEditorChangeEvent(Element source, boolean commit)
      throws PSUnknownNodeTypeException
   {
      super(source);
      doCommit = commit;
   }

   /**
    * @return boolean flag that indicates whether an index needs to be
    * committed after indexing this event.  Defaults to <code>true</code>;
    */
   public boolean doesRequireCommit()
   {
      return doCommit;
   }   

   /**
    * boolean flag that indicates whether an index needs to be
    * committed after indexing this event.
    */
   private boolean doCommit =  true;
}
