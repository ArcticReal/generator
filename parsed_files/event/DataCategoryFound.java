package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataCategoryFound implements Event{

	private List<DataCategory> dataCategorys;

	public DataCategoryFound(List<DataCategory> dataCategorys) {
		this.setDataCategorys(dataCategorys);
	}

	public List<DataCategory> getDataCategorys()	{
		return dataCategorys;
	}

	public void setDataCategorys(List<DataCategory> dataCategorys)	{
		this.dataCategorys = dataCategorys;
	}
}
