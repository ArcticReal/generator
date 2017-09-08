package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebSiteContentTypeFound implements Event{

	private List<WebSiteContentType> webSiteContentTypes;

	public WebSiteContentTypeFound(List<WebSiteContentType> webSiteContentTypes) {
		this.setWebSiteContentTypes(webSiteContentTypes);
	}

	public List<WebSiteContentType> getWebSiteContentTypes()	{
		return webSiteContentTypes;
	}

	public void setWebSiteContentTypes(List<WebSiteContentType> webSiteContentTypes)	{
		this.webSiteContentTypes = webSiteContentTypes;
	}
}
