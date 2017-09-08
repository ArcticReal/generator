package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetDepMethodFound implements Event{

	private List<FixedAssetDepMethod> fixedAssetDepMethods;

	public FixedAssetDepMethodFound(List<FixedAssetDepMethod> fixedAssetDepMethods) {
		this.setFixedAssetDepMethods(fixedAssetDepMethods);
	}

	public List<FixedAssetDepMethod> getFixedAssetDepMethods()	{
		return fixedAssetDepMethods;
	}

	public void setFixedAssetDepMethods(List<FixedAssetDepMethod> fixedAssetDepMethods)	{
		this.fixedAssetDepMethods = fixedAssetDepMethods;
	}
}
