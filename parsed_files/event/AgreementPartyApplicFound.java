package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementPartyApplicFound implements Event{

	private List<AgreementPartyApplic> agreementPartyApplics;

	public AgreementPartyApplicFound(List<AgreementPartyApplic> agreementPartyApplics) {
		this.setAgreementPartyApplics(agreementPartyApplics);
	}

	public List<AgreementPartyApplic> getAgreementPartyApplics()	{
		return agreementPartyApplics;
	}

	public void setAgreementPartyApplics(List<AgreementPartyApplic> agreementPartyApplics)	{
		this.agreementPartyApplics = agreementPartyApplics;
	}
}
