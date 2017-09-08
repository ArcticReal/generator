package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataResourceFound implements Event{

	private List<DataResource> dataResources;

	public DataResourceFound(List<DataResource> dataResources) {
		this.setDataResources(dataResources);
	}

	public List<DataResource> getDataResources()	{
		return dataResources;
	}

	public void setDataResources(List<DataResource> dataResources)	{
		this.dataResources = dataResources;
	}
}
