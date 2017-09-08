package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemContactMechFound implements Event{

	private List<OrderItemContactMech> orderItemContactMechs;

	public OrderItemContactMechFound(List<OrderItemContactMech> orderItemContactMechs) {
		this.setOrderItemContactMechs(orderItemContactMechs);
	}

	public List<OrderItemContactMech> getOrderItemContactMechs()	{
		return orderItemContactMechs;
	}

	public void setOrderItemContactMechs(List<OrderItemContactMech> orderItemContactMechs)	{
		this.orderItemContactMechs = orderItemContactMechs;
	}
}
