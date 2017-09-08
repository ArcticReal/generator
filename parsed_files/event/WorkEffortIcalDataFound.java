package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortIcalDataFound implements Event{

	private List<WorkEffortIcalData> workEffortIcalDatas;

	public WorkEffortIcalDataFound(List<WorkEffortIcalData> workEffortIcalDatas) {
		this.setWorkEffortIcalDatas(workEffortIcalDatas);
	}

	public List<WorkEffortIcalData> getWorkEffortIcalDatas()	{
		return workEffortIcalDatas;
	}

	public void setWorkEffortIcalDatas(List<WorkEffortIcalData> workEffortIcalDatas)	{
		this.workEffortIcalDatas = workEffortIcalDatas;
	}
}
