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

import org.w3c.dom.Element;

/**
 *  This class is used to define a table and it's columns.
 */
class RxTables
{

  RxTables()
  {
  }

  /**
  * Comparison method to get the difference between 2 tables.
  *
  * A list of columns will be returned with the colAction set.
  * If a column exits in the source but not in the target then the
  * column should be added.  If they exist in both but have different
  * types or length then the column should be modified.  If a column
  * exists in the target but does not exits in the source it will not be
  * returned in the resulting column list.
  *
  * @param targetTable - the target to compare with.
  *   Must not be <CODE>null</CODE>
  *
  * @return - A vector of RxColumns with the colAction set.
  */
  public Vector compareTables(RxTables targetTable)
  {
      if(targetTable == null || targetTable.vColumns.size() == 0)
          throw new IllegalArgumentException("Target table must not be empty.");

      Vector vColumnDiffs = new Vector();

      for(int iSourceCol = 0; iSourceCol < vColumns.size();
          ++iSourceCol)
      {
          boolean bFound = false;
          RxColumns sourceCol = (RxColumns)
                  vColumns.get(iSourceCol);

          for(int iTargetCol = 0; iTargetCol < targetTable.vColumns.size();
              ++iTargetCol)
          {
              RxColumns targetCol = (RxColumns)
                  targetTable.vColumns.get(iTargetCol);

              if(targetCol != null &&
                  sourceCol != null)
              {
                  if(targetCol.getColName().compareToIgnoreCase(
                        sourceCol.getColName()) == 0)
                  {
                      bFound = true;

                      String sTargetName = targetCol.getNativeType();
                      String sSourceName = sourceCol.getNativeType();

                      //see if the column has changed
                      if(sTargetName.compareToIgnoreCase(
                          sSourceName) != 0 ||
                          (sourceCol.getColLength() > 0 &&
                          targetCol.getColLength() !=
                          sourceCol.getColLength()))
                      {
                          System.out.println("found column diff for " + sourceCol.getColName() +
                          ". Source type: " +
                             sourceCol.getNativeType() + " Target type: " +
                              targetCol.getNativeType() + " Source length: " +
                        new Integer(sourceCol.getColLength()).toString() +
                              " Target length: " +
                              new Integer(targetCol.getColLength()).toString());

                          sourceCol.setColAction(RxColumns.MODIFY_COLUMN);
                          vColumnDiffs.add(sourceCol);
                      }

                      break;
                  }
              }
          }

          //we need to add this column
          if(!bFound)
          {
              //if this column allows nulls we can just add it
              if(sourceCol.getAllowNull())
              {
                  sourceCol.setColAction(RxColumns.ADD_COLUMN);
                  vColumnDiffs.add(sourceCol);
              }
              else
              {
                  System.out.println("Source col " + sourceCol.getColName() + " not found in target");
                  //if the column does not allow nulls then we must do a modify
                  sourceCol.setColAction(RxColumns.MODIFY_COLUMN);
                  vColumnDiffs.add(sourceCol);
              }
          }
      }

      //look for columns that we do not know about
      for(int iTargetCol = 0; iTargetCol < targetTable.vColumns.size();
          ++iTargetCol)
      {
         RxColumns targetCol = (RxColumns)
                  targetTable.vColumns.get(iTargetCol);

          boolean bFound = false;
          for(int iSourceCol = 0; iSourceCol < vColumns.size();
             ++iSourceCol)
            {
              RxColumns sourceCol = (RxColumns)
                  vColumns.get(iSourceCol);

            if(targetCol != null &&
                  sourceCol != null)
              {
                  if(targetCol.getColName().compareToIgnoreCase(
                        sourceCol.getColName()) == 0)
                  {
                      bFound = true;
                      break;
                 }
             }
            }

          //if we have not found it add it as a user column
          if(!bFound)
          {
              System.out.println("Target col " + targetCol.getColName() + " not found in source");
               targetCol.setColAction(RxColumns.USER_COLUMN);
              vColumnDiffs.add(targetCol);
          }
     }

      return(vColumnDiffs);
  }

  //mutators
  void setTableName(String pTableName){tableName = pTableName;}
   void setNewTableName(String pNewTableName){newTableName = pNewTableName;}
   void setSchema(String pSchema){schema = pSchema;}
   void setDataBase(String pDataBase){dataBase = pDataBase;}
   void setdbLink(String pDbLink){dbLink = pDbLink;}
   void setAction(String pAction){action = pAction;}
   void setDataElement(Element eElement){dataElement = eElement;}
   void setDtdElement(Element dElement){dtdElement = dElement;}
   void setTableAction(int iTableAction){tableAction=iTableAction;}
   void setDataAction(boolean bTableAction){dataAction = bTableAction;}

  //accessors
   String   getTableName(){return tableName;}
   String   getNewTableName(){return newTableName;}
   String   getSchema(){return schema;}
   String   getDataBase(){return dataBase;}
   String   getDbLink(){return dbLink;}
   String   getAction(){return action;}
   Element  getDataElement(){return dataElement;}
   Element  getDtdElement(){return dtdElement;}
   int      getTableAction(){return tableAction;}
   boolean  getDataAction(){return dataAction;}

  //member variables
  private String  tableName    = new String();
   private String  newTableName = new String();
   private String  schema       = new String();
   private String  dataBase     = new String();
   private String  dbLink       = new String();
   private String  action       = new String();
   private int     tableAction  = 0 ;
   private boolean dataAction   = false ;
   public  Vector  vColumns    = new Vector();
   public  Vector  vtSqlStmts   = new Vector();
   public  Vector  vtErrors     = new Vector();
   public  int     columnCount  = 0 ;
   public  String  sError       = new String();
   public  Element dataElement  = null ;
   public  Element dtdElement   = null ;
   public  int     success      = 0 ;
   public  int     iRowCount    = 0 ;


}
