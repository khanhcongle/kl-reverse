package kl.proxy.kl_reverse;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

class FilterBase implements ReadStream<Buffer> {
	
	private ReadStream<Buffer> stream;
	
	private Handler<Buffer> downStreamDataHandler;
	private Handler<Throwable> downStreamExceptionHandler;
	private Handler<Void> downStreamEndHandler;

	ReadStream<Buffer> init(ReadStream<Buffer> s) {
		stream = s;
		stream.handler(buff -> {
			if (downStreamDataHandler == null) {
				return;
			}
			byte[] bytes = new byte[buff.length()];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = buff.getByte(i);
			}
			System.out.println("Body filtered: " + buff.toString());
			downStreamDataHandler.handle(Buffer.buffer(bytes));
		});
		
		stream.exceptionHandler(err -> {
			if (downStreamExceptionHandler == null) {
				return;
			}
			downStreamExceptionHandler.handle(err);
		});
		
		stream.endHandler(v -> {
			if (downStreamEndHandler == null) {
				return;
			}
			downStreamEndHandler.handle(v);
		});
		return this;
	}

	@Override
	public ReadStream<Buffer> pause() {
		stream.pause();
		return this;
	}

	@Override
	public ReadStream<Buffer> resume() {
		stream.resume();
		return this;
	}

	@Override
	public ReadStream<Buffer> fetch(long amount) {
		stream.fetch(amount);
		return this;
	}
	
	/**
	 * This method used by the Vertx, don't set manually
	 */
	@Override
	public ReadStream<Buffer> handler(Handler<Buffer> handler) {
		downStreamDataHandler = handler;
		return this;
	}

	/**
	 * This method used by the Vertx, don't set manually
	 */
	@Override
	public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
		downStreamExceptionHandler = handler;
		return this;
	}

	/**
	 * This method used by the Vertx, don't set manually
	 */
	@Override
	public ReadStream<Buffer> endHandler(Handler<Void> handler) {
		downStreamEndHandler = handler;
		return this;
	}
}
