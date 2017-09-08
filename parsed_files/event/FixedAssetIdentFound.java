package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetIdentFound implements Event{

	private List<FixedAssetIdent> fixedAssetIdents;

	public FixedAssetIdentFound(List<FixedAssetIdent> fixedAssetIdents) {
		this.setFixedAssetIdents(fixedAssetIdents);
	}

	public List<FixedAssetIdent> getFixedAssetIdents()	{
		return fixedAssetIdents;
	}

	public void setFixedAssetIdents(List<FixedAssetIdent> fixedAssetIdents)	{
		this.fixedAssetIdents = fixedAssetIdents;
	}
}
