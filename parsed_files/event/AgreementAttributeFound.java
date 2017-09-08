package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementAttributeFound implements Event{

	private List<AgreementAttribute> agreementAttributes;

	public AgreementAttributeFound(List<AgreementAttribute> agreementAttributes) {
		this.setAgreementAttributes(agreementAttributes);
	}

	public List<AgreementAttribute> getAgreementAttributes()	{
		return agreementAttributes;
	}

	public void setAgreementAttributes(List<AgreementAttribute> agreementAttributes)	{
		this.agreementAttributes = agreementAttributes;
	}
}
