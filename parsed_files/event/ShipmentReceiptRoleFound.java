package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentReceiptRoleFound implements Event{

	private List<ShipmentReceiptRole> shipmentReceiptRoles;

	public ShipmentReceiptRoleFound(List<ShipmentReceiptRole> shipmentReceiptRoles) {
		this.setShipmentReceiptRoles(shipmentReceiptRoles);
	}

	public List<ShipmentReceiptRole> getShipmentReceiptRoles()	{
		return shipmentReceiptRoles;
	}

	public void setShipmentReceiptRoles(List<ShipmentReceiptRole> shipmentReceiptRoles)	{
		this.shipmentReceiptRoles = shipmentReceiptRoles;
	}
}
