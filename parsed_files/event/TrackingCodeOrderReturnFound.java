package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TrackingCodeOrderReturnFound implements Event{

	private List<TrackingCodeOrderReturn> trackingCodeOrderReturns;

	public TrackingCodeOrderReturnFound(List<TrackingCodeOrderReturn> trackingCodeOrderReturns) {
		this.setTrackingCodeOrderReturns(trackingCodeOrderReturns);
	}

	public List<TrackingCodeOrderReturn> getTrackingCodeOrderReturns()	{
		return trackingCodeOrderReturns;
	}

	public void setTrackingCodeOrderReturns(List<TrackingCodeOrderReturn> trackingCodeOrderReturns)	{
		this.trackingCodeOrderReturns = trackingCodeOrderReturns;
	}
}
