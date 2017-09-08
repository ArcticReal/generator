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
import com.skytala.eCommerce.command.AddProductManufacturingRule;
import com.skytala.eCommerce.command.DeleteProductManufacturingRule;
import com.skytala.eCommerce.command.UpdateProductManufacturingRule;
import com.skytala.eCommerce.entity.ProductManufacturingRule;
import com.skytala.eCommerce.entity.ProductManufacturingRuleMapper;
import com.skytala.eCommerce.event.ProductManufacturingRuleAdded;
import com.skytala.eCommerce.event.ProductManufacturingRuleDeleted;
import com.skytala.eCommerce.event.ProductManufacturingRuleFound;
import com.skytala.eCommerce.event.ProductManufacturingRuleUpdated;
import com.skytala.eCommerce.query.FindProductManufacturingRulesBy;

@RestController
@RequestMapping("/api/productManufacturingRule")
public class ProductManufacturingRuleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductManufacturingRule>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductManufacturingRuleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductManufacturingRule
	 * @return a List with the ProductManufacturingRules
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductManufacturingRule> findProductManufacturingRulesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductManufacturingRulesBy query = new FindProductManufacturingRulesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductManufacturingRuleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductManufacturingRuleFound.class,
				event -> sendProductManufacturingRulesFoundMessage(((ProductManufacturingRuleFound) event).getProductManufacturingRules(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductManufacturingRulesFoundMessage(List<ProductManufacturingRule> productManufacturingRules, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productManufacturingRules);
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
	public boolean createProductManufacturingRule(HttpServletRequest request) {

		ProductManufacturingRule productManufacturingRuleToBeAdded = new ProductManufacturingRule();
		try {
			productManufacturingRuleToBeAdded = ProductManufacturingRuleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductManufacturingRule(productManufacturingRuleToBeAdded);

	}

	/**
	 * creates a new ProductManufacturingRule entry in the ofbiz database
	 * 
	 * @param productManufacturingRuleToBeAdded
	 *            the ProductManufacturingRule thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductManufacturingRule(ProductManufacturingRule productManufacturingRuleToBeAdded) {

		AddProductManufacturingRule com = new AddProductManufacturingRule(productManufacturingRuleToBeAdded);
		int usedTicketId;

		synchronized (ProductManufacturingRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductManufacturingRuleAdded.class,
				event -> sendProductManufacturingRuleChangedMessage(((ProductManufacturingRuleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductManufacturingRule(HttpServletRequest request) {

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

		ProductManufacturingRule productManufacturingRuleToBeUpdated = new ProductManufacturingRule();

		try {
			productManufacturingRuleToBeUpdated = ProductManufacturingRuleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductManufacturingRule(productManufacturingRuleToBeUpdated);

	}

	/**
	 * Updates the ProductManufacturingRule with the specific Id
	 * 
	 * @param productManufacturingRuleToBeUpdated the ProductManufacturingRule thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductManufacturingRule(ProductManufacturingRule productManufacturingRuleToBeUpdated) {

		UpdateProductManufacturingRule com = new UpdateProductManufacturingRule(productManufacturingRuleToBeUpdated);

		int usedTicketId;

		synchronized (ProductManufacturingRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductManufacturingRuleUpdated.class,
				event -> sendProductManufacturingRuleChangedMessage(((ProductManufacturingRuleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductManufacturingRule from the database
	 * 
	 * @param productManufacturingRuleId:
	 *            the id of the ProductManufacturingRule thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductManufacturingRuleById(@RequestParam(value = "productManufacturingRuleId") String productManufacturingRuleId) {

		DeleteProductManufacturingRule com = new DeleteProductManufacturingRule(productManufacturingRuleId);

		int usedTicketId;

		synchronized (ProductManufacturingRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductManufacturingRuleDeleted.class,
				event -> sendProductManufacturingRuleChangedMessage(((ProductManufacturingRuleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductManufacturingRuleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productManufacturingRule/\" plus one of the following: "
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
