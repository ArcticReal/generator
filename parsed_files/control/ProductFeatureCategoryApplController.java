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
import com.skytala.eCommerce.command.AddProductFeatureCategoryAppl;
import com.skytala.eCommerce.command.DeleteProductFeatureCategoryAppl;
import com.skytala.eCommerce.command.UpdateProductFeatureCategoryAppl;
import com.skytala.eCommerce.entity.ProductFeatureCategoryAppl;
import com.skytala.eCommerce.entity.ProductFeatureCategoryApplMapper;
import com.skytala.eCommerce.event.ProductFeatureCategoryApplAdded;
import com.skytala.eCommerce.event.ProductFeatureCategoryApplDeleted;
import com.skytala.eCommerce.event.ProductFeatureCategoryApplFound;
import com.skytala.eCommerce.event.ProductFeatureCategoryApplUpdated;
import com.skytala.eCommerce.query.FindProductFeatureCategoryApplsBy;

@RestController
@RequestMapping("/api/productFeatureCategoryAppl")
public class ProductFeatureCategoryApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureCategoryAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureCategoryApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureCategoryAppl
	 * @return a List with the ProductFeatureCategoryAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureCategoryAppl> findProductFeatureCategoryApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureCategoryApplsBy query = new FindProductFeatureCategoryApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureCategoryApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCategoryApplFound.class,
				event -> sendProductFeatureCategoryApplsFoundMessage(((ProductFeatureCategoryApplFound) event).getProductFeatureCategoryAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureCategoryApplsFoundMessage(List<ProductFeatureCategoryAppl> productFeatureCategoryAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureCategoryAppls);
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
	public boolean createProductFeatureCategoryAppl(HttpServletRequest request) {

		ProductFeatureCategoryAppl productFeatureCategoryApplToBeAdded = new ProductFeatureCategoryAppl();
		try {
			productFeatureCategoryApplToBeAdded = ProductFeatureCategoryApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureCategoryAppl(productFeatureCategoryApplToBeAdded);

	}

	/**
	 * creates a new ProductFeatureCategoryAppl entry in the ofbiz database
	 * 
	 * @param productFeatureCategoryApplToBeAdded
	 *            the ProductFeatureCategoryAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureCategoryAppl(ProductFeatureCategoryAppl productFeatureCategoryApplToBeAdded) {

		AddProductFeatureCategoryAppl com = new AddProductFeatureCategoryAppl(productFeatureCategoryApplToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureCategoryApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCategoryApplAdded.class,
				event -> sendProductFeatureCategoryApplChangedMessage(((ProductFeatureCategoryApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureCategoryAppl(HttpServletRequest request) {

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

		ProductFeatureCategoryAppl productFeatureCategoryApplToBeUpdated = new ProductFeatureCategoryAppl();

		try {
			productFeatureCategoryApplToBeUpdated = ProductFeatureCategoryApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureCategoryAppl(productFeatureCategoryApplToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureCategoryAppl with the specific Id
	 * 
	 * @param productFeatureCategoryApplToBeUpdated the ProductFeatureCategoryAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureCategoryAppl(ProductFeatureCategoryAppl productFeatureCategoryApplToBeUpdated) {

		UpdateProductFeatureCategoryAppl com = new UpdateProductFeatureCategoryAppl(productFeatureCategoryApplToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureCategoryApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCategoryApplUpdated.class,
				event -> sendProductFeatureCategoryApplChangedMessage(((ProductFeatureCategoryApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureCategoryAppl from the database
	 * 
	 * @param productFeatureCategoryApplId:
	 *            the id of the ProductFeatureCategoryAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureCategoryApplById(@RequestParam(value = "productFeatureCategoryApplId") String productFeatureCategoryApplId) {

		DeleteProductFeatureCategoryAppl com = new DeleteProductFeatureCategoryAppl(productFeatureCategoryApplId);

		int usedTicketId;

		synchronized (ProductFeatureCategoryApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCategoryApplDeleted.class,
				event -> sendProductFeatureCategoryApplChangedMessage(((ProductFeatureCategoryApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureCategoryApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureCategoryAppl/\" plus one of the following: "
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
