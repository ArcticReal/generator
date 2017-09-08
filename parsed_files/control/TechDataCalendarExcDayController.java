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
import com.skytala.eCommerce.command.AddTechDataCalendarExcDay;
import com.skytala.eCommerce.command.DeleteTechDataCalendarExcDay;
import com.skytala.eCommerce.command.UpdateTechDataCalendarExcDay;
import com.skytala.eCommerce.entity.TechDataCalendarExcDay;
import com.skytala.eCommerce.entity.TechDataCalendarExcDayMapper;
import com.skytala.eCommerce.event.TechDataCalendarExcDayAdded;
import com.skytala.eCommerce.event.TechDataCalendarExcDayDeleted;
import com.skytala.eCommerce.event.TechDataCalendarExcDayFound;
import com.skytala.eCommerce.event.TechDataCalendarExcDayUpdated;
import com.skytala.eCommerce.query.FindTechDataCalendarExcDaysBy;

@RestController
@RequestMapping("/api/techDataCalendarExcDay")
public class TechDataCalendarExcDayController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TechDataCalendarExcDay>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TechDataCalendarExcDayController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TechDataCalendarExcDay
	 * @return a List with the TechDataCalendarExcDays
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TechDataCalendarExcDay> findTechDataCalendarExcDaysBy(@RequestParam Map<String, String> allRequestParams) {

		FindTechDataCalendarExcDaysBy query = new FindTechDataCalendarExcDaysBy(allRequestParams);

		int usedTicketId;

		synchronized (TechDataCalendarExcDayController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarExcDayFound.class,
				event -> sendTechDataCalendarExcDaysFoundMessage(((TechDataCalendarExcDayFound) event).getTechDataCalendarExcDays(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTechDataCalendarExcDaysFoundMessage(List<TechDataCalendarExcDay> techDataCalendarExcDays, int usedTicketId) {
		queryReturnVal.put(usedTicketId, techDataCalendarExcDays);
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
	public boolean createTechDataCalendarExcDay(HttpServletRequest request) {

		TechDataCalendarExcDay techDataCalendarExcDayToBeAdded = new TechDataCalendarExcDay();
		try {
			techDataCalendarExcDayToBeAdded = TechDataCalendarExcDayMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTechDataCalendarExcDay(techDataCalendarExcDayToBeAdded);

	}

	/**
	 * creates a new TechDataCalendarExcDay entry in the ofbiz database
	 * 
	 * @param techDataCalendarExcDayToBeAdded
	 *            the TechDataCalendarExcDay thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTechDataCalendarExcDay(TechDataCalendarExcDay techDataCalendarExcDayToBeAdded) {

		AddTechDataCalendarExcDay com = new AddTechDataCalendarExcDay(techDataCalendarExcDayToBeAdded);
		int usedTicketId;

		synchronized (TechDataCalendarExcDayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarExcDayAdded.class,
				event -> sendTechDataCalendarExcDayChangedMessage(((TechDataCalendarExcDayAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTechDataCalendarExcDay(HttpServletRequest request) {

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

		TechDataCalendarExcDay techDataCalendarExcDayToBeUpdated = new TechDataCalendarExcDay();

		try {
			techDataCalendarExcDayToBeUpdated = TechDataCalendarExcDayMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTechDataCalendarExcDay(techDataCalendarExcDayToBeUpdated);

	}

	/**
	 * Updates the TechDataCalendarExcDay with the specific Id
	 * 
	 * @param techDataCalendarExcDayToBeUpdated the TechDataCalendarExcDay thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTechDataCalendarExcDay(TechDataCalendarExcDay techDataCalendarExcDayToBeUpdated) {

		UpdateTechDataCalendarExcDay com = new UpdateTechDataCalendarExcDay(techDataCalendarExcDayToBeUpdated);

		int usedTicketId;

		synchronized (TechDataCalendarExcDayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarExcDayUpdated.class,
				event -> sendTechDataCalendarExcDayChangedMessage(((TechDataCalendarExcDayUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TechDataCalendarExcDay from the database
	 * 
	 * @param techDataCalendarExcDayId:
	 *            the id of the TechDataCalendarExcDay thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetechDataCalendarExcDayById(@RequestParam(value = "techDataCalendarExcDayId") String techDataCalendarExcDayId) {

		DeleteTechDataCalendarExcDay com = new DeleteTechDataCalendarExcDay(techDataCalendarExcDayId);

		int usedTicketId;

		synchronized (TechDataCalendarExcDayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarExcDayDeleted.class,
				event -> sendTechDataCalendarExcDayChangedMessage(((TechDataCalendarExcDayDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTechDataCalendarExcDayChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/techDataCalendarExcDay/\" plus one of the following: "
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
