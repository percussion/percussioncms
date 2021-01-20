using System;
using System.Collections.Generic;
using System.Text;
using RxTest.RxWebServices;

namespace RxTest
{
    public class PSSecurityTestCase : PSSecurityTestBase
    {
       public PSSecurityTestCase(PSTest test) : base(test)
       {
       }

     /**
      * Test login
      *
      * @throws Exception if the test fails.
      */
      public void test1securitySOAPLogin()
      {

         // login with null client id
         LoginRequest loginRequest = PSWsUtils.getLoginRequest("admin1", "demo", null, null, null);
         LoginResponse value = m_test.m_secService.Login(loginRequest);
         PSFileUtils.RxAssert(value != null, "login is null");

         PSLogin login = value.PSLogin;
         PSFileUtils.RxAssert(login != null, "login is null");
         String session = login.sessionId;
         PSFileUtils.RxAssert(!String.IsNullOrEmpty(session));

         // login with "workbench" client id
         loginRequest = PSWsUtils.getLoginRequest("admin1", "demo", "workbench", null, null);
         value = m_test.m_secService.Login(loginRequest);
         PSFileUtils.RxAssert(value != null, "login is null");

         login = value.PSLogin;
         PSFileUtils.RxAssert(login != null, "login is null");
         session = login.sessionId;
         PSFileUtils.RxAssert(!String.IsNullOrEmpty(session));

         // try to login with a different community and locale
         String newCommunity = null;
         String newLocale = null;
         String defaultCommunity = login.defaultCommunity;
         String defaultLocale    = login.defaultLocaleCode;

         foreach(PSCommunity community in login.Communities)
         {
            if (community.name != defaultCommunity)
            {
               newCommunity = community.name;
               break;
            }
         }

         foreach(PSLocale1 locale in login.Locales)
         {
            if (locale.code != defaultLocale)
            {
               newLocale = locale.code;
               break;
            }
         }

         if(!(String.IsNullOrEmpty(newCommunity) && String.IsNullOrEmpty(newLocale)))
         {
            loginRequest = PSWsUtils.getLoginRequest("admin1", "demo", "workbench", newCommunity, newLocale);
            value = m_test.m_secService.Login(loginRequest);
            login = value.PSLogin;
            PSFileUtils.RxAssert(login != null, "login is null");
            PSFileUtils.RxAssert(!String.IsNullOrEmpty(newCommunity) && newCommunity == login.defaultCommunity);
            PSFileUtils.RxAssert(!String.IsNullOrEmpty(newLocale) && newLocale == login.defaultLocaleCode);

            // switch back
            loginRequest = PSWsUtils.getLoginRequest("admin1", "demo", "workbench", defaultCommunity, defaultLocale);
            value = m_test.m_secService.Login(loginRequest);
            login = value.PSLogin;
         }

      }

     /**
      * Test logout
      *
      * @throws Exception if the test fails
      */
      public void test2securitySOAPLogout()
      {

         // create a new session id
         RefreshSessionRequest   refreshReq  = new RefreshSessionRequest();
         LogoutRequest           logoutReq   = new LogoutRequest();

         // refresh valid session to make sure the session id is valid
         refreshReq.SessionId = m_test.m_rxSession;
         m_test.m_secService.RefreshSession(refreshReq);

         // logout the just created session
         logoutReq.SessionId = m_test.m_rxSession;
         m_test.m_secService.Logout(logoutReq);

         // refresh invalid session to make sure the session was invalidated
         try
         {
            m_test.m_secService.RefreshSession(refreshReq);
            PSFileUtils.RxAssert(false, "Should have thrown exception");
         }
         catch (Exception e)
         {
            // expected exception
            String message = e.Message;  // for warning
         }
      }

     /**
      * Test refresh session
      *
      * @throws Exception
      */
      public void test3securitySOAPRefreshSession()
      {
         m_test.Login();
         RefreshSessionRequest sessionReq = new RefreshSessionRequest();
         sessionReq.SessionId = m_test.m_rxSession;
         m_test.m_secService.RefreshSession(sessionReq);
      }

     /**
      * Test load communities
      *
      * @throws Exception
      */
      public void test4securitySOAPLoadCommunities()
      {

         LoadCommunitiesRequest request = null;
         PSCommunity[] communities = null;

         // load all communities
         request = new LoadCommunitiesRequest();
         request.Name = null;
         communities = m_test.m_secService.LoadCommunities(request);
         PSFileUtils.RxAssert(communities != null && communities.Length > 0);

         int count = communities.Length;

         request = new LoadCommunitiesRequest();
         request.Name = " ";
         communities = m_test.m_secService.LoadCommunities(request);
         PSFileUtils.RxAssert(communities != null && communities.Length == count);

         request = new LoadCommunitiesRequest();
         request.Name = "*";
         communities = m_test.m_secService.LoadCommunities(request);
         PSFileUtils.RxAssert(communities != null && communities.Length == count);

         // try to load a non-existing community
         request = new LoadCommunitiesRequest();
         request.Name = "somecommunity";
         communities = m_test.m_secService.LoadCommunities(request);
         PSFileUtils.RxAssert(communities != null && communities.Length == 0);

         // load test communities
         request = new LoadCommunitiesRequest();
         request.Name = "Enterprise*";
         communities = m_test.m_secService.LoadCommunities(request);
         PSFileUtils.RxAssert(communities != null && communities.Length == 2);
         PSFileUtils.RxAssert(communities[0].name == "Enterprise_Investments");
         PSFileUtils.RxAssert(communities[1].name == "Enterprise_Investments_Admin");

         request = new LoadCommunitiesRequest();
         request.Name = "ENTERPRISE*";
         communities = m_test.m_secService.LoadCommunities(request);
         PSFileUtils.RxAssert(communities != null && communities.Length == 2);
         PSFileUtils.RxAssert(communities[0].name == "Enterprise_Investments");
         PSFileUtils.RxAssert(communities[1].name == "Enterprise_Investments_Admin");

         request = new LoadCommunitiesRequest();
         request.Name = "*investments";
         communities = m_test.m_secService.LoadCommunities(request);
         PSFileUtils.RxAssert(communities != null && communities.Length == 2);
         PSFileUtils.RxAssert(communities[0].name == "Enterprise_Investments");
         PSFileUtils.RxAssert(communities[1].name == "Corporate_Investments");

      }

