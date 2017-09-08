package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetAttributeFound implements Event{

	private List<FixedAssetAttribute> fixedAssetAttributes;

	public FixedAssetAttributeFound(List<FixedAssetAttribute> fixedAssetAttributes) {
		this.setFixedAssetAttributes(fixedAssetAttributes);
	}

	public List<FixedAssetAttribute> getFixedAssetAttributes()	{
		return fixedAssetAttributes;
	}

	public void setFixedAssetAttributes(List<FixedAssetAttribute> fixedAssetAttributes)	{
		this.fixedAssetAttributes = fixedAssetAttributes;
	}
}
