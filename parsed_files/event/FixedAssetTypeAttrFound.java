package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetTypeAttrFound implements Event{

	private List<FixedAssetTypeAttr> fixedAssetTypeAttrs;

	public FixedAssetTypeAttrFound(List<FixedAssetTypeAttr> fixedAssetTypeAttrs) {
		this.setFixedAssetTypeAttrs(fixedAssetTypeAttrs);
	}

	public List<FixedAssetTypeAttr> getFixedAssetTypeAttrs()	{
		return fixedAssetTypeAttrs;
	}

	public void setFixedAssetTypeAttrs(List<FixedAssetTypeAttr> fixedAssetTypeAttrs)	{
		this.fixedAssetTypeAttrs = fixedAssetTypeAttrs;
	}
}
