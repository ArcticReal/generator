package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TrackingCodeTypeFound implements Event{

	private List<TrackingCodeType> trackingCodeTypes;

	public TrackingCodeTypeFound(List<TrackingCodeType> trackingCodeTypes) {
		this.setTrackingCodeTypes(trackingCodeTypes);
	}

	public List<TrackingCodeType> getTrackingCodeTypes()	{
		return trackingCodeTypes;
	}

	public void setTrackingCodeTypes(List<TrackingCodeType> trackingCodeTypes)	{
		this.trackingCodeTypes = trackingCodeTypes;
	}
}
