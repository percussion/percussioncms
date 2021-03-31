/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.finder;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
//import java.util.TreeSet;
import javax.jcr.RepositoryException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.pso.utils.SimplifyParameters;
import com.percussion.server.*;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.impl.finder.PSBaseSlotContentFinder;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;

/**
  * This class is a rewrite of the out of the box Legacy "finder".  That version did not 
  * accept request parameters which meant it either needed to be used with static URL or 
  * invoked multiple times for each set of possible parameters.  To do the latter, you 
  * would have needed a slot registration for each permutation.
  * 
  * @author Erich Heard
  * @version 6.0
  */
public class PSParameterizedLegacyAutoSlotContentFinder extends PSBaseSlotContentFinder	implements IPSSlotContentFinder {
 private static Log _logger = LogFactory.getLog( PSParameterizedLegacyAutoSlotContentFinder.class.getName( ) );
 
 /**
   * This method returns a set of SlotItems retrieved from a legacy XML 
   * query resource.  The resource output should conform to sys_AssemblerInfo.dtd, 
   * but only the content ID (linkurl/@contentid) and the template ID 
   * (linkurl/@variantid) are of import to this routine.  
   */
	protected Set<SlotItem> getSlotItems(IPSAssemblyItem item, IPSTemplateSlot slot, Map<String, Object> params )	throws RepositoryException, PSFilterException {
  _logger.debug( "initializing. . ." );
  //TreeSet<SlotItem> hits = new TreeSet<SlotItem>( new PSBaseSlotContentFinder.SlotItemOrder( ) );
  Set<SlotItem> hits = new LinkedHashSet<SlotItem>(); 
  Map<String, String> args = slot.getFinderArguments( );
  String resource = this.getValue( args, params, "resource", null );
  if(StringUtils.isBlank( resource ) ) 
  {
      String emsg = "The resource parameter is required for a legacy auto slot content finder";
      _logger.error(emsg); 
      throw new IllegalArgumentException(emsg);
  }
  Map<String,String> resourceArgs = new HashMap<String,String>(); 
  resourceArgs.putAll(args); 
  resourceArgs.putAll(SimplifyParameters.simplifyMap(params)); 
  
  
//  StringBuffer url = new StringBuffer( resource );
//  
//  if( params != null ) {
//   if( !params.isEmpty( ) ) {
//    url.append( "?" );
//    Iterator<String> keys = params.keySet( ).iterator( );
//    
//    while( keys.hasNext( ) ) {
//     String key = keys.next( );
//     String[ ] values = ( String[ ] )params.get( key );
//     if( values == null || values.length == 0 ) continue;
//     
//     url.append( key );
//     url.append( "=" );
//     url.append( values[ 0 ] );
//     if( keys.hasNext( ) ) url.append( "&" );
//    }  //  while( keys.hasNext( ) )
//   }  //  if( !params.isEmpty( ) )
//  }  //  if( params != null )
//  
//  resource = url.toString( );
//  _logger.debug( "URL is \"" + resource + "\"." );
  
  IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager( );
  PSRequest req = ( PSRequest )PSRequestInfo.getRequestInfo( "PSREQUEST" );
  //  NOTE:  Passing the parameter map "params" here does not seem to work.  I tried 
  //         setting the flag argument to both true and false to no avail.  It certainly 
  //         contains the correct information.  This looks to be due to the values of 
  //         the params Map being String arrays rather than Strings.
  PSInternalRequest iReq = PSServer.getInternalRequest( resource, req, resourceArgs, false, null );
  ArrayList<Integer> id = new ArrayList<Integer>( );
  
  try {
   Document document = iReq.getResultDoc( );
   NodeList nodes = document.getElementsByTagName( "linkurl" );
   
   for( int i=0; i<nodes.getLength( ); i++ ) {
    Element e = ( Element )nodes.item( i );
    //  NOTE:  Shouldn't folder ID be used here so that - in the case of link snippets - the 
    //         proper link is built?  I suspect as is it will default the first folder it 
    //         encounters.
    String templateId = e.getAttribute( "variantid" );
    String contentId = e.getAttribute( "contentid" );
    PSGuid guid = new PSGuid( PSTypeEnum.TEMPLATE, templateId );
    
    _logger.debug( "found node for CID #" + contentId + " and template #" + templateId + "." );
    id.clear( );
    id.add( Integer.parseInt( contentId ) );
    
    PSComponentSummary summary = mgr.loadComponentSummaries( id ).get( 0 );
    PSLegacyGuid legacyGuid = new PSLegacyGuid( summary.getCurrentLocator( ) );
    
    _logger.debug( "new GUID is " + guid.toString( ) + "\nlegacy GUID is " + legacyGuid.toString( ) );
    hits.add( new PSBaseSlotContentFinder.SlotItem( legacyGuid, guid, i ) );
   }  //  for( int i=0; i<nodes.getLength( ); i++ )
   
   _logger.debug( "found " + hits.size( ) + " items for legacy slot." );
  } catch( PSInternalRequestCallException __ie ) {
   _logger.error( __ie );
   __ie.printStackTrace( );
   throw new RepositoryException( __ie.getMessage( ) );
  }  //  try
  
  return hits;
	}

	public Type getType( ) {
  return com.percussion.services.assembly.IPSSlotContentFinder.Type.AUTOSLOT;
	}
}