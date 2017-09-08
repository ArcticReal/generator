package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderNotificationFound implements Event{

	private List<OrderNotification> orderNotifications;

	public OrderNotificationFound(List<OrderNotification> orderNotifications) {
		this.setOrderNotifications(orderNotifications);
	}

	public List<OrderNotification> getOrderNotifications()	{
		return orderNotifications;
	}

	public void setOrderNotifications(List<OrderNotification> orderNotifications)	{
		this.orderNotifications = orderNotifications;
	}
}
