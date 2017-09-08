package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SubscriptionResourceFound implements Event{

	private List<SubscriptionResource> subscriptionResources;

	public SubscriptionResourceFound(List<SubscriptionResource> subscriptionResources) {
		this.setSubscriptionResources(subscriptionResources);
	}

	public List<SubscriptionResource> getSubscriptionResources()	{
		return subscriptionResources;
	}

	public void setSubscriptionResources(List<SubscriptionResource> subscriptionResources)	{
		this.subscriptionResources = subscriptionResources;
	}
}
