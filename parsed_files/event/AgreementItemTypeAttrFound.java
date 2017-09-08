package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementItemTypeAttrFound implements Event{

	private List<AgreementItemTypeAttr> agreementItemTypeAttrs;

	public AgreementItemTypeAttrFound(List<AgreementItemTypeAttr> agreementItemTypeAttrs) {
		this.setAgreementItemTypeAttrs(agreementItemTypeAttrs);
	}

	public List<AgreementItemTypeAttr> getAgreementItemTypeAttrs()	{
		return agreementItemTypeAttrs;
	}

	public void setAgreementItemTypeAttrs(List<AgreementItemTypeAttr> agreementItemTypeAttrs)	{
		this.agreementItemTypeAttrs = agreementItemTypeAttrs;
	}
}
