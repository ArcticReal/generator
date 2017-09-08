package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContainerFound implements Event{

	private List<Container> containers;

	public ContainerFound(List<Container> containers) {
		this.setContainers(containers);
	}

	public List<Container> getContainers()	{
		return containers;
	}

	public void setContainers(List<Container> containers)	{
		this.containers = containers;
	}
}
