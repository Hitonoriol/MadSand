package hitonoriol.madsand.util;

public class ByteUtils {

	public static byte[] concat(byte[]... arrays) {
		int totalLength = 0;
		for (int i = 0; i < arrays.length; i++)
			totalLength += arrays[i].length;

		byte[] result = new byte[totalLength];

		int currentIndex = 0;
		for (int i = 0; i < arrays.length; i++) {
			System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
			currentIndex += arrays[i].length;
		}

		return result;
	}

	public static byte[] encode8(long l) {
		byte[] result = new byte[8];
		for (int i = 7; i >= 0; i--) {
			result[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
		return result;
	}

	public static long decode8(byte[] b) {
		long result = 0;
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
