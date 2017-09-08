package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DeliveryFound implements Event{

	private List<Delivery> deliverys;

	public DeliveryFound(List<Delivery> deliverys) {
		this.setDeliverys(deliverys);
	}

	public List<Delivery> getDeliverys()	{
		return deliverys;
	}

	public void setDeliverys(List<Delivery> deliverys)	{
		this.deliverys = deliverys;
	}
}
