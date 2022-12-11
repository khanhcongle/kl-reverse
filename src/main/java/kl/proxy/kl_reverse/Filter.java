package kl.proxy.kl_reverse;

import java.util.concurrent.atomic.AtomicBoolean;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

class Filter implements ReadStream<Buffer> {

	private final AtomicBoolean paused = new AtomicBoolean();
	private ReadStream<Buffer> stream;
	private Buffer expected = Buffer.buffer();
	private Handler<Buffer> dataHandler;
	private Handler<Throwable> exceptionHandler;
	private Handler<Void> endHandler;

	ReadStream<Buffer> init(ReadStream<Buffer> s) {
		stream = s;
		stream.handler(buff -> {
			if (dataHandler != null) {
				byte[] bytes = new byte[buff.length()];
				for (int i = 0; i < bytes.length; i++) {
					bytes[i] = buff.getByte(i);
				}
				System.out.println("Body filtered: " + buff.toString());
				
				expected.appendBytes(bytes);
				dataHandler.handle(Buffer.buffer(bytes));
			}
		});
		stream.exceptionHandler(err -> {
			if (exceptionHandler != null) {
				exceptionHandler.handle(err);
			}
		});
		stream.endHandler(v -> {
			if (endHandler != null) {
				endHandler.handle(v);
			}
		});
		return this;
	}

	@Override
	public ReadStream<Buffer> pause() {
		paused.set(true);
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

	@Override
	public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
		exceptionHandler = handler;
		return this;
	}

	@Override
	public ReadStream<Buffer> handler(Handler<Buffer> handler) {
		dataHandler = handler;
		return this;
	}

	@Override
	public ReadStream<Buffer> endHandler(Handler<Void> handler) {
		endHandler = handler;
		return this;
	}
}
