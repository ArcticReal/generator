package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShipmentRouteSegmentFound implements Event{

	private List<ShipmentRouteSegment> shipmentRouteSegments;

	public ShipmentRouteSegmentFound(List<ShipmentRouteSegment> shipmentRouteSegments) {
		this.setShipmentRouteSegments(shipmentRouteSegments);
	}

	public List<ShipmentRouteSegment> getShipmentRouteSegments()	{
		return shipmentRouteSegments;
	}

	public void setShipmentRouteSegments(List<ShipmentRouteSegment> shipmentRouteSegments)	{
		this.shipmentRouteSegments = shipmentRouteSegments;
	}
}
