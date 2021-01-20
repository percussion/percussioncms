using System;
using System.Collections.Generic;
using System.Text;
using RxTest.RxWebServices;

namespace RxTest
{
   public class PSSecurityTestBase
   {
      public PSSecurityTestBase(PSTest test)
      {
         m_test = test;
      }

      protected PSTest m_test;
    }
}
