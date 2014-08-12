package com.json.generators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TestClient {

	public static void main(String[] args) {
		JsonGeneratorFactory factory=JsonGeneratorFactory.getInstance();
		JSONGenerator generator=factory.newJsonGenerator();
		
		Map data=new HashMap();
		
		Properties prop=new Properties();
		
		prop.setProperty("1", "1");
		prop.setProperty("2", "2");
		prop.setProperty("3", "3");
		
		data.put("name", "Rajesh Putta");
		data.put("age", 28);
		data.put("city", new StringBuilder("hyderabad"));
		data.put("subdata", prop);
		data.put("array", new int[]{234,555,777,888});
		data.put("chararray", new char[]{'a','b','c'});
		data.put("doublearray", new double[]{234,555,7772342342342342342.9999999999999999999,33});
		data.put("floatarray", new float[]{1,2,34,35});

		
		Map submap=new HashMap();
		submap.put(1, 10);
		submap.put(2, 20);		
		
		TestPojo tp=new TestPojo();
		
		tp.setAge(28);
		tp.setName("Rajesh");
		tp.setSs(3223.33);
		tp.setData(submap);		
		
		Set set=new HashSet();
		set.add(234);
		set.add(2343);
		set.add("asdfasfd");
		set.add(tp);
		

		data.put("set-pojo", set);
		

		
		data.put("objectarray", new Object[]{submap,set,submap,tp});		
//		data.put("setdata", set);
		
		long pretime=System.currentTimeMillis();
		String json=generator.generateJson(data);
		System.out.println("TIME TAKEN (ms)::"+(System.currentTimeMillis()-pretime));
		
		System.out.println(json);
	}

}
