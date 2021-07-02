/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.taxonomy;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.taxonomy.domain.Attribute_lang;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.jexl.TaxAttMap;
import com.percussion.taxonomy.jexl.TaxNode;
import com.percussion.taxonomy.jexl.TaxNodeList;
import com.percussion.webservices.PSErrorException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Tools for extracting Taxonomy Information
 * 
 * @author Aaron and Shawn
 * 
 */
public class TaxonomyJexl extends PSJexlUtilBase implements IPSJexlExpression
{

   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(TaxonomyJexl.class);

   /**
    * Default constructor.
    */
   public TaxonomyJexl()
   {
   }

   /**
    * Returns a list of taxon attributes for a given taxonomy name or id
    * 
    * @param taxonomy_name  Object The node can be passed as Integer or any Object
    *           where the toString can be converted to an Integer.  If the value cannot be mapped to an Integer then the
    *           Taxonomy is located by name
    * @return a map of taxon attributes with some helper functions {@see TaxAttMap} where the key is the attribute name for a given taxonomy name or id
    * @throws PSErrorException
    * @throws PSExtensionProcessingException
    */
   @IPSJexlMethod(description = "returns a list(TaxAttMap) of taxon attributes given a Taxonomy Name or Taxonomy Id", params =
   {@IPSJexlParam(name = "taxonomy_name", description = "the taxonomy id or taxonomy name")})
   public TaxAttMap getTaxonomyAtrributes(Object taxonomy_name) throws PSErrorException, PSExtensionProcessingException
   {
      TaxAttMap ret = new TaxAttMap();
      List<Attribute_lang> attLang = new ArrayList<Attribute_lang>();
      String taxName = "";
      int taxID = NumberUtils.toInt(taxonomy_name.toString());
      if (taxID == 0)
      {
         taxName = taxonomy_name.toString();
      }
      int langID = 1; // TODO how do we look this up?

      Session session = null;

      try
      {
         session = TaxonomyDBHelper.getSessionFactory().openSession();
         Transaction tx = session.beginTransaction();
         String queryString = "select al from Attribute_lang as al ";
         queryString += "inner join fetch al.attribute as a ";
         queryString += "inner join fetch al.language as l ";
         if (taxID > 0)
         {
            queryString += "where a.taxonomy.id = ? ";
         }
         else
         {
            queryString += "where a.taxonomy.Name = ? ";
         }
         queryString += "and l.id = ? order by a.id";

         Query q = session.createQuery(queryString);
         if (taxID > 0)
         {
            q.setInteger(0, taxID);
         }
         else
         {
            q.setString(0, taxName);
         }
         q.setInteger(1, langID);
         attLang = (List<Attribute_lang>) q.list();
         ret = new TaxAttMap(attLang);
         tx.commit();
      }
      catch (HibernateException e)
      {
         throw new PSExtensionProcessingException(new PSExtensionProcessingException(e.getMessage(), e));
      }
      finally
      {
         try
         {
            session.close();

         }
         catch (HibernateException e)
         {
            throw new PSExtensionProcessingException(new PSExtensionProcessingException(e.getMessage(), e));
         }
      }

      return ret;

   }

