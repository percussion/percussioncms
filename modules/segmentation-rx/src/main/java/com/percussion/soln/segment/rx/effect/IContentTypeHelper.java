package com.percussion.soln.segment.rx.effect;

import javax.jcr.RepositoryException;

public interface IContentTypeHelper {
    
    String retrieveContentTypeNameForItem(int contentId) throws RepositoryException;

}
