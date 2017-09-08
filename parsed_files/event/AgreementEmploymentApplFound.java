package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementEmploymentApplFound implements Event{

	private List<AgreementEmploymentAppl> agreementEmploymentAppls;

	public AgreementEmploymentApplFound(List<AgreementEmploymentAppl> agreementEmploymentAppls) {
		this.setAgreementEmploymentAppls(agreementEmploymentAppls);
	}

	public List<AgreementEmploymentAppl> getAgreementEmploymentAppls()	{
		return agreementEmploymentAppls;
	}

	public void setAgreementEmploymentAppls(List<AgreementEmploymentAppl> agreementEmploymentAppls)	{
		this.agreementEmploymentAppls = agreementEmploymentAppls;
	}
}
