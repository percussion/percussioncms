using System;
using System.Collections.Generic;
using System.Text;
using RxTest.RxWebServices;

namespace RxTest
{
    class PSAssemblyTestCase : PSAssemblyTestBase
    {
       public PSAssemblyTestCase(PSTest test) : base(test)
       {
       }

      public void test1assemblySOAPLoadSlots()
      {
         LoadSlotsRequest request = null;
         PSTemplateSlot[] slots = null;


         // load all slots
         request = new LoadSlotsRequest();
         request.Name = null;
         slots = m_test.m_assService.LoadSlots(request);
         PSFileUtils.RxAssert(slots != null && slots.Length > 0);

         int count = slots.Length;

         request = new LoadSlotsRequest();
         request.Name = " ";
         slots = m_test.m_assService.LoadSlots(request);
         PSFileUtils.RxAssert(slots != null && slots.Length == count);

         request = new LoadSlotsRequest();
         request.Name = "*";
         slots = m_test.m_assService.LoadSlots(request);
         PSFileUtils.RxAssert(slots != null && slots.Length == count);

         // try to load a non-existing slot
         request = new LoadSlotsRequest();
         request.Name = "someslot";
         slots = m_test.m_assService.LoadSlots(request);
         PSFileUtils.RxAssert(slots != null && slots.Length == 0);

         // load test slots
         request = new LoadSlotsRequest();
         request.Name = "rffEvents*";
         slots = m_test.m_assService.LoadSlots(request);
         PSFileUtils.RxAssert(slots != null && slots.Length == 1);

         VerifySlot(slots[0]);

         request = new LoadSlotsRequest();
         request.Name = "RFFEVENTS";
         slots = m_test.m_assService.LoadSlots(request);
         PSFileUtils.RxAssert(slots != null && slots.Length == 1);

         VerifySlot(slots[0]);

         request = new LoadSlotsRequest();
         request.Name = "*List";
         slots = m_test.m_assService.LoadSlots(request);
         PSFileUtils.RxAssert(slots != null && slots.Length == 2);

      }

      public void test2assemblySOAPLoadAssemblyTemplates()
      {
         LoadAssemblyTemplatesRequest request = null;
         PSAssemblyTemplate[] templates = null;


         // load all templates
         request = new LoadAssemblyTemplatesRequest();
         request.Name = null;
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length > 0);

         int count = templates.Length;

         request = new LoadAssemblyTemplatesRequest();
         request.Name = " ";
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length == count);

         request = new LoadAssemblyTemplatesRequest();
         request.Name = "*";
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length == count);

         // try to load a non-existing template
         request = new LoadAssemblyTemplatesRequest();
         request.Name = "sometemplate";
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length == 0);

         // load test templates
         request = new LoadAssemblyTemplatesRequest();
         request.Name = "rffpgeievent";
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length == 1);

         VerifyTemplate(templates[0]);

         request = new LoadAssemblyTemplatesRequest();
         request.Name = "RFFPGEIEVENT";
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length == 1);

         VerifyTemplate(templates[0]);

         request = new LoadAssemblyTemplatesRequest();
         request.Name = "*EiCategoryList";
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length == 1);

         // load template with 1 (or more) site reference
         request = new LoadAssemblyTemplatesRequest();
         request.Name = "rffPgEiCategoryList";
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length == 1);
         PSFileUtils.RxAssert(templates[0].Sites.Length > 0);

         // load template with 2 (or more) site references
         request = new LoadAssemblyTemplatesRequest();
         request.Name = "rffSnImageLink";
         templates = m_test.m_assService.LoadAssemblyTemplates(request);
         PSFileUtils.RxAssert(templates != null && templates.Length == 1);
         PSFileUtils.RxAssert(templates[0].Sites.Length > 1);

      }
   }
}
