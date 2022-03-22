package com.percussion.server.webservices;

import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.error.PSException;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.server.PSRequest;
import org.w3c.dom.Document;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public interface IPSSearchHandler extends IPSPortActionHandler {
    PSWSSearchResponse search(PSRequest request,
                              PSWSSearchRequest searchRequest) throws PSException;

    List<Integer> searchAndGetContentIds(PSRequest request,
                                         PSWSSearchRequest searchReq) throws PSException;

    List<Integer> searchAndGetContentIdsForSearchByStatus(PSRequest request,
                                                          PSWSSearchRequest searchReq) throws PSException;

    void search(PSRequest request, PSWSSearchRequest searchReq,
                Document parent) throws PSException;

    @SuppressWarnings("unchecked")
    void executeKeyFieldSearch(PSRequest request,
                               PSServerItem updateItem) throws PSException;

    /**
     * Iterator for a delimited list of values formated for an external search
     * with an "in" operator.
     */
    public static class PSExternalInValuesIterator implements Iterator<String> {
        /**
         * Construct the iterator
         *
         * @param val The delimited list, assumed not <code>null</code>, may be
         *            empty.
         */
        PSExternalInValuesIterator(String val) {
            mi_toker = new StringTokenizer(val, " ");
            getNext();
        }

        public boolean hasNext() {
            return mi_next != null;
        }

        public String next() {
            String next = mi_next;
            getNext();

            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }

        /**
         * Checks ahead to determine the next possible value
         */
        private void getNext() {
            String next = null;
            while (mi_toker.hasMoreTokens()) {
                String token = mi_toker.nextToken();
                if (token == null || token.equals(""))
                    continue;
                if (token.trim().equalsIgnoreCase("or"))
                    continue;
                next = token;
                break;
            }
            mi_next = next;
        }

        /**
         * Used to walk the string supplied during construction to return values.
         * never <code>null</code> or modified after construction.
         */
        private final StringTokenizer mi_toker;

        /**
         * The next value to return, modified by calls to {@link #getNext()},
         * <code>null</code> if there are no values left to return.
         */
        private String mi_next;
    }
}
