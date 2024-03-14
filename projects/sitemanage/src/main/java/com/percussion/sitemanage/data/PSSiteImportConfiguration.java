package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="SiteImportConfiguration")
public class PSSiteImportConfiguration {

    private String mapQueryParamToPageName;

    public PSSite getSite() {
        return site;
    }

    public void setSite(PSSite site) {
        this.site = site;
    }

    private PSSite site;

    public String getMapQueryParamToPageName() {
        return mapQueryParamToPageName;
    }

    public void setMapQueryParamToPageName(String mapQueryParamToPageName) {
        this.mapQueryParamToPageName = mapQueryParamToPageName;
    }
}
