package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebSitePathAliasFound implements Event{

	private List<WebSitePathAlias> webSitePathAliass;

	public WebSitePathAliasFound(List<WebSitePathAlias> webSitePathAliass) {
		this.setWebSitePathAliass(webSitePathAliass);
	}

	public List<WebSitePathAlias> getWebSitePathAliass()	{
		return webSitePathAliass;
	}

	public void setWebSitePathAliass(List<WebSitePathAlias> webSitePathAliass)	{
		this.webSitePathAliass = webSitePathAliass;
	}
}
