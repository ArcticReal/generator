package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebAnalyticsConfigFound implements Event{

	private List<WebAnalyticsConfig> webAnalyticsConfigs;

	public WebAnalyticsConfigFound(List<WebAnalyticsConfig> webAnalyticsConfigs) {
		this.setWebAnalyticsConfigs(webAnalyticsConfigs);
	}

	public List<WebAnalyticsConfig> getWebAnalyticsConfigs()	{
		return webAnalyticsConfigs;
	}

	public void setWebAnalyticsConfigs(List<WebAnalyticsConfig> webAnalyticsConfigs)	{
		this.webAnalyticsConfigs = webAnalyticsConfigs;
	}
}
