package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnAdjustmentTypeFound implements Event{

	private List<ReturnAdjustmentType> returnAdjustmentTypes;

	public ReturnAdjustmentTypeFound(List<ReturnAdjustmentType> returnAdjustmentTypes) {
		this.setReturnAdjustmentTypes(returnAdjustmentTypes);
	}

	public List<ReturnAdjustmentType> getReturnAdjustmentTypes()	{
		return returnAdjustmentTypes;
	}

	public void setReturnAdjustmentTypes(List<ReturnAdjustmentType> returnAdjustmentTypes)	{
		this.returnAdjustmentTypes = returnAdjustmentTypes;
	}
}
