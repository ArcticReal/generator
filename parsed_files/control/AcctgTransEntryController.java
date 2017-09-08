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
import com.skytala.eCommerce.command.AddAcctgTransEntry;
import com.skytala.eCommerce.command.DeleteAcctgTransEntry;
import com.skytala.eCommerce.command.UpdateAcctgTransEntry;
import com.skytala.eCommerce.entity.AcctgTransEntry;
import com.skytala.eCommerce.entity.AcctgTransEntryMapper;
import com.skytala.eCommerce.event.AcctgTransEntryAdded;
import com.skytala.eCommerce.event.AcctgTransEntryDeleted;
import com.skytala.eCommerce.event.AcctgTransEntryFound;
import com.skytala.eCommerce.event.AcctgTransEntryUpdated;
import com.skytala.eCommerce.query.FindAcctgTransEntrysBy;

@RestController
@RequestMapping("/api/acctgTransEntry")
public class AcctgTransEntryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AcctgTransEntry>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AcctgTransEntryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AcctgTransEntry
	 * @return a List with the AcctgTransEntrys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AcctgTransEntry> findAcctgTransEntrysBy(@RequestParam Map<String, String> allRequestParams) {

		FindAcctgTransEntrysBy query = new FindAcctgTransEntrysBy(allRequestParams);

		int usedTicketId;

		synchronized (AcctgTransEntryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransEntryFound.class,
				event -> sendAcctgTransEntrysFoundMessage(((AcctgTransEntryFound) event).getAcctgTransEntrys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAcctgTransEntrysFoundMessage(List<AcctgTransEntry> acctgTransEntrys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, acctgTransEntrys);
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
	public boolean createAcctgTransEntry(HttpServletRequest request) {

		AcctgTransEntry acctgTransEntryToBeAdded = new AcctgTransEntry();
		try {
			acctgTransEntryToBeAdded = AcctgTransEntryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAcctgTransEntry(acctgTransEntryToBeAdded);

	}

	/**
	 * creates a new AcctgTransEntry entry in the ofbiz database
	 * 
	 * @param acctgTransEntryToBeAdded
	 *            the AcctgTransEntry thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAcctgTransEntry(AcctgTransEntry acctgTransEntryToBeAdded) {

		AddAcctgTransEntry com = new AddAcctgTransEntry(acctgTransEntryToBeAdded);
		int usedTicketId;

		synchronized (AcctgTransEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransEntryAdded.class,
				event -> sendAcctgTransEntryChangedMessage(((AcctgTransEntryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAcctgTransEntry(HttpServletRequest request) {

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

		AcctgTransEntry acctgTransEntryToBeUpdated = new AcctgTransEntry();

		try {
			acctgTransEntryToBeUpdated = AcctgTransEntryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAcctgTransEntry(acctgTransEntryToBeUpdated);

	}

	/**
	 * Updates the AcctgTransEntry with the specific Id
	 * 
	 * @param acctgTransEntryToBeUpdated the AcctgTransEntry thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAcctgTransEntry(AcctgTransEntry acctgTransEntryToBeUpdated) {

		UpdateAcctgTransEntry com = new UpdateAcctgTransEntry(acctgTransEntryToBeUpdated);

		int usedTicketId;

		synchronized (AcctgTransEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransEntryUpdated.class,
				event -> sendAcctgTransEntryChangedMessage(((AcctgTransEntryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AcctgTransEntry from the database
	 * 
	 * @param acctgTransEntryId:
	 *            the id of the AcctgTransEntry thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteacctgTransEntryById(@RequestParam(value = "acctgTransEntryId") String acctgTransEntryId) {

		DeleteAcctgTransEntry com = new DeleteAcctgTransEntry(acctgTransEntryId);

		int usedTicketId;

		synchronized (AcctgTransEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransEntryDeleted.class,
				event -> sendAcctgTransEntryChangedMessage(((AcctgTransEntryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAcctgTransEntryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/acctgTransEntry/\" plus one of the following: "
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
