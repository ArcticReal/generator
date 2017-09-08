package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementContentTypeFound implements Event{

	private List<AgreementContentType> agreementContentTypes;

	public AgreementContentTypeFound(List<AgreementContentType> agreementContentTypes) {
		this.setAgreementContentTypes(agreementContentTypes);
	}

	public List<AgreementContentType> getAgreementContentTypes()	{
		return agreementContentTypes;
	}

	public void setAgreementContentTypes(List<AgreementContentType> agreementContentTypes)	{
		this.agreementContentTypes = agreementContentTypes;
	}
}
