package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AccommodationSpotFound implements Event{

	private List<AccommodationSpot> accommodationSpots;

	public AccommodationSpotFound(List<AccommodationSpot> accommodationSpots) {
		this.setAccommodationSpots(accommodationSpots);
	}

	public List<AccommodationSpot> getAccommodationSpots()	{
		return accommodationSpots;
	}

	public void setAccommodationSpots(List<AccommodationSpot> accommodationSpots)	{
		this.accommodationSpots = accommodationSpots;
	}
}
