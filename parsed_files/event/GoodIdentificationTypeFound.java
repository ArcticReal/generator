package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GoodIdentificationTypeFound implements Event{

	private List<GoodIdentificationType> goodIdentificationTypes;

	public GoodIdentificationTypeFound(List<GoodIdentificationType> goodIdentificationTypes) {
		this.setGoodIdentificationTypes(goodIdentificationTypes);
	}

	public List<GoodIdentificationType> getGoodIdentificationTypes()	{
		return goodIdentificationTypes;
	}

	public void setGoodIdentificationTypes(List<GoodIdentificationType> goodIdentificationTypes)	{
		this.goodIdentificationTypes = goodIdentificationTypes;
	}
}
