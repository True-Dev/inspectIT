package rocks.inspectit.shared.cs.storage.nio.read;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.shared.cs.storage.nio.WriteReadAttachment;
import rocks.inspectit.shared.cs.storage.nio.WriteReadCompletionRunnable;

/**
 * Completion handler for asynchronous reading.
 *
 * @author Ivan Senic
 *
 */
public class ReadingCompletionHandler implements CompletionHandler<Integer, WriteReadAttachment> {

	/**
	 * The log of this class. Can not be assigned via spring because this is not a component.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ReadingCompletionHandler.class);

	/**
	 * {@inheritDoc}
	 * <p>
	 * On reading completion the check if the wanted reading size has been read. If not, a new read
	 * with updated position and size is performed. If yes, the completion runnable is invoke if it
	 * is not null and buffer is flip.
	 */
	@Override
	public void completed(Integer result, WriteReadAttachment attachment) {
		long bytesToReadMore = attachment.getSize() - result.longValue();
		if (bytesToReadMore > 0) {
			long readSize = bytesToReadMore;
			long position = attachment.getPosition() + result.longValue();

			attachment.setSize(readSize);
			attachment.setPosition(position);

			attachment.getFileChannel().read(attachment.getByteBuffer(), position, attachment, this);
		} else {
			ByteBuffer byteBuffer = attachment.getByteBuffer();
			// there is a chance that we can read more bytes that wanted, that's just the
			// possibility coming from Java NIO2
			// in that case, we need to assure that the byte buffer contains only the amount of
			// bytes we wanted to read
			// that will assure that the de-serialization goes without problems
			// keep in mind that in situation described bytesToReadMore is negative
			if (bytesToReadMore < 0) {
				byteBuffer.position(byteBuffer.position() + (int) bytesToReadMore);
			}
			byteBuffer.flip();
			WriteReadCompletionRunnable.RunnableFuture completionRunnableFuture = attachment.getCompletionRunnableFuture();
			if (null != completionRunnableFuture) {
				completionRunnableFuture.getWriteReadCompletionRunnable().markSuccess();
				if (completionRunnableFuture.getWriteReadCompletionRunnable().isFinished()) {
					completionRunnableFuture.run();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void failed(Throwable exc, WriteReadAttachment attachment) {
		LOG.error("Write to the disk failed.", exc);
		WriteReadCompletionRunnable.RunnableFuture completionRunnableFuture = attachment.getCompletionRunnableFuture();
		if (null != completionRunnableFuture) {
			completionRunnableFuture.getWriteReadCompletionRunnable().markFailed();
			if (completionRunnableFuture.getWriteReadCompletionRunnable().isFinished()) {
				completionRunnableFuture.run();
			}
		}
	}

}
