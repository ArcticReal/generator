package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementWorkEffortApplicFound implements Event{

	private List<AgreementWorkEffortApplic> agreementWorkEffortApplics;

	public AgreementWorkEffortApplicFound(List<AgreementWorkEffortApplic> agreementWorkEffortApplics) {
		this.setAgreementWorkEffortApplics(agreementWorkEffortApplics);
	}

	public List<AgreementWorkEffortApplic> getAgreementWorkEffortApplics()	{
		return agreementWorkEffortApplics;
	}

	public void setAgreementWorkEffortApplics(List<AgreementWorkEffortApplic> agreementWorkEffortApplics)	{
		this.agreementWorkEffortApplics = agreementWorkEffortApplics;
	}
}
