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
import com.skytala.eCommerce.command.AddFixedAssetRegistration;
import com.skytala.eCommerce.command.DeleteFixedAssetRegistration;
import com.skytala.eCommerce.command.UpdateFixedAssetRegistration;
import com.skytala.eCommerce.entity.FixedAssetRegistration;
import com.skytala.eCommerce.entity.FixedAssetRegistrationMapper;
import com.skytala.eCommerce.event.FixedAssetRegistrationAdded;
import com.skytala.eCommerce.event.FixedAssetRegistrationDeleted;
import com.skytala.eCommerce.event.FixedAssetRegistrationFound;
import com.skytala.eCommerce.event.FixedAssetRegistrationUpdated;
import com.skytala.eCommerce.query.FindFixedAssetRegistrationsBy;

@RestController
@RequestMapping("/api/fixedAssetRegistration")
public class FixedAssetRegistrationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetRegistration>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetRegistrationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetRegistration
	 * @return a List with the FixedAssetRegistrations
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetRegistration> findFixedAssetRegistrationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetRegistrationsBy query = new FindFixedAssetRegistrationsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetRegistrationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetRegistrationFound.class,
				event -> sendFixedAssetRegistrationsFoundMessage(((FixedAssetRegistrationFound) event).getFixedAssetRegistrations(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetRegistrationsFoundMessage(List<FixedAssetRegistration> fixedAssetRegistrations, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetRegistrations);
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
	public boolean createFixedAssetRegistration(HttpServletRequest request) {

		FixedAssetRegistration fixedAssetRegistrationToBeAdded = new FixedAssetRegistration();
		try {
			fixedAssetRegistrationToBeAdded = FixedAssetRegistrationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetRegistration(fixedAssetRegistrationToBeAdded);

	}

	/**
	 * creates a new FixedAssetRegistration entry in the ofbiz database
	 * 
	 * @param fixedAssetRegistrationToBeAdded
	 *            the FixedAssetRegistration thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetRegistration(FixedAssetRegistration fixedAssetRegistrationToBeAdded) {

		AddFixedAssetRegistration com = new AddFixedAssetRegistration(fixedAssetRegistrationToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetRegistrationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetRegistrationAdded.class,
				event -> sendFixedAssetRegistrationChangedMessage(((FixedAssetRegistrationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetRegistration(HttpServletRequest request) {

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

		FixedAssetRegistration fixedAssetRegistrationToBeUpdated = new FixedAssetRegistration();

		try {
			fixedAssetRegistrationToBeUpdated = FixedAssetRegistrationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetRegistration(fixedAssetRegistrationToBeUpdated);

	}

	/**
	 * Updates the FixedAssetRegistration with the specific Id
	 * 
	 * @param fixedAssetRegistrationToBeUpdated the FixedAssetRegistration thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetRegistration(FixedAssetRegistration fixedAssetRegistrationToBeUpdated) {

		UpdateFixedAssetRegistration com = new UpdateFixedAssetRegistration(fixedAssetRegistrationToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetRegistrationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetRegistrationUpdated.class,
				event -> sendFixedAssetRegistrationChangedMessage(((FixedAssetRegistrationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetRegistration from the database
	 * 
	 * @param fixedAssetRegistrationId:
	 *            the id of the FixedAssetRegistration thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetRegistrationById(@RequestParam(value = "fixedAssetRegistrationId") String fixedAssetRegistrationId) {

		DeleteFixedAssetRegistration com = new DeleteFixedAssetRegistration(fixedAssetRegistrationId);

		int usedTicketId;

		synchronized (FixedAssetRegistrationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetRegistrationDeleted.class,
				event -> sendFixedAssetRegistrationChangedMessage(((FixedAssetRegistrationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetRegistrationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetRegistration/\" plus one of the following: "
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
