package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ElectronicTextFound implements Event{

	private List<ElectronicText> electronicTexts;

	public ElectronicTextFound(List<ElectronicText> electronicTexts) {
		this.setElectronicTexts(electronicTexts);
	}

	public List<ElectronicText> getElectronicTexts()	{
		return electronicTexts;
	}

	public void setElectronicTexts(List<ElectronicText> electronicTexts)	{
		this.electronicTexts = electronicTexts;
	}
}
