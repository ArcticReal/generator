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
import com.skytala.eCommerce.command.AddDeductionType;
import com.skytala.eCommerce.command.DeleteDeductionType;
import com.skytala.eCommerce.command.UpdateDeductionType;
import com.skytala.eCommerce.entity.DeductionType;
import com.skytala.eCommerce.entity.DeductionTypeMapper;
import com.skytala.eCommerce.event.DeductionTypeAdded;
import com.skytala.eCommerce.event.DeductionTypeDeleted;
import com.skytala.eCommerce.event.DeductionTypeFound;
import com.skytala.eCommerce.event.DeductionTypeUpdated;
import com.skytala.eCommerce.query.FindDeductionTypesBy;

@RestController
@RequestMapping("/api/deductionType")
public class DeductionTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DeductionType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DeductionTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DeductionType
	 * @return a List with the DeductionTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DeductionType> findDeductionTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDeductionTypesBy query = new FindDeductionTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (DeductionTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeductionTypeFound.class,
				event -> sendDeductionTypesFoundMessage(((DeductionTypeFound) event).getDeductionTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDeductionTypesFoundMessage(List<DeductionType> deductionTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, deductionTypes);
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
	public boolean createDeductionType(HttpServletRequest request) {

		DeductionType deductionTypeToBeAdded = new DeductionType();
		try {
			deductionTypeToBeAdded = DeductionTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDeductionType(deductionTypeToBeAdded);

	}

	/**
	 * creates a new DeductionType entry in the ofbiz database
	 * 
	 * @param deductionTypeToBeAdded
	 *            the DeductionType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDeductionType(DeductionType deductionTypeToBeAdded) {

		AddDeductionType com = new AddDeductionType(deductionTypeToBeAdded);
		int usedTicketId;

		synchronized (DeductionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeductionTypeAdded.class,
				event -> sendDeductionTypeChangedMessage(((DeductionTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDeductionType(HttpServletRequest request) {

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

		DeductionType deductionTypeToBeUpdated = new DeductionType();

		try {
			deductionTypeToBeUpdated = DeductionTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDeductionType(deductionTypeToBeUpdated);

	}

	/**
	 * Updates the DeductionType with the specific Id
	 * 
	 * @param deductionTypeToBeUpdated the DeductionType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDeductionType(DeductionType deductionTypeToBeUpdated) {

		UpdateDeductionType com = new UpdateDeductionType(deductionTypeToBeUpdated);

		int usedTicketId;

		synchronized (DeductionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeductionTypeUpdated.class,
				event -> sendDeductionTypeChangedMessage(((DeductionTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DeductionType from the database
	 * 
	 * @param deductionTypeId:
	 *            the id of the DeductionType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedeductionTypeById(@RequestParam(value = "deductionTypeId") String deductionTypeId) {

		DeleteDeductionType com = new DeleteDeductionType(deductionTypeId);

		int usedTicketId;

		synchronized (DeductionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeductionTypeDeleted.class,
				event -> sendDeductionTypeChangedMessage(((DeductionTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDeductionTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/deductionType/\" plus one of the following: "
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
