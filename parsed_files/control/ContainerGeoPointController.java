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
import com.skytala.eCommerce.command.AddContainerGeoPoint;
import com.skytala.eCommerce.command.DeleteContainerGeoPoint;
import com.skytala.eCommerce.command.UpdateContainerGeoPoint;
import com.skytala.eCommerce.entity.ContainerGeoPoint;
import com.skytala.eCommerce.entity.ContainerGeoPointMapper;
import com.skytala.eCommerce.event.ContainerGeoPointAdded;
import com.skytala.eCommerce.event.ContainerGeoPointDeleted;
import com.skytala.eCommerce.event.ContainerGeoPointFound;
import com.skytala.eCommerce.event.ContainerGeoPointUpdated;
import com.skytala.eCommerce.query.FindContainerGeoPointsBy;

@RestController
@RequestMapping("/api/containerGeoPoint")
public class ContainerGeoPointController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContainerGeoPoint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContainerGeoPointController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContainerGeoPoint
	 * @return a List with the ContainerGeoPoints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContainerGeoPoint> findContainerGeoPointsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContainerGeoPointsBy query = new FindContainerGeoPointsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContainerGeoPointController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerGeoPointFound.class,
				event -> sendContainerGeoPointsFoundMessage(((ContainerGeoPointFound) event).getContainerGeoPoints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContainerGeoPointsFoundMessage(List<ContainerGeoPoint> containerGeoPoints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, containerGeoPoints);
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
	public boolean createContainerGeoPoint(HttpServletRequest request) {

		ContainerGeoPoint containerGeoPointToBeAdded = new ContainerGeoPoint();
		try {
			containerGeoPointToBeAdded = ContainerGeoPointMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContainerGeoPoint(containerGeoPointToBeAdded);

	}

	/**
	 * creates a new ContainerGeoPoint entry in the ofbiz database
	 * 
	 * @param containerGeoPointToBeAdded
	 *            the ContainerGeoPoint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContainerGeoPoint(ContainerGeoPoint containerGeoPointToBeAdded) {

		AddContainerGeoPoint com = new AddContainerGeoPoint(containerGeoPointToBeAdded);
		int usedTicketId;

		synchronized (ContainerGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerGeoPointAdded.class,
				event -> sendContainerGeoPointChangedMessage(((ContainerGeoPointAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContainerGeoPoint(HttpServletRequest request) {

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

		ContainerGeoPoint containerGeoPointToBeUpdated = new ContainerGeoPoint();

		try {
			containerGeoPointToBeUpdated = ContainerGeoPointMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContainerGeoPoint(containerGeoPointToBeUpdated);

	}

	/**
	 * Updates the ContainerGeoPoint with the specific Id
	 * 
	 * @param containerGeoPointToBeUpdated the ContainerGeoPoint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContainerGeoPoint(ContainerGeoPoint containerGeoPointToBeUpdated) {

		UpdateContainerGeoPoint com = new UpdateContainerGeoPoint(containerGeoPointToBeUpdated);

		int usedTicketId;

		synchronized (ContainerGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerGeoPointUpdated.class,
				event -> sendContainerGeoPointChangedMessage(((ContainerGeoPointUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContainerGeoPoint from the database
	 * 
	 * @param containerGeoPointId:
	 *            the id of the ContainerGeoPoint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontainerGeoPointById(@RequestParam(value = "containerGeoPointId") String containerGeoPointId) {

		DeleteContainerGeoPoint com = new DeleteContainerGeoPoint(containerGeoPointId);

		int usedTicketId;

		synchronized (ContainerGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerGeoPointDeleted.class,
				event -> sendContainerGeoPointChangedMessage(((ContainerGeoPointDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContainerGeoPointChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/containerGeoPoint/\" plus one of the following: "
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
