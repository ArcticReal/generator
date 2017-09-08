package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentStatusFound implements Event{

	private List<ShipmentStatus> shipmentStatuss;

	public ShipmentStatusFound(List<ShipmentStatus> shipmentStatuss) {
		this.setShipmentStatuss(shipmentStatuss);
	}

	public List<ShipmentStatus> getShipmentStatuss()	{
		return shipmentStatuss;
	}

	public void setShipmentStatuss(List<ShipmentStatus> shipmentStatuss)	{
		this.shipmentStatuss = shipmentStatuss;
	}
}
