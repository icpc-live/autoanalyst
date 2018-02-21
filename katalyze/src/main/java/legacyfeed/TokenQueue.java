package legacyfeed;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.Sink;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TokenQueue implements Sink<SimpleMessage> {

	Logger log = LogManager.getLogger(TokenQueue.class);
	
	private TokenStreamProcessor feeder;
	private Thread feederThread;
	private BlockingQueue<SimpleMessage> output;
	
	public TokenQueue(BlockingQueue<Token> input) {
		this.output = new LinkedBlockingQueue<SimpleMessage>();

		feeder = new TokenStreamProcessor(input, this);
		
		feederThread = new Thread() {
			public void run() {
				try {
					feeder.parse();
				} catch (Exception e) {
					send(new SimpleMessage("Exception "+e.getMessage()));
					log.error(String.format("Error parsing token queue %s", e));
				}
			}
		};
		
		feederThread.start();
	}
	
	@Override
	public void send(SimpleMessage message) {
		output.add(message);
	}
	
	public boolean isOpen() {
		return (feederThread.isAlive()) || (!output.isEmpty()); 
	}
	
	public SimpleMessage pop(int timeOutMs) throws Exception {
		return output.poll(timeOutMs, TimeUnit.MILLISECONDS);
	}

}
