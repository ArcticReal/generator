package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TrackingCodeOrderFound implements Event{

	private List<TrackingCodeOrder> trackingCodeOrders;

	public TrackingCodeOrderFound(List<TrackingCodeOrder> trackingCodeOrders) {
		this.setTrackingCodeOrders(trackingCodeOrders);
	}

	public List<TrackingCodeOrder> getTrackingCodeOrders()	{
		return trackingCodeOrders;
	}

	public void setTrackingCodeOrders(List<TrackingCodeOrder> trackingCodeOrders)	{
		this.trackingCodeOrders = trackingCodeOrders;
	}
}
