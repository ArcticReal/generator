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
import com.skytala.eCommerce.command.AddEmplPositionTypeRate;
import com.skytala.eCommerce.command.DeleteEmplPositionTypeRate;
import com.skytala.eCommerce.command.UpdateEmplPositionTypeRate;
import com.skytala.eCommerce.entity.EmplPositionTypeRate;
import com.skytala.eCommerce.entity.EmplPositionTypeRateMapper;
import com.skytala.eCommerce.event.EmplPositionTypeRateAdded;
import com.skytala.eCommerce.event.EmplPositionTypeRateDeleted;
import com.skytala.eCommerce.event.EmplPositionTypeRateFound;
import com.skytala.eCommerce.event.EmplPositionTypeRateUpdated;
import com.skytala.eCommerce.query.FindEmplPositionTypeRatesBy;

@RestController
@RequestMapping("/api/emplPositionTypeRate")
public class EmplPositionTypeRateController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplPositionTypeRate>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplPositionTypeRateController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplPositionTypeRate
	 * @return a List with the EmplPositionTypeRates
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplPositionTypeRate> findEmplPositionTypeRatesBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplPositionTypeRatesBy query = new FindEmplPositionTypeRatesBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplPositionTypeRateController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeRateFound.class,
				event -> sendEmplPositionTypeRatesFoundMessage(((EmplPositionTypeRateFound) event).getEmplPositionTypeRates(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplPositionTypeRatesFoundMessage(List<EmplPositionTypeRate> emplPositionTypeRates, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplPositionTypeRates);
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
	public boolean createEmplPositionTypeRate(HttpServletRequest request) {

		EmplPositionTypeRate emplPositionTypeRateToBeAdded = new EmplPositionTypeRate();
		try {
			emplPositionTypeRateToBeAdded = EmplPositionTypeRateMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplPositionTypeRate(emplPositionTypeRateToBeAdded);

	}

	/**
	 * creates a new EmplPositionTypeRate entry in the ofbiz database
	 * 
	 * @param emplPositionTypeRateToBeAdded
	 *            the EmplPositionTypeRate thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplPositionTypeRate(EmplPositionTypeRate emplPositionTypeRateToBeAdded) {

		AddEmplPositionTypeRate com = new AddEmplPositionTypeRate(emplPositionTypeRateToBeAdded);
		int usedTicketId;

		synchronized (EmplPositionTypeRateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeRateAdded.class,
				event -> sendEmplPositionTypeRateChangedMessage(((EmplPositionTypeRateAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplPositionTypeRate(HttpServletRequest request) {

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

		EmplPositionTypeRate emplPositionTypeRateToBeUpdated = new EmplPositionTypeRate();

		try {
			emplPositionTypeRateToBeUpdated = EmplPositionTypeRateMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplPositionTypeRate(emplPositionTypeRateToBeUpdated);

	}

	/**
	 * Updates the EmplPositionTypeRate with the specific Id
	 * 
	 * @param emplPositionTypeRateToBeUpdated the EmplPositionTypeRate thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplPositionTypeRate(EmplPositionTypeRate emplPositionTypeRateToBeUpdated) {

		UpdateEmplPositionTypeRate com = new UpdateEmplPositionTypeRate(emplPositionTypeRateToBeUpdated);

		int usedTicketId;

		synchronized (EmplPositionTypeRateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeRateUpdated.class,
				event -> sendEmplPositionTypeRateChangedMessage(((EmplPositionTypeRateUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplPositionTypeRate from the database
	 * 
	 * @param emplPositionTypeRateId:
	 *            the id of the EmplPositionTypeRate thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplPositionTypeRateById(@RequestParam(value = "emplPositionTypeRateId") String emplPositionTypeRateId) {

		DeleteEmplPositionTypeRate com = new DeleteEmplPositionTypeRate(emplPositionTypeRateId);

		int usedTicketId;

		synchronized (EmplPositionTypeRateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeRateDeleted.class,
				event -> sendEmplPositionTypeRateChangedMessage(((EmplPositionTypeRateDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplPositionTypeRateChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplPositionTypeRate/\" plus one of the following: "
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
