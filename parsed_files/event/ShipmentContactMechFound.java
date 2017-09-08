package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentContactMechFound implements Event{

	private List<ShipmentContactMech> shipmentContactMechs;

	public ShipmentContactMechFound(List<ShipmentContactMech> shipmentContactMechs) {
		this.setShipmentContactMechs(shipmentContactMechs);
	}

	public List<ShipmentContactMech> getShipmentContactMechs()	{
		return shipmentContactMechs;
	}

	public void setShipmentContactMechs(List<ShipmentContactMech> shipmentContactMechs)	{
		this.shipmentContactMechs = shipmentContactMechs;
	}
}
