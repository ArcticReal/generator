package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementTermAttributeFound implements Event{

	private List<AgreementTermAttribute> agreementTermAttributes;

	public AgreementTermAttributeFound(List<AgreementTermAttribute> agreementTermAttributes) {
		this.setAgreementTermAttributes(agreementTermAttributes);
	}

	public List<AgreementTermAttribute> getAgreementTermAttributes()	{
		return agreementTermAttributes;
	}

	public void setAgreementTermAttributes(List<AgreementTermAttribute> agreementTermAttributes)	{
		this.agreementTermAttributes = agreementTermAttributes;
	}
}
