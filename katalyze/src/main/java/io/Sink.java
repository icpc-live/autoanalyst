package io;

@FunctionalInterface
public interface Sink<T> {
	void send(T message);
}
