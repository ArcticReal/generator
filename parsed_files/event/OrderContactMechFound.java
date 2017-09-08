package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderContactMechFound implements Event{

	private List<OrderContactMech> orderContactMechs;

	public OrderContactMechFound(List<OrderContactMech> orderContactMechs) {
		this.setOrderContactMechs(orderContactMechs);
	}

	public List<OrderContactMech> getOrderContactMechs()	{
		return orderContactMechs;
	}

	public void setOrderContactMechs(List<OrderContactMech> orderContactMechs)	{
		this.orderContactMechs = orderContactMechs;
	}
}
