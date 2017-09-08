package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SubscriptionAttributeFound implements Event{

	private List<SubscriptionAttribute> subscriptionAttributes;

	public SubscriptionAttributeFound(List<SubscriptionAttribute> subscriptionAttributes) {
		this.setSubscriptionAttributes(subscriptionAttributes);
	}

	public List<SubscriptionAttribute> getSubscriptionAttributes()	{
		return subscriptionAttributes;
	}

	public void setSubscriptionAttributes(List<SubscriptionAttribute> subscriptionAttributes)	{
		this.subscriptionAttributes = subscriptionAttributes;
	}
}
