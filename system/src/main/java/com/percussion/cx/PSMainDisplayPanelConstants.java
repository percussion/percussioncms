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

package com.percussion.cx;

public class PSMainDisplayPanelConstants {

	public static int focusedColumn = -1;

	/*
	    * This is a hack to get hold of the currently focused header column Should be
	    * able to access focused column with
	    * tableView.getColumnModel().getSelectionModel().getLeadSelectionIndex();
	    * This does not seem to work in this case, the column returned does not pick
	    * up changes to column with left and right keys after focus is gained with
	    * F8.To get around this we are setting a static variable on
	    * PSMainDisplayPanel from the renderer MainDisplayTableHeaderRenderer which
	    * has access to the column and a hasFocus() method while the header cell is
	    * being rendered.
	    * @return the currently focused column
	    */
	   public static synchronized int getFocusColumn() {
	      return focusedColumn;
	   }

}
