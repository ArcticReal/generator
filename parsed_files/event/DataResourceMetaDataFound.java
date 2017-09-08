package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataResourceMetaDataFound implements Event{

	private List<DataResourceMetaData> dataResourceMetaDatas;

	public DataResourceMetaDataFound(List<DataResourceMetaData> dataResourceMetaDatas) {
		this.setDataResourceMetaDatas(dataResourceMetaDatas);
	}

	public List<DataResourceMetaData> getDataResourceMetaDatas()	{
		return dataResourceMetaDatas;
	}

	public void setDataResourceMetaDatas(List<DataResourceMetaData> dataResourceMetaDatas)	{
		this.dataResourceMetaDatas = dataResourceMetaDatas;
	}
}
