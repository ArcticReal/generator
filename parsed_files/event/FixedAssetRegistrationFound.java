package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetRegistrationFound implements Event{

	private List<FixedAssetRegistration> fixedAssetRegistrations;

	public FixedAssetRegistrationFound(List<FixedAssetRegistration> fixedAssetRegistrations) {
		this.setFixedAssetRegistrations(fixedAssetRegistrations);
	}

	public List<FixedAssetRegistration> getFixedAssetRegistrations()	{
		return fixedAssetRegistrations;
	}

	public void setFixedAssetRegistrations(List<FixedAssetRegistration> fixedAssetRegistrations)	{
		this.fixedAssetRegistrations = fixedAssetRegistrations;
	}
}
