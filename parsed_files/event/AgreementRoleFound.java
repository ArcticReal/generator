package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AgreementRoleFound implements Event{

	private List<AgreementRole> agreementRoles;

	public AgreementRoleFound(List<AgreementRole> agreementRoles) {
		this.setAgreementRoles(agreementRoles);
	}

	public List<AgreementRole> getAgreementRoles()	{
		return agreementRoles;
	}

	public void setAgreementRoles(List<AgreementRole> agreementRoles)	{
		this.agreementRoles = agreementRoles;
	}
}
