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
import com.skytala.eCommerce.command.AddCustRequestType;
import com.skytala.eCommerce.command.DeleteCustRequestType;
import com.skytala.eCommerce.command.UpdateCustRequestType;
import com.skytala.eCommerce.entity.CustRequestType;
import com.skytala.eCommerce.entity.CustRequestTypeMapper;
import com.skytala.eCommerce.event.CustRequestTypeAdded;
import com.skytala.eCommerce.event.CustRequestTypeDeleted;
import com.skytala.eCommerce.event.CustRequestTypeFound;
import com.skytala.eCommerce.event.CustRequestTypeUpdated;
import com.skytala.eCommerce.query.FindCustRequestTypesBy;

@RestController
@RequestMapping("/api/custRequestType")
public class CustRequestTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestType
	 * @return a List with the CustRequestTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestType> findCustRequestTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestTypesBy query = new FindCustRequestTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestTypeFound.class,
				event -> sendCustRequestTypesFoundMessage(((CustRequestTypeFound) event).getCustRequestTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestTypesFoundMessage(List<CustRequestType> custRequestTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestTypes);
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
	public boolean createCustRequestType(HttpServletRequest request) {

		CustRequestType custRequestTypeToBeAdded = new CustRequestType();
		try {
			custRequestTypeToBeAdded = CustRequestTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestType(custRequestTypeToBeAdded);

	}

	/**
	 * creates a new CustRequestType entry in the ofbiz database
	 * 
	 * @param custRequestTypeToBeAdded
	 *            the CustRequestType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestType(CustRequestType custRequestTypeToBeAdded) {

		AddCustRequestType com = new AddCustRequestType(custRequestTypeToBeAdded);
		int usedTicketId;

		synchronized (CustRequestTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestTypeAdded.class,
				event -> sendCustRequestTypeChangedMessage(((CustRequestTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestType(HttpServletRequest request) {

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

		CustRequestType custRequestTypeToBeUpdated = new CustRequestType();

		try {
			custRequestTypeToBeUpdated = CustRequestTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestType(custRequestTypeToBeUpdated);

	}

	/**
	 * Updates the CustRequestType with the specific Id
	 * 
	 * @param custRequestTypeToBeUpdated the CustRequestType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestType(CustRequestType custRequestTypeToBeUpdated) {

		UpdateCustRequestType com = new UpdateCustRequestType(custRequestTypeToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestTypeUpdated.class,
				event -> sendCustRequestTypeChangedMessage(((CustRequestTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestType from the database
	 * 
	 * @param custRequestTypeId:
	 *            the id of the CustRequestType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestTypeById(@RequestParam(value = "custRequestTypeId") String custRequestTypeId) {

		DeleteCustRequestType com = new DeleteCustRequestType(custRequestTypeId);

		int usedTicketId;

		synchronized (CustRequestTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestTypeDeleted.class,
				event -> sendCustRequestTypeChangedMessage(((CustRequestTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestType/\" plus one of the following: "
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
