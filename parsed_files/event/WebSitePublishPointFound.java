package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebSitePublishPointFound implements Event{

	private List<WebSitePublishPoint> webSitePublishPoints;

	public WebSitePublishPointFound(List<WebSitePublishPoint> webSitePublishPoints) {
		this.setWebSitePublishPoints(webSitePublishPoints);
	}

	public List<WebSitePublishPoint> getWebSitePublishPoints()	{
		return webSitePublishPoints;
	}

	public void setWebSitePublishPoints(List<WebSitePublishPoint> webSitePublishPoints)	{
		this.webSitePublishPoints = webSitePublishPoints;
	}
}
