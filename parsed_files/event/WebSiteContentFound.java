package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebSiteContentFound implements Event{

	private List<WebSiteContent> webSiteContents;

	public WebSiteContentFound(List<WebSiteContent> webSiteContents) {
		this.setWebSiteContents(webSiteContents);
	}

	public List<WebSiteContent> getWebSiteContents()	{
		return webSiteContents;
	}

	public void setWebSiteContents(List<WebSiteContent> webSiteContents)	{
		this.webSiteContents = webSiteContents;
	}
}
