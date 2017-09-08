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
import com.skytala.eCommerce.command.AddGoodIdentification;
import com.skytala.eCommerce.command.DeleteGoodIdentification;
import com.skytala.eCommerce.command.UpdateGoodIdentification;
import com.skytala.eCommerce.entity.GoodIdentification;
import com.skytala.eCommerce.entity.GoodIdentificationMapper;
import com.skytala.eCommerce.event.GoodIdentificationAdded;
import com.skytala.eCommerce.event.GoodIdentificationDeleted;
import com.skytala.eCommerce.event.GoodIdentificationFound;
import com.skytala.eCommerce.event.GoodIdentificationUpdated;
import com.skytala.eCommerce.query.FindGoodIdentificationsBy;

@RestController
@RequestMapping("/api/goodIdentification")
public class GoodIdentificationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GoodIdentification>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GoodIdentificationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GoodIdentification
	 * @return a List with the GoodIdentifications
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GoodIdentification> findGoodIdentificationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindGoodIdentificationsBy query = new FindGoodIdentificationsBy(allRequestParams);

		int usedTicketId;

		synchronized (GoodIdentificationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GoodIdentificationFound.class,
				event -> sendGoodIdentificationsFoundMessage(((GoodIdentificationFound) event).getGoodIdentifications(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGoodIdentificationsFoundMessage(List<GoodIdentification> goodIdentifications, int usedTicketId) {
		queryReturnVal.put(usedTicketId, goodIdentifications);
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
	public boolean createGoodIdentification(HttpServletRequest request) {

		GoodIdentification goodIdentificationToBeAdded = new GoodIdentification();
		try {
			goodIdentificationToBeAdded = GoodIdentificationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGoodIdentification(goodIdentificationToBeAdded);

	}

	/**
	 * creates a new GoodIdentification entry in the ofbiz database
	 * 
	 * @param goodIdentificationToBeAdded
	 *            the GoodIdentification thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGoodIdentification(GoodIdentification goodIdentificationToBeAdded) {

		AddGoodIdentification com = new AddGoodIdentification(goodIdentificationToBeAdded);
		int usedTicketId;

		synchronized (GoodIdentificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GoodIdentificationAdded.class,
				event -> sendGoodIdentificationChangedMessage(((GoodIdentificationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGoodIdentification(HttpServletRequest request) {

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

		GoodIdentification goodIdentificationToBeUpdated = new GoodIdentification();

		try {
			goodIdentificationToBeUpdated = GoodIdentificationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGoodIdentification(goodIdentificationToBeUpdated);

	}

	/**
	 * Updates the GoodIdentification with the specific Id
	 * 
	 * @param goodIdentificationToBeUpdated the GoodIdentification thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGoodIdentification(GoodIdentification goodIdentificationToBeUpdated) {

		UpdateGoodIdentification com = new UpdateGoodIdentification(goodIdentificationToBeUpdated);

		int usedTicketId;

		synchronized (GoodIdentificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GoodIdentificationUpdated.class,
				event -> sendGoodIdentificationChangedMessage(((GoodIdentificationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GoodIdentification from the database
	 * 
	 * @param goodIdentificationId:
	 *            the id of the GoodIdentification thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletegoodIdentificationById(@RequestParam(value = "goodIdentificationId") String goodIdentificationId) {

		DeleteGoodIdentification com = new DeleteGoodIdentification(goodIdentificationId);

		int usedTicketId;

		synchronized (GoodIdentificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GoodIdentificationDeleted.class,
				event -> sendGoodIdentificationChangedMessage(((GoodIdentificationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGoodIdentificationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/goodIdentification/\" plus one of the following: "
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
