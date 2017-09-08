package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TrackingCodeVisitFound implements Event{

	private List<TrackingCodeVisit> trackingCodeVisits;

	public TrackingCodeVisitFound(List<TrackingCodeVisit> trackingCodeVisits) {
		this.setTrackingCodeVisits(trackingCodeVisits);
	}

	public List<TrackingCodeVisit> getTrackingCodeVisits()	{
		return trackingCodeVisits;
	}

	public void setTrackingCodeVisits(List<TrackingCodeVisit> trackingCodeVisits)	{
		this.trackingCodeVisits = trackingCodeVisits;
	}
}