   /**
    * Returns a list of related nodes (taxons) for a given node_id and
    * relationship_type_name. e.g. 'Related To'
    * 
    * @param node_id Object The node can be passed as Integer or any Object
    *           where the toString can be converted to an Integer
    * @param relationship_type_name
    * @return List of related nodes {@see TaxNodeList}
    * @throws PSErrorException
    * @throws PSExtensionProcessingException
    */
   @IPSJexlMethod(description = "returns a list(TaxNodeList) of related Taxons for a Node Id (String or Integer) and relationship Type e.g 'Related To'", params =
   {@IPSJexlParam(name = "node_id", description = "the node_id an individual id any Object where the toString can be converted to an Integer"),
         @IPSJexlParam(name = "relationship_type_name", description = "relationship_type_name 'Similar' or 'Related To'")})
   public TaxNodeList getRelatedNodes(Object node_id, String relationship_type_name) throws PSErrorException,
         PSExtensionProcessingException
   {

      // toInt returns 0 if it cannot convert
      int nodeID = NumberUtils.toInt(StringUtils.trimToEmpty(node_id.toString()));

      TaxNodeList ret = new TaxNodeList();
      List<Node> nodes = new ArrayList<Node>();

      int langID = 1; // TODO how do we look this up?

      Session session = null;

      try
      {
         session = TaxonomyDBHelper.getSessionFactory().openSession();
         Transaction tx = session.beginTransaction();

         String queryString = "select rn.related_node from Related_node as rn ";
         queryString += "inner join  rn.relationship as rt ";
         queryString += "where rn.node.id = ? and rt.Relationship_type = ?";

         Query q = session.createQuery(queryString);
         q.setInteger(0, nodeID);
         q.setString(1, relationship_type_name);
         nodes = (List<Node>) q.list();
         for (Node node : nodes)
         {
            TaxNode taxNode = new TaxNode(node);
            ret.add(taxNode);
         }
         tx.commit();
      }
      catch (HibernateException e)
      {
         throw new PSExtensionProcessingException(new PSExtensionProcessingException(e.getMessage(), e));
      }
      finally
      {
         try
         {
            session.close();

         }
         catch (HibernateException e)
         {
            throw new PSExtensionProcessingException(new PSExtensionProcessingException(e.getMessage(), e));
         }
      }

      return ret;

   }

   /**
    * Returns a list of taxonomy nodes (taxons) for from a string of nodes IDs
    * 
    * @param node_ids - An Object or Collection of Objects where the toString
    *           can be converted to an Integer or a comma separated list of ids
    * @return list of nodes with some helper functions {@see TaxNodeList}
    * @throws PSErrorException
    * @throws PSExtensionProcessingException
    */
   @IPSJexlMethod(description = "returns a list of taxonomy nodes (TaxNodeList) for from a comma separated string of nodes ID(s) or integer", params =
   {@IPSJexlParam(name = "node_ids", description = "An Object or Collection of Objects where the toString can be converted to an Integer or a comma separated list of ids")})
   public TaxNodeList getNodesFromIds(Object node_ids) throws PSErrorException, PSExtensionProcessingException
   {

      ArrayList<Integer> the_ids_array = convertParamToArrayList(node_ids);

      if (the_ids_array.size() == 0)
      {
         return new TaxNodeList();
      }
      List<Node> nodes = new ArrayList<Node>();
      TaxNodeList ret = new TaxNodeList();
      int langID = 1; // TODO how do we look this up?

      Session session = null;

      try
      {
         session = TaxonomyDBHelper.getSessionFactory().openSession();
         Transaction tx = session.beginTransaction();
         String queryString = "select distinct n from Node n ";
         queryString += "left join fetch n.taxonomy ";
         queryString += "left join fetch n.nodeEditors ne ";
         queryString += "left join fetch n.relatedNodesForNodeId rn ";
         queryString += "left join fetch n.node_status ";
         queryString += "join fetch n.values v ";
         queryString += "join fetch v.attribute a ";
         queryString += "left join fetch rn.relationship rt ";
         queryString += "left join fetch a.attribute_langs al ";
         queryString += "join fetch al.language ";
         queryString += "join fetch v.lang ";
         queryString += "where ";
         // we already checked for sql safeness above
         queryString += "n.id in (" + StringUtils.join(the_ids_array, ",") + ") ";
         queryString += "and al.language.id = ? ";
         queryString += "and v.lang.id = ? order by n.id";

         Query q = session.createQuery(queryString);
         q.setInteger(0, langID);
         q.setInteger(1, langID);
         nodes = (List<Node>) q.list();

         for (Node node : nodes)
         {
            TaxNode taxNode = new TaxNode(node);
            ret.add(taxNode);
         }
         tx.commit();
      }
      catch (HibernateException e)
      {
         throw new PSExtensionProcessingException(new PSExtensionProcessingException(e.getMessage(), e));
      }
      finally
      {
         try
         {
            session.close();

         }
         catch (HibernateException e)
         {
            throw new PSExtensionProcessingException(new PSExtensionProcessingException(e.getMessage(), e));
         }
      }

      return ret;

   }

   

