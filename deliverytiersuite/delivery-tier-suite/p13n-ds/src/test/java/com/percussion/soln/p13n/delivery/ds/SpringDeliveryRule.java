package integrationtest.p13n.ds;

import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;

public class SpringDeliveryRule implements IDeliverySnippetFilter {

    public List<IDeliveryResponseSnippetItem> filter(IDeliverySnippetFilterContext context,
            List<IDeliveryResponseSnippetItem> items) {
        return items;
    }

}
