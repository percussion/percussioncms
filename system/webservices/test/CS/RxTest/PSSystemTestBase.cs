using System;
using System.Collections.Generic;
using System.Text;
using System.Collections.Specialized;
using RxTest.RxWebServices;


namespace RxTest
{
    class PSSystemTestBase
    {
       public PSSystemTestBase(PSTest test, PSContentTestCase testContent)
      {
         m_test = test;
         m_testContent = testContent;
      }

      protected PSRelationship[] loadRelationships(PSRelationshipFilter filter)
      {
         LoadRelationshipsRequest lrReq = new LoadRelationshipsRequest();
         lrReq.PSRelationshipFilter = filter;
         return m_test.m_sysService.LoadRelationships(lrReq);
      }

     /**
      * Removes all relationship instances with the specified relationship name.
      * 
      * @param configName the name of the specified relationship config, assumed
      *    not <code>null</code> or empty.
      * 
      * @throws Exception
      */
      protected void cleanupRelationships(String configName)
      {
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.Configurations = new String[] { configName };
         PSRelationship[] rels = loadRelationships(filter);
      
         if (rels.Length > 0)
         {
            long[] ids = new long[rels.Length];

            for (int i = 0; i < rels.Length; i++)
            {
               ids[i] = rels[i].id;
            }

            m_test.m_sysService.DeleteRelationships(ids);
         }
      }

      protected PSRelationship createRelationship(long ownerId, long dependentId, String relationshipName)
      {
         CreateRelationshipRequest crReq = new CreateRelationshipRequest();
         long owner     = ownerId;
         long dependent = dependentId;
         crReq.Name = relationshipName;
         crReq.OwnerId = owner;
         crReq.DependentId = dependent;
         CreateRelationshipResponse response = m_test.m_sysService.CreateRelationship(crReq);
         PSRelationship rel = response.PSRelationship;
         PSFileUtils.RxAssert(rel != null);

         return rel;
      }

     /**
      * Get the resulting transitions as a map
      * 
      * @param resp The response from getAllowedTransitions(), assumed not 
      * <code>null</code>.
      * 
      * @return A map of transition trigger/name to label, never 
      * <code>null</code>.
      */
      public StringDictionary getTransitionsMap(GetAllowedTransitionsResponse resp)
      {
         StringDictionary results = new StringDictionary();

         String[] trans    = resp.Transition;
         String[] labels   = resp.Label;
         
         if (trans == null)
         {
            PSFileUtils.RxAssert(labels == null);
         }
         else
         {
            PSFileUtils.RxAssert(trans.Length == labels.Length);

            for (int i = 0; i < trans.Length; i++)
            {
               results.Add(trans[i], labels[i]);
            }
         }

         return results;
      }

     /**
      * compareStringDictionaries
      */
       public void compareStringDictionaries(StringDictionary sd1, StringDictionary sd2)
       {
          if (sd1.Equals(sd2))
          {
             PSFileUtils.RxAssert(true);
          }
       }

       /**
        * Convenient method to create a relationship property from the
        * specified name and value.
        * 
        * @param name the name of the property, assumed not <code>null</code> or 
        *    empty.
        * @param value the value of the property, may be <code>null</code> or empty.
        * 
        * @return the created property, never <code>null</code>.
        */
       protected Property1 getProperty(String name, String value)
       {
          Property1 property = new Property1();

          property.name    = name;
          property.value   = value;

          return property;
       }


       public PSLogin getLogin(String userName, String password, String clientId, String comunity, String localeCode)
       {
          LoginRequest loginRequest = PSWsUtils.getLoginRequest(userName, password, clientId, comunity, localeCode);
          LoginResponse loginResponce = m_test.m_secService.Login(loginRequest);

          return loginResponce.PSLogin;
       }

       
      protected PSTest              m_test;
      protected PSContentTestCase   m_testContent;
      protected String              TYPE_FOLDER_CONTENT  = "FolderContent";
      protected String              TYPE_ACTIVE_ASSEMBLY = "ActiveAssembly";
      protected String              TYPE_NEW_COPY        = "NewCopy";
   }
}
