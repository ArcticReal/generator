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
import com.skytala.eCommerce.command.AddFixedAssetDepMethod;
import com.skytala.eCommerce.command.DeleteFixedAssetDepMethod;
import com.skytala.eCommerce.command.UpdateFixedAssetDepMethod;
import com.skytala.eCommerce.entity.FixedAssetDepMethod;
import com.skytala.eCommerce.entity.FixedAssetDepMethodMapper;
import com.skytala.eCommerce.event.FixedAssetDepMethodAdded;
import com.skytala.eCommerce.event.FixedAssetDepMethodDeleted;
import com.skytala.eCommerce.event.FixedAssetDepMethodFound;
import com.skytala.eCommerce.event.FixedAssetDepMethodUpdated;
import com.skytala.eCommerce.query.FindFixedAssetDepMethodsBy;

@RestController
@RequestMapping("/api/fixedAssetDepMethod")
public class FixedAssetDepMethodController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetDepMethod>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetDepMethodController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetDepMethod
	 * @return a List with the FixedAssetDepMethods
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetDepMethod> findFixedAssetDepMethodsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetDepMethodsBy query = new FindFixedAssetDepMethodsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetDepMethodController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetDepMethodFound.class,
				event -> sendFixedAssetDepMethodsFoundMessage(((FixedAssetDepMethodFound) event).getFixedAssetDepMethods(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetDepMethodsFoundMessage(List<FixedAssetDepMethod> fixedAssetDepMethods, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetDepMethods);
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
	public boolean createFixedAssetDepMethod(HttpServletRequest request) {

		FixedAssetDepMethod fixedAssetDepMethodToBeAdded = new FixedAssetDepMethod();
		try {
			fixedAssetDepMethodToBeAdded = FixedAssetDepMethodMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetDepMethod(fixedAssetDepMethodToBeAdded);

	}

	/**
	 * creates a new FixedAssetDepMethod entry in the ofbiz database
	 * 
	 * @param fixedAssetDepMethodToBeAdded
	 *            the FixedAssetDepMethod thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetDepMethod(FixedAssetDepMethod fixedAssetDepMethodToBeAdded) {

		AddFixedAssetDepMethod com = new AddFixedAssetDepMethod(fixedAssetDepMethodToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetDepMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetDepMethodAdded.class,
				event -> sendFixedAssetDepMethodChangedMessage(((FixedAssetDepMethodAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetDepMethod(HttpServletRequest request) {

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

		FixedAssetDepMethod fixedAssetDepMethodToBeUpdated = new FixedAssetDepMethod();

		try {
			fixedAssetDepMethodToBeUpdated = FixedAssetDepMethodMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetDepMethod(fixedAssetDepMethodToBeUpdated);

	}

	/**
	 * Updates the FixedAssetDepMethod with the specific Id
	 * 
	 * @param fixedAssetDepMethodToBeUpdated the FixedAssetDepMethod thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetDepMethod(FixedAssetDepMethod fixedAssetDepMethodToBeUpdated) {

		UpdateFixedAssetDepMethod com = new UpdateFixedAssetDepMethod(fixedAssetDepMethodToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetDepMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetDepMethodUpdated.class,
				event -> sendFixedAssetDepMethodChangedMessage(((FixedAssetDepMethodUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetDepMethod from the database
	 * 
	 * @param fixedAssetDepMethodId:
	 *            the id of the FixedAssetDepMethod thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetDepMethodById(@RequestParam(value = "fixedAssetDepMethodId") String fixedAssetDepMethodId) {

		DeleteFixedAssetDepMethod com = new DeleteFixedAssetDepMethod(fixedAssetDepMethodId);

		int usedTicketId;

		synchronized (FixedAssetDepMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetDepMethodDeleted.class,
				event -> sendFixedAssetDepMethodChangedMessage(((FixedAssetDepMethodDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetDepMethodChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetDepMethod/\" plus one of the following: "
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
