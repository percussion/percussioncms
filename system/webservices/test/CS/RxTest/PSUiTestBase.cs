using System;
using System.Collections.Generic;
using System.Text;

namespace RxTest
{
    class PSUiTestBase
    {
       public PSUiTestBase(PSTest test)
      {
         m_test = test;
      }

      protected PSTest              m_test;
    }
}
