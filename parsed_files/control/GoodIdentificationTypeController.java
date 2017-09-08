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
import com.skytala.eCommerce.command.AddGoodIdentificationType;
import com.skytala.eCommerce.command.DeleteGoodIdentificationType;
import com.skytala.eCommerce.command.UpdateGoodIdentificationType;
import com.skytala.eCommerce.entity.GoodIdentificationType;
import com.skytala.eCommerce.entity.GoodIdentificationTypeMapper;
import com.skytala.eCommerce.event.GoodIdentificationTypeAdded;
import com.skytala.eCommerce.event.GoodIdentificationTypeDeleted;
import com.skytala.eCommerce.event.GoodIdentificationTypeFound;
import com.skytala.eCommerce.event.GoodIdentificationTypeUpdated;
import com.skytala.eCommerce.query.FindGoodIdentificationTypesBy;

@RestController
@RequestMapping("/api/goodIdentificationType")
public class GoodIdentificationTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GoodIdentificationType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GoodIdentificationTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GoodIdentificationType
	 * @return a List with the GoodIdentificationTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GoodIdentificationType> findGoodIdentificationTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindGoodIdentificationTypesBy query = new FindGoodIdentificationTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (GoodIdentificationTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GoodIdentificationTypeFound.class,
				event -> sendGoodIdentificationTypesFoundMessage(((GoodIdentificationTypeFound) event).getGoodIdentificationTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGoodIdentificationTypesFoundMessage(List<GoodIdentificationType> goodIdentificationTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, goodIdentificationTypes);
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
	public boolean createGoodIdentificationType(HttpServletRequest request) {

		GoodIdentificationType goodIdentificationTypeToBeAdded = new GoodIdentificationType();
		try {
			goodIdentificationTypeToBeAdded = GoodIdentificationTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGoodIdentificationType(goodIdentificationTypeToBeAdded);

	}

	/**
	 * creates a new GoodIdentificationType entry in the ofbiz database
	 * 
	 * @param goodIdentificationTypeToBeAdded
	 *            the GoodIdentificationType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGoodIdentificationType(GoodIdentificationType goodIdentificationTypeToBeAdded) {

		AddGoodIdentificationType com = new AddGoodIdentificationType(goodIdentificationTypeToBeAdded);
		int usedTicketId;

		synchronized (GoodIdentificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GoodIdentificationTypeAdded.class,
				event -> sendGoodIdentificationTypeChangedMessage(((GoodIdentificationTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGoodIdentificationType(HttpServletRequest request) {

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

		GoodIdentificationType goodIdentificationTypeToBeUpdated = new GoodIdentificationType();

		try {
			goodIdentificationTypeToBeUpdated = GoodIdentificationTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGoodIdentificationType(goodIdentificationTypeToBeUpdated);

	}

	/**
	 * Updates the GoodIdentificationType with the specific Id
	 * 
	 * @param goodIdentificationTypeToBeUpdated the GoodIdentificationType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGoodIdentificationType(GoodIdentificationType goodIdentificationTypeToBeUpdated) {

		UpdateGoodIdentificationType com = new UpdateGoodIdentificationType(goodIdentificationTypeToBeUpdated);

		int usedTicketId;

		synchronized (GoodIdentificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GoodIdentificationTypeUpdated.class,
				event -> sendGoodIdentificationTypeChangedMessage(((GoodIdentificationTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GoodIdentificationType from the database
	 * 
	 * @param goodIdentificationTypeId:
	 *            the id of the GoodIdentificationType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletegoodIdentificationTypeById(@RequestParam(value = "goodIdentificationTypeId") String goodIdentificationTypeId) {

		DeleteGoodIdentificationType com = new DeleteGoodIdentificationType(goodIdentificationTypeId);

		int usedTicketId;

		synchronized (GoodIdentificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GoodIdentificationTypeDeleted.class,
				event -> sendGoodIdentificationTypeChangedMessage(((GoodIdentificationTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGoodIdentificationTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/goodIdentificationType/\" plus one of the following: "
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
