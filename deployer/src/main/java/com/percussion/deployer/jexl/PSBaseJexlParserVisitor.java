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
package com.percussion.deployer.jexl;

import org.apache.commons.jexl3.parser.ASTAddNode;
import org.apache.commons.jexl3.parser.ASTAndNode;
import org.apache.commons.jexl3.parser.ASTAnnotatedStatement;
import org.apache.commons.jexl3.parser.ASTAnnotation;
import org.apache.commons.jexl3.parser.ASTArguments;
import org.apache.commons.jexl3.parser.ASTArrayAccess;
import org.apache.commons.jexl3.parser.ASTArrayLiteral;
import org.apache.commons.jexl3.parser.ASTAssignment;
import org.apache.commons.jexl3.parser.ASTBitwiseAndNode;
import org.apache.commons.jexl3.parser.ASTBitwiseComplNode;
import org.apache.commons.jexl3.parser.ASTBitwiseOrNode;
import org.apache.commons.jexl3.parser.ASTBitwiseXorNode;
import org.apache.commons.jexl3.parser.ASTBlock;
import org.apache.commons.jexl3.parser.ASTBreak;
import org.apache.commons.jexl3.parser.ASTConstructorNode;
import org.apache.commons.jexl3.parser.ASTContinue;
import org.apache.commons.jexl3.parser.ASTDivNode;
import org.apache.commons.jexl3.parser.ASTEQNode;
import org.apache.commons.jexl3.parser.ASTERNode;
import org.apache.commons.jexl3.parser.ASTEWNode;
import org.apache.commons.jexl3.parser.ASTEmptyFunction;
import org.apache.commons.jexl3.parser.ASTEmptyMethod;
import org.apache.commons.jexl3.parser.ASTExtendedLiteral;
import org.apache.commons.jexl3.parser.ASTFalseNode;
import org.apache.commons.jexl3.parser.ASTForeachStatement;
import org.apache.commons.jexl3.parser.ASTFunctionNode;
import org.apache.commons.jexl3.parser.ASTGENode;
import org.apache.commons.jexl3.parser.ASTGTNode;
import org.apache.commons.jexl3.parser.ASTIdentifier;
import org.apache.commons.jexl3.parser.ASTIdentifierAccess;
import org.apache.commons.jexl3.parser.ASTIfStatement;
import org.apache.commons.jexl3.parser.ASTJexlScript;
import org.apache.commons.jexl3.parser.ASTJxltLiteral;
import org.apache.commons.jexl3.parser.ASTLENode;
import org.apache.commons.jexl3.parser.ASTLTNode;
import org.apache.commons.jexl3.parser.ASTMapEntry;
import org.apache.commons.jexl3.parser.ASTMapLiteral;
import org.apache.commons.jexl3.parser.ASTMethodNode;
import org.apache.commons.jexl3.parser.ASTModNode;
import org.apache.commons.jexl3.parser.ASTMulNode;
import org.apache.commons.jexl3.parser.ASTNENode;
import org.apache.commons.jexl3.parser.ASTNEWNode;
import org.apache.commons.jexl3.parser.ASTNRNode;
import org.apache.commons.jexl3.parser.ASTNSWNode;
import org.apache.commons.jexl3.parser.ASTNotNode;
import org.apache.commons.jexl3.parser.ASTNullLiteral;
import org.apache.commons.jexl3.parser.ASTNumberLiteral;
import org.apache.commons.jexl3.parser.ASTOrNode;
import org.apache.commons.jexl3.parser.ASTRangeNode;
import org.apache.commons.jexl3.parser.ASTReference;
import org.apache.commons.jexl3.parser.ASTReferenceExpression;
import org.apache.commons.jexl3.parser.ASTReturnStatement;
import org.apache.commons.jexl3.parser.ASTSWNode;
import org.apache.commons.jexl3.parser.ASTSetAddNode;
import org.apache.commons.jexl3.parser.ASTSetAndNode;
import org.apache.commons.jexl3.parser.ASTSetDivNode;
import org.apache.commons.jexl3.parser.ASTSetLiteral;
import org.apache.commons.jexl3.parser.ASTSetModNode;
import org.apache.commons.jexl3.parser.ASTSetMultNode;
import org.apache.commons.jexl3.parser.ASTSetOrNode;
import org.apache.commons.jexl3.parser.ASTSetSubNode;
import org.apache.commons.jexl3.parser.ASTSetXorNode;
import org.apache.commons.jexl3.parser.ASTSizeFunction;
import org.apache.commons.jexl3.parser.ASTSizeMethod;
import org.apache.commons.jexl3.parser.ASTStringLiteral;
import org.apache.commons.jexl3.parser.ASTSubNode;
import org.apache.commons.jexl3.parser.ASTTernaryNode;
import org.apache.commons.jexl3.parser.ASTTrueNode;
import org.apache.commons.jexl3.parser.ASTUnaryMinusNode;
import org.apache.commons.jexl3.parser.ASTVar;
import org.apache.commons.jexl3.parser.ASTWhileStatement;
import org.apache.commons.jexl3.parser.ParserVisitor;
import org.apache.commons.jexl3.parser.SimpleNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * @author vamsinukala
 * 
 */
