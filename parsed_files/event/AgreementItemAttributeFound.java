package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementItemAttributeFound implements Event{

	private List<AgreementItemAttribute> agreementItemAttributes;

	public AgreementItemAttributeFound(List<AgreementItemAttribute> agreementItemAttributes) {
		this.setAgreementItemAttributes(agreementItemAttributes);
	}

	public List<AgreementItemAttribute> getAgreementItemAttributes()	{
		return agreementItemAttributes;
	}

	public void setAgreementItemAttributes(List<AgreementItemAttribute> agreementItemAttributes)	{
		this.agreementItemAttributes = agreementItemAttributes;
	}
}
