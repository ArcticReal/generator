package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyBenefitFound implements Event{

	private List<PartyBenefit> partyBenefits;

	public PartyBenefitFound(List<PartyBenefit> partyBenefits) {
		this.setPartyBenefits(partyBenefits);
	}

	public List<PartyBenefit> getPartyBenefits()	{
		return partyBenefits;
	}

	public void setPartyBenefits(List<PartyBenefit> partyBenefits)	{
		this.partyBenefits = partyBenefits;
	}
}
