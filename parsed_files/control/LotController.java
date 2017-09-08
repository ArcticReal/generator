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
import com.skytala.eCommerce.command.AddLot;
import com.skytala.eCommerce.command.DeleteLot;
import com.skytala.eCommerce.command.UpdateLot;
import com.skytala.eCommerce.entity.Lot;
import com.skytala.eCommerce.entity.LotMapper;
import com.skytala.eCommerce.event.LotAdded;
import com.skytala.eCommerce.event.LotDeleted;
import com.skytala.eCommerce.event.LotFound;
import com.skytala.eCommerce.event.LotUpdated;
import com.skytala.eCommerce.query.FindLotsBy;

@RestController
@RequestMapping("/api/lot")
public class LotController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Lot>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public LotController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Lot
	 * @return a List with the Lots
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Lot> findLotsBy(@RequestParam Map<String, String> allRequestParams) {

		FindLotsBy query = new FindLotsBy(allRequestParams);

		int usedTicketId;

		synchronized (LotController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(LotFound.class,
				event -> sendLotsFoundMessage(((LotFound) event).getLots(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendLotsFoundMessage(List<Lot> lots, int usedTicketId) {
		queryReturnVal.put(usedTicketId, lots);
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
	public boolean createLot(HttpServletRequest request) {

		Lot lotToBeAdded = new Lot();
		try {
			lotToBeAdded = LotMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createLot(lotToBeAdded);

	}

	/**
	 * creates a new Lot entry in the ofbiz database
	 * 
	 * @param lotToBeAdded
	 *            the Lot thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createLot(Lot lotToBeAdded) {

		AddLot com = new AddLot(lotToBeAdded);
		int usedTicketId;

		synchronized (LotController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(LotAdded.class,
				event -> sendLotChangedMessage(((LotAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateLot(HttpServletRequest request) {

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

		Lot lotToBeUpdated = new Lot();

		try {
			lotToBeUpdated = LotMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateLot(lotToBeUpdated);

	}

	/**
	 * Updates the Lot with the specific Id
	 * 
	 * @param lotToBeUpdated the Lot thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateLot(Lot lotToBeUpdated) {

		UpdateLot com = new UpdateLot(lotToBeUpdated);

		int usedTicketId;

		synchronized (LotController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(LotUpdated.class,
				event -> sendLotChangedMessage(((LotUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Lot from the database
	 * 
	 * @param lotId:
	 *            the id of the Lot thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletelotById(@RequestParam(value = "lotId") String lotId) {

		DeleteLot com = new DeleteLot(lotId);

		int usedTicketId;

		synchronized (LotController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(LotDeleted.class,
				event -> sendLotChangedMessage(((LotDeleted) event).isSuccess(), usedTicketId));

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

	public void sendLotChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/lot/\" plus one of the following: "
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
