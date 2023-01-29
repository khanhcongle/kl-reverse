package kl.proxy.kl_reverse.proxy;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

class FilterBase implements ReadStream<Buffer> {
	
	private ReadStream<Buffer> stream;
	
	private Handler<Buffer> downStreamDataHandler;
	private Handler<Throwable> downStreamExceptionHandler;
	private Handler<Void> downStreamEndHandler;
	
	public FilterBase(ReadStream<Buffer> s, Handler<Buffer> handler) {
		stream = s;
		init(handler, null, null);
	}
	
	public FilterBase(
			ReadStream<Buffer> s,
			Handler<Buffer> handler,
			Handler<Throwable> exceptionHandler,
			Handler<Void> endHandler) {
		
		stream = s;
		init(handler, exceptionHandler, endHandler);
	}

	private ReadStream<Buffer> init(
			Handler<Buffer> handler,
			Handler<Throwable> exceptionHandler,
			Handler<Void> endHandler) {
		/*
		 * Data handler
		 */
		stream.handler(buff -> {
			if (handler != null) {
				handler.handle(buff);
			}
			if (downStreamDataHandler == null) {
				return;
			}
			downStreamDataHandler.handle(buff);
		});

		/*
		 * Exception handler
		 */
		stream.exceptionHandler(err -> {
			System.out.println("stream.exceptionHandler...");
			if (exceptionHandler != null) {
				exceptionHandler.handle(err);
			}
			if (downStreamExceptionHandler == null) {
				return;
			}
			downStreamExceptionHandler.handle(err);
		});

		/*
		 * End handler
		 */
		stream.endHandler(v -> {
			if (endHandler != null) {
				endHandler.handle(v);
			}
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
