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
import com.skytala.eCommerce.command.AddProductConfigProduct;
import com.skytala.eCommerce.command.DeleteProductConfigProduct;
import com.skytala.eCommerce.command.UpdateProductConfigProduct;
import com.skytala.eCommerce.entity.ProductConfigProduct;
import com.skytala.eCommerce.entity.ProductConfigProductMapper;
import com.skytala.eCommerce.event.ProductConfigProductAdded;
import com.skytala.eCommerce.event.ProductConfigProductDeleted;
import com.skytala.eCommerce.event.ProductConfigProductFound;
import com.skytala.eCommerce.event.ProductConfigProductUpdated;
import com.skytala.eCommerce.query.FindProductConfigProductsBy;

@RestController
@RequestMapping("/api/productConfigProduct")
public class ProductConfigProductController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductConfigProduct>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductConfigProductController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductConfigProduct
	 * @return a List with the ProductConfigProducts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductConfigProduct> findProductConfigProductsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductConfigProductsBy query = new FindProductConfigProductsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductConfigProductController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigProductFound.class,
				event -> sendProductConfigProductsFoundMessage(((ProductConfigProductFound) event).getProductConfigProducts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductConfigProductsFoundMessage(List<ProductConfigProduct> productConfigProducts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productConfigProducts);
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
	public boolean createProductConfigProduct(HttpServletRequest request) {

		ProductConfigProduct productConfigProductToBeAdded = new ProductConfigProduct();
		try {
			productConfigProductToBeAdded = ProductConfigProductMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductConfigProduct(productConfigProductToBeAdded);

	}

	/**
	 * creates a new ProductConfigProduct entry in the ofbiz database
	 * 
	 * @param productConfigProductToBeAdded
	 *            the ProductConfigProduct thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductConfigProduct(ProductConfigProduct productConfigProductToBeAdded) {

		AddProductConfigProduct com = new AddProductConfigProduct(productConfigProductToBeAdded);
		int usedTicketId;

		synchronized (ProductConfigProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigProductAdded.class,
				event -> sendProductConfigProductChangedMessage(((ProductConfigProductAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductConfigProduct(HttpServletRequest request) {

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

		ProductConfigProduct productConfigProductToBeUpdated = new ProductConfigProduct();

		try {
			productConfigProductToBeUpdated = ProductConfigProductMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductConfigProduct(productConfigProductToBeUpdated);

	}

	/**
	 * Updates the ProductConfigProduct with the specific Id
	 * 
	 * @param productConfigProductToBeUpdated the ProductConfigProduct thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductConfigProduct(ProductConfigProduct productConfigProductToBeUpdated) {

		UpdateProductConfigProduct com = new UpdateProductConfigProduct(productConfigProductToBeUpdated);

		int usedTicketId;

		synchronized (ProductConfigProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigProductUpdated.class,
				event -> sendProductConfigProductChangedMessage(((ProductConfigProductUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductConfigProduct from the database
	 * 
	 * @param productConfigProductId:
	 *            the id of the ProductConfigProduct thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductConfigProductById(@RequestParam(value = "productConfigProductId") String productConfigProductId) {

		DeleteProductConfigProduct com = new DeleteProductConfigProduct(productConfigProductId);

		int usedTicketId;

		synchronized (ProductConfigProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigProductDeleted.class,
				event -> sendProductConfigProductChangedMessage(((ProductConfigProductDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductConfigProductChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productConfigProduct/\" plus one of the following: "
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
