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
package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Unit test for PSJdbcTableSchema.
 */
public class PSJdbcTableSchemaTest extends TestCase
{
   public PSJdbcTableSchemaTest(String name)
   {
      super(name);
   }

   /**
    * Test the def
    */
   public void testDef() throws Exception
   {
      PSJdbcDataTypeMap dataTypeMap =  new PSJdbcDataTypeMap("MSSQL", "inetdae7",
         null);

      PSJdbcColumnDef col;
      ArrayList coldefs = new ArrayList();
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col1",
         PSJdbcTableComponent.ACTION_REPLACE, Types.CHAR, "10", true, null));
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col2",
         PSJdbcTableComponent.ACTION_CREATE, Types.DATE, "15", true, null));
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col3",
         PSJdbcTableComponent.ACTION_DELETE, Types.INTEGER, null, true, null));

      PSJdbcTableSchema tableSchema = new PSJdbcTableSchema("myTable", coldefs.iterator());
      tableSchema.setCreate(false);
      tableSchema.setAlter(true);
      tableSchema.setDelOldData(true);

      List pkcols = new ArrayList();
      pkcols.add("col1");
      pkcols.add("col2");
      PSJdbcPrimaryKey pk = new PSJdbcPrimaryKey(pkcols.iterator(),
         PSJdbcTableComponent.ACTION_REPLACE);
      tableSchema.setPrimaryKey(pk);


      List ukcols = new ArrayList();
      ukcols.add("col1");
      PSJdbcUpdateKey uk = new PSJdbcUpdateKey(ukcols.iterator());
      tableSchema.setUpdateKey(uk);

      List fkCols = new ArrayList();
      String[] fcol1 = {"col1", "etable", "ecol1"};
      String[] fcol2 = {"col2", "etable", "ecol2"};
      fkCols.add(fcol1);
      fkCols.add(fcol2);
      PSJdbcForeignKey fk = new PSJdbcForeignKey(fkCols.iterator(),
         PSJdbcTableComponent.ACTION_DELETE);
      List<PSJdbcForeignKey> fks = new ArrayList<>();
      fks.add(fk);
      tableSchema.setForeignKeys(fks);

      List indexCols = new ArrayList();
      indexCols.add("col2");
      indexCols.add("col3");
      PSJdbcIndex index1 = new PSJdbcIndex("index1", indexCols.iterator(),
         PSJdbcTableComponent.ACTION_CREATE);
      tableSchema.setIndex(index1);
      indexCols.clear();
      indexCols.add("col2");
      PSJdbcIndex index2 = new PSJdbcIndex("index1", indexCols.iterator(),
         PSJdbcTableComponent.ACTION_CREATE);
      tableSchema.setIndex(index2);


      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = tableSchema.toXml(doc);
      PSJdbcTableSchema tableSchema2 = new PSJdbcTableSchema(el, dataTypeMap);
      assertTrue(tableSchema.equals(tableSchema2));

      List dataCols = new ArrayList();
      dataCols.add(new PSJdbcColumnData("col1", "foo"));
      dataCols.add(new PSJdbcColumnData("col3", "1"));
      List dataRows = new ArrayList();
      dataRows.add(new PSJdbcRowData(dataCols.iterator(),
         PSJdbcRowData.ACTION_INSERT));
      PSJdbcTableData tableData = new PSJdbcTableData("myTable",
         dataRows.iterator());
      tableSchema.setCreate(true);
      tableSchema.setAlter(false);
      tableSchema.setTableData(tableData);
      Element defEl = tableSchema.toXml(doc);
      Element dataEl = tableData.toXml(doc);
      tableSchema2 = new PSJdbcTableSchema(defEl, dataTypeMap);
      PSJdbcTableData tableData2 = new PSJdbcTableData(dataEl);
      tableSchema2.setTableData(tableData2);
      assertTrue(tableSchema.equals(tableSchema2));

   }

    /**
     * Tests if the tableSchema has been altered and checks if a new index has
     * been created. Since the foreign key name is absent, and so checks if the
     * first column name has been used to construct the index name.Then, tests
     * if createForeignKeyIndexes property has been set to "yes".
     * 
     * @throws Exception
     */

   public void testAddingNewIndexWithoutIndex() throws Exception
   {
      PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap("MYSQL", "mysql", null);
      ArrayList<PSJdbcColumnDef> coldefs = createColumnDef(dataTypeMap);
      PSJdbcTableSchema tableSchema = createTableSchema(coldefs);
      setPrimaryKey(tableSchema);
      PSJdbcForeignKey fk = createForeignKey(tableSchema);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = tableSchema.toXml(doc);
      PSJdbcTableSchema tableSchema2 = new PSJdbcTableSchema(el, dataTypeMap);

      Iterator<?> itIndex = tableSchema2.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE);
      PSJdbcIndex index = (PSJdbcIndex) itIndex.next();
      String IndexName = index.getName();
      String fkcolname = fk.getForeignKeyColumnNames().get(0);

      // Makes sure if the table schema has changed
      assertFalse((tableSchema.equals(tableSchema2)));

      // Makes sure if a new index definition of non unique have been added to
      // table schema.
      assertFalse((tableSchema.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE)).hasNext());
      assertTrue((tableSchema2.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE)).hasNext());

      // Makes sure if the index name has taken the first foreign key column name.
      assertTrue((StringUtils.equalsIgnoreCase(IndexName, "IX_" + fkcolname)));

      // Makes sure if the createForeignkeyIndexes attribute for mysql.
      assertTrue(dataTypeMap.isCreateForeignKeyIndexes());

   }

   /**
    * Tests if the tableSchema has been altered. Tests, If the columns of new
    * index are same set as the columns of the foreign key. Lastly, checks if
    * the index is unique.
    * 
    * @throws Exception
    */

   public void testAddingNewIndex() throws Exception
   {
      PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap("MYSQL", "mysql", null);
      ArrayList<PSJdbcColumnDef> coldefs = createColumnDef(dataTypeMap);
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col3", PSJdbcTableComponent.ACTION_DELETE, Types.INTEGER, null,
              true, null));
      PSJdbcTableSchema tableSchema1 = createTableSchema(coldefs);
      setPrimaryKey(tableSchema1);
      PSJdbcForeignKey fk = createForeignKey(tableSchema1);

      List<String> indexCols = new ArrayList<String>();
      indexCols.add("col3");
      PSJdbcIndex index1 = new PSJdbcIndex("IX_col1", indexCols.iterator(), PSJdbcTableComponent.ACTION_CREATE);
      tableSchema1.setIndex(index1);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = tableSchema1.toXml(doc);
      PSJdbcTableSchema tableSchema2 = new PSJdbcTableSchema(el, dataTypeMap);
      PSJdbcIndex newIndex = (PSJdbcIndex) tableSchema2.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE).next();

      // Makes sure if the table schema has changed
      assertFalse((tableSchema1.equals(tableSchema2)));

      // Makes sure if a new index definition of non unique have been added to
      // table schema.
      assertFalse((tableSchema1.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE)).hasNext());
      assertTrue((tableSchema2.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE)).hasNext());

      // Makes sure if the number of foreign key columns and eqal to that of
      // index columns.
      assertTrue(fk.getForeignKeyColumnNames().size() == newIndex.getIndexColumnNames().size());

      // Makes sure if the new index that got created has the same set of
      // columns as foreign key.
      assertTrue(fk.getForeignKeyColumnNames().containsAll(newIndex.getIndexColumnNames()));

      // Makes sure if the index name of newly created index has a unique name.
      assertTrue(tableSchema1.doesIndexNameExists(newIndex.getName()));

   }

   /**
    * Tests if the table schema has been altered when list of foreign key
    * columns has the same columns or is super set of columns of index. Tests,
    * If the columns of new index are same set as the columns of the foreign
    * key. Lastly, checks if the index is unique.
    * 
    * @throws Exception
    */

   public void testAddingNewIndexWhenFKContainsIndexColumns() throws Exception
   {
      PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap("MYSQL", "mysql", null);
      ArrayList<PSJdbcColumnDef> coldefs = createColumnDef(dataTypeMap);
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col3", PSJdbcTableComponent.ACTION_DELETE, Types.INTEGER, null,
              true, null));
      PSJdbcTableSchema tableSchema1 = createTableSchema(coldefs);
      setPrimaryKey(tableSchema1);

      List<String[]> fkCols = new ArrayList<String[]>();
      String[] fcol1 =
      {"col1", "etable", "ecol1"};
        fkCols.add(fcol1);
      String[] fcol2 =
      {"col2", "etable", "ecol1"};
        fkCols.add(fcol2);
      PSJdbcForeignKey fk = new PSJdbcForeignKey(fkCols.iterator(), PSJdbcTableComponent.ACTION_DELETE);
      List<PSJdbcForeignKey> fks = new ArrayList<>();
      fks.add(fk);
      tableSchema1.setForeignKeys(fks);

      List<String> indexCols = new ArrayList<String>();
      indexCols.add("col2");
      PSJdbcIndex index1 = new PSJdbcIndex("IX_Name", indexCols.iterator(), PSJdbcTableComponent.ACTION_CREATE);
      tableSchema1.setIndex(index1);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = tableSchema1.toXml(doc);
      PSJdbcTableSchema tableSchema2 = new PSJdbcTableSchema(el, dataTypeMap);
      PSJdbcIndex newIndex = (PSJdbcIndex) tableSchema2.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE).next();

      // Makes sure if the table schema has changed
      assertFalse((tableSchema1.equals(tableSchema2)));

      // Makes sure if a new index definition of non unique have been added to
      // table schema.
      assertFalse((tableSchema1.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE)).hasNext());
      assertTrue((tableSchema2.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE)).hasNext());

      // Makes sure if the new index that got created has the same set of
      // columns as foreign key.
      assertTrue(fk.getForeignKeyColumnNames().containsAll(newIndex.getIndexColumnNames()));

      // Makes sure if the index name of newly created index has a unique name.
      assertTrue(tableSchema1.doesIndexNameExists(newIndex.getName()));

   }

   /**
    * Tests if the tableSchema has been altered.Lastly, checks if the index is
    * unique when the index contains all foreign key columns.
    * 
    * @throws Exception
    */

   public void testAddingNewIndexWhenIndexContainsFKColumns() throws Exception
   {
      PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap("MYSQL", "mysql", null);
      ArrayList<PSJdbcColumnDef> coldefs = createColumnDef(dataTypeMap);
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col3", PSJdbcTableComponent.ACTION_DELETE, Types.INTEGER, null,
              true, null));
      PSJdbcTableSchema tableSchema1 = createTableSchema(coldefs);
      setPrimaryKey(tableSchema1);

      List<String[]> fkCols = new ArrayList<String[]>();
      String[] fcol1 =
      {"col1", "etable", "ecol1"};
      fkCols.add(fcol1);
      PSJdbcForeignKey fk = new PSJdbcForeignKey(fkCols.iterator(), PSJdbcTableComponent.ACTION_DELETE);
      List<PSJdbcForeignKey> fks = new ArrayList<>();
      fks.add(fk);
      tableSchema1.setForeignKeys(fks);

      List<String> indexCols = new ArrayList<String>();
      indexCols.add("col1");
      indexCols.add("col2");
      PSJdbcIndex index1 = new PSJdbcIndex("IX_Name", indexCols.iterator(), PSJdbcTableComponent.ACTION_CREATE);
      tableSchema1.setIndex(index1);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = tableSchema1.toXml(doc);
      PSJdbcTableSchema tableSchema2 = new PSJdbcTableSchema(el, dataTypeMap);
      PSJdbcIndex newIndex = (PSJdbcIndex) tableSchema2.getIndexes().next();

      // Makes sure if the table schema has not changed
      assertTrue((tableSchema1.equals(tableSchema2)));

   }

   /**
    * Tests if the tableSchema has been altered. Also, checks if the index is
    * unique.
    * 
    * @throws Exception
    */

   public void testAddingNewIndexWithFKName() throws Exception
   {
      PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap("MYSQL", "mysql", null);
      ArrayList<PSJdbcColumnDef> coldefs = createColumnDef(dataTypeMap);
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col3", PSJdbcTableComponent.ACTION_DELETE, Types.INTEGER, null,
              true, null));
      PSJdbcTableSchema tableSchema1 = createTableSchema(coldefs);
      setPrimaryKey(tableSchema1);

      String foreignKeyName = "FK_Name";
      List<String[]> fkCols = new ArrayList<String[]>();
      String[] fcol1 =
      {"col1", "etable", "ecol1"};
      fkCols.add(fcol1);
      PSJdbcForeignKey fk = new PSJdbcForeignKey(foreignKeyName, fkCols.iterator(),
              PSJdbcTableComponent.ACTION_DELETE);
      List<PSJdbcForeignKey> fks = new ArrayList<>();
      fks.add(fk);
      tableSchema1.setForeignKeys(fks);

      List<String> indexCols = new ArrayList<String>();
      indexCols.add("col2");
      PSJdbcIndex index1 = new PSJdbcIndex("IX_Name", indexCols.iterator(), PSJdbcTableComponent.ACTION_CREATE);
      tableSchema1.setIndex(index1);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = tableSchema1.toXml(doc);
      PSJdbcTableSchema tableSchema2 = new PSJdbcTableSchema(el, dataTypeMap);
      PSJdbcIndex newIndex = (PSJdbcIndex) tableSchema2.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE).next();

      // Makes sure if the table schema has changed.
      assertFalse((tableSchema1.equals(tableSchema2)));

      // Makes sure if the new index that got created has the same set of
      // columns as foreign key.
      assertTrue(fk.getForeignKeyColumnNames().containsAll(newIndex.getIndexColumnNames()));

      // Makes sure if the index name of newly created index has a unique name.
      assertTrue(tableSchema1.doesIndexNameExists(newIndex.getName()));
   }
    
    
   /**
    * Sets a foreign key for a given table schema
    * 
    * @param tableschema table schema object
    * 
    * @return fk Foreign Key definition 
    * 
    * @throws PSJdbcTableFactoryException if error occurs
    */
    
   private PSJdbcForeignKey createForeignKey(PSJdbcTableSchema tableSchema) throws PSJdbcTableFactoryException
   {
      List<String[]> fkCols = new ArrayList<String[]>();
      String[] fcol1 =
      {"col1", "etable", "ecol1"};
      fkCols.add(fcol1);
      String[] fcol2 =
      {"col2", "etable", "ecol1"};
      fkCols.add(fcol2);

      PSJdbcForeignKey fk = new PSJdbcForeignKey(fkCols.iterator(), PSJdbcTableComponent.ACTION_DELETE);
       List<PSJdbcForeignKey> fks = new ArrayList<>();
       fks.add(fk);
      tableSchema.setForeignKeys(fks);
      return fk;
   }
    
   /**
    * Sets a primary key for a given table schema
    * 
    * @param tableschema table schema object
    * 
    * @throws PSJdbcTableFactoryException if error occurs
    */

   private void setPrimaryKey(PSJdbcTableSchema tableSchema) throws PSJdbcTableFactoryException
   {
      List<String> pkcols = new ArrayList<String>();
      pkcols.add("col1");
      PSJdbcPrimaryKey pk = new PSJdbcPrimaryKey(pkcols.iterator(), PSJdbcTableComponent.ACTION_REPLACE);
      tableSchema.setPrimaryKey(pk);
   }
    
   /**
    * Creates a table schema from column definitions
    * 
    * @param colDefs columns definition object
    * 
    * @return tableSchema Table schema object
    * 
    * @throws PSJdbcTableFactoryException if error occurs
    */

   private PSJdbcTableSchema createTableSchema(ArrayList<PSJdbcColumnDef> colDefs) throws PSJdbcTableFactoryException
   {
      PSJdbcTableSchema tableSchema = new PSJdbcTableSchema("myTable", colDefs.iterator());
      tableSchema.setCreate(false);
      tableSchema.setAlter(true);
      tableSchema.setDelOldData(true);
      return tableSchema;
   }
    
   /**
    * Creates a column definition from a dataTypeMap
    * 
    * @param dataTypeMap data type map object
    * 
    * @returns coldefs Column definition object
    */

   private ArrayList<PSJdbcColumnDef> createColumnDef(PSJdbcDataTypeMap dataTypeMap)
   {
      ArrayList<PSJdbcColumnDef> coldefs = new ArrayList<PSJdbcColumnDef>();
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col1", PSJdbcTableComponent.ACTION_REPLACE, Types.CHAR, "10",
              true, null));
      coldefs.add(new PSJdbcColumnDef(dataTypeMap, "col2", PSJdbcTableComponent.ACTION_CREATE, Types.DATE, "15",
              true, null));
      return coldefs;
   }

    // collect all tests into a TestSuite and return it
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new PSJdbcTableSchemaTest("testDef"));
        return suite;
    }

}
