package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataResourcePurposeFound implements Event{

	private List<DataResourcePurpose> dataResourcePurposes;

	public DataResourcePurposeFound(List<DataResourcePurpose> dataResourcePurposes) {
		this.setDataResourcePurposes(dataResourcePurposes);
	}

	public List<DataResourcePurpose> getDataResourcePurposes()	{
		return dataResourcePurposes;
	}

	public void setDataResourcePurposes(List<DataResourcePurpose> dataResourcePurposes)	{
		this.dataResourcePurposes = dataResourcePurposes;
	}
}
