package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataResourceTypeFound implements Event{

	private List<DataResourceType> dataResourceTypes;

	public DataResourceTypeFound(List<DataResourceType> dataResourceTypes) {
		this.setDataResourceTypes(dataResourceTypes);
	}

	public List<DataResourceType> getDataResourceTypes()	{
		return dataResourceTypes;
	}

	public void setDataResourceTypes(List<DataResourceType> dataResourceTypes)	{
		this.dataResourceTypes = dataResourceTypes;
	}
}
