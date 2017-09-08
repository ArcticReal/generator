package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementGeographicalApplicFound implements Event{

	private List<AgreementGeographicalApplic> agreementGeographicalApplics;

	public AgreementGeographicalApplicFound(List<AgreementGeographicalApplic> agreementGeographicalApplics) {
		this.setAgreementGeographicalApplics(agreementGeographicalApplics);
	}

	public List<AgreementGeographicalApplic> getAgreementGeographicalApplics()	{
		return agreementGeographicalApplics;
	}

	public void setAgreementGeographicalApplics(List<AgreementGeographicalApplic> agreementGeographicalApplics)	{
		this.agreementGeographicalApplics = agreementGeographicalApplics;
	}
}
