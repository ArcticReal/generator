package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnItemShipmentFound implements Event{

	private List<ReturnItemShipment> returnItemShipments;

	public ReturnItemShipmentFound(List<ReturnItemShipment> returnItemShipments) {
		this.setReturnItemShipments(returnItemShipments);
	}

	public List<ReturnItemShipment> getReturnItemShipments()	{
		return returnItemShipments;
	}

	public void setReturnItemShipments(List<ReturnItemShipment> returnItemShipments)	{
		this.returnItemShipments = returnItemShipments;
	}
}
