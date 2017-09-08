package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementContentFound implements Event{

	private List<AgreementContent> agreementContents;

	public AgreementContentFound(List<AgreementContent> agreementContents) {
		this.setAgreementContents(agreementContents);
	}

	public List<AgreementContent> getAgreementContents()	{
		return agreementContents;
	}

	public void setAgreementContents(List<AgreementContent> agreementContents)	{
		this.agreementContents = agreementContents;
	}
}
