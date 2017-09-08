package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnAdjustmentFound implements Event{

	private List<ReturnAdjustment> returnAdjustments;

	public ReturnAdjustmentFound(List<ReturnAdjustment> returnAdjustments) {
		this.setReturnAdjustments(returnAdjustments);
	}

	public List<ReturnAdjustment> getReturnAdjustments()	{
		return returnAdjustments;
	}

	public void setReturnAdjustments(List<ReturnAdjustment> returnAdjustments)	{
		this.returnAdjustments = returnAdjustments;
	}
}
