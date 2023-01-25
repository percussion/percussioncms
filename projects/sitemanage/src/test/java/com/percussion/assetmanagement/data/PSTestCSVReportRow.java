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

package com.percussion.assetmanagement.data;

import com.percussion.share.data.PSAbstractBaseCSVReportRow;

/***
 * Used to test the CSV row base class
 * @author natechadwick
 *
 */
public class PSTestCSVReportRow extends PSAbstractBaseCSVReportRow {

	public String col1;
	public String col2;
	public String col3multiline;
	public String col4empty;
	
	@Override
	public String toCSVRow() {
		return this.delimitValue(col1) + "," + this.delimitValue(col2)+ "," + this.delimitValue(col3multiline)+","+this.delimitValue(col4empty)+this.endRow();
	}

	@Override
	public String getHeaderRow() {
		// TODO Auto-generated method stub
		return null;
	}

}
