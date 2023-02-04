package kl.proxy.kl_reverse.proxy;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

class BufferingReadStream implements ReadStream<Buffer> {

	  private final ReadStream<Buffer> stream;
	  private final Buffer content;
	  private Handler<Void> endHandler;
	  private Handler<Buffer> bufferHandler;

	  public BufferingReadStream(ReadStream<Buffer> stream, Buffer content, Handler<Buffer> bufferHandler) {
	    this.stream = stream;
	    this.content = content;
	    this.bufferHandler = bufferHandler;
	  }

	  @Override
	  public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
	    stream.exceptionHandler(handler);
	    return this;
	  }

	  @Override
	  public ReadStream<Buffer> handler(Handler<Buffer> handler) {
	    if (handler != null) {
	      stream.handler(buff -> {
	        content.appendBuffer(buff);
	        handler.handle(buff);
	        bufferHandler.handle(buff);
	      });
	    } else {
	      stream.handler(null);
	    }
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

	  @Override
	  public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
	    if (endHandler != null) {
	      stream.endHandler(v -> {
	        endHandler.handle(null);
	      });
	    } else {
	      stream.endHandler(null);
	    }
	    return this;
	  }
}
