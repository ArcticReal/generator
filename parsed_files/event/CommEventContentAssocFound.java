package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommEventContentAssocFound implements Event{

	private List<CommEventContentAssoc> commEventContentAssocs;

	public CommEventContentAssocFound(List<CommEventContentAssoc> commEventContentAssocs) {
		this.setCommEventContentAssocs(commEventContentAssocs);
	}

	public List<CommEventContentAssoc> getCommEventContentAssocs()	{
		return commEventContentAssocs;
	}

	public void setCommEventContentAssocs(List<CommEventContentAssoc> commEventContentAssocs)	{
		this.commEventContentAssocs = commEventContentAssocs;
	}
}
