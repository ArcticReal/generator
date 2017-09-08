package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentTypeAttrFound implements Event{

	private List<ShipmentTypeAttr> shipmentTypeAttrs;

	public ShipmentTypeAttrFound(List<ShipmentTypeAttr> shipmentTypeAttrs) {
		this.setShipmentTypeAttrs(shipmentTypeAttrs);
	}

	public List<ShipmentTypeAttr> getShipmentTypeAttrs()	{
		return shipmentTypeAttrs;
	}

	public void setShipmentTypeAttrs(List<ShipmentTypeAttr> shipmentTypeAttrs)	{
		this.shipmentTypeAttrs = shipmentTypeAttrs;
	}
}
