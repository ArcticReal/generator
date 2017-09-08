package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetFound implements Event{

	private List<FixedAsset> fixedAssets;

	public FixedAssetFound(List<FixedAsset> fixedAssets) {
		this.setFixedAssets(fixedAssets);
	}

	public List<FixedAsset> getFixedAssets()	{
		return fixedAssets;
	}

	public void setFixedAssets(List<FixedAsset> fixedAssets)	{
		this.fixedAssets = fixedAssets;
	}
}
