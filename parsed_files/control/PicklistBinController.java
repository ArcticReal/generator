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
import com.skytala.eCommerce.command.AddPicklistBin;
import com.skytala.eCommerce.command.DeletePicklistBin;
import com.skytala.eCommerce.command.UpdatePicklistBin;
import com.skytala.eCommerce.entity.PicklistBin;
import com.skytala.eCommerce.entity.PicklistBinMapper;
import com.skytala.eCommerce.event.PicklistBinAdded;
import com.skytala.eCommerce.event.PicklistBinDeleted;
import com.skytala.eCommerce.event.PicklistBinFound;
import com.skytala.eCommerce.event.PicklistBinUpdated;
import com.skytala.eCommerce.query.FindPicklistBinsBy;

@RestController
@RequestMapping("/api/picklistBin")
public class PicklistBinController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PicklistBin>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PicklistBinController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PicklistBin
	 * @return a List with the PicklistBins
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PicklistBin> findPicklistBinsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPicklistBinsBy query = new FindPicklistBinsBy(allRequestParams);

		int usedTicketId;

		synchronized (PicklistBinController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistBinFound.class,
				event -> sendPicklistBinsFoundMessage(((PicklistBinFound) event).getPicklistBins(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPicklistBinsFoundMessage(List<PicklistBin> picklistBins, int usedTicketId) {
		queryReturnVal.put(usedTicketId, picklistBins);
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
	public boolean createPicklistBin(HttpServletRequest request) {

		PicklistBin picklistBinToBeAdded = new PicklistBin();
		try {
			picklistBinToBeAdded = PicklistBinMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPicklistBin(picklistBinToBeAdded);

	}

	/**
	 * creates a new PicklistBin entry in the ofbiz database
	 * 
	 * @param picklistBinToBeAdded
	 *            the PicklistBin thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPicklistBin(PicklistBin picklistBinToBeAdded) {

		AddPicklistBin com = new AddPicklistBin(picklistBinToBeAdded);
		int usedTicketId;

		synchronized (PicklistBinController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistBinAdded.class,
				event -> sendPicklistBinChangedMessage(((PicklistBinAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePicklistBin(HttpServletRequest request) {

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

		PicklistBin picklistBinToBeUpdated = new PicklistBin();

		try {
			picklistBinToBeUpdated = PicklistBinMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePicklistBin(picklistBinToBeUpdated);

	}

	/**
	 * Updates the PicklistBin with the specific Id
	 * 
	 * @param picklistBinToBeUpdated the PicklistBin thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePicklistBin(PicklistBin picklistBinToBeUpdated) {

		UpdatePicklistBin com = new UpdatePicklistBin(picklistBinToBeUpdated);

		int usedTicketId;

		synchronized (PicklistBinController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistBinUpdated.class,
				event -> sendPicklistBinChangedMessage(((PicklistBinUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PicklistBin from the database
	 * 
	 * @param picklistBinId:
	 *            the id of the PicklistBin thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepicklistBinById(@RequestParam(value = "picklistBinId") String picklistBinId) {

		DeletePicklistBin com = new DeletePicklistBin(picklistBinId);

		int usedTicketId;

		synchronized (PicklistBinController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistBinDeleted.class,
				event -> sendPicklistBinChangedMessage(((PicklistBinDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPicklistBinChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/picklistBin/\" plus one of the following: "
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
