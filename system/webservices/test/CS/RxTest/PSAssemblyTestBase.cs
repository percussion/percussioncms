using System;
using System.Collections.Generic;
using System.Text;
using RxTest.RxWebServices;

namespace RxTest
{
    class PSAssemblyTestBase
    {
      protected PSAssemblyTestBase(PSTest test)
      {
         m_test = test;
      }

       protected void VerifySlot(PSTemplateSlot slots)
       {
          PSFileUtils.RxAssert(slots != null);
          PSFileUtils.RxAssert(slots.description                     == "Slot populated by the auto index query"); 
          PSFileUtils.RxAssert(slots.id                              == 21474836985); 
          PSFileUtils.RxAssert(slots.label                           == "Events slot");
          PSFileUtils.RxAssert(slots.name                            == "rffEvents");
          PSFileUtils.RxAssert(slots.relationshipName                == "ActiveAssembly");
          PSFileUtils.RxAssert(slots.AllowedContent[0].contentTypeId == 8589934898);
          PSFileUtils.RxAssert(slots.AllowedContent[0].templateId    == 17179869688);
          PSFileUtils.RxAssert(slots.Arguments[1].name               == "type");
          PSFileUtils.RxAssert(slots.Arguments[1].Value              == "sql");
       }

       protected void VerifyTemplate(PSAssemblyTemplate template)
       {
          PSFileUtils.RxAssert(template != null); 
          PSFileUtils.RxAssert(template.description      == "EI Full Page Event");
          PSFileUtils.RxAssert(template.id               == 17179869709);
          PSFileUtils.RxAssert(template.assembler        == "Java/global/percussion/assembly/velocityAssembler");
          PSFileUtils.RxAssert(template.assemblyUrl      == "../assembler/render");
          PSFileUtils.RxAssert(template.label            == "P - EI Event");
          PSFileUtils.RxAssert(template.mimeType         == "text/html");
          PSFileUtils.RxAssert(template.name             == "rffPgEiEvent");
          PSFileUtils.RxAssert(template.relationshipType == "Normal");
          PSFileUtils.RxAssert(template.Sites[0].id      == 38654705965);
          PSFileUtils.RxAssert(template.Sites[0].name    == "Enterprise Investments");
       }


      protected PSTest m_test;
   }
}
