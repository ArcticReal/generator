package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemAssocFound implements Event{

	private List<OrderItemAssoc> orderItemAssocs;

	public OrderItemAssocFound(List<OrderItemAssoc> orderItemAssocs) {
		this.setOrderItemAssocs(orderItemAssocs);
	}

	public List<OrderItemAssoc> getOrderItemAssocs()	{
		return orderItemAssocs;
	}

	public void setOrderItemAssocs(List<OrderItemAssoc> orderItemAssocs)	{
		this.orderItemAssocs = orderItemAssocs;
	}
}
