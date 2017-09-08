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
import com.skytala.eCommerce.command.AddSecurityGroup;
import com.skytala.eCommerce.command.DeleteSecurityGroup;
import com.skytala.eCommerce.command.UpdateSecurityGroup;
import com.skytala.eCommerce.entity.SecurityGroup;
import com.skytala.eCommerce.entity.SecurityGroupMapper;
import com.skytala.eCommerce.event.SecurityGroupAdded;
import com.skytala.eCommerce.event.SecurityGroupDeleted;
import com.skytala.eCommerce.event.SecurityGroupFound;
import com.skytala.eCommerce.event.SecurityGroupUpdated;
import com.skytala.eCommerce.query.FindSecurityGroupsBy;

@RestController
@RequestMapping("/api/securityGroup")
public class SecurityGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SecurityGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SecurityGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SecurityGroup
	 * @return a List with the SecurityGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SecurityGroup> findSecurityGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSecurityGroupsBy query = new FindSecurityGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (SecurityGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SecurityGroupFound.class,
				event -> sendSecurityGroupsFoundMessage(((SecurityGroupFound) event).getSecurityGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSecurityGroupsFoundMessage(List<SecurityGroup> securityGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, securityGroups);
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
	public boolean createSecurityGroup(HttpServletRequest request) {

		SecurityGroup securityGroupToBeAdded = new SecurityGroup();
		try {
			securityGroupToBeAdded = SecurityGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSecurityGroup(securityGroupToBeAdded);

	}

	/**
	 * creates a new SecurityGroup entry in the ofbiz database
	 * 
	 * @param securityGroupToBeAdded
	 *            the SecurityGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSecurityGroup(SecurityGroup securityGroupToBeAdded) {

		AddSecurityGroup com = new AddSecurityGroup(securityGroupToBeAdded);
		int usedTicketId;

		synchronized (SecurityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SecurityGroupAdded.class,
				event -> sendSecurityGroupChangedMessage(((SecurityGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSecurityGroup(HttpServletRequest request) {

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

		SecurityGroup securityGroupToBeUpdated = new SecurityGroup();

		try {
			securityGroupToBeUpdated = SecurityGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSecurityGroup(securityGroupToBeUpdated);

	}

	/**
	 * Updates the SecurityGroup with the specific Id
	 * 
	 * @param securityGroupToBeUpdated the SecurityGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSecurityGroup(SecurityGroup securityGroupToBeUpdated) {

		UpdateSecurityGroup com = new UpdateSecurityGroup(securityGroupToBeUpdated);

		int usedTicketId;

		synchronized (SecurityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SecurityGroupUpdated.class,
				event -> sendSecurityGroupChangedMessage(((SecurityGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SecurityGroup from the database
	 * 
	 * @param securityGroupId:
	 *            the id of the SecurityGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesecurityGroupById(@RequestParam(value = "securityGroupId") String securityGroupId) {

		DeleteSecurityGroup com = new DeleteSecurityGroup(securityGroupId);

		int usedTicketId;

		synchronized (SecurityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SecurityGroupDeleted.class,
				event -> sendSecurityGroupChangedMessage(((SecurityGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSecurityGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/securityGroup/\" plus one of the following: "
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
