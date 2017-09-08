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
import com.skytala.eCommerce.command.AddJobInterviewType;
import com.skytala.eCommerce.command.DeleteJobInterviewType;
import com.skytala.eCommerce.command.UpdateJobInterviewType;
import com.skytala.eCommerce.entity.JobInterviewType;
import com.skytala.eCommerce.entity.JobInterviewTypeMapper;
import com.skytala.eCommerce.event.JobInterviewTypeAdded;
import com.skytala.eCommerce.event.JobInterviewTypeDeleted;
import com.skytala.eCommerce.event.JobInterviewTypeFound;
import com.skytala.eCommerce.event.JobInterviewTypeUpdated;
import com.skytala.eCommerce.query.FindJobInterviewTypesBy;

@RestController
@RequestMapping("/api/jobInterviewType")
public class JobInterviewTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<JobInterviewType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public JobInterviewTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a JobInterviewType
	 * @return a List with the JobInterviewTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<JobInterviewType> findJobInterviewTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindJobInterviewTypesBy query = new FindJobInterviewTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (JobInterviewTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobInterviewTypeFound.class,
				event -> sendJobInterviewTypesFoundMessage(((JobInterviewTypeFound) event).getJobInterviewTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendJobInterviewTypesFoundMessage(List<JobInterviewType> jobInterviewTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, jobInterviewTypes);
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
	public boolean createJobInterviewType(HttpServletRequest request) {

		JobInterviewType jobInterviewTypeToBeAdded = new JobInterviewType();
		try {
			jobInterviewTypeToBeAdded = JobInterviewTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createJobInterviewType(jobInterviewTypeToBeAdded);

	}

	/**
	 * creates a new JobInterviewType entry in the ofbiz database
	 * 
	 * @param jobInterviewTypeToBeAdded
	 *            the JobInterviewType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createJobInterviewType(JobInterviewType jobInterviewTypeToBeAdded) {

		AddJobInterviewType com = new AddJobInterviewType(jobInterviewTypeToBeAdded);
		int usedTicketId;

		synchronized (JobInterviewTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobInterviewTypeAdded.class,
				event -> sendJobInterviewTypeChangedMessage(((JobInterviewTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateJobInterviewType(HttpServletRequest request) {

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

		JobInterviewType jobInterviewTypeToBeUpdated = new JobInterviewType();

		try {
			jobInterviewTypeToBeUpdated = JobInterviewTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateJobInterviewType(jobInterviewTypeToBeUpdated);

	}

	/**
	 * Updates the JobInterviewType with the specific Id
	 * 
	 * @param jobInterviewTypeToBeUpdated the JobInterviewType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateJobInterviewType(JobInterviewType jobInterviewTypeToBeUpdated) {

		UpdateJobInterviewType com = new UpdateJobInterviewType(jobInterviewTypeToBeUpdated);

		int usedTicketId;

		synchronized (JobInterviewTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobInterviewTypeUpdated.class,
				event -> sendJobInterviewTypeChangedMessage(((JobInterviewTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a JobInterviewType from the database
	 * 
	 * @param jobInterviewTypeId:
	 *            the id of the JobInterviewType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletejobInterviewTypeById(@RequestParam(value = "jobInterviewTypeId") String jobInterviewTypeId) {

		DeleteJobInterviewType com = new DeleteJobInterviewType(jobInterviewTypeId);

		int usedTicketId;

		synchronized (JobInterviewTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobInterviewTypeDeleted.class,
				event -> sendJobInterviewTypeChangedMessage(((JobInterviewTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendJobInterviewTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/jobInterviewType/\" plus one of the following: "
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
