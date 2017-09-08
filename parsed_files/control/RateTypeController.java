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
import com.skytala.eCommerce.command.AddRateType;
import com.skytala.eCommerce.command.DeleteRateType;
import com.skytala.eCommerce.command.UpdateRateType;
import com.skytala.eCommerce.entity.RateType;
import com.skytala.eCommerce.entity.RateTypeMapper;
import com.skytala.eCommerce.event.RateTypeAdded;
import com.skytala.eCommerce.event.RateTypeDeleted;
import com.skytala.eCommerce.event.RateTypeFound;
import com.skytala.eCommerce.event.RateTypeUpdated;
import com.skytala.eCommerce.query.FindRateTypesBy;

@RestController
@RequestMapping("/api/rateType")
public class RateTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RateType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RateTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RateType
	 * @return a List with the RateTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RateType> findRateTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindRateTypesBy query = new FindRateTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (RateTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RateTypeFound.class,
				event -> sendRateTypesFoundMessage(((RateTypeFound) event).getRateTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRateTypesFoundMessage(List<RateType> rateTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, rateTypes);
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
	public boolean createRateType(HttpServletRequest request) {

		RateType rateTypeToBeAdded = new RateType();
		try {
			rateTypeToBeAdded = RateTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRateType(rateTypeToBeAdded);

	}

	/**
	 * creates a new RateType entry in the ofbiz database
	 * 
	 * @param rateTypeToBeAdded
	 *            the RateType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRateType(RateType rateTypeToBeAdded) {

		AddRateType com = new AddRateType(rateTypeToBeAdded);
		int usedTicketId;

		synchronized (RateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RateTypeAdded.class,
				event -> sendRateTypeChangedMessage(((RateTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRateType(HttpServletRequest request) {

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

		RateType rateTypeToBeUpdated = new RateType();

		try {
			rateTypeToBeUpdated = RateTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRateType(rateTypeToBeUpdated);

	}

	/**
	 * Updates the RateType with the specific Id
	 * 
	 * @param rateTypeToBeUpdated the RateType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRateType(RateType rateTypeToBeUpdated) {

		UpdateRateType com = new UpdateRateType(rateTypeToBeUpdated);

		int usedTicketId;

		synchronized (RateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RateTypeUpdated.class,
				event -> sendRateTypeChangedMessage(((RateTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RateType from the database
	 * 
	 * @param rateTypeId:
	 *            the id of the RateType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterateTypeById(@RequestParam(value = "rateTypeId") String rateTypeId) {

		DeleteRateType com = new DeleteRateType(rateTypeId);

		int usedTicketId;

		synchronized (RateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RateTypeDeleted.class,
				event -> sendRateTypeChangedMessage(((RateTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRateTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/rateType/\" plus one of the following: "
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
