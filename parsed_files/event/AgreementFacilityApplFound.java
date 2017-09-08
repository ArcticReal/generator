package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementFacilityApplFound implements Event{

	private List<AgreementFacilityAppl> agreementFacilityAppls;

	public AgreementFacilityApplFound(List<AgreementFacilityAppl> agreementFacilityAppls) {
		this.setAgreementFacilityAppls(agreementFacilityAppls);
	}

	public List<AgreementFacilityAppl> getAgreementFacilityAppls()	{
		return agreementFacilityAppls;
	}

	public void setAgreementFacilityAppls(List<AgreementFacilityAppl> agreementFacilityAppls)	{
		this.agreementFacilityAppls = agreementFacilityAppls;
	}
}
