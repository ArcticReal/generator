package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetTypeGlAccountFound implements Event{

	private List<FixedAssetTypeGlAccount> fixedAssetTypeGlAccounts;

	public FixedAssetTypeGlAccountFound(List<FixedAssetTypeGlAccount> fixedAssetTypeGlAccounts) {
		this.setFixedAssetTypeGlAccounts(fixedAssetTypeGlAccounts);
	}

	public List<FixedAssetTypeGlAccount> getFixedAssetTypeGlAccounts()	{
		return fixedAssetTypeGlAccounts;
	}

	public void setFixedAssetTypeGlAccounts(List<FixedAssetTypeGlAccount> fixedAssetTypeGlAccounts)	{
		this.fixedAssetTypeGlAccounts = fixedAssetTypeGlAccounts;
	}
}
