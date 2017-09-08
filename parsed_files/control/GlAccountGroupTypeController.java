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
import com.skytala.eCommerce.command.AddGlAccountGroupType;
import com.skytala.eCommerce.command.DeleteGlAccountGroupType;
import com.skytala.eCommerce.command.UpdateGlAccountGroupType;
import com.skytala.eCommerce.entity.GlAccountGroupType;
import com.skytala.eCommerce.entity.GlAccountGroupTypeMapper;
import com.skytala.eCommerce.event.GlAccountGroupTypeAdded;
import com.skytala.eCommerce.event.GlAccountGroupTypeDeleted;
import com.skytala.eCommerce.event.GlAccountGroupTypeFound;
import com.skytala.eCommerce.event.GlAccountGroupTypeUpdated;
import com.skytala.eCommerce.query.FindGlAccountGroupTypesBy;

@RestController
@RequestMapping("/api/glAccountGroupType")
public class GlAccountGroupTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountGroupType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountGroupTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountGroupType
	 * @return a List with the GlAccountGroupTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountGroupType> findGlAccountGroupTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountGroupTypesBy query = new FindGlAccountGroupTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountGroupTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupTypeFound.class,
				event -> sendGlAccountGroupTypesFoundMessage(((GlAccountGroupTypeFound) event).getGlAccountGroupTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountGroupTypesFoundMessage(List<GlAccountGroupType> glAccountGroupTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountGroupTypes);
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
	public boolean createGlAccountGroupType(HttpServletRequest request) {

		GlAccountGroupType glAccountGroupTypeToBeAdded = new GlAccountGroupType();
		try {
			glAccountGroupTypeToBeAdded = GlAccountGroupTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountGroupType(glAccountGroupTypeToBeAdded);

	}

	/**
	 * creates a new GlAccountGroupType entry in the ofbiz database
	 * 
	 * @param glAccountGroupTypeToBeAdded
	 *            the GlAccountGroupType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountGroupType(GlAccountGroupType glAccountGroupTypeToBeAdded) {

		AddGlAccountGroupType com = new AddGlAccountGroupType(glAccountGroupTypeToBeAdded);
		int usedTicketId;

		synchronized (GlAccountGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupTypeAdded.class,
				event -> sendGlAccountGroupTypeChangedMessage(((GlAccountGroupTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountGroupType(HttpServletRequest request) {

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

		GlAccountGroupType glAccountGroupTypeToBeUpdated = new GlAccountGroupType();

		try {
			glAccountGroupTypeToBeUpdated = GlAccountGroupTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountGroupType(glAccountGroupTypeToBeUpdated);

	}

	/**
	 * Updates the GlAccountGroupType with the specific Id
	 * 
	 * @param glAccountGroupTypeToBeUpdated the GlAccountGroupType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountGroupType(GlAccountGroupType glAccountGroupTypeToBeUpdated) {

		UpdateGlAccountGroupType com = new UpdateGlAccountGroupType(glAccountGroupTypeToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupTypeUpdated.class,
				event -> sendGlAccountGroupTypeChangedMessage(((GlAccountGroupTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountGroupType from the database
	 * 
	 * @param glAccountGroupTypeId:
	 *            the id of the GlAccountGroupType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountGroupTypeById(@RequestParam(value = "glAccountGroupTypeId") String glAccountGroupTypeId) {

		DeleteGlAccountGroupType com = new DeleteGlAccountGroupType(glAccountGroupTypeId);

		int usedTicketId;

		synchronized (GlAccountGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupTypeDeleted.class,
				event -> sendGlAccountGroupTypeChangedMessage(((GlAccountGroupTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountGroupTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountGroupType/\" plus one of the following: "
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
