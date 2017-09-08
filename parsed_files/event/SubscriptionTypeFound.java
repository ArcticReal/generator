package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SubscriptionTypeFound implements Event{

	private List<SubscriptionType> subscriptionTypes;

	public SubscriptionTypeFound(List<SubscriptionType> subscriptionTypes) {
		this.setSubscriptionTypes(subscriptionTypes);
	}

	public List<SubscriptionType> getSubscriptionTypes()	{
		return subscriptionTypes;
	}

	public void setSubscriptionTypes(List<SubscriptionType> subscriptionTypes)	{
		this.subscriptionTypes = subscriptionTypes;
	}
}
