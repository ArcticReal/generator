package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderShipmentFound implements Event{

	private List<OrderShipment> orderShipments;

	public OrderShipmentFound(List<OrderShipment> orderShipments) {
		this.setOrderShipments(orderShipments);
	}

	public List<OrderShipment> getOrderShipments()	{
		return orderShipments;
	}

	public void setOrderShipments(List<OrderShipment> orderShipments)	{
		this.orderShipments = orderShipments;
	}
}
