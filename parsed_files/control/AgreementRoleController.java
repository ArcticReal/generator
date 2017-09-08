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
import com.skytala.eCommerce.command.AddAgreementRole;
import com.skytala.eCommerce.command.DeleteAgreementRole;
import com.skytala.eCommerce.command.UpdateAgreementRole;
import com.skytala.eCommerce.entity.AgreementRole;
import com.skytala.eCommerce.entity.AgreementRoleMapper;
import com.skytala.eCommerce.event.AgreementRoleAdded;
import com.skytala.eCommerce.event.AgreementRoleDeleted;
import com.skytala.eCommerce.event.AgreementRoleFound;
import com.skytala.eCommerce.event.AgreementRoleUpdated;
import com.skytala.eCommerce.query.FindAgreementRolesBy;

@RestController
@RequestMapping("/api/agreementRole")
public class AgreementRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementRole
	 * @return a List with the AgreementRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementRole> findAgreementRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementRolesBy query = new FindAgreementRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementRoleFound.class,
				event -> sendAgreementRolesFoundMessage(((AgreementRoleFound) event).getAgreementRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementRolesFoundMessage(List<AgreementRole> agreementRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementRoles);
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
	public boolean createAgreementRole(HttpServletRequest request) {

		AgreementRole agreementRoleToBeAdded = new AgreementRole();
		try {
			agreementRoleToBeAdded = AgreementRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementRole(agreementRoleToBeAdded);

	}

	/**
	 * creates a new AgreementRole entry in the ofbiz database
	 * 
	 * @param agreementRoleToBeAdded
	 *            the AgreementRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementRole(AgreementRole agreementRoleToBeAdded) {

		AddAgreementRole com = new AddAgreementRole(agreementRoleToBeAdded);
		int usedTicketId;

		synchronized (AgreementRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementRoleAdded.class,
				event -> sendAgreementRoleChangedMessage(((AgreementRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementRole(HttpServletRequest request) {

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

		AgreementRole agreementRoleToBeUpdated = new AgreementRole();

		try {
			agreementRoleToBeUpdated = AgreementRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementRole(agreementRoleToBeUpdated);

	}

	/**
	 * Updates the AgreementRole with the specific Id
	 * 
	 * @param agreementRoleToBeUpdated the AgreementRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementRole(AgreementRole agreementRoleToBeUpdated) {

		UpdateAgreementRole com = new UpdateAgreementRole(agreementRoleToBeUpdated);

		int usedTicketId;

		synchronized (AgreementRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementRoleUpdated.class,
				event -> sendAgreementRoleChangedMessage(((AgreementRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementRole from the database
	 * 
	 * @param agreementRoleId:
	 *            the id of the AgreementRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementRoleById(@RequestParam(value = "agreementRoleId") String agreementRoleId) {

		DeleteAgreementRole com = new DeleteAgreementRole(agreementRoleId);

		int usedTicketId;

		synchronized (AgreementRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementRoleDeleted.class,
				event -> sendAgreementRoleChangedMessage(((AgreementRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementRole/\" plus one of the following: "
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
