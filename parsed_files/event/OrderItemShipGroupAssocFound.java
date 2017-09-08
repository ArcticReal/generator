package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemShipGroupAssocFound implements Event{

	private List<OrderItemShipGroupAssoc> orderItemShipGroupAssocs;

	public OrderItemShipGroupAssocFound(List<OrderItemShipGroupAssoc> orderItemShipGroupAssocs) {
		this.setOrderItemShipGroupAssocs(orderItemShipGroupAssocs);
	}

	public List<OrderItemShipGroupAssoc> getOrderItemShipGroupAssocs()	{
		return orderItemShipGroupAssocs;
	}

	public void setOrderItemShipGroupAssocs(List<OrderItemShipGroupAssoc> orderItemShipGroupAssocs)	{
		this.orderItemShipGroupAssocs = orderItemShipGroupAssocs;
	}
}
