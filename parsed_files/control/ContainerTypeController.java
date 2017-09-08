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
import com.skytala.eCommerce.command.AddContainerType;
import com.skytala.eCommerce.command.DeleteContainerType;
import com.skytala.eCommerce.command.UpdateContainerType;
import com.skytala.eCommerce.entity.ContainerType;
import com.skytala.eCommerce.entity.ContainerTypeMapper;
import com.skytala.eCommerce.event.ContainerTypeAdded;
import com.skytala.eCommerce.event.ContainerTypeDeleted;
import com.skytala.eCommerce.event.ContainerTypeFound;
import com.skytala.eCommerce.event.ContainerTypeUpdated;
import com.skytala.eCommerce.query.FindContainerTypesBy;

@RestController
@RequestMapping("/api/containerType")
public class ContainerTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContainerType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContainerTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContainerType
	 * @return a List with the ContainerTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContainerType> findContainerTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContainerTypesBy query = new FindContainerTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContainerTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerTypeFound.class,
				event -> sendContainerTypesFoundMessage(((ContainerTypeFound) event).getContainerTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContainerTypesFoundMessage(List<ContainerType> containerTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, containerTypes);
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
	public boolean createContainerType(HttpServletRequest request) {

		ContainerType containerTypeToBeAdded = new ContainerType();
		try {
			containerTypeToBeAdded = ContainerTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContainerType(containerTypeToBeAdded);

	}

	/**
	 * creates a new ContainerType entry in the ofbiz database
	 * 
	 * @param containerTypeToBeAdded
	 *            the ContainerType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContainerType(ContainerType containerTypeToBeAdded) {

		AddContainerType com = new AddContainerType(containerTypeToBeAdded);
		int usedTicketId;

		synchronized (ContainerTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerTypeAdded.class,
				event -> sendContainerTypeChangedMessage(((ContainerTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContainerType(HttpServletRequest request) {

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

		ContainerType containerTypeToBeUpdated = new ContainerType();

		try {
			containerTypeToBeUpdated = ContainerTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContainerType(containerTypeToBeUpdated);

	}

	/**
	 * Updates the ContainerType with the specific Id
	 * 
	 * @param containerTypeToBeUpdated the ContainerType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContainerType(ContainerType containerTypeToBeUpdated) {

		UpdateContainerType com = new UpdateContainerType(containerTypeToBeUpdated);

		int usedTicketId;

		synchronized (ContainerTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerTypeUpdated.class,
				event -> sendContainerTypeChangedMessage(((ContainerTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContainerType from the database
	 * 
	 * @param containerTypeId:
	 *            the id of the ContainerType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontainerTypeById(@RequestParam(value = "containerTypeId") String containerTypeId) {

		DeleteContainerType com = new DeleteContainerType(containerTypeId);

		int usedTicketId;

		synchronized (ContainerTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerTypeDeleted.class,
				event -> sendContainerTypeChangedMessage(((ContainerTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContainerTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/containerType/\" plus one of the following: "
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
