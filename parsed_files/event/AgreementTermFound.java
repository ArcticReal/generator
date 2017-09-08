package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementTermFound implements Event{

	private List<AgreementTerm> agreementTerms;

	public AgreementTermFound(List<AgreementTerm> agreementTerms) {
		this.setAgreementTerms(agreementTerms);
	}

	public List<AgreementTerm> getAgreementTerms()	{
		return agreementTerms;
	}

	public void setAgreementTerms(List<AgreementTerm> agreementTerms)	{
		this.agreementTerms = agreementTerms;
	}
}
