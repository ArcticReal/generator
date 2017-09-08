package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GoodIdentificationFound implements Event{

	private List<GoodIdentification> goodIdentifications;

	public GoodIdentificationFound(List<GoodIdentification> goodIdentifications) {
		this.setGoodIdentifications(goodIdentifications);
	}

	public List<GoodIdentification> getGoodIdentifications()	{
		return goodIdentifications;
	}

	public void setGoodIdentifications(List<GoodIdentification> goodIdentifications)	{
		this.goodIdentifications = goodIdentifications;
	}
}
