package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TrackingCodeFound implements Event{

	private List<TrackingCode> trackingCodes;

	public TrackingCodeFound(List<TrackingCode> trackingCodes) {
		this.setTrackingCodes(trackingCodes);
	}

	public List<TrackingCode> getTrackingCodes()	{
		return trackingCodes;
	}

	public void setTrackingCodes(List<TrackingCode> trackingCodes)	{
		this.trackingCodes = trackingCodes;
	}
}
