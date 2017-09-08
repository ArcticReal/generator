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
import com.skytala.eCommerce.command.AddGlAccountCategoryType;
import com.skytala.eCommerce.command.DeleteGlAccountCategoryType;
import com.skytala.eCommerce.command.UpdateGlAccountCategoryType;
import com.skytala.eCommerce.entity.GlAccountCategoryType;
import com.skytala.eCommerce.entity.GlAccountCategoryTypeMapper;
import com.skytala.eCommerce.event.GlAccountCategoryTypeAdded;
import com.skytala.eCommerce.event.GlAccountCategoryTypeDeleted;
import com.skytala.eCommerce.event.GlAccountCategoryTypeFound;
import com.skytala.eCommerce.event.GlAccountCategoryTypeUpdated;
import com.skytala.eCommerce.query.FindGlAccountCategoryTypesBy;

@RestController
@RequestMapping("/api/glAccountCategoryType")
public class GlAccountCategoryTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountCategoryType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountCategoryTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountCategoryType
	 * @return a List with the GlAccountCategoryTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountCategoryType> findGlAccountCategoryTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountCategoryTypesBy query = new FindGlAccountCategoryTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountCategoryTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountCategoryTypeFound.class,
				event -> sendGlAccountCategoryTypesFoundMessage(((GlAccountCategoryTypeFound) event).getGlAccountCategoryTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountCategoryTypesFoundMessage(List<GlAccountCategoryType> glAccountCategoryTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountCategoryTypes);
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
	public boolean createGlAccountCategoryType(HttpServletRequest request) {

		GlAccountCategoryType glAccountCategoryTypeToBeAdded = new GlAccountCategoryType();
		try {
			glAccountCategoryTypeToBeAdded = GlAccountCategoryTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountCategoryType(glAccountCategoryTypeToBeAdded);

	}

	/**
	 * creates a new GlAccountCategoryType entry in the ofbiz database
	 * 
	 * @param glAccountCategoryTypeToBeAdded
	 *            the GlAccountCategoryType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountCategoryType(GlAccountCategoryType glAccountCategoryTypeToBeAdded) {

		AddGlAccountCategoryType com = new AddGlAccountCategoryType(glAccountCategoryTypeToBeAdded);
		int usedTicketId;

		synchronized (GlAccountCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountCategoryTypeAdded.class,
				event -> sendGlAccountCategoryTypeChangedMessage(((GlAccountCategoryTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountCategoryType(HttpServletRequest request) {

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

		GlAccountCategoryType glAccountCategoryTypeToBeUpdated = new GlAccountCategoryType();

		try {
			glAccountCategoryTypeToBeUpdated = GlAccountCategoryTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountCategoryType(glAccountCategoryTypeToBeUpdated);

	}

	/**
	 * Updates the GlAccountCategoryType with the specific Id
	 * 
	 * @param glAccountCategoryTypeToBeUpdated the GlAccountCategoryType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountCategoryType(GlAccountCategoryType glAccountCategoryTypeToBeUpdated) {

		UpdateGlAccountCategoryType com = new UpdateGlAccountCategoryType(glAccountCategoryTypeToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountCategoryTypeUpdated.class,
				event -> sendGlAccountCategoryTypeChangedMessage(((GlAccountCategoryTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountCategoryType from the database
	 * 
	 * @param glAccountCategoryTypeId:
	 *            the id of the GlAccountCategoryType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountCategoryTypeById(@RequestParam(value = "glAccountCategoryTypeId") String glAccountCategoryTypeId) {

		DeleteGlAccountCategoryType com = new DeleteGlAccountCategoryType(glAccountCategoryTypeId);

		int usedTicketId;

		synchronized (GlAccountCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountCategoryTypeDeleted.class,
				event -> sendGlAccountCategoryTypeChangedMessage(((GlAccountCategoryTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountCategoryTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountCategoryType/\" plus one of the following: "
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
