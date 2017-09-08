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
import com.skytala.eCommerce.command.AddSegmentGroupRole;
import com.skytala.eCommerce.command.DeleteSegmentGroupRole;
import com.skytala.eCommerce.command.UpdateSegmentGroupRole;
import com.skytala.eCommerce.entity.SegmentGroupRole;
import com.skytala.eCommerce.entity.SegmentGroupRoleMapper;
import com.skytala.eCommerce.event.SegmentGroupRoleAdded;
import com.skytala.eCommerce.event.SegmentGroupRoleDeleted;
import com.skytala.eCommerce.event.SegmentGroupRoleFound;
import com.skytala.eCommerce.event.SegmentGroupRoleUpdated;
import com.skytala.eCommerce.query.FindSegmentGroupRolesBy;

@RestController
@RequestMapping("/api/segmentGroupRole")
public class SegmentGroupRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SegmentGroupRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SegmentGroupRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SegmentGroupRole
	 * @return a List with the SegmentGroupRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SegmentGroupRole> findSegmentGroupRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSegmentGroupRolesBy query = new FindSegmentGroupRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (SegmentGroupRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupRoleFound.class,
				event -> sendSegmentGroupRolesFoundMessage(((SegmentGroupRoleFound) event).getSegmentGroupRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSegmentGroupRolesFoundMessage(List<SegmentGroupRole> segmentGroupRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, segmentGroupRoles);
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
	public boolean createSegmentGroupRole(HttpServletRequest request) {

		SegmentGroupRole segmentGroupRoleToBeAdded = new SegmentGroupRole();
		try {
			segmentGroupRoleToBeAdded = SegmentGroupRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSegmentGroupRole(segmentGroupRoleToBeAdded);

	}

	/**
	 * creates a new SegmentGroupRole entry in the ofbiz database
	 * 
	 * @param segmentGroupRoleToBeAdded
	 *            the SegmentGroupRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSegmentGroupRole(SegmentGroupRole segmentGroupRoleToBeAdded) {

		AddSegmentGroupRole com = new AddSegmentGroupRole(segmentGroupRoleToBeAdded);
		int usedTicketId;

		synchronized (SegmentGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupRoleAdded.class,
				event -> sendSegmentGroupRoleChangedMessage(((SegmentGroupRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSegmentGroupRole(HttpServletRequest request) {

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

		SegmentGroupRole segmentGroupRoleToBeUpdated = new SegmentGroupRole();

		try {
			segmentGroupRoleToBeUpdated = SegmentGroupRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSegmentGroupRole(segmentGroupRoleToBeUpdated);

	}

	/**
	 * Updates the SegmentGroupRole with the specific Id
	 * 
	 * @param segmentGroupRoleToBeUpdated the SegmentGroupRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSegmentGroupRole(SegmentGroupRole segmentGroupRoleToBeUpdated) {

		UpdateSegmentGroupRole com = new UpdateSegmentGroupRole(segmentGroupRoleToBeUpdated);

		int usedTicketId;

		synchronized (SegmentGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupRoleUpdated.class,
				event -> sendSegmentGroupRoleChangedMessage(((SegmentGroupRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SegmentGroupRole from the database
	 * 
	 * @param segmentGroupRoleId:
	 *            the id of the SegmentGroupRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesegmentGroupRoleById(@RequestParam(value = "segmentGroupRoleId") String segmentGroupRoleId) {

		DeleteSegmentGroupRole com = new DeleteSegmentGroupRole(segmentGroupRoleId);

		int usedTicketId;

		synchronized (SegmentGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupRoleDeleted.class,
				event -> sendSegmentGroupRoleChangedMessage(((SegmentGroupRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSegmentGroupRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/segmentGroupRole/\" plus one of the following: "
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
