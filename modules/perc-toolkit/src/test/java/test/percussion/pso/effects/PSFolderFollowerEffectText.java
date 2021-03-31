/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * test.percussion.pso.effects PSFolderFollowerEffectText.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.effects;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.pso.effects.PSFolderFollowerEffect;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;

public class PSFolderFollowerEffectText
{
   TestableFolderFollowerEffect cut; 
   
   @Before
   public void setUp() throws Exception
   {
      cut = new TestableFolderFollowerEffect(); 
   }
   @Test
   public final void testGetFolderParents()
   {
     
   }
   @Test
   public final void testProcessRelations()
   {
      
   }
   @Test
   public final void testAddMissing()
   {
      
   }
   @Test
   public final void testRemoveExtra()
   {
      
   }
   @Test
   public final void testSubtractRels()
   {
      List<PSRelationship> main = new ArrayList<PSRelationship>();
      List<PSRelationship> sub = new ArrayList<PSRelationship>(); 
      
      PSRelationshipConfig fccfg = new PSRelationshipConfig("FolderContent", PSRelationshipConfig.RS_TYPE_SYSTEM, 
            PSRelationshipConfig.CATEGORY_FOLDER);
      PSLocator loca = new PSLocator(1,1); 
      PSLocator locb = new PSLocator(2,1); 
      PSLocator locc = new PSLocator(3,1); 
      PSLocator locd = new PSLocator(99,1); 
      main.add(new PSRelationship(1, loca, locd, fccfg)); 
      main.add(new PSRelationship(2, locb, locd, fccfg)); 
      main.add(new PSRelationship(3, locc, locd, fccfg));
      sub.add(new PSRelationship(4, loca, locd, fccfg));
      sub.add(new PSRelationship(5, locc, locd, fccfg)); 
      
      List<PSRelationship> result = cut.subtractRels(main, sub); 
      assertNotNull(result); 
      assertEquals(1,result.size()); 
      assertEquals(2,result.get(0).getOwner().getId()); 
      
    }
   
   private class TestableFolderFollowerEffect extends PSFolderFollowerEffect
   {
      @Override
      public void addMissing(PSLocator item, List<PSRelationship> missingList) throws PSErrorException
      {
         super.addMissing(item, missingList); 
      }
      
      @Override      
      public void removeExtra(List<PSRelationship> extras) throws PSErrorsException, PSErrorException
      {
         super.removeExtra(extras); 
      }
      
      @Override
      public List<PSRelationship> subtractRels(List<PSRelationship> mainList, List<PSRelationship> subList)
      {
         return super.subtractRels(mainList, subList); 
      }
   }
}
