package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataResourceAttributeFound implements Event{

	private List<DataResourceAttribute> dataResourceAttributes;

	public DataResourceAttributeFound(List<DataResourceAttribute> dataResourceAttributes) {
		this.setDataResourceAttributes(dataResourceAttributes);
	}

	public List<DataResourceAttribute> getDataResourceAttributes()	{
		return dataResourceAttributes;
	}

	public void setDataResourceAttributes(List<DataResourceAttribute> dataResourceAttributes)	{
		this.dataResourceAttributes = dataResourceAttributes;
	}
}
