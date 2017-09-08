package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class NeedTypeFound implements Event{

	private List<NeedType> needTypes;

	public NeedTypeFound(List<NeedType> needTypes) {
		this.setNeedTypes(needTypes);
	}

	public List<NeedType> getNeedTypes()	{
		return needTypes;
	}

	public void setNeedTypes(List<NeedType> needTypes)	{
		this.needTypes = needTypes;
	}
}
