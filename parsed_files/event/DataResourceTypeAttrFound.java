package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataResourceTypeAttrFound implements Event{

	private List<DataResourceTypeAttr> dataResourceTypeAttrs;

	public DataResourceTypeAttrFound(List<DataResourceTypeAttr> dataResourceTypeAttrs) {
		this.setDataResourceTypeAttrs(dataResourceTypeAttrs);
	}

	public List<DataResourceTypeAttr> getDataResourceTypeAttrs()	{
		return dataResourceTypeAttrs;
	}

	public void setDataResourceTypeAttrs(List<DataResourceTypeAttr> dataResourceTypeAttrs)	{
		this.dataResourceTypeAttrs = dataResourceTypeAttrs;
	}
}
