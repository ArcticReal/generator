package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddCommEventContentAssoc;
import com.skytala.eCommerce.command.DeleteCommEventContentAssoc;
import com.skytala.eCommerce.command.UpdateCommEventContentAssoc;
import com.skytala.eCommerce.entity.CommEventContentAssoc;
import com.skytala.eCommerce.entity.CommEventContentAssocMapper;
import com.skytala.eCommerce.event.CommEventContentAssocAdded;
import com.skytala.eCommerce.event.CommEventContentAssocDeleted;
import com.skytala.eCommerce.event.CommEventContentAssocFound;
import com.skytala.eCommerce.event.CommEventContentAssocUpdated;
import com.skytala.eCommerce.query.FindCommEventContentAssocsBy;

@RestController
@RequestMapping("/api/commEventContentAssoc")
public class CommEventContentAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommEventContentAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommEventContentAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommEventContentAssoc
	 * @return a List with the CommEventContentAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommEventContentAssoc> findCommEventContentAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommEventContentAssocsBy query = new FindCommEventContentAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (CommEventContentAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommEventContentAssocFound.class,
				event -> sendCommEventContentAssocsFoundMessage(((CommEventContentAssocFound) event).getCommEventContentAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommEventContentAssocsFoundMessage(List<CommEventContentAssoc> commEventContentAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, commEventContentAssocs);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createCommEventContentAssoc(HttpServletRequest request) {

		CommEventContentAssoc commEventContentAssocToBeAdded = new CommEventContentAssoc();
		try {
			commEventContentAssocToBeAdded = CommEventContentAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommEventContentAssoc(commEventContentAssocToBeAdded);

	}

	/**
	 * creates a new CommEventContentAssoc entry in the ofbiz database
	 * 
	 * @param commEventContentAssocToBeAdded
	 *            the CommEventContentAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommEventContentAssoc(CommEventContentAssoc commEventContentAssocToBeAdded) {

		AddCommEventContentAssoc com = new AddCommEventContentAssoc(commEventContentAssocToBeAdded);
		int usedTicketId;

		synchronized (CommEventContentAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommEventContentAssocAdded.class,
				event -> sendCommEventContentAssocChangedMessage(((CommEventContentAssocAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateCommEventContentAssoc(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		CommEventContentAssoc commEventContentAssocToBeUpdated = new CommEventContentAssoc();

		try {
			commEventContentAssocToBeUpdated = CommEventContentAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommEventContentAssoc(commEventContentAssocToBeUpdated);

	}

	/**
	 * Updates the CommEventContentAssoc with the specific Id
	 * 
	 * @param commEventContentAssocToBeUpdated the CommEventContentAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommEventContentAssoc(CommEventContentAssoc commEventContentAssocToBeUpdated) {

		UpdateCommEventContentAssoc com = new UpdateCommEventContentAssoc(commEventContentAssocToBeUpdated);

		int usedTicketId;

		synchronized (CommEventContentAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommEventContentAssocUpdated.class,
				event -> sendCommEventContentAssocChangedMessage(((CommEventContentAssocUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a CommEventContentAssoc from the database
	 * 
	 * @param commEventContentAssocId:
	 *            the id of the CommEventContentAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommEventContentAssocById(@RequestParam(value = "commEventContentAssocId") String commEventContentAssocId) {

		DeleteCommEventContentAssoc com = new DeleteCommEventContentAssoc(commEventContentAssocId);

		int usedTicketId;

		synchronized (CommEventContentAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommEventContentAssocDeleted.class,
				event -> sendCommEventContentAssocChangedMessage(((CommEventContentAssocDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendCommEventContentAssocChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/commEventContentAssoc/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}
