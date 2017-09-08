package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SubscriptionCommEventFound implements Event{

	private List<SubscriptionCommEvent> subscriptionCommEvents;

	public SubscriptionCommEventFound(List<SubscriptionCommEvent> subscriptionCommEvents) {
		this.setSubscriptionCommEvents(subscriptionCommEvents);
	}

	public List<SubscriptionCommEvent> getSubscriptionCommEvents()	{
		return subscriptionCommEvents;
	}

	public void setSubscriptionCommEvents(List<SubscriptionCommEvent> subscriptionCommEvents)	{
		this.subscriptionCommEvents = subscriptionCommEvents;
	}
}
