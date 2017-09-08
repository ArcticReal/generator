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
import com.skytala.eCommerce.command.AddAcctgTransType;
import com.skytala.eCommerce.command.DeleteAcctgTransType;
import com.skytala.eCommerce.command.UpdateAcctgTransType;
import com.skytala.eCommerce.entity.AcctgTransType;
import com.skytala.eCommerce.entity.AcctgTransTypeMapper;
import com.skytala.eCommerce.event.AcctgTransTypeAdded;
import com.skytala.eCommerce.event.AcctgTransTypeDeleted;
import com.skytala.eCommerce.event.AcctgTransTypeFound;
import com.skytala.eCommerce.event.AcctgTransTypeUpdated;
import com.skytala.eCommerce.query.FindAcctgTransTypesBy;

@RestController
@RequestMapping("/api/acctgTransType")
public class AcctgTransTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AcctgTransType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AcctgTransTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AcctgTransType
	 * @return a List with the AcctgTransTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AcctgTransType> findAcctgTransTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAcctgTransTypesBy query = new FindAcctgTransTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (AcctgTransTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransTypeFound.class,
				event -> sendAcctgTransTypesFoundMessage(((AcctgTransTypeFound) event).getAcctgTransTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAcctgTransTypesFoundMessage(List<AcctgTransType> acctgTransTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, acctgTransTypes);
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
	public boolean createAcctgTransType(HttpServletRequest request) {

		AcctgTransType acctgTransTypeToBeAdded = new AcctgTransType();
		try {
			acctgTransTypeToBeAdded = AcctgTransTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAcctgTransType(acctgTransTypeToBeAdded);

	}

	/**
	 * creates a new AcctgTransType entry in the ofbiz database
	 * 
	 * @param acctgTransTypeToBeAdded
	 *            the AcctgTransType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAcctgTransType(AcctgTransType acctgTransTypeToBeAdded) {

		AddAcctgTransType com = new AddAcctgTransType(acctgTransTypeToBeAdded);
		int usedTicketId;

		synchronized (AcctgTransTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransTypeAdded.class,
				event -> sendAcctgTransTypeChangedMessage(((AcctgTransTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAcctgTransType(HttpServletRequest request) {

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

		AcctgTransType acctgTransTypeToBeUpdated = new AcctgTransType();

		try {
			acctgTransTypeToBeUpdated = AcctgTransTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAcctgTransType(acctgTransTypeToBeUpdated);

	}

	/**
	 * Updates the AcctgTransType with the specific Id
	 * 
	 * @param acctgTransTypeToBeUpdated the AcctgTransType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAcctgTransType(AcctgTransType acctgTransTypeToBeUpdated) {

		UpdateAcctgTransType com = new UpdateAcctgTransType(acctgTransTypeToBeUpdated);

		int usedTicketId;

		synchronized (AcctgTransTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransTypeUpdated.class,
				event -> sendAcctgTransTypeChangedMessage(((AcctgTransTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AcctgTransType from the database
	 * 
	 * @param acctgTransTypeId:
	 *            the id of the AcctgTransType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteacctgTransTypeById(@RequestParam(value = "acctgTransTypeId") String acctgTransTypeId) {

		DeleteAcctgTransType com = new DeleteAcctgTransType(acctgTransTypeId);

		int usedTicketId;

		synchronized (AcctgTransTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransTypeDeleted.class,
				event -> sendAcctgTransTypeChangedMessage(((AcctgTransTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAcctgTransTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/acctgTransType/\" plus one of the following: "
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
