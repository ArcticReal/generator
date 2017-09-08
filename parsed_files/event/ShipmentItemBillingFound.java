package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentItemBillingFound implements Event{

	private List<ShipmentItemBilling> shipmentItemBillings;

	public ShipmentItemBillingFound(List<ShipmentItemBilling> shipmentItemBillings) {
		this.setShipmentItemBillings(shipmentItemBillings);
	}

	public List<ShipmentItemBilling> getShipmentItemBillings()	{
		return shipmentItemBillings;
	}

	public void setShipmentItemBillings(List<ShipmentItemBilling> shipmentItemBillings)	{
		this.shipmentItemBillings = shipmentItemBillings;
	}
}
