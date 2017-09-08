package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementItemTypeFound implements Event{

	private List<AgreementItemType> agreementItemTypes;

	public AgreementItemTypeFound(List<AgreementItemType> agreementItemTypes) {
		this.setAgreementItemTypes(agreementItemTypes);
	}

	public List<AgreementItemType> getAgreementItemTypes()	{
		return agreementItemTypes;
	}

	public void setAgreementItemTypes(List<AgreementItemType> agreementItemTypes)	{
		this.agreementItemTypes = agreementItemTypes;
	}
}
