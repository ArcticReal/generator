package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebSiteRoleFound implements Event{

	private List<WebSiteRole> webSiteRoles;

	public WebSiteRoleFound(List<WebSiteRole> webSiteRoles) {
		this.setWebSiteRoles(webSiteRoles);
	}

	public List<WebSiteRole> getWebSiteRoles()	{
		return webSiteRoles;
	}

	public void setWebSiteRoles(List<WebSiteRole> webSiteRoles)	{
		this.webSiteRoles = webSiteRoles;
	}
}
