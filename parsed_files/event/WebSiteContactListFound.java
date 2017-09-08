package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebSiteContactListFound implements Event{

	private List<WebSiteContactList> webSiteContactLists;

	public WebSiteContactListFound(List<WebSiteContactList> webSiteContactLists) {
		this.setWebSiteContactLists(webSiteContactLists);
	}

	public List<WebSiteContactList> getWebSiteContactLists()	{
		return webSiteContactLists;
	}

	public void setWebSiteContactLists(List<WebSiteContactList> webSiteContactLists)	{
		this.webSiteContactLists = webSiteContactLists;
	}
}
