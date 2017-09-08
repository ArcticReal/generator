package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class UnemploymentClaimFound implements Event{

	private List<UnemploymentClaim> unemploymentClaims;

	public UnemploymentClaimFound(List<UnemploymentClaim> unemploymentClaims) {
		this.setUnemploymentClaims(unemploymentClaims);
	}

	public List<UnemploymentClaim> getUnemploymentClaims()	{
		return unemploymentClaims;
	}

	public void setUnemploymentClaims(List<UnemploymentClaim> unemploymentClaims)	{
		this.unemploymentClaims = unemploymentClaims;
	}
}
