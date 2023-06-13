package hitonoriol.madsand.util;

public class ByteUtils {

	public static byte[] concat(byte[]... arrays) {
		int totalLength = 0;
		for (byte[] array : arrays)
			totalLength += array.length;

		byte[] result = new byte[totalLength];

		int currentIndex = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, currentIndex, array.length);
			currentIndex += array.length;
		}

		return result;
	}

	public static byte[] encode8(long l) {
		var result = new byte[8];
		for (var i = 7; i >= 0; i--) {
			result[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
		return result;
	}

	public static long decode8(byte[] b) {
		long result = 0L;
		for (int i = 0; i < 8; i++) {
			result <<= 8;
			result |= (b[i] & 0xFF);
		}
		return result;
	}

	public static byte[] encode2(int val) {
		byte data[] = new byte[2];
		data[1] = (byte) (val & 0xFF);
		data[0] = (byte) ((val >> 8) & 0xFF);
		return data;
	}

	public static int decode2(byte[] bytes) {
		return (bytes[0] << 8) | (bytes[1] & 0xFF);
	}

}
