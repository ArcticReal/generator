package parser;

import java.io.File;

public class Test {

	
	public void test() {
		File file = new File("/home/work/workspace/ControllingParser/service_xmls/");
		for(File f: file.listFiles()) {
			System.out.println(f.getAbsolutePath());
			
		}
	}
}
