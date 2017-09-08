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
import com.skytala.eCommerce.command.AddFinAccountType;
import com.skytala.eCommerce.command.DeleteFinAccountType;
import com.skytala.eCommerce.command.UpdateFinAccountType;
import com.skytala.eCommerce.entity.FinAccountType;
import com.skytala.eCommerce.entity.FinAccountTypeMapper;
import com.skytala.eCommerce.event.FinAccountTypeAdded;
import com.skytala.eCommerce.event.FinAccountTypeDeleted;
import com.skytala.eCommerce.event.FinAccountTypeFound;
import com.skytala.eCommerce.event.FinAccountTypeUpdated;
import com.skytala.eCommerce.query.FindFinAccountTypesBy;

@RestController
@RequestMapping("/api/finAccountType")
public class FinAccountTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountType
	 * @return a List with the FinAccountTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountType> findFinAccountTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountTypesBy query = new FindFinAccountTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeFound.class,
				event -> sendFinAccountTypesFoundMessage(((FinAccountTypeFound) event).getFinAccountTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountTypesFoundMessage(List<FinAccountType> finAccountTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountTypes);
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
	public boolean createFinAccountType(HttpServletRequest request) {

		FinAccountType finAccountTypeToBeAdded = new FinAccountType();
		try {
			finAccountTypeToBeAdded = FinAccountTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountType(finAccountTypeToBeAdded);

	}

	/**
	 * creates a new FinAccountType entry in the ofbiz database
	 * 
	 * @param finAccountTypeToBeAdded
	 *            the FinAccountType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountType(FinAccountType finAccountTypeToBeAdded) {

		AddFinAccountType com = new AddFinAccountType(finAccountTypeToBeAdded);
		int usedTicketId;

		synchronized (FinAccountTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeAdded.class,
				event -> sendFinAccountTypeChangedMessage(((FinAccountTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountType(HttpServletRequest request) {

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

		FinAccountType finAccountTypeToBeUpdated = new FinAccountType();

		try {
			finAccountTypeToBeUpdated = FinAccountTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountType(finAccountTypeToBeUpdated);

	}

	/**
	 * Updates the FinAccountType with the specific Id
	 * 
	 * @param finAccountTypeToBeUpdated the FinAccountType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountType(FinAccountType finAccountTypeToBeUpdated) {

		UpdateFinAccountType com = new UpdateFinAccountType(finAccountTypeToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeUpdated.class,
				event -> sendFinAccountTypeChangedMessage(((FinAccountTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountType from the database
	 * 
	 * @param finAccountTypeId:
	 *            the id of the FinAccountType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountTypeById(@RequestParam(value = "finAccountTypeId") String finAccountTypeId) {

		DeleteFinAccountType com = new DeleteFinAccountType(finAccountTypeId);

		int usedTicketId;

		synchronized (FinAccountTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeDeleted.class,
				event -> sendFinAccountTypeChangedMessage(((FinAccountTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountType/\" plus one of the following: "
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
