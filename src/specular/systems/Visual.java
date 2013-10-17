package specular.systems;

import java.math.BigInteger;

public class Visual {
	public static String bin2hex(byte[] data) {
		return String.format("%0" + (data.length * 2) + 'X', new BigInteger(1,
				data));
	}

	// return bin data from hexadecimal string
	public static byte[] hex2bin(String data) {
		if (data.length() % 2 != 0)
			return null;
		byte hexa[] = data.getBytes();
		byte bin[] = new byte[hexa.length / 2];
		for (int a = 0; a < hexa.length; a++) {
			byte tmp1 = hexa[a];
			if (tmp1 <= '9' && tmp1 >= '0')
				tmp1 -= '0';
			else if (tmp1 <= 'F' && tmp1 >= 'A')
				tmp1 -= 'A' - 10;
			else
				return null;
			byte tmp2 = hexa[a + 1];
			if (tmp2 <= '9' && tmp2 >= '0')
				tmp2 -= '0';
			else if (tmp2 <= 'F' && tmp2 >= 'A')
				tmp2 -= 'A' - 10;
			else
				return null;
			int n = tmp1 * 16 + tmp2;
			a++;
			bin[a / 2] = (byte) n;
		}
		return bin;
	}
}
