package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemRoleFound implements Event{

	private List<OrderItemRole> orderItemRoles;

	public OrderItemRoleFound(List<OrderItemRole> orderItemRoles) {
		this.setOrderItemRoles(orderItemRoles);
	}

	public List<OrderItemRole> getOrderItemRoles()	{
		return orderItemRoles;
	}

	public void setOrderItemRoles(List<OrderItemRole> orderItemRoles)	{
		this.orderItemRoles = orderItemRoles;
	}
}
