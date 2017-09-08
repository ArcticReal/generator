package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnItemTypeMapFound implements Event{

	private List<ReturnItemTypeMap> returnItemTypeMaps;

	public ReturnItemTypeMapFound(List<ReturnItemTypeMap> returnItemTypeMaps) {
		this.setReturnItemTypeMaps(returnItemTypeMaps);
	}

	public List<ReturnItemTypeMap> getReturnItemTypeMaps()	{
		return returnItemTypeMaps;
	}

	public void setReturnItemTypeMaps(List<ReturnItemTypeMap> returnItemTypeMaps)	{
		this.returnItemTypeMaps = returnItemTypeMaps;
	}
}
