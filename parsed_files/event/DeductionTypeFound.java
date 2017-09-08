package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DeductionTypeFound implements Event{

	private List<DeductionType> deductionTypes;

	public DeductionTypeFound(List<DeductionType> deductionTypes) {
		this.setDeductionTypes(deductionTypes);
	}

	public List<DeductionType> getDeductionTypes()	{
		return deductionTypes;
	}

	public void setDeductionTypes(List<DeductionType> deductionTypes)	{
		this.deductionTypes = deductionTypes;
	}
}
