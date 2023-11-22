package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class ByteUtil {

	/**
	 * 객체를 바이트 배열로 변환하는 메서드
	 * Serialization
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static byte[] objectToBytes(Object obj) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(obj);
			return bos.toByteArray();
		}
	}

	/**
	 * 바이트 배열을 객체로 복원하는 메서드
	 * Deserialization
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object bytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			 ObjectInput in = new ObjectInputStream(bis)) {
			return in.readObject();
		}
	}
}
