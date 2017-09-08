package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementTypeAttrFound implements Event{

	private List<AgreementTypeAttr> agreementTypeAttrs;

	public AgreementTypeAttrFound(List<AgreementTypeAttr> agreementTypeAttrs) {
		this.setAgreementTypeAttrs(agreementTypeAttrs);
	}

	public List<AgreementTypeAttr> getAgreementTypeAttrs()	{
		return agreementTypeAttrs;
	}

	public void setAgreementTypeAttrs(List<AgreementTypeAttr> agreementTypeAttrs)	{
		this.agreementTypeAttrs = agreementTypeAttrs;
	}
}
