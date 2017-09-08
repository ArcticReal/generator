package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentPackageContentFound implements Event{

	private List<ShipmentPackageContent> shipmentPackageContents;

	public ShipmentPackageContentFound(List<ShipmentPackageContent> shipmentPackageContents) {
		this.setShipmentPackageContents(shipmentPackageContents);
	}

	public List<ShipmentPackageContent> getShipmentPackageContents()	{
		return shipmentPackageContents;
	}

	public void setShipmentPackageContents(List<ShipmentPackageContent> shipmentPackageContents)	{
		this.shipmentPackageContents = shipmentPackageContents;
	}
}
