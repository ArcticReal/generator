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
import com.skytala.eCommerce.command.AddAcctgTransEntryType;
import com.skytala.eCommerce.command.DeleteAcctgTransEntryType;
import com.skytala.eCommerce.command.UpdateAcctgTransEntryType;
import com.skytala.eCommerce.entity.AcctgTransEntryType;
import com.skytala.eCommerce.entity.AcctgTransEntryTypeMapper;
import com.skytala.eCommerce.event.AcctgTransEntryTypeAdded;
import com.skytala.eCommerce.event.AcctgTransEntryTypeDeleted;
import com.skytala.eCommerce.event.AcctgTransEntryTypeFound;
import com.skytala.eCommerce.event.AcctgTransEntryTypeUpdated;
import com.skytala.eCommerce.query.FindAcctgTransEntryTypesBy;

@RestController
@RequestMapping("/api/acctgTransEntryType")
public class AcctgTransEntryTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AcctgTransEntryType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AcctgTransEntryTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AcctgTransEntryType
	 * @return a List with the AcctgTransEntryTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AcctgTransEntryType> findAcctgTransEntryTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAcctgTransEntryTypesBy query = new FindAcctgTransEntryTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (AcctgTransEntryTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransEntryTypeFound.class,
				event -> sendAcctgTransEntryTypesFoundMessage(((AcctgTransEntryTypeFound) event).getAcctgTransEntryTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAcctgTransEntryTypesFoundMessage(List<AcctgTransEntryType> acctgTransEntryTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, acctgTransEntryTypes);
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
	public boolean createAcctgTransEntryType(HttpServletRequest request) {

		AcctgTransEntryType acctgTransEntryTypeToBeAdded = new AcctgTransEntryType();
		try {
			acctgTransEntryTypeToBeAdded = AcctgTransEntryTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAcctgTransEntryType(acctgTransEntryTypeToBeAdded);

	}

	/**
	 * creates a new AcctgTransEntryType entry in the ofbiz database
	 * 
	 * @param acctgTransEntryTypeToBeAdded
	 *            the AcctgTransEntryType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAcctgTransEntryType(AcctgTransEntryType acctgTransEntryTypeToBeAdded) {

		AddAcctgTransEntryType com = new AddAcctgTransEntryType(acctgTransEntryTypeToBeAdded);
		int usedTicketId;

		synchronized (AcctgTransEntryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransEntryTypeAdded.class,
				event -> sendAcctgTransEntryTypeChangedMessage(((AcctgTransEntryTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAcctgTransEntryType(HttpServletRequest request) {

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

		AcctgTransEntryType acctgTransEntryTypeToBeUpdated = new AcctgTransEntryType();

		try {
			acctgTransEntryTypeToBeUpdated = AcctgTransEntryTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAcctgTransEntryType(acctgTransEntryTypeToBeUpdated);

	}

	/**
	 * Updates the AcctgTransEntryType with the specific Id
	 * 
	 * @param acctgTransEntryTypeToBeUpdated the AcctgTransEntryType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAcctgTransEntryType(AcctgTransEntryType acctgTransEntryTypeToBeUpdated) {

		UpdateAcctgTransEntryType com = new UpdateAcctgTransEntryType(acctgTransEntryTypeToBeUpdated);

		int usedTicketId;

		synchronized (AcctgTransEntryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransEntryTypeUpdated.class,
				event -> sendAcctgTransEntryTypeChangedMessage(((AcctgTransEntryTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AcctgTransEntryType from the database
	 * 
	 * @param acctgTransEntryTypeId:
	 *            the id of the AcctgTransEntryType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteacctgTransEntryTypeById(@RequestParam(value = "acctgTransEntryTypeId") String acctgTransEntryTypeId) {

		DeleteAcctgTransEntryType com = new DeleteAcctgTransEntryType(acctgTransEntryTypeId);

		int usedTicketId;

		synchronized (AcctgTransEntryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransEntryTypeDeleted.class,
				event -> sendAcctgTransEntryTypeChangedMessage(((AcctgTransEntryTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAcctgTransEntryTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/acctgTransEntryType/\" plus one of the following: "
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
