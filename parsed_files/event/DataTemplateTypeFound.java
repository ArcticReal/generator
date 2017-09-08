package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataTemplateTypeFound implements Event{

	private List<DataTemplateType> dataTemplateTypes;

	public DataTemplateTypeFound(List<DataTemplateType> dataTemplateTypes) {
		this.setDataTemplateTypes(dataTemplateTypes);
	}

	public List<DataTemplateType> getDataTemplateTypes()	{
		return dataTemplateTypes;
	}

	public void setDataTemplateTypes(List<DataTemplateType> dataTemplateTypes)	{
		this.dataTemplateTypes = dataTemplateTypes;
	}
}
