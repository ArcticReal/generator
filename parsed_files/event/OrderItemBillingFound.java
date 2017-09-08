package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemBillingFound implements Event{

	private List<OrderItemBilling> orderItemBillings;

	public OrderItemBillingFound(List<OrderItemBilling> orderItemBillings) {
		this.setOrderItemBillings(orderItemBillings);
	}

	public List<OrderItemBilling> getOrderItemBillings()	{
		return orderItemBillings;
	}

	public void setOrderItemBillings(List<OrderItemBilling> orderItemBillings)	{
		this.orderItemBillings = orderItemBillings;
	}
}
