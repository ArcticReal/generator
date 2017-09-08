package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BenefitTypeFound implements Event{

	private List<BenefitType> benefitTypes;

	public BenefitTypeFound(List<BenefitType> benefitTypes) {
		this.setBenefitTypes(benefitTypes);
	}

	public List<BenefitType> getBenefitTypes()	{
		return benefitTypes;
	}

	public void setBenefitTypes(List<BenefitType> benefitTypes)	{
		this.benefitTypes = benefitTypes;
	}
}
