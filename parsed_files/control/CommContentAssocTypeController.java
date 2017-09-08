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
import com.skytala.eCommerce.command.AddCommContentAssocType;
import com.skytala.eCommerce.command.DeleteCommContentAssocType;
import com.skytala.eCommerce.command.UpdateCommContentAssocType;
import com.skytala.eCommerce.entity.CommContentAssocType;
import com.skytala.eCommerce.entity.CommContentAssocTypeMapper;
import com.skytala.eCommerce.event.CommContentAssocTypeAdded;
import com.skytala.eCommerce.event.CommContentAssocTypeDeleted;
import com.skytala.eCommerce.event.CommContentAssocTypeFound;
import com.skytala.eCommerce.event.CommContentAssocTypeUpdated;
import com.skytala.eCommerce.query.FindCommContentAssocTypesBy;

@RestController
@RequestMapping("/api/commContentAssocType")
public class CommContentAssocTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommContentAssocType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommContentAssocTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommContentAssocType
	 * @return a List with the CommContentAssocTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommContentAssocType> findCommContentAssocTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommContentAssocTypesBy query = new FindCommContentAssocTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (CommContentAssocTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommContentAssocTypeFound.class,
				event -> sendCommContentAssocTypesFoundMessage(((CommContentAssocTypeFound) event).getCommContentAssocTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommContentAssocTypesFoundMessage(List<CommContentAssocType> commContentAssocTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, commContentAssocTypes);
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
	public boolean createCommContentAssocType(HttpServletRequest request) {

		CommContentAssocType commContentAssocTypeToBeAdded = new CommContentAssocType();
		try {
			commContentAssocTypeToBeAdded = CommContentAssocTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommContentAssocType(commContentAssocTypeToBeAdded);

	}

	/**
	 * creates a new CommContentAssocType entry in the ofbiz database
	 * 
	 * @param commContentAssocTypeToBeAdded
	 *            the CommContentAssocType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommContentAssocType(CommContentAssocType commContentAssocTypeToBeAdded) {

		AddCommContentAssocType com = new AddCommContentAssocType(commContentAssocTypeToBeAdded);
		int usedTicketId;

		synchronized (CommContentAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommContentAssocTypeAdded.class,
				event -> sendCommContentAssocTypeChangedMessage(((CommContentAssocTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommContentAssocType(HttpServletRequest request) {

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

		CommContentAssocType commContentAssocTypeToBeUpdated = new CommContentAssocType();

		try {
			commContentAssocTypeToBeUpdated = CommContentAssocTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommContentAssocType(commContentAssocTypeToBeUpdated);

	}

	/**
	 * Updates the CommContentAssocType with the specific Id
	 * 
	 * @param commContentAssocTypeToBeUpdated the CommContentAssocType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommContentAssocType(CommContentAssocType commContentAssocTypeToBeUpdated) {

		UpdateCommContentAssocType com = new UpdateCommContentAssocType(commContentAssocTypeToBeUpdated);

		int usedTicketId;

		synchronized (CommContentAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommContentAssocTypeUpdated.class,
				event -> sendCommContentAssocTypeChangedMessage(((CommContentAssocTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommContentAssocType from the database
	 * 
	 * @param commContentAssocTypeId:
	 *            the id of the CommContentAssocType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommContentAssocTypeById(@RequestParam(value = "commContentAssocTypeId") String commContentAssocTypeId) {

		DeleteCommContentAssocType com = new DeleteCommContentAssocType(commContentAssocTypeId);

		int usedTicketId;

		synchronized (CommContentAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommContentAssocTypeDeleted.class,
				event -> sendCommContentAssocTypeChangedMessage(((CommContentAssocTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommContentAssocTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/commContentAssocType/\" plus one of the following: "
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
