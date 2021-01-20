/*[ TestMakeInternalRequest.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.extensions.testing;

import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.PSServer;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.data.PSInternalRequestCallException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * A post-exit to test internal request handler lookups and context cloning.
 * <p>
 * The parameters to the exit are:
 * <table border="1">
 * <tr><th>Param #</th><th>Name</th><th>Description</th><th>Required?</th></tr>
 * <tr>
 * <td>1</td>
 * <td>internalRequestPath</td>
 * <td>specifies the application and page of the dataset to which the internal
 * request is to be made.  The specified application will be searched for a
 * request page that responds to the specified page and extension.  May o
 * ptionally include a "query string" -- name/value pairs, separated by equals,
 * delimited by ampersand, and identified as the portion of the path following
 * a question mark.  May be as little as "<code>appName/pageName</code>" or as
 * much as "<code>http://127.0.0.1:9992/Rhythmyx/AppTest/nov.xml?x=y&test=5
 * </code>".</td>
 * <td>yes</td>
 * </tr>
 * </table>
 */
public class TestMakeInternalRequest extends PSDefaultExtension
      implements IPSResultDocumentProcessor
{
   /**
    * Never modifies the stylesheet.
    * @return <code>false</code> always.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }


   /**
    * Makes an internal request to the resource specified in the supplied
    * <code>params</code> and creates an XML document that contains the results.
    * Several parameters are added to the internal request context:
    * <table border="1">
    * <tr><th>Param</th><th>Value</th></tr>
    * <tr><td>bravo</td><td>override-by-extraparams</td></tr>
    * <tr><td>charlie</td><td>override-by-extraparams</td></tr>
    * <tr><td>extraParams</td><td>processed</td></tr>
    * </table>
    * <p>
    * See the {@link com.percussion.extension.IPSResultDocumentProcessor#processResultDocument
    * IPSResultDocumentProcessor} interface for a description of this method's
    * parameters.
    *
    * @return A document that contains the results of the internal request,
    * the request path of the internal request, and the contents of both the
    * original and internal request's contexts.
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (params.length != 1)
         throw new PSParameterMismatchException(params.length, 1);

      if (null == params[0] ||  0 == params[0].toString().trim().length())
         throw new PSExtensionProcessingException(
            IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
            new Object[] { "Internal request path", params[0] } );

      String path = params[0].toString().trim();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot( doc, "InternalRequest" );

      // create "extra parameters" for the internal request to test if they
      // are added to the internal request context (and not the original)
      Map extraParams = new HashMap();
      extraParams.put( "bravo", "override-by-extraparams" );
      extraParams.put( "charlie", "override-by-extraparams" );
      extraParams.put( "extraParams", "processed" );

      IPSInternalRequest ir =
         request.getInternalRequest( path, extraParams, true );
      if (ir != null)
      {
         // log the state as XML in the result document
         IPSRequestContext irContext = ir.getRequestContext();
         String irPath = irContext.getRequestFileURL();
         PSXmlDocumentBuilder.addElement( doc, root, "path", irPath );

         // make the request and log the result
         try
         {
            Document irDoc = ir.getResultDoc();
            if (irDoc != null)
            {
               Element irRoot = irDoc.getDocumentElement();
               if (irRoot != null)
                  PSXmlDocumentBuilder.copyTree(doc, root, irRoot, true);
            }

            // log internal request's parameters
            Element paramNode = doc.createElement("cloneParams");
            paramNode.setAttribute( "inheritParams", "true" );
            Iterator paramIter = irContext.getParametersIterator();
            while (paramIter.hasNext())
            {
               Map.Entry entry = (Map.Entry) paramIter.next();
               PSXmlDocumentBuilder.addElement( doc, paramNode,
                  entry.getKey().toString(), entry.getValue().toString() );
            }
            root.appendChild( paramNode );

         } catch (UnsupportedOperationException  e)
         {
            // we must have hit an update resource
            throw new PSExtensionProcessingException(
               IPSExtensionErrors.EXT_PARAM_VALUE_INVALID,
               "this exit cannot be used to make requests to update resources");

         } catch (PSInternalRequestCallException e)
         {
            PSXmlDocumentBuilder.addElement( doc, root, "error",
               e.getClass().toString() );
         }
      }
      else
      {
         PSXmlDocumentBuilder.addElement( doc, root, "path", "null" );
         PSXmlDocumentBuilder.addElement( doc, root, "cloneParams", "null" );
      }

      /*
       * Now make the request again, but don't inherit current context
       */
      ir = request.getInternalRequest( path, extraParams, false );
      if (ir != null)
      {
         // log the state as XML in the result document
         IPSRequestContext irContext = ir.getRequestContext();
         String irPath = irContext.getRequestFileURL();
         PSXmlDocumentBuilder.addElement( doc, root, "path", irPath );

         // make the request and log the result
         try
         {
            Document irDoc = ir.getResultDoc();
            if (irDoc != null)
            {
               Element irRoot = irDoc.getDocumentElement();
               if (irRoot != null)
                  PSXmlDocumentBuilder.copyTree(doc, root, irRoot, true);
            }

            // log internal request's parameters
            Element paramNode = doc.createElement("cloneParams");
            paramNode.setAttribute( "inheritParams", "false" );
            Iterator paramIter = irContext.getParametersIterator();
            while (paramIter.hasNext())
            {
               Map.Entry entry = (Map.Entry) paramIter.next();
               PSXmlDocumentBuilder.addElement( doc, paramNode,
                  entry.getKey().toString(), entry.getValue().toString() );
            }
            root.appendChild( paramNode );

         } catch (UnsupportedOperationException  e)
         {
            // we must have hit an update resource
            throw new PSExtensionProcessingException(
               IPSExtensionErrors.EXT_PARAM_VALUE_INVALID,
               "this exit cannot be used to make requests to update resources");

         } catch (PSInternalRequestCallException e)
         {
            PSXmlDocumentBuilder.addElement( doc, root, "error",
               e.getClass().toString() );
         }
      }
      else
      {
         PSXmlDocumentBuilder.addElement( doc, root, "path", "null" );
         PSXmlDocumentBuilder.addElement( doc, root, "cloneParams", "null" );
      }


      // log original request's parameters
      Element paramNode = doc.createElement("origParams");
      Iterator paramIter = request.getParametersIterator();
      while (paramIter.hasNext())
      {
         Map.Entry entry = (Map.Entry) paramIter.next();
         PSXmlDocumentBuilder.addElement( doc, paramNode,
            entry.getKey().toString(), entry.getValue().toString() );
      }
      root.appendChild( paramNode );

      return doc;
   }
}
