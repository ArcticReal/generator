package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SubscriptionActivityFound implements Event{

	private List<SubscriptionActivity> subscriptionActivitys;

	public SubscriptionActivityFound(List<SubscriptionActivity> subscriptionActivitys) {
		this.setSubscriptionActivitys(subscriptionActivitys);
	}

	public List<SubscriptionActivity> getSubscriptionActivitys()	{
		return subscriptionActivitys;
	}

	public void setSubscriptionActivitys(List<SubscriptionActivity> subscriptionActivitys)	{
		this.subscriptionActivitys = subscriptionActivitys;
	}
}
