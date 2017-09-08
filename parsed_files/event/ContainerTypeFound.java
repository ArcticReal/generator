package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContainerTypeFound implements Event{

	private List<ContainerType> containerTypes;

	public ContainerTypeFound(List<ContainerType> containerTypes) {
		this.setContainerTypes(containerTypes);
	}

	public List<ContainerType> getContainerTypes()	{
		return containerTypes;
	}

	public void setContainerTypes(List<ContainerType> containerTypes)	{
		this.containerTypes = containerTypes;
	}
}
