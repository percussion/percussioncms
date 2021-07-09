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
package com.percussion.tablefactory;

import java.util.Vector;

/**
* This class is used to define a column
*/
public class RxColumns
{
  RxColumns()
  {
  }

  //mutators
  void setColName(String pcolName){colName = pcolName;}
  void setJdbcDataType(String pdataType){dataType = pdataType;}
  void setColNo(int pcolNo){colNo = pcolNo;}
  void setNativeType(String pnativeType){nativeType = pnativeType;}
  void setColLength(int pnativeColLength){nativeColLength = pnativeColLength;}
  void setKey(boolean pbKey){bKey = pbKey;}
  void setAllowNull(boolean pallowNull){allowNull = pallowNull;}
  void setOrdinalPosition(int pOrdinal){ordPosition = pOrdinal;}
  void setDecimal(int pDecimal){decimal = pDecimal;}
  void setColValue(String pColValue){colValue = pColValue;}
  void setColAction(int pColAction){colAction = pColAction;}

  //accessors
  String  getColName(){return colName;}
  String  getJdbcDataType(){return dataType;}
  int     getColNo(){return colNo;}
  String  getNativeType(){return nativeType;}
  int     getColLength(){return nativeColLength;}
  boolean getKey(){return bKey;}
  boolean getAllowNull(){return allowNull;}
  int     getOrdinalPosition(){return ordPosition;}
  int     getDecimal(){return decimal;}
  String  getColValue(){return colValue;}
  int     getColAction(){return colAction;}
  public String getColumnDef()
   {
      StringBuffer buf = new StringBuffer();

      buf.append(colName);
      buf.append(" ");
      buf.append(nativeType);
      if (nativeColLength > 0)
      {
         buf.append("(");
         buf.append(new Integer(nativeColLength).toString());
         buf.append(")");
      }

      String nullClause = "";
      if (allowNull)
         nullClause = " NULL";
      else
         nullClause = " NOT NULL";

      buf.append(nullClause);

      return buf.toString();
   }


  //member variables
  private String  colName         = new String();
  private String  dataType        = new String();
  private int     colNo           = 0 ;
  private String  nativeType      = new String();
  private int     nativeColLength = 0 ;
  private boolean bKey            = false ;
  private boolean allowNull       = false ;
  private int     ordPosition     = 0 ;
  private int     decimal         = 0 ;
  private int     colAction       = 0;
  private String  colValue        = new String();
  public  Vector  vtOldValues     = new Vector();

  public static final int ADD_COLUMN = 1;
  public static final int MODIFY_COLUMN = 2;
  public static final int DELETE_COLUMN = 3;
  public static final int USER_COLUMN = 4;
  public static final int NO_ACTION = 0;
}

