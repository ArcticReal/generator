package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetMaintFound implements Event{

	private List<FixedAssetMaint> fixedAssetMaints;

	public FixedAssetMaintFound(List<FixedAssetMaint> fixedAssetMaints) {
		this.setFixedAssetMaints(fixedAssetMaints);
	}

	public List<FixedAssetMaint> getFixedAssetMaints()	{
		return fixedAssetMaints;
	}

	public void setFixedAssetMaints(List<FixedAssetMaint> fixedAssetMaints)	{
		this.fixedAssetMaints = fixedAssetMaints;
	}
}
