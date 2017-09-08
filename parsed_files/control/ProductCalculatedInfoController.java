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
import com.skytala.eCommerce.command.AddProductCalculatedInfo;
import com.skytala.eCommerce.command.DeleteProductCalculatedInfo;
import com.skytala.eCommerce.command.UpdateProductCalculatedInfo;
import com.skytala.eCommerce.entity.ProductCalculatedInfo;
import com.skytala.eCommerce.entity.ProductCalculatedInfoMapper;
import com.skytala.eCommerce.event.ProductCalculatedInfoAdded;
import com.skytala.eCommerce.event.ProductCalculatedInfoDeleted;
import com.skytala.eCommerce.event.ProductCalculatedInfoFound;
import com.skytala.eCommerce.event.ProductCalculatedInfoUpdated;
import com.skytala.eCommerce.query.FindProductCalculatedInfosBy;

@RestController
@RequestMapping("/api/productCalculatedInfo")
public class ProductCalculatedInfoController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCalculatedInfo>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCalculatedInfoController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCalculatedInfo
	 * @return a List with the ProductCalculatedInfos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCalculatedInfo> findProductCalculatedInfosBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCalculatedInfosBy query = new FindProductCalculatedInfosBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCalculatedInfoController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCalculatedInfoFound.class,
				event -> sendProductCalculatedInfosFoundMessage(((ProductCalculatedInfoFound) event).getProductCalculatedInfos(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCalculatedInfosFoundMessage(List<ProductCalculatedInfo> productCalculatedInfos, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCalculatedInfos);
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
	public boolean createProductCalculatedInfo(HttpServletRequest request) {

		ProductCalculatedInfo productCalculatedInfoToBeAdded = new ProductCalculatedInfo();
		try {
			productCalculatedInfoToBeAdded = ProductCalculatedInfoMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCalculatedInfo(productCalculatedInfoToBeAdded);

	}

	/**
	 * creates a new ProductCalculatedInfo entry in the ofbiz database
	 * 
	 * @param productCalculatedInfoToBeAdded
	 *            the ProductCalculatedInfo thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCalculatedInfo(ProductCalculatedInfo productCalculatedInfoToBeAdded) {

		AddProductCalculatedInfo com = new AddProductCalculatedInfo(productCalculatedInfoToBeAdded);
		int usedTicketId;

		synchronized (ProductCalculatedInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCalculatedInfoAdded.class,
				event -> sendProductCalculatedInfoChangedMessage(((ProductCalculatedInfoAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCalculatedInfo(HttpServletRequest request) {

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

		ProductCalculatedInfo productCalculatedInfoToBeUpdated = new ProductCalculatedInfo();

		try {
			productCalculatedInfoToBeUpdated = ProductCalculatedInfoMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCalculatedInfo(productCalculatedInfoToBeUpdated);

	}

	/**
	 * Updates the ProductCalculatedInfo with the specific Id
	 * 
	 * @param productCalculatedInfoToBeUpdated the ProductCalculatedInfo thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCalculatedInfo(ProductCalculatedInfo productCalculatedInfoToBeUpdated) {

		UpdateProductCalculatedInfo com = new UpdateProductCalculatedInfo(productCalculatedInfoToBeUpdated);

		int usedTicketId;

		synchronized (ProductCalculatedInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCalculatedInfoUpdated.class,
				event -> sendProductCalculatedInfoChangedMessage(((ProductCalculatedInfoUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCalculatedInfo from the database
	 * 
	 * @param productCalculatedInfoId:
	 *            the id of the ProductCalculatedInfo thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCalculatedInfoById(@RequestParam(value = "productCalculatedInfoId") String productCalculatedInfoId) {

		DeleteProductCalculatedInfo com = new DeleteProductCalculatedInfo(productCalculatedInfoId);

		int usedTicketId;

		synchronized (ProductCalculatedInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCalculatedInfoDeleted.class,
				event -> sendProductCalculatedInfoChangedMessage(((ProductCalculatedInfoDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCalculatedInfoChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCalculatedInfo/\" plus one of the following: "
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
