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
import com.skytala.eCommerce.command.AddContainer;
import com.skytala.eCommerce.command.DeleteContainer;
import com.skytala.eCommerce.command.UpdateContainer;
import com.skytala.eCommerce.entity.Container;
import com.skytala.eCommerce.entity.ContainerMapper;
import com.skytala.eCommerce.event.ContainerAdded;
import com.skytala.eCommerce.event.ContainerDeleted;
import com.skytala.eCommerce.event.ContainerFound;
import com.skytala.eCommerce.event.ContainerUpdated;
import com.skytala.eCommerce.query.FindContainersBy;

@RestController
@RequestMapping("/api/container")
public class ContainerController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Container>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContainerController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Container
	 * @return a List with the Containers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Container> findContainersBy(@RequestParam Map<String, String> allRequestParams) {

		FindContainersBy query = new FindContainersBy(allRequestParams);

		int usedTicketId;

		synchronized (ContainerController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerFound.class,
				event -> sendContainersFoundMessage(((ContainerFound) event).getContainers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContainersFoundMessage(List<Container> containers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, containers);
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
	public boolean createContainer(HttpServletRequest request) {

		Container containerToBeAdded = new Container();
		try {
			containerToBeAdded = ContainerMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContainer(containerToBeAdded);

	}

	/**
	 * creates a new Container entry in the ofbiz database
	 * 
	 * @param containerToBeAdded
	 *            the Container thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContainer(Container containerToBeAdded) {

		AddContainer com = new AddContainer(containerToBeAdded);
		int usedTicketId;

		synchronized (ContainerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerAdded.class,
				event -> sendContainerChangedMessage(((ContainerAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContainer(HttpServletRequest request) {

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

		Container containerToBeUpdated = new Container();

		try {
			containerToBeUpdated = ContainerMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContainer(containerToBeUpdated);

	}

	/**
	 * Updates the Container with the specific Id
	 * 
	 * @param containerToBeUpdated the Container thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContainer(Container containerToBeUpdated) {

		UpdateContainer com = new UpdateContainer(containerToBeUpdated);

		int usedTicketId;

		synchronized (ContainerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerUpdated.class,
				event -> sendContainerChangedMessage(((ContainerUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Container from the database
	 * 
	 * @param containerId:
	 *            the id of the Container thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontainerById(@RequestParam(value = "containerId") String containerId) {

		DeleteContainer com = new DeleteContainer(containerId);

		int usedTicketId;

		synchronized (ContainerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContainerDeleted.class,
				event -> sendContainerChangedMessage(((ContainerDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContainerChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/container/\" plus one of the following: "
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
