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
import com.skytala.eCommerce.command.AddFacilityGroupMember;
import com.skytala.eCommerce.command.DeleteFacilityGroupMember;
import com.skytala.eCommerce.command.UpdateFacilityGroupMember;
import com.skytala.eCommerce.entity.FacilityGroupMember;
import com.skytala.eCommerce.entity.FacilityGroupMemberMapper;
import com.skytala.eCommerce.event.FacilityGroupMemberAdded;
import com.skytala.eCommerce.event.FacilityGroupMemberDeleted;
import com.skytala.eCommerce.event.FacilityGroupMemberFound;
import com.skytala.eCommerce.event.FacilityGroupMemberUpdated;
import com.skytala.eCommerce.query.FindFacilityGroupMembersBy;

@RestController
@RequestMapping("/api/facilityGroupMember")
public class FacilityGroupMemberController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityGroupMember>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityGroupMemberController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityGroupMember
	 * @return a List with the FacilityGroupMembers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityGroupMember> findFacilityGroupMembersBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityGroupMembersBy query = new FindFacilityGroupMembersBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityGroupMemberController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupMemberFound.class,
				event -> sendFacilityGroupMembersFoundMessage(((FacilityGroupMemberFound) event).getFacilityGroupMembers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityGroupMembersFoundMessage(List<FacilityGroupMember> facilityGroupMembers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityGroupMembers);
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
	public boolean createFacilityGroupMember(HttpServletRequest request) {

		FacilityGroupMember facilityGroupMemberToBeAdded = new FacilityGroupMember();
		try {
			facilityGroupMemberToBeAdded = FacilityGroupMemberMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityGroupMember(facilityGroupMemberToBeAdded);

	}

	/**
	 * creates a new FacilityGroupMember entry in the ofbiz database
	 * 
	 * @param facilityGroupMemberToBeAdded
	 *            the FacilityGroupMember thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityGroupMember(FacilityGroupMember facilityGroupMemberToBeAdded) {

		AddFacilityGroupMember com = new AddFacilityGroupMember(facilityGroupMemberToBeAdded);
		int usedTicketId;

		synchronized (FacilityGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupMemberAdded.class,
				event -> sendFacilityGroupMemberChangedMessage(((FacilityGroupMemberAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityGroupMember(HttpServletRequest request) {

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

		FacilityGroupMember facilityGroupMemberToBeUpdated = new FacilityGroupMember();

		try {
			facilityGroupMemberToBeUpdated = FacilityGroupMemberMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityGroupMember(facilityGroupMemberToBeUpdated);

	}

	/**
	 * Updates the FacilityGroupMember with the specific Id
	 * 
	 * @param facilityGroupMemberToBeUpdated the FacilityGroupMember thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityGroupMember(FacilityGroupMember facilityGroupMemberToBeUpdated) {

		UpdateFacilityGroupMember com = new UpdateFacilityGroupMember(facilityGroupMemberToBeUpdated);

		int usedTicketId;

		synchronized (FacilityGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupMemberUpdated.class,
				event -> sendFacilityGroupMemberChangedMessage(((FacilityGroupMemberUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityGroupMember from the database
	 * 
	 * @param facilityGroupMemberId:
	 *            the id of the FacilityGroupMember thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityGroupMemberById(@RequestParam(value = "facilityGroupMemberId") String facilityGroupMemberId) {

		DeleteFacilityGroupMember com = new DeleteFacilityGroupMember(facilityGroupMemberId);

		int usedTicketId;

		synchronized (FacilityGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupMemberDeleted.class,
				event -> sendFacilityGroupMemberChangedMessage(((FacilityGroupMemberDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityGroupMemberChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityGroupMember/\" plus one of the following: "
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
