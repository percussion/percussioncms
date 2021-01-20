using System;
using System.Collections.Generic;
using System.Text;
using RxTest.RxWebServices;


namespace RxTest
{
    class PSUiTestCase : PSUiTestBase
    {
       public PSUiTestCase(PSTest test) : base(test)
      {
      }


     /**
      * Testing loadActions method.
      * @throws Exception if an error occurs
      */
      public void testUiSOAPLoadActions()
      {

         LoadActionsRequest req = new LoadActionsRequest();
         PSAction[] allActions = m_test.m_uiService.LoadActions(req);
         PSFileUtils.RxAssert(allActions.Length > 0);

         req.Name = "Past*";
         PSAction[] actions = m_test.m_uiService.LoadActions(req);
         PSFileUtils.RxAssert(actions.Length > 0 && allActions.Length > actions.Length);

         foreach(PSAction action in actions)
         {
            PSFileUtils.RxAssert(String.Compare(action.name, 0, "Past", 0, 4) == 0);
            PSFileUtils.RxAssert(action.Usage.Length > 0);
            if(action.name == "Paste")
            {
               PSFileUtils.RxAssert(action.Children.Length > 0);
            }
         }

         req.Name = "Unknown";
         PSAction[] unknown = m_test.m_uiService.LoadActions(req);
         PSFileUtils.RxAssert(unknown.Length == 0);

      }

     /**
      * Testing loadDisplayFormats method.
      * @throws Exception if an error occurs
      */
      public void testUiSOAPLoadDisplayFormats()
      {

         LoadDisplayFormatsRequest req = new LoadDisplayFormatsRequest();

         PSDisplayFormat[] all = m_test.m_uiService.LoadDisplayFormats(req);
         PSFileUtils.RxAssert(all.Length > 0);

         foreach(PSDisplayFormat df in all)
         {
            PSFileUtils.RxAssert(df.Columns.Length > 0);

            CommunityRef[] comms = df.Communities;
            PSFileUtils.RxAssert(comms.Length > 0);
            foreach(CommunityRef comm in comms)
            {
               PSFileUtils.RxAssert(comm.name.Length > 0);
            }
         }

         req.Name = "Related*";
         PSDisplayFormat[] related = m_test.m_uiService.LoadDisplayFormats(req);
         PSFileUtils.RxAssert(related.Length < all.Length);

         req.Name = "Unknown";
         PSDisplayFormat[] unknown = m_test.m_uiService.LoadDisplayFormats(req);
         PSFileUtils.RxAssert(unknown.Length == 0);

      }

     /**
      * Testing loadSearches method.
      * @throws Exception if an error occurs
      */
      public void testUiSOAPLoadSearches()
      {

         LoadSearchesRequest req = new LoadSearchesRequest();
         PSSearchDef[] all = m_test.m_uiService.LoadSearches(req);
         PSFileUtils.RxAssert(all.Length > 0);
         foreach(PSSearchDef s in all)
         {
            //PSFileUtils.RxAssert(df.getColumns().length > 0);
            CommunityRef[] comms = s.Communities;
            PSFileUtils.RxAssert(comms.Length > 0);
            foreach (CommunityRef comm in comms)
            {
               PSFileUtils.RxAssert(comm.name.Length > 0);
            }
         }

         req.Name = "*search*";
         PSSearchDef[] searches = m_test.m_uiService.LoadSearches(req);
         PSFileUtils.RxAssert(searches.Length > 0);
         foreach(PSSearchDef s in searches)
         {
            String nameLower = s.name.ToLower();
            PSFileUtils.RxAssert(nameLower.Contains("search"));
         }
      }

     /**
      * Testing loadSearches method.
      * @throws Exception if an error occurs
      */
      public void testUiSOAPLoadViews()
      {

         LoadViewsRequest req = new LoadViewsRequest();
         PSViewDef[] all = m_test.m_uiService.LoadViews(req);
         PSFileUtils.RxAssert(all.Length > 0);
         foreach(PSViewDef s in all)
         {
            //PSFileUtils.RxAssert(df.getColumns().length > 0);
            CommunityRef[] comms = s.Communities;
            PSFileUtils.RxAssert(comms.Length > 0);
            foreach(CommunityRef comm in comms)
            {
               PSFileUtils.RxAssert(comm.name.Length > 0);
            }
         }

         req.Name = "*view*";
         PSViewDef[] views = m_test.m_uiService.LoadViews(req);
         PSFileUtils.RxAssert(views.Length > 0);
         foreach(PSViewDef v in views)
         {
            String nameLower = v.name.ToLower();
            PSFileUtils.RxAssert(nameLower.Contains("view"));
         }
      }

     /**
      * Testing both load searches and views, make sure they don't
      * load the same server object which has the same id.
      *
      * @throws Exception if an error occurs.
      */
      public void testUiSOAPLoadSearchesViews()
      {

         LoadSearchesRequest sreq = new LoadSearchesRequest();
         PSSearchDef[] allSearches = m_test.m_uiService.LoadSearches(sreq);
         PSFileUtils.RxAssert(allSearches.Length > 0);

         LoadViewsRequest vreq = new LoadViewsRequest();
         PSViewDef[] allViews = m_test.m_uiService.LoadViews(vreq);
         PSFileUtils.RxAssert(allViews.Length > 0);

         // make sure search objects do not include any view objects
         foreach(PSSearchDef search in allSearches)
         {
            long id = search.id;
            foreach (PSViewDef view in allViews)
            {
               PSFileUtils.RxAssert(view.id != id);
            }
         }
      }
   }
}
