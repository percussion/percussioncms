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
package com.percussion.rxfix.dbfixes;

import com.percussion.cms.IPSConstants;
import com.percussion.rxfix.IPSFix;
import com.percussion.rxfix.PSFixResult.Status;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSConnectionHelper;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This fix removes relationship slots that are no longer referenced. Any given
 * slot should be accessible either as an inline slot, these are constant across
 * content types, or a variant slot, which are specific to a particular content
 * type's variant.
 * 
 * <p>
 * The algorithm is to make a list of the inline slots, and then iterate over
 * all content types. For each content type, we do a query that returns the list
 * of slots. The two lists are then unioned into a new list, and that list is
 * used to find relationship records that are orphaned.
 */
public class PSFixOrphanedSlots extends PSFixDBBase implements IPSFix
{
   /**
    * Query all content type ids from the table
    */
   public static final PSStringTemplate ms_findAllTypes = new PSStringTemplate(
         "SELECT T.CONTENTTYPEID FROM {schema}.CONTENTTYPES T");

   /**
    * Query all variant slots for a given content type
    */
   public static final PSStringTemplate ms_findVariantSlots = new PSStringTemplate(
         "SELECT T.SLOTID " + "FROM {schema}.PSX_TEMPLATE V, "
               + "{schema}.PSX_CONTENTTYPE_TEMPLATE CTT, "
               + "{schema}.RXVARIANTSLOTTYPE T "
               + "WHERE V.TEMPLATE_ID = T.VARIANTID "
               + "AND V.TEMPLATE_ID = CTT.TEMPLATE_ID "
               + "AND CTT.CONTENTTYPEID = ?");

   /**
    * Find orphaned relationships who reference slots that are no longer in the
    * available slots for a given content type
    */
   public static final PSStringTemplate ms_orphanedRelationshipSlots = new PSStringTemplate(
         "SELECT DISTINCT P.RID " + "FROM {schema}.CONTENTSTATUS C, "
               + "{schema}." + IPSConstants.PSX_RELATIONSHIPS + " R, "
               + "{schema}." + IPSConstants.PSX_RELATIONSHIPPROPERTIES + " P "
               + "WHERE C.CONTENTTYPEID = ? AND "
               + "R.OWNER_ID = C.CONTENTID AND R.RID = P.RID AND "
               + "P.PROPERTYNAME = 'sys_slotid' AND "
               + "P.PROPERTYVALUE NOT IN ({valuelist})");

   /**
    * Delete specified relationship from the IPSConstants.PSX_RELATIONSHIPS
    * table
    */
   public static final PSStringTemplate ms_deleteFromRelationships = new PSStringTemplate(
         "DELETE FROM {schema}." + IPSConstants.PSX_RELATIONSHIPS
               + " WHERE RID = ?");

   /**
    * Delete specified relationship from the
    * IPSConstants.PSX_RELATIONSHIPPROPERTIES table
    */
   public static final PSStringTemplate ms_deleteFromRelationshipProps = new PSStringTemplate(
         "DELETE FROM {schema}." + IPSConstants.PSX_RELATIONSHIPPROPERTIES
               + " WHERE RID = ?");

   /**
    * Ctor
    * @throws SQLException 
    * @throws NamingException 
    */
   public PSFixOrphanedSlots() throws NamingException, SQLException
         {
      super();
   }

   @Override
   @SuppressWarnings("unchecked")
   public void fix(boolean preview)
         throws Exception
   {
      super.fix(preview);
      Connection c = PSConnectionHelper.getDbConnection();
      boolean anyproblems = false;

      try
      {
         Set inlineSlots = new HashSet();
         PreparedStatement st = PSPreparedStatement.getPreparedStatement(c,
               ms_slotIdTypes.expand(m_defDict));
         ResultSet rs = st.executeQuery();
         while (rs.next())
         {
            inlineSlots.add(new Integer(rs.getInt(1)));
         }
         rs.close();
         st.close();

         PreparedStatement deleteFromRel = PSPreparedStatement
               .getPreparedStatement(c, ms_deleteFromRelationships
                     .expand(m_defDict));

         PreparedStatement deleteFromRelProps = PSPreparedStatement
               .getPreparedStatement(c, ms_deleteFromRelationshipProps
                     .expand(m_defDict));

         st = PSPreparedStatement.getPreparedStatement(c, ms_findAllTypes
               .expand(m_defDict));
         rs = st.executeQuery();

         // Loop over the complete set of content type ids
         while (rs.next())
         {
            int contenttypeid = rs.getInt(1);

            // Get the variant slot ids
            PreparedStatement stslots = PSPreparedStatement
                  .getPreparedStatement(c, ms_findVariantSlots
                        .expand(m_defDict));
            stslots.setInt(1, contenttypeid);
            ResultSet rsslots = stslots.executeQuery();
            Set variantslots = new HashSet();
            while (rsslots.next())
            {
               variantslots.add(new Integer(rsslots.getInt(1)));
            }

            // Create the union
            Set union = new HashSet();
            union.addAll(inlineSlots);
            union.addAll(variantslots);

            // Create the query to find orphaned slots
            StringBuilder valuelist = new StringBuilder(40);
            Iterator iter = union.iterator();
            while (iter.hasNext())
            {
               Integer slotid = (Integer) iter.next();
               valuelist.append("'");
               valuelist.append(slotid.toString());
               valuelist.append("'");
               if (iter.hasNext())
                  valuelist.append(",");
            }

            Map dict = new HashMap(m_defDict);
            dict.put("valuelist", valuelist.toString());
            String orsquery = ms_orphanedRelationshipSlots.expand(dict);
            PreparedStatement storphanedrid = PSPreparedStatement
                  .getPreparedStatement(c, orsquery);
            storphanedrid.setInt(1, contenttypeid);
            ResultSet rsorphanedrid = storphanedrid.executeQuery();
            List removedRelationships = new ArrayList();

            // Loop over the orphaned relationships
            while (rsorphanedrid.next())
            {
               int rid = rsorphanedrid.getInt(1);
               removedRelationships.add(new Integer(rid));

               if (!preview)
               {
                  deleteFromRelProps.setInt(1, rid);
                  deleteFromRelProps.executeUpdate();
                  deleteFromRel.setInt(1, rid);
                  deleteFromRel.executeUpdate();
               }
            }
            rsorphanedrid.close();
            storphanedrid.close();
            if (removedRelationships.size() > 0)
            {
               anyproblems = true;
               log(preview ? Status.PREVIEW : Status.SUCCESS, 
                     Integer.toString(contenttypeid), 
                     (preview ? "Would remove " : "Removed ")
                     + " orphaned relationship records for content type"
                     );
               logDebug(idsToString(removedRelationships), "relationships");
            }
         }

         rs.close();
         st.close();

         if (anyproblems == false)
         {
            logInfo(null, "No problems found");
         }
      }
      finally
      {
         c.close();
      }
   }

   @Override
   public String getOperation()
   {
      return "Fix orphaned slots";
   }
}
