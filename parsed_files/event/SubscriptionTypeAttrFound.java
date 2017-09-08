package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SubscriptionTypeAttrFound implements Event{

	private List<SubscriptionTypeAttr> subscriptionTypeAttrs;

	public SubscriptionTypeAttrFound(List<SubscriptionTypeAttr> subscriptionTypeAttrs) {
		this.setSubscriptionTypeAttrs(subscriptionTypeAttrs);
	}

	public List<SubscriptionTypeAttr> getSubscriptionTypeAttrs()	{
		return subscriptionTypeAttrs;
	}

	public void setSubscriptionTypeAttrs(List<SubscriptionTypeAttr> subscriptionTypeAttrs)	{
		this.subscriptionTypeAttrs = subscriptionTypeAttrs;
	}
}
