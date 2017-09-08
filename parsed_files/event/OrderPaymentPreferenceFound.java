package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderPaymentPreferenceFound implements Event{

	private List<OrderPaymentPreference> orderPaymentPreferences;

	public OrderPaymentPreferenceFound(List<OrderPaymentPreference> orderPaymentPreferences) {
		this.setOrderPaymentPreferences(orderPaymentPreferences);
	}

	public List<OrderPaymentPreference> getOrderPaymentPreferences()	{
		return orderPaymentPreferences;
	}

	public void setOrderPaymentPreferences(List<OrderPaymentPreference> orderPaymentPreferences)	{
		this.orderPaymentPreferences = orderPaymentPreferences;
	}
}
