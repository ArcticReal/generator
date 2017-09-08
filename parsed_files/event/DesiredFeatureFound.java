package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DesiredFeatureFound implements Event{

	private List<DesiredFeature> desiredFeatures;

	public DesiredFeatureFound(List<DesiredFeature> desiredFeatures) {
		this.setDesiredFeatures(desiredFeatures);
	}

	public List<DesiredFeature> getDesiredFeatures()	{
		return desiredFeatures;
	}

	public void setDesiredFeatures(List<DesiredFeature> desiredFeatures)	{
		this.desiredFeatures = desiredFeatures;
	}
}
