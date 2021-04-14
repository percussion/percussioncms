/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.preview;
import java.util.List;
public interface SiteFolderFinder
{
   /**
    * Find the possible site folder previews for an item.  The set of possible site id / folder id pairs
    * is restricted by the optional folder id and site id parameters.
    * The return value is a List of Maps. Each Map contains the <code>sys_siteid</code> and <code>sys_folderid</code>
    * for the selected folder.  Other keys in the map are "sitename" and "fullpath".    
    * @param contentid the content id of the selected item.  Must not be null or empty. 
    * @param folderid the folder id, if known.  Leave this blank if you do not know the folder id. 
    * @param siteid
    * @return the List of possible site folders. 
    * @throws Exception
    */
   public List<SiteFolderLocation> findSiteFolderLocations(String contentid,
         String folderid, String siteid) throws Exception;
}
