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
import com.skytala.eCommerce.command.AddValidResponsibility;
import com.skytala.eCommerce.command.DeleteValidResponsibility;
import com.skytala.eCommerce.command.UpdateValidResponsibility;
import com.skytala.eCommerce.entity.ValidResponsibility;
import com.skytala.eCommerce.entity.ValidResponsibilityMapper;
import com.skytala.eCommerce.event.ValidResponsibilityAdded;
import com.skytala.eCommerce.event.ValidResponsibilityDeleted;
import com.skytala.eCommerce.event.ValidResponsibilityFound;
import com.skytala.eCommerce.event.ValidResponsibilityUpdated;
import com.skytala.eCommerce.query.FindValidResponsibilitysBy;

@RestController
@RequestMapping("/api/validResponsibility")
public class ValidResponsibilityController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ValidResponsibility>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ValidResponsibilityController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ValidResponsibility
	 * @return a List with the ValidResponsibilitys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ValidResponsibility> findValidResponsibilitysBy(@RequestParam Map<String, String> allRequestParams) {

		FindValidResponsibilitysBy query = new FindValidResponsibilitysBy(allRequestParams);

		int usedTicketId;

		synchronized (ValidResponsibilityController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValidResponsibilityFound.class,
				event -> sendValidResponsibilitysFoundMessage(((ValidResponsibilityFound) event).getValidResponsibilitys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendValidResponsibilitysFoundMessage(List<ValidResponsibility> validResponsibilitys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, validResponsibilitys);
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
	public boolean createValidResponsibility(HttpServletRequest request) {

		ValidResponsibility validResponsibilityToBeAdded = new ValidResponsibility();
		try {
			validResponsibilityToBeAdded = ValidResponsibilityMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createValidResponsibility(validResponsibilityToBeAdded);

	}

	/**
	 * creates a new ValidResponsibility entry in the ofbiz database
	 * 
	 * @param validResponsibilityToBeAdded
	 *            the ValidResponsibility thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createValidResponsibility(ValidResponsibility validResponsibilityToBeAdded) {

		AddValidResponsibility com = new AddValidResponsibility(validResponsibilityToBeAdded);
		int usedTicketId;

		synchronized (ValidResponsibilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValidResponsibilityAdded.class,
				event -> sendValidResponsibilityChangedMessage(((ValidResponsibilityAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateValidResponsibility(HttpServletRequest request) {

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

		ValidResponsibility validResponsibilityToBeUpdated = new ValidResponsibility();

		try {
			validResponsibilityToBeUpdated = ValidResponsibilityMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateValidResponsibility(validResponsibilityToBeUpdated);

	}

	/**
	 * Updates the ValidResponsibility with the specific Id
	 * 
	 * @param validResponsibilityToBeUpdated the ValidResponsibility thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateValidResponsibility(ValidResponsibility validResponsibilityToBeUpdated) {

		UpdateValidResponsibility com = new UpdateValidResponsibility(validResponsibilityToBeUpdated);

		int usedTicketId;

		synchronized (ValidResponsibilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValidResponsibilityUpdated.class,
				event -> sendValidResponsibilityChangedMessage(((ValidResponsibilityUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ValidResponsibility from the database
	 * 
	 * @param validResponsibilityId:
	 *            the id of the ValidResponsibility thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletevalidResponsibilityById(@RequestParam(value = "validResponsibilityId") String validResponsibilityId) {

		DeleteValidResponsibility com = new DeleteValidResponsibility(validResponsibilityId);

		int usedTicketId;

		synchronized (ValidResponsibilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValidResponsibilityDeleted.class,
				event -> sendValidResponsibilityChangedMessage(((ValidResponsibilityDeleted) event).isSuccess(), usedTicketId));

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

	public void sendValidResponsibilityChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/validResponsibility/\" plus one of the following: "
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
