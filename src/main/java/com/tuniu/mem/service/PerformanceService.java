package com.tuniu.mem.service;

public interface PerformanceService {

	public void setBySingle(String key, String value);

	public void setByPool(String key, String value);
}
