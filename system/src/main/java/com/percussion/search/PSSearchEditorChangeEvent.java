/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
