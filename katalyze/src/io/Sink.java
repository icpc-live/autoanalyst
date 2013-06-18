package io;

public interface Sink<T> {
	void send(T message);
}
