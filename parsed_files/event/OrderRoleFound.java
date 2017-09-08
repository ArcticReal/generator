package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderRoleFound implements Event{

	private List<OrderRole> orderRoles;

	public OrderRoleFound(List<OrderRole> orderRoles) {
		this.setOrderRoles(orderRoles);
	}

	public List<OrderRole> getOrderRoles()	{
		return orderRoles;
	}

	public void setOrderRoles(List<OrderRole> orderRoles)	{
		this.orderRoles = orderRoles;
	}
}
