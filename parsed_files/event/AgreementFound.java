package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementFound implements Event{

	private List<Agreement> agreements;

	public AgreementFound(List<Agreement> agreements) {
		this.setAgreements(agreements);
	}

	public List<Agreement> getAgreements()	{
		return agreements;
	}

	public void setAgreements(List<Agreement> agreements)	{
		this.agreements = agreements;
	}
}
