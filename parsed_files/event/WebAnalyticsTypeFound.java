package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebAnalyticsTypeFound implements Event{

	private List<WebAnalyticsType> webAnalyticsTypes;

	public WebAnalyticsTypeFound(List<WebAnalyticsType> webAnalyticsTypes) {
		this.setWebAnalyticsTypes(webAnalyticsTypes);
	}

	public List<WebAnalyticsType> getWebAnalyticsTypes()	{
		return webAnalyticsTypes;
	}

	public void setWebAnalyticsTypes(List<WebAnalyticsType> webAnalyticsTypes)	{
		this.webAnalyticsTypes = webAnalyticsTypes;
	}
}