   /**
    * Returns a list of taxonomy nodes (taxons) for a given taxonomy name
    * All Taxons are loaded in memory normally you should use {@link #getNodesFromIds(Object)}
    * as this may slow down page assembly for large Taxonomies.
    * 
    * @param taxonomy_name  Object The node can be passed as Integer or any Object
    *           where the toString can be converted to an Integer.  If the value cannot be mapped to an Integer then the
    *           Taxonomy is located by name
    * @return list of nodes with some helper functions {@see TaxNodeList}
    * @throws PSErrorException
    * @throws PSExtensionProcessingException
    */
   @IPSJexlMethod(description = "returns a list of taxonomy nodes (TaxNodeList) for a given taxonomy name or id", params =
   {@IPSJexlParam(name = "taxonomy_name", description = "the taxonomy name or id")})
   public TaxNodeList getTaxonomyNodes(Object taxonomy_name) throws PSErrorException, PSExtensionProcessingException
   {
      TaxNodeList ret = new TaxNodeList();
      List<Node> nodes = new ArrayList<Node>();
      String taxName = "";
      int taxID = NumberUtils.toInt(taxonomy_name.toString());
      if (taxID == 0)
      {
         taxName = taxonomy_name.toString();
      }
      int langID = 1; // TODO how do we look this up?

      Session session = null;

      try
      {
         session = TaxonomyDBHelper.getSessionFactory().openSession();
         Transaction tx = session.beginTransaction();
         
         String queryString = "select distinct n from Node n ";
         queryString += "left join fetch n.taxonomy ";
         queryString += "left join fetch n.nodeEditors ne ";
         queryString += "left join fetch n.relatedNodesForNodeId rn ";
         queryString += "left join fetch n.node_status ";
         queryString += "join fetch n.values v ";
         queryString += "join fetch v.attribute a ";
         queryString += "left join fetch rn.relationship rt ";
         queryString += "left join fetch a.attribute_langs al ";
         queryString += "join fetch al.language ";
         queryString += "join fetch v.lang ";
         queryString += "where ";
         // we already checked for sql safeness above
         if (taxID > 0)
         {
            queryString += "n.taxonomy.id = ? ";
         }
         else
         {
            queryString += "n.taxonomy.Name = ? ";
         }
         queryString += "and al.language.id = ? ";
         queryString += "and v.lang.id = ? order by n.id";
        
         Query q = session.createQuery(queryString);
         if (taxID > 0)
         {
            q.setInteger(0, taxID);
         }
         else
         {
            q.setString(0, taxName);
         }
         q.setInteger(1, langID);
         q.setInteger(2, langID);
         nodes = (List<Node>) q.list();
         for (Node node : nodes)
         {
            TaxNode taxNode = new TaxNode(node);
            ret.add(taxNode);
         }
         tx.commit();
      }
      catch (HibernateException e)
      {
         throw new PSExtensionProcessingException(new PSExtensionProcessingException(e.getMessage(), e));
      }
      finally
      {
         try
         {
            session.close();

         }
         catch (HibernateException e)
         {
            throw new PSExtensionProcessingException(new PSExtensionProcessingException(e.getMessage(), e));
         }
      }

      return ret;

   }
   
   /**
    * Allow Jexl Function to handle multiple object types, check for type
    * And return a list of integers.  This can handle comma seperated list as well
    * as Collection, and an Integer represented as itself or a String
    * @param node_ids the Object to convert
    * @return The list of integers
    */
   private ArrayList<Integer> convertParamToArrayList(Object node_ids)
   {
      ArrayList<Integer> the_ids_array = new ArrayList<Integer>();
      if (node_ids instanceof String)
      {
         String node_idsStr = (String) node_ids;
         for (String s_id : StringUtils.split(StringUtils.trimToEmpty(node_idsStr), ","))
         {
            the_ids_array.add(NumberUtils.toInt(StringUtils.trimToEmpty(s_id)));
         }
      }
      else if (node_ids instanceof Integer)
      {
         the_ids_array.add((Integer) node_ids);
      }
      else if (node_ids instanceof Collection)
      {
         Collection<?> idsCol = (Collection<?>) node_ids;
         for (Object o : idsCol)
         {
            if (o instanceof String)
            {
               StringUtils.trimToEmpty((String) o);
            }
            else if (o instanceof Integer)
            {
               the_ids_array.add((Integer) o);
            }

         }
      }
      return the_ids_array;
   }
}
