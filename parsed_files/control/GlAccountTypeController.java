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
import com.skytala.eCommerce.command.AddGlAccountType;
import com.skytala.eCommerce.command.DeleteGlAccountType;
import com.skytala.eCommerce.command.UpdateGlAccountType;
import com.skytala.eCommerce.entity.GlAccountType;
import com.skytala.eCommerce.entity.GlAccountTypeMapper;
import com.skytala.eCommerce.event.GlAccountTypeAdded;
import com.skytala.eCommerce.event.GlAccountTypeDeleted;
import com.skytala.eCommerce.event.GlAccountTypeFound;
import com.skytala.eCommerce.event.GlAccountTypeUpdated;
import com.skytala.eCommerce.query.FindGlAccountTypesBy;

@RestController
@RequestMapping("/api/glAccountType")
public class GlAccountTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountType
	 * @return a List with the GlAccountTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountType> findGlAccountTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountTypesBy query = new FindGlAccountTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountTypeFound.class,
				event -> sendGlAccountTypesFoundMessage(((GlAccountTypeFound) event).getGlAccountTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountTypesFoundMessage(List<GlAccountType> glAccountTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountTypes);
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
	public boolean createGlAccountType(HttpServletRequest request) {

		GlAccountType glAccountTypeToBeAdded = new GlAccountType();
		try {
			glAccountTypeToBeAdded = GlAccountTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountType(glAccountTypeToBeAdded);

	}

	/**
	 * creates a new GlAccountType entry in the ofbiz database
	 * 
	 * @param glAccountTypeToBeAdded
	 *            the GlAccountType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountType(GlAccountType glAccountTypeToBeAdded) {

		AddGlAccountType com = new AddGlAccountType(glAccountTypeToBeAdded);
		int usedTicketId;

		synchronized (GlAccountTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountTypeAdded.class,
				event -> sendGlAccountTypeChangedMessage(((GlAccountTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountType(HttpServletRequest request) {

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

		GlAccountType glAccountTypeToBeUpdated = new GlAccountType();

		try {
			glAccountTypeToBeUpdated = GlAccountTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountType(glAccountTypeToBeUpdated);

	}

	/**
	 * Updates the GlAccountType with the specific Id
	 * 
	 * @param glAccountTypeToBeUpdated the GlAccountType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountType(GlAccountType glAccountTypeToBeUpdated) {

		UpdateGlAccountType com = new UpdateGlAccountType(glAccountTypeToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountTypeUpdated.class,
				event -> sendGlAccountTypeChangedMessage(((GlAccountTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountType from the database
	 * 
	 * @param glAccountTypeId:
	 *            the id of the GlAccountType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountTypeById(@RequestParam(value = "glAccountTypeId") String glAccountTypeId) {

		DeleteGlAccountType com = new DeleteGlAccountType(glAccountTypeId);

		int usedTicketId;

		synchronized (GlAccountTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountTypeDeleted.class,
				event -> sendGlAccountTypeChangedMessage(((GlAccountTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountType/\" plus one of the following: "
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
