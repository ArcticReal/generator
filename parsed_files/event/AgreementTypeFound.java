package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementTypeFound implements Event{

	private List<AgreementType> agreementTypes;

	public AgreementTypeFound(List<AgreementType> agreementTypes) {
		this.setAgreementTypes(agreementTypes);
	}

	public List<AgreementType> getAgreementTypes()	{
		return agreementTypes;
	}

	public void setAgreementTypes(List<AgreementType> agreementTypes)	{
		this.agreementTypes = agreementTypes;
	}
}
