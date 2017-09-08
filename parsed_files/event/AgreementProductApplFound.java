package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementProductApplFound implements Event{

	private List<AgreementProductAppl> agreementProductAppls;

	public AgreementProductApplFound(List<AgreementProductAppl> agreementProductAppls) {
		this.setAgreementProductAppls(agreementProductAppls);
	}

	public List<AgreementProductAppl> getAgreementProductAppls()	{
		return agreementProductAppls;
	}

	public void setAgreementProductAppls(List<AgreementProductAppl> agreementProductAppls)	{
		this.agreementProductAppls = agreementProductAppls;
	}
}
