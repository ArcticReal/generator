package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementItemFound implements Event{

	private List<AgreementItem> agreementItems;

	public AgreementItemFound(List<AgreementItem> agreementItems) {
		this.setAgreementItems(agreementItems);
	}

	public List<AgreementItem> getAgreementItems()	{
		return agreementItems;
	}

	public void setAgreementItems(List<AgreementItem> agreementItems)	{
		this.agreementItems = agreementItems;
	}
}