/**
 * A basic  visitor pattern for a jexl expression or a script. 
 */
public abstract class PSBaseJexlParserVisitor extends ParserVisitor
{

  


private static final Logger log = LogManager
         .getLogger(PSBaseJexlParserVisitor.class);


   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTJexlScript, Object)
    */
   public Object visit(ASTJexlScript arg0, Object arg1)
   {
      log.debug("Visiting ASTJexlScript");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTBlock, Object)
    */
   public Object visit(ASTBlock arg0, Object arg1)
   {
      log.debug("Visiting ASTBlock");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTEmptyFunction, Object)
    */
   public Object visit(ASTEmptyFunction arg0, Object arg1)
   {
      log.debug("Visiting ASTEmptyFunction");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTSizeFunction, Object)
    */
   public Object visit(ASTSizeFunction arg0, Object arg1)
   {
      log.debug("Visiting ASTSizeFunction");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTIdentifier, Object)
    */
   public Object visit(ASTIdentifier arg0, Object arg1)
   {
      log.debug("Visiting ASTIdentifier {}", arg0.toString());
      return arg1;
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTAssignment, Object)
    */
   public Object visit(ASTAssignment arg0, Object arg1)
   {
      log.debug("Visiting ASTAssignment");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTOrNode, Object)
    */
   public Object visit(ASTOrNode arg0, Object arg1)
   {
      log.debug("Visiting ASTOrNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTAndNode, Object)
    */
   public Object visit(ASTAndNode arg0, Object arg1)
   {
      log.debug("Visiting ASTAndNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTBitwiseOrNode, Object)
    */
   public Object visit(ASTBitwiseOrNode arg0, Object arg1)
   {
      log.debug("Visiting ASTBitwiseOrNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTBitwiseXorNode, Object)
    */
   public Object visit(ASTBitwiseXorNode arg0, Object arg1)
   {
      log.debug("Visiting ASTBitwiseXorNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTBitwiseAndNode, Object)
    */
   public Object visit(ASTBitwiseAndNode arg0, Object arg1)
   {
      log.debug("Visiting ASTBitwiseAndNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTEQNode, Object)
    */
   public Object visit(ASTEQNode arg0, Object arg1)
   {
      log.debug("Visiting ASTEQNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTNENode, Object)
    */
   public Object visit(ASTNENode arg0, Object arg1)
   {
      log.debug("Visiting ASTNENode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTAddNode, Object)
    */
   public Object visit(ASTAddNode arg0, Object arg1)
   {
      log.debug("Visiting ASTAddNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTLTNode, Object)
    */
   public Object visit(ASTLTNode arg0, Object arg1)
   {
      log.debug("Visiting ASTLTNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTGTNode, Object)
    */
   public Object visit(ASTGTNode arg0, Object arg1)
   {
      log.debug("Visiting ASTGTNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTLENode, Object)
    */
   public Object visit(ASTLENode arg0, Object arg1)
   {
      log.debug("Visiting ASTLENode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTGENode, Object)
    */
   public Object visit(ASTGENode arg0, Object arg1)
   {
      log.debug("Visiting ASTGENode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTMulNode, Object)
    */
   public Object visit(ASTMulNode arg0, Object arg1)
   {
      log.debug("Visiting ASTMulNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTDivNode, Object)
    */
   public Object visit(ASTDivNode arg0, Object arg1)
   {
      log.debug("Visiting ASTDivNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTModNode, Object)
    */
   public Object visit(ASTModNode arg0, Object arg1)
   {
      log.debug("Visiting ASTModNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTUnaryMinusNode, Object)
    */
   public Object visit(ASTUnaryMinusNode arg0, Object arg1)
   {
      log.debug("Visiting ASTUnaryMinusNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTBitwiseComplNode, Object)
    */
   public Object visit(ASTBitwiseComplNode arg0, Object arg1)
   {
      log.debug("Visiting ASTBitwiseComplNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTNotNode, Object)
    */
   public Object visit(ASTNotNode arg0, Object arg1)
   {
      log.debug("Visiting ASTNotNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTNullLiteral, Object)
    */
   public Object visit(ASTNullLiteral arg0, Object arg1)
   {
      log.debug("Visiting ASTNullLiteral");
      return arg1;
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTTrueNode, Object)
    */
   public Object visit(ASTTrueNode arg0, Object arg1)
   {
      log.debug("Visiting ASTTrueNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTFalseNode, Object)
    */
   public Object visit(ASTFalseNode arg0, Object arg1)
   {
      log.debug("Visiting ASTFalseNode");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTStringLiteral, Object)
    */
   public Object visit(ASTStringLiteral arg0, Object arg1)
   {
      log.debug("Visiting ASTStringLiteral(defaultImpl)");
      return null;
   }
   
   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTReferenceExpression,Object)
    */
   public Object visit(ASTReferenceExpression arg0, Object arg1)
   {
      log.debug("Visiting ASTReferenceExpression");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTIfStatement,Object)
    */
   public Object visit(ASTIfStatement arg0, Object arg1)
   {
      log.debug("Visiting ASTIfStatement");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTReference,Object)
    */
   public Object visit(ASTReference arg0, Object arg1)
   {
      log.debug("Visiting ASTReference");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTWhileStatement,Object)
    */
   public Object visit(ASTWhileStatement arg0, Object arg1)
   {
      log.debug("Visiting ASTWhileStatement");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTForeachStatement,Object)
    */
   public Object visit(ASTForeachStatement arg0, Object arg1)
   {
      log.debug("Visiting ASTForeachStatement");
      return doVisit(arg0, arg1);
   }


   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTArrayAccess, Object)
    */
   public Object visit(ASTArrayAccess arg0, Object arg1)
   {
      log.debug("Visiting ASTArrayAccess");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTSizeMethod,Object)
    */
   public Object visit(ASTSizeMethod arg0, Object arg1)
   {
      log.debug("Visiting ASTSizeMethod");
      return doVisit(arg0, arg1);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTMapLiteral,Object)
    */
   public Object visit(ASTMapLiteral arg0, Object arg1)
   {
      log.debug("Visiting ASTMapLiteralMethod");
      return doVisit(arg0,arg1);
   }
   
   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTArrayLiteral,Object)
    */
   public Object visit(ASTArrayLiteral arg0, Object arg1)
   {
      log.debug("Visiting ASTArrayLiteralMethod");
      return doVisit(arg0,arg1);
   }
   
   /**
    * (non-Javadoc)
    * 
    * @see ParserVisitor#visit(ASTMapEntry,Object)
    */
   public Object visit(ASTMapEntry arg0, Object arg1)
   {
      log.debug("Visiting ASTMapEntryMethod");
      return doVisit(arg0,arg1);
   }
   
   protected  Object visit(ASTBreak node, Object data)
   {
       log.debug("Visiting ASTBreak");
       return doVisit(node,data);
   }


   protected  Object visit(ASTReturnStatement node, Object data)
   {
       log.debug("Visiting ASTReturnStatement");
       return doVisit(node,data);
   }


   protected  Object visit(ASTVar node, Object data)
   {
       log.debug("Visiting ASTVar");
       return doVisit(node,data);
   }
 

   protected  Object visit(ASTTernaryNode node, Object data)
   {
       log.debug("Visiting ASTTernaryNode");
       return doVisit(node,data);
   }


   protected  Object visit(ASTERNode node, Object data)
   {
       log.debug("Visiting ASTERNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTNRNode node, Object data)
   {
       log.debug("Visiting ASTNRNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTSWNode node, Object data)
   {
       log.debug("Visiting ASTSWNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTNSWNode node, Object data)
   {
       log.debug("Visiting ASTNSWNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTEWNode node, Object data)
   {
       log.debug("Visiting ASTEWNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTNEWNode node, Object data)
   {
       log.debug("Visiting ASTNEWNode");
       return doVisit(node,data);
   }


   protected  Object visit(ASTSubNode node, Object data)
   {
       log.debug("Visiting ASTSubNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTNumberLiteral node, Object data)
   {
       log.debug("Visiting ASTNumberLiteral");
       return doVisit(node,data);
   }


   protected  Object visit(ASTSetLiteral node, Object data)
   {
       log.debug("Visiting ASTSetLiteral");
       return doVisit(node,data);
   }

   protected  Object visit(ASTExtendedLiteral node, Object data)
   {
       log.debug("Visiting ASTExtendedLiteral");
       return doVisit(node,data);
   }


   protected  Object visit(ASTRangeNode node, Object data)
   {
       log.debug("Visiting ASTRangeNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTEmptyMethod node, Object data)
   {
       log.debug("Visiting ASTEmptyMethod");
       return doVisit(node,data);
   }


   protected  Object visit(ASTFunctionNode node, Object data)
   {
       log.debug("Visiting ASTFunctionNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTMethodNode node, Object data)
   {
       log.debug("Visiting ASTMethodNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTConstructorNode node, Object data)
   {
       log.debug("Visiting ASTConstructorNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTIdentifierAccess node, Object data)
   {
       log.debug("Visiting ASTIdentifierAccess");
       return doVisit(node,data);
   }

   protected  Object visit(ASTArguments node, Object data)
   {
       log.debug("Visiting ASTArguments");
       return doVisit(node,data);
   }

   protected  Object visit(ASTSetAddNode node, Object data)
   {
       log.debug("Visiting ASTSetAddNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTSetSubNode node, Object data)
   {
       log.debug("Visiting ASTSetSubNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTSetMultNode node, Object data)
   {
       log.debug("Visiting ASTSetMultNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTSetDivNode node, Object data)
   {
       log.debug("Visiting ASTSetDivNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTSetModNode node, Object data)
   {
       log.debug("Visiting ASTSetModNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTSetAndNode node, Object data)
   {
       log.debug("Visiting ASTSetAndNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTSetOrNode node, Object data)
   {
       log.debug("Visiting ASTSetOrNode");
       return doVisit(node,data);
   }
   
   protected  Object visit(ASTSetXorNode node, Object data)
   {
       log.debug("Visiting ASTSetXorNode");
       return doVisit(node,data);
   }

   protected  Object visit(ASTJxltLiteral node, Object data)
   {
       log.debug("Visiting ASTJxltLiteral");
       return doVisit(node,data);
   }

   protected  Object visit(ASTAnnotation node, Object data)
   {
       log.debug("Visiting ASTAnnotation");
       return doVisit(node,data);
   }

   protected  Object visit(ASTAnnotatedStatement node, Object data)
   {
       log.debug("Visiting ASTAnnotatedStatement");
       return doVisit(node,data);
   }
   
   @Override
   protected Object visit(ASTContinue node, Object data)
   {
       log.debug("Visiting ASTContinue");
       return doVisit(node,data);
   }
   
   /**
    * This must be implemented by the subclass. This helper method visits the 
    * child nodes of a simple node
    * @param arg0 the simple node that has child nodes to  be visited
    * @param arg1 the original expression node or script node
    * @return the original expression node
    */
   protected abstract Object doVisit(SimpleNode arg0, Object arg1);


}
