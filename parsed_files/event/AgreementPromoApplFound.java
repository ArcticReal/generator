package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementPromoApplFound implements Event{

	private List<AgreementPromoAppl> agreementPromoAppls;

	public AgreementPromoApplFound(List<AgreementPromoAppl> agreementPromoAppls) {
		this.setAgreementPromoAppls(agreementPromoAppls);
	}

	public List<AgreementPromoAppl> getAgreementPromoAppls()	{
		return agreementPromoAppls;
	}

	public void setAgreementPromoAppls(List<AgreementPromoAppl> agreementPromoAppls)	{
		this.agreementPromoAppls = agreementPromoAppls;
	}
}
