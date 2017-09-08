package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContainerGeoPointFound implements Event{

	private List<ContainerGeoPoint> containerGeoPoints;

	public ContainerGeoPointFound(List<ContainerGeoPoint> containerGeoPoints) {
		this.setContainerGeoPoints(containerGeoPoints);
	}

	public List<ContainerGeoPoint> getContainerGeoPoints()	{
		return containerGeoPoints;
	}

	public void setContainerGeoPoints(List<ContainerGeoPoint> containerGeoPoints)	{
		this.containerGeoPoints = containerGeoPoints;
	}
}
