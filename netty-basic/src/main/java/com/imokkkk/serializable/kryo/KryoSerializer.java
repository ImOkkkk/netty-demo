package com.imokkkk.serializable.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author liuwy
 * @date 2023-05-06 13:19
 * @since 1.0
 */
public class KryoSerializer {
    private static final KryoFactory kryoFactory = new KryoFactory();

    public static <T> byte[] serialize(T obj) {
        Kryo kryo = kryoFactory.getKryo();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryo.writeClassAndObject(output, obj);
        output.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static <T> T deSerialize(byte[] data, Class<T> clz) {
        Kryo kryo = kryoFactory.getKryo();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        Input input = new Input(byteArrayInputStream);
        input.close();
        return (T) kryo.readClassAndObject(input);
    }
}
