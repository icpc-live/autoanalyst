package model;

public interface LifeCycleAware {
	void stop() throws Exception;
	void start() throws Exception;
}
