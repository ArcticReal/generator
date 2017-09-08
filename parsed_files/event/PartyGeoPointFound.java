package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyGeoPointFound implements Event{

	private List<PartyGeoPoint> partyGeoPoints;

	public PartyGeoPointFound(List<PartyGeoPoint> partyGeoPoints) {
		this.setPartyGeoPoints(partyGeoPoints);
	}

	public List<PartyGeoPoint> getPartyGeoPoints()	{
		return partyGeoPoints;
	}

	public void setPartyGeoPoints(List<PartyGeoPoint> partyGeoPoints)	{
		this.partyGeoPoints = partyGeoPoints;
	}
}
