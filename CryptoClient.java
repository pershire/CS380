import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.interfaces.RSAPublicKey;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

public class CryptoClient {

	public static void main(String[] args) throws UnknownHostException,
			IOException, ClassNotFoundException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		try (Socket socket = new Socket("45.50.5.238", 38008)) {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			FileInputStream fis = new FileInputStream("public.bin");
			ObjectInputStream ois = new ObjectInputStream(fis);
			RSAPublicKey pkey = (RSAPublicKey) ois.readObject();
			Cipher cipher = Cipher.getInstance("AES");
			Cipher pkCipher = Cipher.getInstance("RSA");
			Key key = KeyGenerator.getInstance("AES").generateKey();
			cipher.init(Cipher.ENCRYPT_MODE, key);
			pkCipher.init(Cipher.ENCRYPT_MODE, pkey);
			byte[] skey = key.getEncoded();
			byte[] out = pkCipher.doFinal(skey);
			byte[] packet = genPacket(out, out.length);
			os.write(packet, 0, packet.length);
			while (true) {
				int code[] = new int[4];
				code[0] = is.read();
				code[1] = is.read();
				code[2] = is.read();
				code[3] = is.read();
				System.out.println("0x"
						+ Integer.toHexString(code[0]).toUpperCase()
						+ Integer.toHexString(code[1]).toUpperCase()
						+ Integer.toHexString(code[2]).toUpperCase()
						+ Integer.toHexString(code[3]).toUpperCase());
				if (code[0] == 0xCA && code[1] == 0xFE && code[2] == 0xBA
						&& code[3] == 0xBE) {
					break;
				} else {
					System.exit(0);
				}
			}

		}
	}

	private static byte[] genPacket(byte[] out, int length) {
		byte arr[] = new byte[28 + length];
		int udpLen = arr.length - 20;
		for (int j = 0; j < 28; j++) {
			arr[j] = 0;
		}
		arr[0] = 0x45;
		arr[3] = (byte) arr.length;
		arr[2] = (byte) (arr.length >> 8);
		arr[6] = (byte) 0x40;
		arr[8] = (byte) 50;
		arr[9] = (byte) 0x11;
		arr[12] = (byte) 127;
		arr[13] = (byte) 0;
		arr[14] = (byte) 0;
		arr[15] = (byte) 1;
		arr[16] = (byte) 0x2d;
		arr[17] = (byte) 0x32;
		arr[18] = (byte) 0x5;
		arr[19] = (byte) 0xee;
		long csum = checksum(arr);
		arr[11] = (byte) csum;
		csum = csum >> 8;
		arr[10] = (byte) csum;
		arr[20] = (byte) 38008>>8;
		arr[21] = (byte) 38008;
		arr[22] = (byte) 38008 >> 8;
		arr[23] = (byte) 38008;
		arr[25] = (byte) udpLen;
		arr[24] = (byte) (udpLen >> 8);
		csum = checksum(udpCs(arr));
		arr[27] = (byte) csum;
		csum = csum >> 8;
		arr[26] = (byte) csum;
		for (int i = 0; i < out.length; i++) {
			arr[28 + i] = out[i];
		}
		return arr;
	}

	private static long checksum(byte[] arr) {
		int length = arr.length;
		int i = 0;
		long sum = 0;
		long data;

		while (length > 1) {
			data = (((arr[i] << 8) & 0xFF00) | ((arr[i + 1]) & 0xFF));
			sum += data;
			if ((sum & 0xFFFF0000) > 0) {
				sum = sum & 0xFFFF;
				sum += 1;
			}

			i += 2;
			length -= 2;
		}
		if (length > 0) {
			sum += (arr[i] << 8 & 0xFF00);
			if ((sum & 0xFFFF0000) > 0) {
				sum = sum & 0xFFFF;
				sum += 1;
			}
		}
		sum = ~sum;
		sum = sum & 0xFFFF;
		return sum;

	}

	private static byte[] udpCs(byte[] arr) {
		byte ret[] = new byte[20];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = 0;
		}
		ret[0] = arr[12];
		ret[1] = arr[13];
		ret[2] = arr[14];
		ret[3] = arr[15];
		ret[4] = arr[16];
		ret[5] = arr[17];
		ret[6] = arr[18];
		ret[7] = arr[19];
		ret[8] = (byte) 0;
		ret[9] = arr[9];
		ret[11] = arr[25];
		ret[10] = arr[24];
		for (int i = 12; i < ret.length; i++) {
			ret[i] = arr[i + 8];
		}
		return ret;
	}

}