     /**
      * Test load roles
      *
      * @throws Exception
      */
      public void test5securitySOAPLoadRoles()
      {

         LoadRolesRequest request = null;
         PSRole[] roles = null;

         // load all roles
         request = new LoadRolesRequest();
         request.Name = null;
         roles = m_test.m_secService.LoadRoles(request);
         PSFileUtils.RxAssert(roles != null && roles.Length > 0);

         int count = roles.Length;

         request = new LoadRolesRequest();
         request.Name =" ";
         roles = m_test.m_secService.LoadRoles(request);
         PSFileUtils.RxAssert(roles != null && roles.Length == count);

         request = new LoadRolesRequest();
         request.Name = "*";
         roles = m_test.m_secService.LoadRoles(request);
         PSFileUtils.RxAssert(roles != null && roles.Length == count);

         // try to load a non-existing role
         request = new LoadRolesRequest();
         request.Name = "somerole";
         roles = m_test.m_secService.LoadRoles(request);
         PSFileUtils.RxAssert(roles != null && roles.Length == 0);

         // load fast forward admin roles
         request = new LoadRolesRequest();
         request.Name = "*admi*";
         roles = m_test.m_secService.LoadRoles(request);
         PSFileUtils.RxAssert(roles != null && roles.Length == 5);

         request = new LoadRolesRequest();
         request.Name = "*ADMIN*";
         roles = m_test.m_secService.LoadRoles(request);
         PSFileUtils.RxAssert(roles != null && roles.Length == 5);

         request = new LoadRolesRequest();
         request.Name = "admin";
         roles = m_test.m_secService.LoadRoles(request);
         PSFileUtils.RxAssert(roles != null && roles.Length == 1);

      }

     /**
      * Test the FilterByRuntimeVisibility web service, assumes FF is installed.
      *
      * @throws Exception if the test fails.
      */
       public void test6SecuritySOAPFilterByRuntimeVisibility()
       {

          // ensure in EI community
          SwitchCommunityRequest switchCommunityReq = new SwitchCommunityRequest();
          switchCommunityReq.Name = "Enterprise_Investments";

          LoadAssemblyTemplatesRequest loadTemplatesReq = new LoadAssemblyTemplatesRequest();
          loadTemplatesReq.Name = null;
          PSAssemblyTemplate[] templates = m_test.m_assService.LoadAssemblyTemplates(loadTemplatesReq);


          long[] ids = new long[templates.Length];
          int count = 0;

          // count the number of EI Sites to figure out how many
          // templates will be in this comunity.  This is not guaranteed
          // but if implemented correctly it should work
          for (int inc = 0; inc < templates.Length; ++inc)
          {
             ids[inc] = templates[inc].id;
             foreach (Reference site in templates[inc].Sites)
             {
                if (site.name == "Enterprise Investments")
                {
                   ++count;
                }
             }
          }

          FilterByRuntimeVisibilityResponse results = m_test.m_secService.FilterByRuntimeVisibility(ids);

         PSFileUtils.RxAssert(results.Ids.Length == count);

          long[] expected;

          // test filter runtime
          ids = new long[2];
          ids[0] = 38654705965;
          ids[1] = 38654705967;
          expected = new long[] { ids[0] };

          results = m_test.m_secService.FilterByRuntimeVisibility(ids);
          PSWsUtils.compareLongArrays(results.Ids, expected);

          // test "passthru"
          ids[0] = 90194313526;
          ids[1] = 38654705965;
          results = m_test.m_secService.FilterByRuntimeVisibility(ids);
          PSWsUtils.compareLongArrays(ids, results.Ids);

          ids[0] = 90194313526;
          ids[1] = 38654705967;
          expected = new long[] { ids[0] };
          results = m_test.m_secService.FilterByRuntimeVisibility(ids);
          PSWsUtils.compareLongArrays(expected, results.Ids);

          // test no results
          ids = new long[1];
          ids[0] = 38654705967;
          expected = new long[0];
          results = m_test.m_secService.FilterByRuntimeVisibility(ids);
          PSFileUtils.RxAssert(results.Ids.Length == 0);

       }
    }
}
