package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemShipGrpInvResFound implements Event{

	private List<OrderItemShipGrpInvRes> orderItemShipGrpInvRess;

	public OrderItemShipGrpInvResFound(List<OrderItemShipGrpInvRes> orderItemShipGrpInvRess) {
		this.setOrderItemShipGrpInvRess(orderItemShipGrpInvRess);
	}

	public List<OrderItemShipGrpInvRes> getOrderItemShipGrpInvRess()	{
		return orderItemShipGrpInvRess;
	}

	public void setOrderItemShipGrpInvRess(List<OrderItemShipGrpInvRes> orderItemShipGrpInvRess)	{
		this.orderItemShipGrpInvRess = orderItemShipGrpInvRess;
	}
}
